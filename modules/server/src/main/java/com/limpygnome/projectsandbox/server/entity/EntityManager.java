package com.limpygnome.projectsandbox.server.entity;

import com.limpygnome.projectsandbox.server.entity.death.MapBoundsKiller;
import com.limpygnome.projectsandbox.server.entity.physics.collisions.CollisionDetection;
import com.limpygnome.projectsandbox.server.entity.physics.collisions.CollisionResult;
import com.limpygnome.projectsandbox.server.Controller;
import com.limpygnome.projectsandbox.server.entity.physics.collisions.map.CollisionMapResult;
import com.limpygnome.projectsandbox.server.entity.physics.spatial.QuadTree;
import com.limpygnome.projectsandbox.server.network.packet.imp.entity.EntityUpdatesOutboundPacket;
import com.limpygnome.projectsandbox.server.player.PlayerInfo;
import com.limpygnome.projectsandbox.server.service.EventLogicCycleService;
import com.limpygnome.projectsandbox.server.util.IdCounterProvider;
import com.limpygnome.projectsandbox.server.util.counters.IdCounterConsumer;
import com.limpygnome.projectsandbox.server.world.map.WorldMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Handles all of the entities in the world.
 *
 * Thread-safe.
 */
public class EntityManager implements EventLogicCycleService, IdCounterConsumer
{
    private final static Logger LOG = LogManager.getLogger(EntityManager.class);

    private Controller controller;
    private WorldMap map;

    /* Used for testing collision detection.  */
    @Autowired
    private CollisionDetection collisionDetection;

    /* Used for converting entity types to classes. */
    @Autowired
    private EntityTypeMappingStoreService entityTypeMappingStoreService;

    /* Used for efficient collision detection and network updates. */
    private QuadTree quadTree;

    /* A map of entity id -> entity. */
    private final Map<Short, Entity> entities;

    /* Counter for producing entity identifiers. */
    private IdCounterProvider idCounterProvider;

    /* Used to track entities in a global state i.e. created or pending deletion. */
    private List<Entity> entitiesGlobalState;

    public EntityManager(Controller controller, WorldMap map)
    {
        this.controller = controller;
        this.map = map;

        // Setup collections...
        this.entities = new ConcurrentHashMap<>();
        this.idCounterProvider = new IdCounterProvider(this);
        this.entitiesGlobalState = new LinkedList<>();

        // Inject dependencies...
        controller.inject(this);
    }

    public synchronized void postMapLoad()
    {
        this.quadTree = new QuadTree(map);
    }

    public synchronized Entity fetch(Short key)
    {
        return entities.get(key);
    }

    /**
     * Entities should not be added this way, only through the respawn manager.
     *
     * @param entity
     * @return
     */
    public boolean add(Entity entity)
    {
        // Fetch the next available identifier
        Short entityId = idCounterProvider.nextId(entity.id);

        // Check we found an identifier
        if (entityId == null)
        {
            LOG.error("Unable to create identifier for entity - {}", entity);
            return false;
        }

        // Assign id to entity
        entity.id = entityId;

        // Add mapping
        // TODO: consider removal of sync (entity), may be too excessive...
        synchronized (entity)
        {
            synchronized (this)
            {
                // Update state to created - for update to all players!
                entity.setState(EntityState.CREATED);

                // Add entity to pending map
                entities.put(entityId, entity);

                // Add to quadtree
                quadTree.update(entity);

                LOG.debug("Added entity to world - entity: {}", entity);
            }
        }

        return true;
    }

    @Override
    public synchronized boolean containsId(short id)
    {
        return entities.containsKey(id);
    }

    public synchronized void remove(Entity entity)
    {
        synchronized (entity)
        {
            if (entity.id != null && entities.containsKey(entity.id))
            {
                // Mark entity for deletion
                entity.setState(EntityState.PENDING_DELETED);

                LOG.debug("Entity set for removal - ent id: {}", entity.id);

                // Invoke entity event handler
                entity.eventPendingDeleted(controller);
            }
        }
    }

    @Override
    public synchronized void logic()
    {
        try
        {
            // Execute logic for each entity
            executeEntityLogic();

            // Perform collision detection
            performCollisionDetection();

            // Build and distribute update packets
            sendEntityUpdatesToPlayers();

            // Update state of entities
            updateEntityStates();
        }
        catch (Exception e)
        {
            LOG.error("Exception during entity-manager logic", e);
        }
    }

    public QuadTree getQuadTree()
    {
        return quadTree;
    }

    public Map<Short, Entity> getEntities()
    {
        return entities;
    }

    public List<Entity> getGlobalStateEntities()
    {
        return entitiesGlobalState;
    }

