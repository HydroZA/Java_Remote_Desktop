/* 
 * AUTHOR: James Legge
 * STUDENT#: 17008250
 * INSTITUTION: London Metropolitan University
 * SUBJECT: CS6P05 Project
 * PROJECT TITLE: Using Asymmetrical Encryption and Digital Signatures to Create a Secure Remote Desktop Environment
 * Project Supervisor: Dr. Qicheng Yu
 */
package rdp;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.logging.Level;

public class ClientHandler implements Runnable
{

    private final User user;
    private final Socket s;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Server server;
    private volatile boolean terminated;
    private ClientHandler partner;
    private boolean isStreaming;

    public ClientHandler(Server server, Socket s, User user, DataInputStream dis, DataOutputStream dos)
    {
        this.terminated = false;
        this.user = user;
        this.dis = dis;
        this.dos = dos;
        this.s = s;
        this.server = server;
        this.isStreaming = false;
    }

    protected User getUser()
    {
        return this.user;
    }

    protected DataInputStream getDis()
    {
        return dis;
    }

    protected Socket getSocket()
    {
        return s;
    }

    @Override
    public void run()
    {
        while (!terminated)
        {
            try
            {
                Packet.Type tp = Packet.Type.values()[dis.readInt()];

                switch (tp)
                {
                    case ADD_FRIEND:
                    {
                        PacketAddFriend paf = new PacketAddFriend().deserialize(dis);
                        boolean isSuccess = server.addFriend(paf.getUsername(), paf.getFriendName());

                        PacketStatus ps = new PacketStatus();
                        ps.setSuccess(isSuccess);
                        if (isSuccess)
                        {
                            ps.setMessage("Successfully Added Friend");
                            ps.send(dos);

                            String[] friends = server.getFriends(user);
                            PacketFriends pf = new PacketFriends(friends);
                            pf.send(dos);
                            user.setFriends(friends);
                        }
                        else
                        {
                            ps.setMessage("Failed to Add Friend");
                            ps.send(dos);
                        }
                        break;
                    }
                    case REMOVE_FRIEND:
                    {
                        PacketRemoveFriend prf = new PacketRemoveFriend().deserialize(dis);

                        boolean isSuccess = server.removeFriend(prf.getUsername(), prf.getFriendName());
                        PacketStatus ps = new PacketStatus();
                        ps.setSuccess(isSuccess);
                        if (isSuccess)
                        {
                            ps.setMessage("Successfully Removed Friend");
                            ps.send(dos);

                            String[] friends = server.getFriends(user);
                            PacketFriends pf = new PacketFriends(friends);
                            pf.send(dos);
                            user.setFriends(friends);
                        }
                        else
                        {
                            ps.setMessage("Failed to Remove Friend");
                            ps.send(dos);
                        }
                        break;
                    }
                    case CONNECT_REQUEST:
                    {
                        PacketConnectRequest pc = new PacketConnectRequest().deserialize(dis);
                        String partnerUsername = pc.getPartner();
                        String requester = pc.getRequester();

                        // if we are the initiator -> send our request to the partner
                        if (requester.equals(user.getUsername()))
                        {
                            boolean userFound = false;
                            for (ClientHandler ch : Server.clientHandlers)
                            {
                                if (ch.getUser().getUsername().equals(partnerUsername))
                                {
                                    userFound = true;
                                    this.partner = ch;
                                    pc.send(ch.dos);
                                    break;
                                }
                            }
                            if (!userFound)
                            {
                                // Tell the initiator that the user is not online
                                ServerMain.LOG.log(Level.INFO, "{0} attempted to connect to {1} but they are not online", new Object[]
                                {
                                    user.getUsername(), partnerUsername
                                });
                                PacketStatus ps = new PacketStatus(false, "User is not online");
                                ps.send(dos);
                            }
                        }
                        // We are the relay responder -> send our reply the requester
                        else
                        {
                            for (ClientHandler ch : Server.clientHandlers)
                            {
                                if (ch.getUser().getUsername().equals(requester))
                                {
                                    isStreaming = pc.isAccepted();
                                    this.partner = ch;
                                    pc.send(ch.dos);
                                    break;
                                }
                            }

                        }
                        break;
                    }
                    case FRAME:
                    {
                        if (isStreaming)
                        {
                            PacketFrame pf = new PacketFrame().deserialize(dis);
                            pf.send(partner.dos);
                        }
                        else
                        {
                            new PacketFrame().deserialize(dis);
                        }
                        break;
                    }

                    case LOGOUT:
                    {
                        server.logout(user);
                        terminated = true;
                        break;
                    }
                    case STOP_STREAMING:
                    {
                        isStreaming = false;
                        PacketStopStreaming pss = new PacketStopStreaming();
                        pss.sendOnlyType(partner.dos);

                        break;
                    }
                }
            }
            catch (Exception e)
            {
                ServerMain.LOG.log(Level.SEVERE, "Closing ClientHandler thread for user: {0}", user.getUsername());
                try
                {
                    server.logout(user);
                    break;
                }
                catch (SQLException | InterruptedException | IOException ex)
                {
                    ServerMain.LOG.severe(ex.toString());
                    break;
                }
            }
        }
    }
}
