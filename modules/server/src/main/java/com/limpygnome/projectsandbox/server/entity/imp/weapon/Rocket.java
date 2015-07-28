package com.limpygnome.projectsandbox.server.entity.imp.weapon;

import com.limpygnome.projectsandbox.server.Controller;
import com.limpygnome.projectsandbox.server.entity.Entity;
import com.limpygnome.projectsandbox.server.entity.annotation.EntityType;
import com.limpygnome.projectsandbox.server.entity.death.AbstractKiller;
import com.limpygnome.projectsandbox.server.entity.death.RocketKiller;
import com.limpygnome.projectsandbox.server.entity.physics.Vector2;
import com.limpygnome.projectsandbox.server.entity.physics.collisions.CollisionResult;
import com.limpygnome.projectsandbox.server.entity.physics.collisions.CollisionResultMap;
import com.limpygnome.projectsandbox.server.entity.physics.proximity.DefaultProximity;
import com.limpygnome.projectsandbox.server.player.PlayerInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.limpygnome.projectsandbox.server.constant.weapon.RocketConstants.*;

/**
 * An Rocket fired by a rocket weapon.
 */
@EntityType(typeId = 600, typeName = "weapon/rocket")
public class Rocket extends Entity
{
    private final static Logger LOG = LogManager.getLogger(Rocket.class);

    private PlayerInfo playerInfoOwner;
    private long gameTimeCreated;
    private float speedStep;
    private boolean exploded;

    public Rocket(Controller controller, PlayerInfo playerInfoOwner, float initialSpeed)
    {
        super((short) 9, (short) 12);

        this.playerInfoOwner = playerInfoOwner;
        this.gameTimeCreated = controller.gameTime();
        this.speedStep = initialSpeed;
        this.exploded = false;

        setMaxHealth(10);
    }

    public Rocket(Controller controller, PlayerInfo playerInfo)
    {
        this(controller, playerInfo, ROCKET_SPEED_STEP);
    }

    @Override
    public String friendlyName()
    {
        return "Rocket";
    }

    @Override
    public PlayerInfo[] getPlayers()
    {
        return new PlayerInfo[]{ playerInfoOwner };
    }

    @Override
    public void logic(Controller controller)
    {
        // Check if Rocket has expired
        float lifespan = controller.gameTime() - gameTimeCreated;

        if (lifespan > ROCKET_LIFESPAN_MS)
        {
            remove();
        }
        else
        {
            // Check if to increment rocket speed
            if (this.speedStep < ROCKET_SPEED)
            {
                // Increment by speed step, towards reaching max speed
                this.speedStep += ROCKET_SPEED_STEP;

                // Check step has not exceeded max speed
                if (this.speedStep > ROCKET_SPEED)
                {
                    this.speedStep = ROCKET_SPEED;
                }
            }

            // Move rocket
            Vector2 offset = Vector2.vectorFromAngle(rotation, speedStep);
            positionOffset(offset);
        }

        super.logic(controller);
    }

    @Override
    public void eventHandleCollision(Controller controller, Entity entCollider, Entity entVictim, Entity entOther, CollisionResult result)
    {
        performCollisionExplosion(controller);
    }

    @Override
    public void eventHandleCollisionMap(Controller controller, CollisionResultMap collisionResultMap)
    {
        performCollisionExplosion(controller);
    }

    @Override
    public synchronized void eventHandleDeath(Controller controller, AbstractKiller killer)
    {
        // Do nothing...
    }

    private synchronized void performCollisionExplosion(Controller controller)
    {
        if (!exploded)
        {
            // Apply damage to entities
            DefaultProximity.applyLinearRadiusDamage(controller, this, ROCKET_BLAST_RADIUS, ROCKET_BLAST_DAMAGE, true, RocketKiller.class);

            // Mark this entity for removal
            remove();

            // Ensure this call never happens again
            exploded = true;
        }
    }

}