    /**
     * Used to add an entity with a global EntityState of either CREATED or PENDING_DELETION.
     *
     * @param entity the entity
     */
    public synchronized void addEntityGlobalState(Entity entity)
    {
        synchronized (entitiesGlobalState)
        {
            entitiesGlobalState.add(entity);
        }
    }

    private void executeEntityLogic()
    {
        Entity entity;

        for (Map.Entry<Short, Entity> kv : entities.entrySet())
        {
            entity = kv.getValue();

            // We won't run logic for deleted or dead enities
            if (!entity.isDeleted() && !entity.isDead())
            {
                entity.eventLogic(controller);
            }
        }
    }

    private synchronized void performCollisionDetection()
    {
        // TODO: consider how to isolate synchronization, can deadlock if events use e.g. playerservice...
        Entity entityA;
        Entity entityB;

        // Fetch map boundaries
        float mapMaxX = map.getMaxX();
        float mapMaxY = map.getMaxY();

        // Perform collision check for each entity
        CollisionResult collisionResult;
        Collection<CollisionMapResult> mapResults;

        for (Map.Entry<Short, Entity> kv : entities.entrySet())
        {
            entityA = kv.getValue();

            // Check entity is not deleted or dead
            if (!entityA.isDeleted() && !entityA.isDead())
            {
                // TODO: CRITICAL - upgrade with quadtree, N^2 - really bad...

                // Perform collision detection/handling with other ents
                for (Map.Entry<Short, Entity> kv2 : entities.entrySet())
                {
                    entityB = kv2.getValue();

                    // Check next entity is not: dead, deleted or the same ent
                    if (!entityB.isDeleted() && !entityB.isDead() &&  entityA.id != entityB.id)
                    {
                        // Perform collision detection
                        collisionResult = collisionDetection.collision(entityA, entityB);

                        // Check if a collision occurred
                        if (collisionResult.collision)
                        {
                            // Inform both ents of event
                            entityA.eventCollisionEntity(controller, entityB, entityA, entityB, collisionResult);
                            entityB.eventCollisionEntity(controller, entityB, entityA, entityA, collisionResult);

                            // Check if our original entity is now deleted
                            // -- Only the two above events should be able to kill it
                            if (entityA.isDeleted() || entityA.isDead())
                            {
                                break;
                            }
                        }
                    }
                }

                // Check entity has still not been deleted or dead
                if (!entityA.isDeleted() && !entityA.isDead())
                {
                    // Perform collision with map
                    mapResults = collisionDetection.collisionMap(entityA);

                    for (CollisionMapResult mapResult : mapResults)
                    {
                        entityA.eventCollisionMap(controller, mapResult);
                    }

                    // Update position for ent
                    entityA.position.copy(entityA.positionNew);

                    // Check ent is not outside map
                    if  (!entityA.isDeleted() && !entityA.isDead() &&
                            (
                                    entityA.positionNew.x < 0.0f || entityA.positionNew.y < 0.0f ||
                                            entityA.positionNew.x > mapMaxX || entityA.positionNew.y > mapMaxY
                            )
                            )
                    {
                        LOG.warn("Entity went outside the map - ent: {}, pos: {}", entityA, entityA.positionNew);

                        // Kill the ent...
                        entityA.kill(controller, null, MapBoundsKiller.class);

                    }
                }
            }

        }
    }

    private void sendEntityUpdatesToPlayers()
    {
        // Iterate each player and provide updates within radius
        EntityUpdatesOutboundPacket packet;

        List<PlayerInfo> players = controller.playerService.getPlayers();

        synchronized (players)
        {
            for (PlayerInfo playerInfo : players)
            {
                try
                {
                    // Build updates packet
                    packet = new EntityUpdatesOutboundPacket();
                    packet.build(this, playerInfo, false);

                    // Send updates
                    controller.packetService.send(playerInfo, packet);
                }
                catch (IOException e)
                {
                    LOG.error("Failed to build entity updates packet for player - player id: {}", playerInfo.playerId, e);
                }
            }
        }
    }

    private synchronized void updateEntityStates()
    {
        // Clear global states from this logic cycle
        synchronized (entitiesGlobalState)
        {
            entitiesGlobalState.clear();
        }

        // Iterate each entity and transition their state
        Iterator<Map.Entry<Short, Entity>> iterator = entities.entrySet().iterator();

        Map.Entry<Short, Entity> kv;
        Entity entity;

        while (iterator.hasNext())
        {
            kv = iterator.next();
            entity = kv.getValue();

            // Remove deleted entities, else transition to next state...
            if (entity.getState() == EntityState.DELETED)
            {
                iterator.remove();
            }
            else
            {
                entity.transitionState();
            }
        }
    }

}
