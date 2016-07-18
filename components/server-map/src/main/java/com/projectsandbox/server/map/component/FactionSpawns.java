package com.projectsandbox.server.map.component;

import com.projectsandbox.components.server.Controller;
import com.projectsandbox.components.server.world.map.WorldMap;
import com.projectsandbox.components.server.world.map.MapComponent;
import com.projectsandbox.components.server.world.spawn.Spawn;
import com.projectsandbox.server.map.component.helper.SpawnParserHelper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by limpygnome on 18/07/16.
 */
@Component
public class FactionSpawns implements MapComponent
{
    @Autowired
    private SpawnParserHelper spawnParserHelper;

    @Override
    public void load(Controller controller, JSONObject mapData, WorldMap map) throws IOException
    {
        JSONArray rawSpawnData = (JSONArray) mapData.get("factionSpawns");
        JSONObject rawFactionSpawns;

        for (Object factionData : rawSpawnData)
        {
            rawFactionSpawns = (JSONObject) factionData;
            buildFactionSpawn(controller, map, rawFactionSpawns);
        }
    }

    @Override
    public void persist(Controller controller, WorldMap map)
    {
    }


    protected void buildFactionSpawn(Controller controller, WorldMap map, JSONObject factionData)
    {
        short factionId = (short) (long) factionData.get("id");

        com.projectsandbox.components.server.world.spawn.FactionSpawns factionSpawns = new com.projectsandbox.components.server.world.spawn.FactionSpawns(factionId);

        // Parse spawns
        JSONArray spawnsData = (JSONArray) factionData.get("spawns");

        if (spawnsData != null)
        {
            Spawn spawn;
            for (Object spawnData : spawnsData)
            {
                spawn = spawnParserHelper.parseSpawn((JSONObject) spawnData);
                factionSpawns.addSpawn(spawn);
            }
        }

        // Add to map
        // TODO: should add to "spawnData" in this map...
        map.respawnManager.factionSpawnsAdd(map.getMapId(), factionSpawns);
    }

}