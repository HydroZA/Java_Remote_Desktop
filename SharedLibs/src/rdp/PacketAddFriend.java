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
public class PacketAddFriend extends Packet<PacketAddFriend>
{

    private String username, friendName;

    public PacketAddFriend(String username, String friendName)
    {
        super(Packet.Type.ADD_FRIEND);
        this.username = username;
        this.friendName = friendName;
    }

    public PacketAddFriend()
    {
        super(Packet.Type.ADD_FRIEND);
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getFriendName()
    {
        return friendName;
    }

    public void setFriendName(String friendName)
    {
        this.friendName = friendName;
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException
    {
        dos.writeUTF(getUsername());
        dos.writeUTF(getFriendName());
    }

    @Override
    public PacketAddFriend deserialize(DataInputStream dis) throws IOException
    {
        setUsername(dis.readUTF());
        setFriendName(dis.readUTF());
        
        return this;
    }

}
