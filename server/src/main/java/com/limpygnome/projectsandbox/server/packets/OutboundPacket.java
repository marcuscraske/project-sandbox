package com.limpygnome.projectsandbox.server.packets;

import com.limpygnome.projectsandbox.server.players.PlayerInfo;
import com.limpygnome.projectsandbox.server.utils.ByteHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;

/**
 *
 * @author limpygnome
 */
public abstract class OutboundPacket extends Packet
{
    protected ByteArrayOutputStream buffer;

    public OutboundPacket()
    {
        buffer = new ByteArrayOutputStream();
    }

    public OutboundPacket(byte mainType)
    {
        this();

        buffer.write(mainType);
    }

    public OutboundPacket(byte mainType, byte subType)
    {
        this();
        
        // Write header data
        buffer.write(mainType);
        buffer.write(subType);
    }

    public byte[] getPacketData()
    {
        return buffer.toByteArray();
    }

    protected void write(LinkedList<Object> packetData) throws IOException
    {
        byte[] data = ByteHelper.convertListOfObjects(packetData);
        buffer.write(data);
    }

    public void send(PlayerInfo player)
    {
        player.socket.send(buffer.toByteArray());
    }
}
