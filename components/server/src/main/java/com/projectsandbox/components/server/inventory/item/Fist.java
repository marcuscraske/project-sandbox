package com.projectsandbox.components.server.inventory.item;

import com.projectsandbox.components.server.Controller;
import com.projectsandbox.components.server.inventory.annotation.InventoryItemTypeId;

/**
 * Created by limpygnome on 04/05/15.
 */
@InventoryItemTypeId(typeId = 1)
public class Fist extends AbstractInventoryItem
{
    public static final long serialVersionUID = 1L;

    @Override
    public String eventFetchItemText(Controller controller)
    {
        return "";
    }
}