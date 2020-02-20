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
public class PacketFrame extends Packet<PacketFrame>
{
    private int size;
    private byte[] frame;

    public PacketFrame(byte[] frm, int size)
    {
        super(Packet.Type.FRAME);
        this.frame = frm;
        this.size = size;
    }
    
    public PacketFrame()
    {
        super(Packet.Type.FRAME);
    }
    
    public byte[] getFrame()
    {
        return frame;
    }

    public void setFrame(byte[] frame)
    {
        this.frame = frame;
    }

    public int getSize()
    {
        return size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException
    {
        dos.writeInt(size);
        dos.write(frame);
    }

    @Override
    public PacketFrame deserialize(DataInputStream dis) throws IOException
    {
        this.size = dis.readInt();
        frame = new byte[size];
        dis.readFully(frame, 0, size);
        return this;
    }

}
