package com.projectsandbox.components.server.entity.ai.pathfinding.astar;

import com.projectsandbox.components.server.entity.ai.pathfinding.Node;
import com.projectsandbox.components.server.entity.ai.pathfinding.Path;
import com.projectsandbox.components.server.entity.ai.pathfinding.TilePosition;
import com.projectsandbox.components.server.world.map.type.tile.TileWorldMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Used to produce a path result from A*.
 */
public class AStarPath extends Path
{
    public HashSet<Node> closedNodes;
    public TreeSet<Node> openNodes;
    public HashMap<TilePosition, Node> nodes;

    public AStarPath()
    {
        this.closedNodes = new HashSet<>();
        this.openNodes = new TreeSet<>();
        this.nodes = new HashMap<>();
    }

    public void finalizePath(TileWorldMap tileMap, int targetX, int targetY)
    {
        // Build final path
        Node nodeTarget = nodes.get(new TilePosition(targetX, targetY));

        // The end/target node must be present and have a parent; if no parent, start/target node are the same...
        if (nodeTarget != null && nodeTarget.parent != null)
        {
            finalPath = new Node[nodeTarget.searchDepth + 1];
            int i = nodeTarget.searchDepth + 1;

            // TODO: consider more checking in this region; may impact performance though
            while (--i >= 0 && nodeTarget != null)
            {
                finalPath[i] = nodeTarget;
                nodeTarget.buildAndCacheXY(tileMap);
                nodeTarget = nodeTarget.parent;
            }
        }
        else
        {
            finalPath = new Node[0];
        }

        // Node separation is tile size
        nodeSeparation = tileMap.tileMapData.tileSize;

        // Destroy data structures used during search
        closedNodes.clear();
        closedNodes = null;
        openNodes.clear();
        openNodes = null;
    }

}
