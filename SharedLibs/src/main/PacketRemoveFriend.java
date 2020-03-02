package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class PacketRemoveFriend extends Packet <PacketRemoveFriend>
{
    private String username, friendName;
    
    public PacketRemoveFriend(String username, String friendName)
    {
        super(Packet.Type.REMOVE_FRIEND);
        this.username = username;
        this.friendName = friendName;
    }
    public PacketRemoveFriend()
    {
        super(Packet.Type.REMOVE_FRIEND);
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
    public PacketRemoveFriend deserialize(DataInputStream dis) throws IOException
    {
        setUsername(dis.readUTF());
        setFriendName(dis.readUTF());
        return this;
    }
    
}
