package com.limpygnome.projectsandbox.inventory;

/**
 *
 * @author limpygnome
 */
public class Smg extends Weapon
{
    public Smg(Inventory inventory)
    {
        super(
                inventory,
                (short) 60,
                (short) 5,
                120,
                1500
        );
    }
}