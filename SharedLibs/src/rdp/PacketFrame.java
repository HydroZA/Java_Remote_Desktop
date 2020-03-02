/* 
 * AUTHOR: James Legge
 * STUDENT#: 17008250
 * INSTITUTION: London Metropolitan University
 * SUBJECT: CS6P05 Project
 * PROJECT TITLE: Using Asymmetrical Encryption and Digital Signatures to Create a Secure Remote Desktop Environment
 * Project Supervisor: Dr. Qicheng Yu
 */
package rdp;

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
