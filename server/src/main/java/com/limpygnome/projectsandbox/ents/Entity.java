package com.limpygnome.projectsandbox.ents;

import com.limpygnome.projectsandbox.Controller;
import com.limpygnome.projectsandbox.ents.physics.CollisionResult;
import com.limpygnome.projectsandbox.ents.physics.SAT;
import com.limpygnome.projectsandbox.ents.physics.Vector2;
import com.limpygnome.projectsandbox.ents.physics.Vertices;
import com.limpygnome.projectsandbox.textures.Texture;

/**
 *
 * @author limpygnome
 */
public strictfp abstract class Entity
{
    public enum StateChange
    {
        NONE,
        /**
         * Indicates a world update needs to be sent out before the entity
         * can be deleted.
         */
        PENDING_DELETED,
        /**
         * Indicates the entity can now be deleted.
         */
        DELETED,
        UPDATED,
        CREATED
    }
    
    public short id;
    
    // State flags
    private StateChange state;
    
    // State data
    public Texture texture;
    public short width;
    public short height;
    public Vector2 position;
    public Vector2 positionNew;
    public Vertices cachedVertices;
    public float rotation;
    
    public Entity(Texture texture, short width, short height)
    {
        this.id = 0;
        
        // Create initial/default state data
        this.texture = texture;
        this.width = width;
        this.height = height;
        this.position = new Vector2();
        this.positionNew = new Vector2();
        this.cachedVertices = new Vertices(this);
        this.rotation = 0.0f;
        this.state = StateChange.CREATED;
    }
    
    public void logic(Controller controller)
    {
        // Nothing by default...
    }
    
    public void positionOffset(Vector2 offset)
    {
        position(positionNew.x + offset.x, positionNew.y + offset.y);
    }
    
    public void positionOffset(float x, float y)
    {
        position(positionNew.x + x, positionNew.y + y);
    }
    
    public void position(Vector2 position)
    {
        position(position.x, position.y);
    }
    
    /**
     * Updates the position of the entity, as well as any needed state changes
     * needed.
     * 
     * @param x The new X position.
     * @param y The new Y position.
     */
    public void position(float x, float y)
    {   
        // Update positionNew
        positionNew.x = x;
        positionNew.y = y;
        
        // Rebuild cached vertices
        cachedVertices = new Vertices(this);
        
        // Update state to updated
        setState(StateChange.UPDATED);
    }
    
    public StateChange getState()
    {
        return state;
    }
    
    public synchronized void setState(StateChange state)
    {
        // Only allow delete state to progress
        if(this.state == StateChange.PENDING_DELETED && state == StateChange.DELETED)
        {
            this.state = state;
        }
        else if(this.state != StateChange.DELETED && this.state != StateChange.PENDING_DELETED)
        {
            this.state = state;
        }
    }
}
