package com.limpygnome.projectsandbox.server.world.map.packet;

import com.limpygnome.projectsandbox.server.packet.OutboundPacket;
import com.limpygnome.projectsandbox.server.world.map.open.OpenWorldMap;
import com.limpygnome.projectsandbox.server.world.map.open.OpenWorldMapProperties;

import java.io.IOException;

/**
 * Packet data for an implementation of {@link OpenWorldMap}, intended to be cached and sent to multiple users
 * once it has been built.
 */
public class OpenMapDataOutboundPacket extends OutboundPacket
{

    public OpenMapDataOutboundPacket()
    {
        super((byte)'M', (byte)'O');
    }

    public void build(OpenWorldMap map) throws IOException
    {
        OpenWorldMapProperties properties = map.getProperties();

        // Write limits of map
        packetData.add(properties.getLimitWidth());
        packetData.add(properties.getLimitHeight());

        // Write background data
        String background = properties.getBackground();

        // -- 0: Flag to indicate if background enabled
        // -- 1: texture (string), if enabled is true

        if (background != null && background.length() > 0)
        {
            packetData.add(true);
            packetData.addUtf8(background);
        }
        else
        {
            packetData.add(false);
        }
    }

}
