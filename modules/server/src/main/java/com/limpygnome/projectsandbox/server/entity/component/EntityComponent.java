package com.limpygnome.projectsandbox.server.entity.component;

/**
 * Interface implemented by all entity components.
 *
 * A new instance should be created for each entity, thus an instance is not a singleton. But it should not require
 * storing a reference to the entity, since this will be passed in calls inherited from type {@link ComponentEvent}.
 */
public interface EntityComponent
{

}