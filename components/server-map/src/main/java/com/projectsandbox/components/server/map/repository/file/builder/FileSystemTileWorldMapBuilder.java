package com.projectsandbox.components.server.map.repository.file.builder;

import com.projectsandbox.components.server.Controller;
import com.projectsandbox.components.server.world.map.MapService;
import com.projectsandbox.components.server.world.map.WorldMap;
import com.projectsandbox.components.server.world.map.type.tile.TileWorldMap;
import org.springframework.stereotype.Component;

/**
 * Implementation for {@link TileWorldMap}.
 */
@Component
public class FileSystemTileWorldMapBuilder extends FileSystemOpenWorldMapBuilder.FileSystemGenericWoldMapBuilder
{

    @Override
    public String getBuilderName()
    {
        return "tile-world-map";
    }

    @Override
    public WorldMap createMapInstance(Controller controller, MapService mapService, short mapId)
    {
        return new TileWorldMap(mapId);
    }

}