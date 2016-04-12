package com.limpygnome.projectsandbox.server.entity.component.imp;

import com.limpygnome.projectsandbox.game.entity.living.Player;
import com.limpygnome.projectsandbox.server.Controller;
import com.limpygnome.projectsandbox.server.entity.Entity;
import com.limpygnome.projectsandbox.server.entity.PlayerEntity;
import com.limpygnome.projectsandbox.server.entity.component.EntityComponent;
import com.limpygnome.projectsandbox.server.entity.component.event.CollisionEntityComponentEvent;
import com.limpygnome.projectsandbox.server.entity.component.event.DeathComponentEvent;
import com.limpygnome.projectsandbox.server.entity.component.event.LogicComponentEvent;
import com.limpygnome.projectsandbox.server.entity.death.AbstractKiller;
import com.limpygnome.projectsandbox.server.entity.physics.Vector2;
import com.limpygnome.projectsandbox.server.entity.physics.collisions.CollisionResult;
import com.limpygnome.projectsandbox.server.entity.respawn.pending.EntityPendingRespawn;
import com.limpygnome.projectsandbox.server.entity.respawn.pending.PositionPendingRespawn;
import com.limpygnome.projectsandbox.server.player.PlayerInfo;
import com.limpygnome.projectsandbox.server.player.PlayerKeys;
import com.limpygnome.projectsandbox.server.world.map.WorldMap;
import com.limpygnome.projectsandbox.server.world.spawn.Spawn;

/**
 * Created by limpygnome on 12/04/16.
 */
public class PlayerEjectionComponent implements EntityComponent, LogicComponentEvent, DeathComponentEvent, CollisionEntityComponentEvent
{

    /**
     * The space between a vehicle and an ejected player.
     */
    public static final float EJECT_SPACING = 2.0f;

    protected Vector2[] playerEjectPositions;

    // Indicates that the driver, player zero, was spawned in this vehicle - thus respawn vehicle with player on death
    protected boolean flagDriverSpawned;

    public PlayerEjectionComponent(PlayerEntity playerEntity, Vector2[] playerEjectPositions)
    {
        if (playerEjectPositions == null || playerEjectPositions.length == 0 || playerEjectPositions[0] == null)
        {
            throw new IllegalArgumentException("Player ejection positions must have at least one non-null item");
        }
        else if (playerEjectPositions.length < 1)
        {
            throw new IllegalArgumentException("Must be at least one eject position");
        }

        this.playerEjectPositions = playerEjectPositions;

        // Check if spawned entity has player/driver; they'll need respawn later if so...
        this.flagDriverSpawned = (playerEntity.getPlayer() != null);
    }

    @Override
    public synchronized void eventLogic(Controller controller, Entity entity)
    {
        PlayerEntity playerEntity = (PlayerEntity) entity;
        PlayerInfo playerInfoDriver = playerEntity.getPlayer();
        PlayerInfo[] players = entity.getPlayers();

        // Check if players want to get out / are still connected
        PlayerInfo playerInfo;

        for (int i = 0; i < players.length; i++)
        {
            playerInfo = players[i];

            if (playerInfo != null)
            {
                if (!playerInfo.isConnected())
                {
                    // Free the seat...
                    players[i] = null;
                }
                else if (playerInfo.isKeyDown(PlayerKeys.Action))
                {
                    // Set action key to handled
                    playerInfo.setKey(PlayerKeys.Action, false);

                    // Fetch ejection seat
                    Vector2 ejectPosition;

                    if (playerEjectPositions.length >= i)
                    {
                        ejectPosition = playerEjectPositions[0];
                    }
                    else
                    {
                        ejectPosition = playerEjectPositions[i];
                    }

                    // Eject player from the vehicle
                    playerEject(controller, entity, playerInfo, ejectPosition);

                    // Free-up the space
                    players[i] = null;

                    // Reset spawn flag if driver
                    if (playerInfo == playerInfoDriver)
                    {
                        flagDriverSpawned = false;
                    }

                    // NOTE: future event hook could go here...
                }
            }
        }
    }

    private synchronized void playerEject(Controller controller, Entity entity, PlayerInfo playerInfo, Vector2 ejectPosition)
    {
        WorldMap worldMap = entity.map;

        // Offset position so that the player exits to the left of the vehicle
        Vector2 plyPos = ejectPosition.clone();

        // Create new player ent in position of vehicle
        Entity entityPlayer = controller.playerEntityService.createPlayer(worldMap, playerInfo);

        // Add player to pos offset
        float plyx = playerEjectVectorPos(ejectPosition.x, entityPlayer.width / 2.0f);
        float plyy = playerEjectVectorPos(ejectPosition.y, entityPlayer.height / 2.0f);
        plyPos = Vector2.add(plyPos, plyx, plyy);

        // Rotate pos to align with vehicle
        plyPos.rotate(0.0f, 0.0f, entity.rotation);

        // Add pos of vehicle to pos
        plyPos = Vector2.add(plyPos, entity.positionNew);

        // Spawn player
        worldMap.respawnManager.respawn(new PositionPendingRespawn(
                controller,
                entityPlayer,
                new Spawn(plyPos.x, plyPos.y, entity.rotation)
        ));
    }

    private synchronized float playerEjectVectorPos(float coord, float value)
    {
        if (coord == 0)
        {
            return 0.0f;
        }
        else if (coord < 0)
        {
            return (value * -1.0f) + EJECT_SPACING;
        }
        else
        {
            return value + EJECT_SPACING;
        }
    }

    @Override
    public void eventDeath(Controller controller, Entity entity, AbstractKiller killer)
    {
        PlayerEntity playerEntity = (PlayerEntity) entity;
        WorldMap worldMap = entity.map;

        // Respawn players in vehicle
        PlayerInfo[] players = playerEntity.getPlayers();
        PlayerInfo playerInfo;

        for (int i = 0; i < players.length; i++)
        {
            playerInfo = players[i];

            if (playerInfo != null && !(flagDriverSpawned && i == 0))
            {
                // Create and respawn player
                Entity entityPlayer = controller.playerEntityService.createPlayer(worldMap, playerInfo);
                worldMap.respawnManager.respawn(new EntityPendingRespawn(controller, entityPlayer));

                // Set seat to empty
                players[i] = null;
            }
        }
    }

    @Override
    public void eventCollisionEntity(Controller controller, Entity entity, Entity entityOther, CollisionResult result)
    {
        // Check if player
        if (entityOther instanceof PlayerEntity && !(entityOther instanceof AbstractVehicle))
        {
            // Check if they're holding down action key to get in vehicle
            PlayerEntity ply = (Player) entityOther;
            PlayerInfo playerInfo = ply.getPlayer();

            if (playerInfo.isKeyDown(PlayerKeys.Action))
            {
                // Set action key off/handled
                playerInfo.setKey(PlayerKeys.Action, false);

                // Check for next available seat
                PlayerInfo[] players = getPlayers();
                PlayerInfo plyInSeat;

                for (int i = 0; i < players.length; i++)
                {
                    plyInSeat = players[i];

                    if (plyInSeat == null || !plyInSeat.isConnected())
                    {
                        // Set the player to use this (vehicle) entity
                        controller.playerService.setPlayerEnt(playerInfo, this);

                        // Add as passenger
                        setPlayer(playerInfo, i);

                        // Invoke event hook
                        eventPlayerEnter(playerInfo, i);

                        break;
                    }
                }
            }
        }
    }

}
