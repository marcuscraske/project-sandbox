package com.limpygnome.projectsandbox.server.world.map;

import com.limpygnome.projectsandbox.server.Controller;
import com.limpygnome.projectsandbox.server.packet.imp.map.MapDataOutboundPacket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a (world) map, an environment/area in which a player interacts.
 */
public class WorldMap
{
    private final static Logger LOG = LogManager.getLogger(WorldMap.class);

    private final Controller controller;
    private final MapService mapService;

    /**
     * Unique identifier for this map.
     *
     * TODO: convert to UUID.
     */
    public short mapId;

    /**
     * Properties and cached values for this map.
     */
    public WorldMapProperties properties;

    /**
     * Tile data for this map.
     */
    public WorldMapTileData tileData;

    /**
     * Cached packet of data sent to client for map.
     *
     * WARNING: if this is used elsewhere, it needs thread protection.
     */
    public MapDataOutboundPacket packet;

    /**
     * Creates a new instance and sets up internal state ready for tile data.
     *
     * @param controller
     * @param mapService The map manager to which this instance belongs
     * @param mapId The unique identifier for this map
     */
    public WorldMap(Controller controller, MapService mapService, short mapId)
    {
        this.controller = controller;
        this.mapService = mapService;
        this.mapId = mapId;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb      .append("map{properties:\n")
                .append(properties.toString())
                .append("\n,\ntile data:\n")
                .append(tileData.toString())
                .append("\n}");
        
        return sb.toString();
    }
    
}
