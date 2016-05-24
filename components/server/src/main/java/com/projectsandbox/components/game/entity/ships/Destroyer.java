package com.projectsandbox.components.game.entity.ships;

import com.projectsandbox.components.server.Controller;
import com.projectsandbox.components.server.entity.PlayerEntity;
import com.projectsandbox.components.server.entity.annotation.EntityType;
import com.projectsandbox.components.server.entity.component.imp.PlayerEjectionComponent;
import com.projectsandbox.components.server.entity.component.imp.SpaceMovementComponent;
import com.projectsandbox.components.server.entity.component.imp.VelocityComponent;
import com.projectsandbox.components.server.entity.physics.Vector2;
import com.projectsandbox.components.server.inventory.Inventory;
import com.projectsandbox.components.server.inventory.item.weapon.Gatling;
import com.projectsandbox.components.server.inventory.item.weapon.RocketLauncher;
import com.projectsandbox.components.server.player.PlayerInfo;
import com.projectsandbox.components.server.world.map.WorldMap;
import com.projectsandbox.components.server.world.spawn.Spawn;

@EntityType(typeId = 210, typeName = "ships/destroyer")
public class Destroyer extends PlayerEntity
{

    public Destroyer(WorldMap map, PlayerInfo[] players)
    {
        super(
                map,
                (short) 167,
                (short) 231,
                players,
                new Inventory[1]
        );

        components.register(new PlayerEjectionComponent(this, new Vector2[]
        {
                new Vector2(0.0f, 50.0f)
        }));

        components.register(new VelocityComponent(
                10000.0f      // Mass
        ));

        components.register(new SpaceMovementComponent(
                20.0f,      // Speed limit
                0.4f,       // Acceleration
                0.05f       // Turning speed
        ));

        setMaxHealth(400.0f);
    }

    public Destroyer(WorldMap map)
    {
        this(map, new PlayerInfo[1]);
    }

    @Override
    public String entityName()
    {
        return "Destroyer";
    }

    @Override
    public synchronized void eventReset(Controller controller, Spawn spawn)
    {
        super.eventReset(controller, spawn);

        Inventory inventory = new Inventory(this);
        inventory.add(new RocketLauncher());
        inventory.add(new Gatling());
        setInventory(0, inventory);
    }

}
