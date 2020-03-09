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
public class PacketCancelLogin extends Packet <PacketCancelLogin>
{

    public PacketCancelLogin()
    {
        super(Packet.Type.CANCEL_LOGIN);
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException
    {
        throw new UnsupportedOperationException("Not supported "); 
    }

    @Override
    public PacketCancelLogin deserialize(DataInputStream dis) throws IOException
    {
        throw new UnsupportedOperationException("Not supported");
    }
    
}
