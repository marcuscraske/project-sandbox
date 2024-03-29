package com.projectsandbox.components.server.entity.ai.pathfinding;

import com.projectsandbox.components.server.entity.physics.Vector2;
import com.projectsandbox.components.server.world.map.type.tile.TileWorldMap;

/**
 * Created by limpygnome on 01/09/15.
 */
public class Node implements Comparable<Node>
{
    public Node parent;

    public int tileX;
    public int tileY;
    public float pathCost;
    public float heuristicCost;
    public int searchDepth;

    public Vector2 cachedVector;

    public Node(int tileX, int tileY)
    {
        this.tileX = tileX;
        this.tileY = tileY;
    }

    public Node(int tileX, int tileY, float pathCost, int searchDepth)
    {
        this.tileX = tileX;
        this.tileY = tileY;
        this.pathCost = pathCost;
        this.searchDepth = searchDepth;
    }

    public void buildAndCacheXY(TileWorldMap map)
    {
        float x = ((float) tileX * map.tileMapData.tileSize) + map.tileMapData.tileSizeHalf;
        float y = ((float) tileY * map.tileMapData.tileSize) + map.tileMapData.tileSizeHalf;

        this.cachedVector = new Vector2(x, y);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (tileX != node.tileX) return false;
        return tileY == node.tileY;
    }

    @Override
    public int hashCode()
    {
        int result = 23;
        result = 31 * result + tileX;
        result = 31 * result + tileY;
        return result;
    }

    @Override
    public int compareTo(Node o)
    {
        float totalCost = heuristicCost + pathCost;
        float oTotalCost = o.heuristicCost + o.pathCost;

        if (totalCost > oTotalCost)
        {
            return 1;
        }
        else
        {
            return -1;
        }
    }

    @Override
    public String toString()
    {
        return "[tx: " + tileX + ", ty: " + tileY + ", xy: " + cachedVector + ", h cost: " + heuristicCost + "]";
    }
}
