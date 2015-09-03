package com.limpygnome.projectsandbox.server.entity.physics.pathfinding;

import com.limpygnome.projectsandbox.server.entity.physics.Vector2;
import com.limpygnome.projectsandbox.server.world.Map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Used to hold the result of nodes to traverse and follow from a path-finding computation.
 */
public class Path
{
    public HashSet<Node> closedNodes;
    public TreeSet<Node> openNodes;
    public HashMap<TilePosition, Node> nodes;

    public Node[] finalPath;
    public float nodeSeparation;

    public Path()
    {
        this.closedNodes = new HashSet<>();
        this.openNodes = new TreeSet<>();
        this.nodes = new HashMap<>();
    }

    public Node getNode(int index)
    {
        return finalPath[index];
    }

    public int getTotalNodes()
    {
        return finalPath.length;
    }

    public float getTargetNodeDistance(Vector2 vector)
    {
        if (finalPath.length > 0)
        {
            Node targetNode = finalPath[finalPath.length - 1];
            return Vector2.distance(vector, targetNode.cachedVector);
        }

        return 0.0f;
    }

    public void finalizePath(Map map, int targetX, int targetY)
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
                nodeTarget.buildAndCacheXY(map);
                nodeTarget = nodeTarget.parent;
            }
        }
        else
        {
            finalPath = new Node[0];
        }

        // Node separation is tile size
        nodeSeparation = (float) map.tileSize;

        // Destroy data structures used during search
        closedNodes.clear();
        closedNodes = null;
        openNodes.clear();
        openNodes = null;
    }

}
