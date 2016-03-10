projectSandbox.network.entities =
{
    handlePacket: function(packet)
    {
        var subType = packet.readChar();

        switch (subType)
        {
            case "U":
                this.packetUpdates(packet);
                break;

            default:
                console.error("engine/network/entities - unknown sub-type - " + subType);
                break;
        }
    },

    packetUpdates: function(packet)
    {
        var updateType;
        var id;

        while (packet.hasMoreData())
        {
            // Retrieve update type and entity id
            updateType = packet.readChar();
            id = packet.readShort();

            // Handle update based on type
            switch(updateType)
            {
                case "C":
                    this.packetUpdatesEntCreated(packet, id);
                    break;
                case "U":
                    this.packetUpdatesEntUpdated(packet, id);
                    break;
                case "D":
                    this.packetUpdatesEntDeleted(packet, id);
                    break;
                default:
                    console.log("engine/network/entities - unknown entity update type '" + updateType + "'");
                    break;
            }
        }
    },

    packetUpdatesEntCreated: function(packet, id)
    {
        // Parse data
        var entityType = packet.readShort();
        var maxHealth = packet.readFloat();

        // Create entity based on type
        var ent = null;

        switch(entityType)
        {
            default:
                console.warn("engine/network/entities - unhandled ent type " + entityType);
                break;
            case 0:
                ent = new Entity();
                break;
            case 1:
                ent = new Player();
                break;
            case 500:
                ent = new Sentry();
                break;
            case 510:
                ent = new Pedestrian();
                break;
            case 600:
                ent = new Rocket();
                break;
            case 20:
                ent = new IceCreamVan();
                break;
            case 21:
                ent = new RocketCar();
                break;
            case 22:
                ent = new Bus();
                break;
            case 1201:
                ent = new HealthPickup();
                break;
        }

        if (ent != null)
        {
            // Set ID
            ent.id = id;

            // Set max health
            ent.maxHealth = maxHealth;

            // TODO: read custom byte data here

            // Add to world
            projectSandbox.entities.set(id, ent);

            console.debug("engine/network/entities - entity " + id + " created");
        }
    },

    UPDATEMASK_ALIVE: 1,
    UPDATEMASK_X: 2,
    UPDATEMASK_Y: 4,
    UPDATEMASK_ROTATION: 8,
    UPDATEMASK_HEALTH: 16,

    packetUpdatesEntUpdated: function(packet, id)
    {
        // Find entity
        ent = projectSandbox.entities.get(id);

        if (ent)
        {
            // Read mask
            var mask = packet.readByte();

            // Read updated params
            if ((mask & this.UPDATEMASK_ALIVE) == this.UPDATEMASK_ALIVE)
            {
                // Set ent to alive
                if (ent.dead)
                {
                    ent.dead = false;
                    console.debug("engine/network/entities - entity alive - entity id: " + id);
                }
            }
            else
            {
                // Set ent to dead
                if (!ent.dead)
                {
                    ent.dead = true;
                    console.debug("engine/network/entities - entity dead - entity id: " + id);

                    // Raise death event
                    this.invokeEntityDeath(ent);
                }
            }

            if ((mask & this.UPDATEMASK_X) == this.UPDATEMASK_X)
            {
                ent.x = packet.readFloat();
            }
            if ((mask & this.UPDATEMASK_Y) == this.UPDATEMASK_Y)
            {
                ent.y = packet.readFloat();
            }
            if ((mask & this.UPDATEMASK_ROTATION) == this.UPDATEMASK_ROTATION)
            {
                ent.rotation = packet.readFloat();
            }
            if ((mask & this.UPDATEMASK_HEALTH) == this.UPDATEMASK_HEALTH)
            {
                ent.health = packet.readFloat();
            }

            // Allow ent to parse custom update bytes
            offset = ent.readBytesUpdate(packet);
        }
        else
        {
            console.warn("engine/network/entities - entity with id " + id + " not found for update");
        }
    },

    packetUpdatesEntDeleted: function(packet, id)
    {
        // Remove entity from the world
        var entity = projectSandbox.entities.get(id);

        if (entity != null)
        {
            // Remove from world
            projectSandbox.entities.delete(id);

            // Raise death event
            this.invokeEntityDeath(entity);

            console.debug("engine/network/entities - entity " + id + " deleted");
        }
        else
        {
            console.warn("engine/network/entities - entity " + id + " not found for deletion");
        }
    },

    invokeEntityDeath: function(entity)
    {
        // Invoke death event
        if (entity.eventDeath)
        {
            entity.eventDeath();
        }
    }
}