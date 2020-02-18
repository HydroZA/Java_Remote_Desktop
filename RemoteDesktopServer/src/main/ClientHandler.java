package main;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable
{

    private User user;
    private Socket s;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Server server;
    private volatile boolean terminated;
    private ClientHandler partner;
    
    public ClientHandler(Server server, Socket s, User user, DataInputStream dis, DataOutputStream dos)
    {
        this.terminated = false;
        this.user = user;
        this.dis = dis;
        this.dos = dos;
        this.s = s;
        this.server = server;
    }

    public User getUser()
    {
        return this.user;
    }

    public DataInputStream getDis()
    {
        return dis;
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
                        String partner = pc.getPartner();
                        String requester = pc.getRequester();

                        if (requester.equals(user.getUsername()))
                        {
                            for (ClientHandler ch : Server.clientHandlers)
                            {
                                if (ch.getUser().getUsername().equals(partner))
                                {
                                    this.partner = ch;
                                    pc.send(ch.dos);
                                }
                            }
                        }
                        else
                        {
                            for (ClientHandler ch : Server.clientHandlers)
                            {
                                if (ch.getUser().getUsername().equals(requester))
                                {
                                    pc.send(ch.dos);
                                }
                            }
                        }
                        break;
                    }
                    case FRAME:
                    {
                        PacketFrame pf = new PacketFrame().deserialize(dis);
                        pf.send(partner.dos);
                    }

                    case LOGOUT:
                    {
                        server.logout(user);
                        s.close();
                        terminated = true;
                        break;
                    }
                }
            }

            catch (Exception e)
            {
                System.out.println("Failure in ClientHandler thread for user: " + user.getUsername());
                e.printStackTrace();
                break;
            }
        }
    }
}

