package com.limpygnome.projectsandbox.server.entity.component.imp;

import com.limpygnome.projectsandbox.server.Controller;
import com.limpygnome.projectsandbox.server.entity.Entity;
import com.limpygnome.projectsandbox.server.entity.PlayerEntity;
import com.limpygnome.projectsandbox.server.entity.component.EntityComponent;
import com.limpygnome.projectsandbox.server.entity.component.event.CollisionMapComponentEvent;
import com.limpygnome.projectsandbox.server.entity.component.event.LogicComponentEvent;
import com.limpygnome.projectsandbox.server.entity.physics.Vector2;
import com.limpygnome.projectsandbox.server.entity.physics.collisions.CollisionResultMap;
import com.limpygnome.projectsandbox.server.player.PlayerInfo;
import com.limpygnome.projectsandbox.server.player.PlayerKeys;

/**
 * Created by limpygnome on 14/04/16.
 */
public class SpaceMovementComponent implements EntityComponent, LogicComponentEvent, CollisionMapComponentEvent
{

    @Override
    public void eventLogic(Controller controller, Entity entity)
    {
        PlayerEntity playerEntity = (PlayerEntity) entity;
        PlayerInfo playerDriver = ((PlayerEntity) entity).getPlayer();

        if (playerDriver != null)
        {

            VelocityComponent velocityComponent = (VelocityComponent) entity.components.fetchComponent(VelocityComponent.class);

            // Handle changing the rotation
            float angleOffset = 0.0f;

            if (playerDriver.isKeyDown(PlayerKeys.MovementLeft))
            {
                angleOffset -= 0.2f;
            }

            if (playerDriver.isKeyDown(PlayerKeys.MovementRight))
            {
                angleOffset += 0.2f;
            }

            entity.rotationOffset(angleOffset);

            // Handle acceleration
            Vector2 offset = null;

            if (playerDriver.isKeyDown(PlayerKeys.MovementUp))
            {
                offset = Vector2.vectorFromAngle(entity.rotation, 0.5f);
            }
            else if (playerDriver.isKeyDown(PlayerKeys.MovementDown))
            {
                offset = Vector2.vectorFromAngle(entity.rotation, -0.5f);
            }

            if (offset != null)
            {
                velocityComponent.offset(offset);
            }
        }

    }

    @Override
    public void eventCollisionMap(Controller controller, Entity entity, CollisionResultMap collisionResultMap)
    {
        VelocityComponent velocityComponent = (VelocityComponent) entity.components.fetchComponent(VelocityComponent.class);
        velocityComponent.invert();
    }

}