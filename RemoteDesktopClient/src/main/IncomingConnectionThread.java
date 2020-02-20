package main;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;

public class IncomingConnectionThread implements Runnable
{
    private volatile boolean terminated;
    private final DataInputStream dis;
    private MainUI mui;
    private LoginUI lui;
    private IncomingStreamUI isui;
    private OutgoingStreamUI osui;
    private Client client;
    private Thread LoginThread;

    public Thread getLoginThread()
    {
        return LoginThread;
    }

    public void setLoginThread(Thread LoginThread)
    {
        this.LoginThread = LoginThread;
    }
    
    public IncomingConnectionThread(DataInputStream dis, MainUI parentUI)
    {
        this.dis = dis;
        this.mui = parentUI;
        this.terminated = false;
    }

    public IncomingConnectionThread(DataInputStream dis, LoginUI lui)
    {       
        this.dis = dis;
        this.lui = lui;
        this.terminated = false;
    }

    public MainUI getMainUI()
    {
        return mui;
    }

    public DataInputStream getDis()
    {
        return this.dis;
    }
    
    public void setMainUI(MainUI parentUI)
    {
        this.mui = parentUI;
    }

    public Client getClient()
    {
        return client;
    }

    public void setClient(Client client)
    {
        this.client = client;
    }
    
    public void terminate()
    {
        this.terminated = true;
    }
    
    private boolean intToBoolean(int i, int trueValue)
    {
        switch (trueValue)
        {
            case 0:
                return i == 0;
            case 1:
                return i == 1;
            default:
                return i == 1;
        }
    }
    
    @Override
    public void run()
    {
        System.out.println("IncomingConnectionThread Initiated");
        while (!terminated)
        {
            try
            {
                Packet.Type tp = Packet.Type.values()[dis.readInt()];
              //  System.out.println("Incoming: " +tp);
                switch (tp)
                {
                    case STATUS:
                    {
                        PacketStatus ps = new PacketStatus().deserialize(dis);
                        String message = ps.getMessage();
                        
                        if (message.equals("Sucessfully logged in"))
                        {
                            client.setLoggedIn(true);
                            LoginThread.resume();
                        }     
                        else if (message.equals("Invalid Login Credentials"))
                        {
                            client.setLoggedIn(false);
                            LoginThread.resume();
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(this.mui, message, ps.isSuccess() ? "Sucessful" : "Failed", ps.isSuccess() ? INFORMATION_MESSAGE : ERROR_MESSAGE);
                        }
                        
                        break;
                    }
                    case FRIENDS:
                    {
                        PacketFriends pf = new PacketFriends().deserialize(dis);
                        client.getUser().setFriends(pf.getFriends());
                        mui.updateFriendsList();
                        break;
                    }
                    case CONNECT_REQUEST:
                    {
                        PacketConnectRequest pc = new PacketConnectRequest().deserialize(dis);
                                                                    
                        // If the packet we received is a response to our request->
                        if (pc.getStatus() == PacketConnectRequest.Status.ASSIGNED)
                        {
                            if (pc.isAccepted())
                            {
                                client.streamRequestAccepted(pc);
                            }
                            else
                            {
                                JOptionPane.showMessageDialog(mui, "Your Stream Request was Denied", "STREAM REQUEST DENIED", ERROR_MESSAGE);
                            }
                        }

                        // Otherwise it must be another client requesting to connect
                        else
                        {
                            if (client.isAllowingIncomingConnections())
                            {
                                String requester = pc.getRequester();
                                boolean isAccepted = intToBoolean(JOptionPane.showOptionDialog(mui, "\'" + requester + "\' has requested to connect, do you accept?", "INCOMING CONNECTION", YES_NO_OPTION, QUESTION_MESSAGE, null, null, null), 0);

                                if (isAccepted)
                                {
                                    mui.setVisible(false);
                                    osui = new OutgoingStreamUI(mui, pc);
                                    osui.setVisible(true);
                                }
                                pc.setAccepted(isAccepted);
                            }
                            else
                            {
                                pc.setAccepted(false);
                            }
                            
                            pc.setStatus(PacketConnectRequest.Status.ASSIGNED);
                            pc.send(client.getDos());
                        }
                        break;
                    }
                    case FRAME:
                    {
                        if (isui.isVisible())
                        {
                            PacketFrame pf = new PacketFrame().deserialize(dis);
                            byte[] frame = pf.getFrame();
                            isui.updateFrame(frame);
                        }
                    }
                }
            }
            catch (SocketException e)
            {
                terminated = true;
            }
            catch (IOException e)
            {
                terminated = true;                
            }
        }
        System.out.println("IncomingConnectionThread Terminated");
    }

    public IncomingStreamUI getIsui()
    {
        return isui;
    }

    public void setIsui(IncomingStreamUI isui)
    {
        this.isui = isui;
    }

    public LoginUI getLui()
    {
        return lui;
    }

    public void setLui(LoginUI lui)
    {
        this.lui = lui;
    }
    
}
