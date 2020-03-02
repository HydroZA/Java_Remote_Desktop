/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author James Legge
 */
public class PacketStopStreaming extends Packet<PacketStopStreaming>
{
    
    public PacketStopStreaming()
    {
        super(Packet.Type.STOP_STREAMING);
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PacketStopStreaming deserialize(DataInputStream dis) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
