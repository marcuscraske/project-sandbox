package com.limpygnome.projectsandbox.server.packets.types.players.global;

import com.limpygnome.projectsandbox.server.packets.OutboundPacket;
import com.limpygnome.projectsandbox.server.players.PlayerInfo;

/**
 * Used to update the data regarding all players on the server, which is sent to each player.
 */
public class PlayerDataUpdatesOutboundPacket extends OutboundPacket
{

    public void writePlayerJoined(PlayerInfo playerInfo)
    {
        packetData.add('J');
        packetData.add(playerInfo.playerId);

        packetData.add(playerInfo.session.displayName);
    }

    public void writePlayerInfoUpdates(PlayerInfo playerInfo, boolean forced)
    {
        if (forced || playerInfo.session.metrics.isDirtyAndResetDirtyFlag())
        {
            packetData.add('U');
            packetData.add(playerInfo.playerId);

            packetData.add(playerInfo.session.metrics.kills);
            packetData.add(playerInfo.session.metrics.deaths);
            packetData.add(playerInfo.session.metrics.score);
        }
    }

    public void writePlayerLeft(PlayerInfo playerInfo)
    {
        packetData.add('L');
        packetData.add(playerInfo.playerId);
    }

}
