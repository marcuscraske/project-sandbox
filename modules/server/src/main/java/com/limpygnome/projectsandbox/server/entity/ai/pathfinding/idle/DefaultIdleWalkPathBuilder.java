package com.limpygnome.projectsandbox.server.entity.ai.pathfinding.idle;

import com.limpygnome.projectsandbox.server.Controller;
import com.limpygnome.projectsandbox.server.entity.Entity;
import com.limpygnome.projectsandbox.server.entity.ai.pathfinding.IdleWalkPathBuilder;
import com.limpygnome.projectsandbox.server.entity.ai.pathfinding.Node;
import com.limpygnome.projectsandbox.server.entity.ai.pathfinding.Path;
import com.limpygnome.projectsandbox.server.world.Map;
import com.limpygnome.projectsandbox.server.world.MapPosition;

import com.limpygnome.projectsandbox.server.world.TileType;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by limpygnome on 07/09/15.
 */
public class DefaultIdleWalkPathBuilder implements IdleWalkPathBuilder
{

    @Override
    public Path build(Controller controller, Map map, Entity entity, int maxDepth)
    {
        MapPosition entityPosition = controller.mapManager.main.positionFromReal(entity.positionNew);

        // Build path to first pedestrian node; this will be our starting place
        List<Node> pathNodes = new LinkedList<>();

        Node lastNode = findStartNode(map, pathNodes, entityPosition, maxDepth);
        pathNodes.add(lastNode);

        // Build next nodes to walk...
        Random random = new Random(System.currentTimeMillis());
        HashSet<Node> pathNodesPresent = new HashSet<>();
        int depth = 0;

        while (lastNode != null && ++depth <= maxDepth)
        {
            lastNode = buildNextStep(map, random, pathNodesPresent, lastNode);

            // Add the newly found node...
            if (lastNode != null)
            {
                pathNodes.add(lastNode);
            }
        }

        // Build into path object
        Node[] finalPath = pathNodes.toArray(new Node[pathNodes.size()]);

        // TODO: this feels a little hacky, improve...
        Path path = new Path();
        path.finalPath = finalPath;
        path.nodeSeparation = map.tileSize / 2.0f;

        // Ensure every node has vector built
        for (int i = 0; i < path.finalPath.length; i++)
        {
            path.finalPath[i].buildAndCacheXY(map);
        }

        return path;
    }

    private Node findStartNode(Map map, List<Node> pathNodes, MapPosition entityPosition, int maxDepth)
    {
        // NOTE: this is very similar to the A* path-finding code in some ways...

        // Build start node
        Node startNode = new Node(entityPosition.tileX, entityPosition.tileY, 0.0f, 1);

        // Search for closest pedestrian node
        TreeSet<Node> openNodes = new TreeSet<>();
        HashSet<Node> closedNodes = new HashSet<>();
        openNodes.add(startNode);

        Node currentNode;
        Node newNode;
        TileType tileType;
        int depth = 0;

        while (depth <= maxDepth && !openNodes.isEmpty())
        {
            // Pick closest node
            currentNode = openNodes.pollFirst();

            // Update depth
            depth = Math.max(depth, currentNode.searchDepth);

            // Check if current node is suitable as candidate
            tileType = map.tileTypes[map.tiles[currentNode.tileY][currentNode.tileX]];

            if (tileType.properties.pedestrian)
            {
                return currentNode;
            }

            // Add current node to searched nodes, to avoid repeating search later
            closedNodes.add(currentNode);

            // Add neighbours
            for (int y = -1; y <= 1; y++)
            {
                for (int x = -1; x <= 1; x++)
                {
                    newNode = new Node(currentNode.tileX + x, currentNode.tileY + y, currentNode.pathCost + 1, currentNode.searchDepth + 1);

                    if (!closedNodes.contains(newNode))
                    {
                        openNodes.add(newNode);
                    }
                }
            }
        }

        return null;
    }

    private Node buildNextStep(Map map, Random random, Set<Node> pathNodesPresent, Node lastNode)
    {
        // TODO: favour moving in a certain direction...else random...

        // TODO: return null if not found...
        // Fetch all available neighbouring pedestrian nodes
        List<Node> neighboursNew = new LinkedList<>();
        List<Node> neighboursUsed = new LinkedList<>();

        Node node;
        int tileX, tileY;
        TileType tileType;

        for (int y = -1; y <= 1; y++)
        {
            for (int x = -1; x <= 1; x++)
            {
                // Skip center and diagonals
                if (!(y == 0 || x == 0) || (y == 0 && x == 0))
                {
                    continue;
                }

                // Build position
                tileY = lastNode.tileY + y;
                tileX = lastNode.tileX + x;

                // Check if node is within map and pedestrian
                if (tileX < 0 || tileY < 0 || tileX >= map.width || tileY >= map.height)
                {
                    continue;
                }

                tileType = map.tileTypes[map.tiles[tileY][tileX]];

                if (!tileType.properties.pedestrian)
                {
                    continue;
                }

                // Build node
                node = new Node(tileX, tileY);

                // Check if present
                if (pathNodesPresent.contains(node))
                {
                    neighboursUsed.add(node);
                }
                else
                {
                    neighboursNew.add(node);
                }
            }
        }

        // Pick either a random new node, else an old node, randomly
        if (!neighboursNew.isEmpty())
        {
            node = neighboursNew.get(random.nextInt(neighboursNew.size()));
        }
        else if (!neighboursUsed.isEmpty())
        {
            node = neighboursUsed.get(random.nextInt(neighboursUsed.size()));
        }
        else
        {
            node = null;
        }

        return node;
    }

}