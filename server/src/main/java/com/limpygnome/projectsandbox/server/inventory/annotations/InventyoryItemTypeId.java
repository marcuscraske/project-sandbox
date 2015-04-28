package com.limpygnome.projectsandbox.server.inventory.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The type of an entity.
 * 
 * @author limpygnome
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface InventyoryItemTypeId
{
    public short typeId();
}