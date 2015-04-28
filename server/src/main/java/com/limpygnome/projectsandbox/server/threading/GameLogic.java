package com.limpygnome.projectsandbox.server.threading;

import com.limpygnome.projectsandbox.server.Controller;
import com.limpygnome.projectsandbox.server.packets.outbound.EntityUpdatesPacket;
import java.io.IOException;

/**
 *
 * @author limpygnome
 */
public class GameLogic implements Runnable
{
    public static final int TICK_RATE = 60;
    
    private Controller controller;
    
    public GameLogic(Controller controller)
    {
        this.controller = controller;
    }
    
    public void run()
    {
        try
        {
            long timeStart, timeEnd, timeNext;
            while(!Thread.interrupted())
            {
                timeStart = System.currentTimeMillis();
                
                try
                {
                    // Run logic for entities
                    controller.entityManager.logic();

                    // Build update data
                    EntityUpdatesPacket packet = new EntityUpdatesPacket();
                    packet.build(controller.entityManager, false);
                    
                    // Write update packet to each client
                    byte[] data = packet.getPacketData();
                    controller.endpoint.broadcast(data);
                }
                catch(IOException ex)
                {
                    ex.printStackTrace(System.err);
                }
                
                // Sleep for another cycle
                timeEnd = System.currentTimeMillis();
                timeNext = TICK_RATE - (timeEnd - timeStart);
                
                if(timeNext > 0)
                {
                    Thread.sleep(timeNext);
                }
                else
                {
                    System.err.println("Warning: game logic cycle took " + (timeEnd - timeStart) + " ms or longer.");
                }
            }
        }
        catch(InterruptedException ex)
        {
            ex.printStackTrace(System.err);
        }
    }
}