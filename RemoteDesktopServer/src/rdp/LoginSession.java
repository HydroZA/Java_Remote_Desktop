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
import java.sql.SQLException;
import java.util.logging.Level;
import javax.net.ssl.SSLSocket;
import static rdp.Server.users;

/**
 *
 * @author James Legge
 */
public class LoginSession implements Runnable
{

    private final Server server;
    private final SSLSocket s;
    private final DataInputStream dis;
    private final DataOutputStream dos;

    public LoginSession(Server server, DataInputStream dis, DataOutputStream dos, SSLSocket s)
    {
        this.server = server;
        this.dis = dis;
        this.dos = dos;
        this.s = s;
    }

    @Override
    public void run()
    {
        boolean endSession = false;
        while (!endSession)
        {
            try
            {
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
                            ps.setMessage("Sucessfully logged in");
                            ps.send(dos);
                            String[] friends = server.getFriends(user);
                            PacketFriends pf = new PacketFriends(friends);
                            pf.send(dos);
                            user.setFriends(friends);

                            users.add(user);
                            ClientHandler ch = new ClientHandler(server, s, user, dis, dos);
                            Thread t = new Thread(ch);
                            Server.clientHandlers.add(ch);
                            t.start();

                            ServerMain.LOG.log(Level.INFO, "Successful login to account ''{0}'' by {1}", new Object[]
                            {
                                user.getUsername(), s
                            });
                            
                            endSession = true;
                        }
                        else
                        {
                            ps.setMessage("Invalid Login Credentials");
                            ps.send(dos);
                            ServerMain.LOG.warning("Failed login by " + s);
                        }
                        break;
                    }
                    case REGISTER:
                    {
                        PacketRegister pr = new PacketRegister().deserialize(dis);

                        boolean isSuccess = server.register(pr.getUser());
                        PacketStatus ps = new PacketStatus();
                        ps.setSuccess(isSuccess);

                        if (isSuccess)
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
                    case CANCEL_LOGIN:
                    {
                        ServerMain.LOG.log(Level.INFO, "Client cancelled login atttempt: {0}", s.toString());
                        s.close();
                        endSession = true;
                        break;
                    }
                    default:
                    {
                        // Ignore the packet as it's not meant for this thread
                        ServerMain.LOG.warning("LoginSession thread got invalid packet type");
                    }
                }
            }
            catch (IOException | InterruptedException | SQLException e)
            {
                endSession = true;
            }
        }
    }

}
