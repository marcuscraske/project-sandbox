package com.limpygnome.projectsandbox.game.entity.vehicle;

import com.limpygnome.projectsandbox.server.Controller;
import com.limpygnome.projectsandbox.server.entity.annotation.EntityType;
import com.limpygnome.projectsandbox.server.entity.physics.Vector2;
import com.limpygnome.projectsandbox.server.inventory.Inventory;
import com.limpygnome.projectsandbox.server.inventory.item.weapon.RocketLauncher;
import com.limpygnome.projectsandbox.server.player.PlayerInfo;
import com.limpygnome.projectsandbox.server.world.map.WorldMap;
import com.limpygnome.projectsandbox.server.world.spawn.Spawn;

@EntityType(typeId = 21, typeName = "vehicle/rocket-car")
public class RocketCar extends AbstractVehicle
{
    public Inventory inventory;

    public RocketCar(WorldMap map, PlayerInfo[] players)
    {
        super(
                map,
                players,
                (short) 24,
                (short) 36,
                new Vector2[]
                {
                        new Vector2(+16.0f, 0.0f),
                        new Vector2(-16.0f, 0.0f),
                }
        );

        accelerationFactor = 0.4f;
        deaccelerationMultiplier = 0.9f;
        steeringAngle = 0.6f;
        maxSpeed = 12.0f;
    }

    public RocketCar(WorldMap map)
    {
        this(map, null);
    }

    @Override
    public String friendlyNameVehicle()
    {
        return "Rocket Car";
    }

    @Override
    public void eventReset(Controller controller, Spawn spawn)
    {
        super.eventReset(controller, spawn);

        // Load default inventory
        this.inventory = new Inventory(this);

        RocketLauncher rocketLauncher = new RocketLauncher();
        this.inventory.add(rocketLauncher);
    }

    @Override
    public void logic(Controller controller)
    {
        // Run logic for inventory
        if (inventory != null)
        {
            inventory.logic(controller);
        }

        super.logic(controller);
    }

    @Override
    public void eventPlayerEnter(PlayerInfo playerInfo, int seat)
    {
        switch (seat)
        {
            case 0:
                inventory.setOwner(playerInfo);
                break;
        }
    }

    @Override
    public void eventPlayerExit(PlayerInfo playerInfo, int seat)
    {
        switch (seat)
        {
            case 0:
                inventory.setOwner(null);
                break;
        }
    }

}