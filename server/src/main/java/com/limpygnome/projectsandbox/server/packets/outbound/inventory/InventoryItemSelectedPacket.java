package com.limpygnome.projectsandbox.server.packets.outbound.inventory;

import com.limpygnome.projectsandbox.server.packets.OutboundPacket;
import com.limpygnome.projectsandbox.server.inventory.InventoryItem;

import java.io.IOException;
import java.util.LinkedList;

/**
 *
 * @author limpygnome
 */
public class InventoryItemSelectedPacket extends OutboundPacket
{
    public InventoryItemSelectedPacket()
    {
        super((byte) 'I', (byte) 'S');
    }
    
    public void build(InventoryItem selectedItem) throws IOException
    {
        LinkedList<Object> packetData = new LinkedList<>();
        
        packetData.add(selectedItem.getTypeId());
        
        // Write packet data
        write(packetData);
    }
}