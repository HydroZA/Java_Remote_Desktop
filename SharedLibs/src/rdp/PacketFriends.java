/* 
 * AUTHOR: James Legge
 * STUDENT#: 17008250
 * INSTITUTION: London Metropolitan University
 * SUBJECT: CS6P05 Project
 * PROJECT TITLE: Using Asymmetrical Encryption and Digital Signatures to Create a Secure Remote Desktop Environment
 * Project Supervisor: Dr. Qicheng Yu
 */
package rdp;

/*
    This class is used when the user first logs in and the friends are sent
    to the client.

    The PacketGetFriends class is used when the client requests friends after
    they have already logged in.
 */


import java.io.*;

public class PacketFriends extends Packet <PacketFriends>
{
    private String[] friends;

    public PacketFriends(String[] friends)
    {
        super(Packet.Type.FRIENDS);
        this.friends = friends;
    }

    public PacketFriends()
    {
        super(Packet.Type.FRIENDS);
        this.friends = friends;
    }

    public String[] getFriends()
    {
        return friends;
    }

    public void setFriends(String[] friends)
    {
        this.friends = friends;
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException
    {
        ObjectOutputStream os = new ObjectOutputStream(dos);
        os.writeObject(this.friends);
    }

    @Override
    public PacketFriends deserialize(DataInputStream dis) throws IOException
    {
        ObjectInputStream is = new ObjectInputStream(dis);
        try
        {
            this.friends = (String[]) is.readObject();
            return this;
        }
        catch (ClassNotFoundException e)
        {
            throw new IOException(e);
        }        
    }
}
