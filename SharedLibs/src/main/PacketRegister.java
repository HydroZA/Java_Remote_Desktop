package main;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author James Legge
 */
public class PacketRegister extends Packet <PacketRegister>
{
    private String username, password;
    
    public PacketRegister(String username, String password)
    {
        super(Packet.Type.REGISTER);
        this.username = username;
        this.password = password;
    }
    public PacketRegister()
    {
        super(Packet.Type.REGISTER);
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public User getUser()
    {
        return new User(this.username, this.password);
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException
    {
        dos.writeUTF(username);
        dos.writeUTF(password);
    }

    @Override
    public PacketRegister deserialize(DataInputStream dis) throws IOException
    {
        this.username = dis.readUTF();
        this.password = dis.readUTF();

        return this;
    }

}
