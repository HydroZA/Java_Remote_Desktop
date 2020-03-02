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
public class PacketLogin extends Packet <PacketLogin>
{
    private String username, password;
    
    public PacketLogin(String username, String password)
    {
        super(Packet.Type.LOGIN);
        this.username = username;
        this.password = password;
    }
    
    public PacketLogin()
    {
        super(Packet.Type.LOGIN);
    }
    
    public void setUsername(String username)
    {
        this.username = username;
    }
    
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    public String getUsername()
    {
        return this.username;
    }
    public String getPassword()
    {
        return this.password;
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
    public PacketLogin deserialize(DataInputStream dis) throws IOException
    {
        this.username = dis.readUTF();
        this.password = dis.readUTF();
        
        return this;
    }
}
