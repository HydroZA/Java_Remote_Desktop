/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.SocketException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import static main.Server.clientHandlers;
import static main.Server.users;

/**
 *
 * @author James Legge
 */
public class LoginServer implements Runnable
{
    private final Server server;
    private final SSLServerSocket ss;
    private Thread thisThread;

    public Thread getThisThread()
    {
        return thisThread;
    }

    public void setThisThread(Thread thisThread)
    {
        this.thisThread = thisThread;
    }
    
    public LoginServer(Server server, SSLServerSocket ss)
    {
        this.server = server;
        this.ss = ss;
    }
    
    @Override
    public void run()
    {
        while (true)
            {
                try
                {
                    SSLSocket s = (SSLSocket) ss.accept();
                    
                    
                    System.out.println("Incoming connection: " + s.getInetAddress().toString());

                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());                    
                                        
                    Packet.Type tp = Packet.Type.values()[dis.readInt()];

                    switch (tp)
                    {
                        case LOGIN:
                        {
                            PacketLogin pl = new PacketLogin().deserialize(dis);
                            User user = pl.getUser();

                            boolean isSuccess = server.login(user);
                            PacketStatus ps = new PacketStatus();
                            ps.setSuccess(isSuccess);
                            if (isSuccess)
                            {
                                System.out.println(
                                    "\n*********** SECURE CONNECTION ESTABLISHED ***********\n"
                                  + "Client IP: " + s.getInetAddress().toString() +"\n"
                                  + "Username: " + user.getUsername() + "\n"
                                  + "Security Protocol: " + s.getEnabledProtocols()[0] + "\n"
                                  + "Cipher Suite: " + s.getSession().getCipherSuite() + "\n"
                                  //+ "Certificate: " + s.getSession().getLocalCertificates()[0] + "\n"        
                                  + "************ END SECURE CONNECTION STATS ************\n"
                                );

                                ps.setMessage("Sucessfully logged in");
                                ps.send(dos);
                                String[] friends = server.getFriends(user);
                                PacketFriends pf = new PacketFriends(friends);
                                pf.send(dos);
                                user.setFriends(friends);

                                users.add(user);
                                ClientHandler ch = new ClientHandler(server, s, user, dis, dos);
                                Thread t = new Thread(ch);
                                clientHandlers.add(ch);
                                ch.setThisThread(t);
                                t.start();
                                
                                System.out.println("Successful login to account \'" + user.getUsername() + "\' by " + s);
                            }
                            else
                            {
                                ps.setMessage("Invalid Login Credentials");
                                ps.send(dos);
                                System.out.println("Failed login by " + s);
                            }
                            break;
                        }
                        case REGISTER:
                        {
                            PacketRegister pr = new PacketRegister().deserialize(dis);
                            
                            boolean isSuccess = server.register(pr.getUser());
                            PacketStatus ps = new PacketStatus();
                            ps.setSuccess(isSuccess);
                            
                            if(isSuccess)
                            {
                                ps.setMessage("Registered Successfully");                               
                            }
                            else
                            {
                                ps.setMessage("Failed to Register");       
                            }
                            ps.send(dos);
                            break;
                        }
                        default:
                        {
                            // Ignore the packet as it's not meant for this thread
                            System.out.println("Login thread got invalid packet type");
                            break;
                        }
                    }
                }
                catch (SocketException e)
                {
                    System.out.println("Socket exception in Server class");
                    ServerMain.setServerRunning(false);
                    e.getMessage();
                    break;
                }
                catch (Exception e)
                {
                    ServerMain.setServerRunning(false);
                    e.getMessage();
                    break;
                }
            }
    }
    
}
