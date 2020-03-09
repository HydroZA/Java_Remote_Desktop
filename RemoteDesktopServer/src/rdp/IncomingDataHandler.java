/* 
 * AUTHOR: James Legge
 * STUDENT#: 17008250
 * INSTITUTION: London Metropolitan University
 * SUBJECT: CS6P05 Project
 * PROJECT TITLE: Using Asymmetrical Encryption and Digital Signatures to Create a Secure Remote Desktop Environment
 * Project Supervisor: Dr. Qicheng Yu
 */
package rdp;

import java.awt.HeadlessException;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;

public class IncomingDataHandler implements Runnable
{

    private volatile boolean terminated;
    private final DataInputStream dis;
    private MainUI mui;
    private LoginUI lui;
    private IncomingStreamUI isui;
    private OutgoingStreamUI osui;
    private Client client;

    public IncomingDataHandler(DataInputStream dis, MainUI parentUI)
    {
        this.dis = dis;
        this.mui = parentUI;
        this.terminated = false;
    }

    public IncomingDataHandler(DataInputStream dis, LoginUI lui)
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
        Client.log.info("IncomingConnectionThread Initiated");
        while (!terminated)
        {
            try
            {
                int i = dis.readInt();
                Packet.Type tp;
                try
                {
                    tp = Packet.Type.values()[i];
                }
                catch (IndexOutOfBoundsException e)
                {
                    Client.log.warning("Incoming Connection Thread received invalid request: " + e.toString());
                    continue;
                }
                switch (tp)
                {
                    case STATUS:
                    {
                        PacketStatus ps = new PacketStatus().deserialize(dis);
                        String message = ps.getMessage();

                        switch (message)
                        {
                            case "Sucessfully logged in":
                                client.setLoggedIn(true);
                                client.loginSuccessful(lui);
                                break;
                            case "Invalid Login Credentials":
                                client.setLoggedIn(false);
                                client.loginFailed(lui);
                                break;
                            case "Registered Successfully":
                            {
                                JOptionPane.showMessageDialog(lui, message, "Sucessful", INFORMATION_MESSAGE);
                                break;
                            }
                            case "Failed to Register":
                            {
                                JOptionPane.showMessageDialog(lui, message, "Failed", ERROR_MESSAGE);
                                break;
                            }
                            default:
                                JOptionPane.showMessageDialog(this.mui, message, ps.isSuccess() ? "Sucessful" : "Failed", ps.isSuccess() ? INFORMATION_MESSAGE : ERROR_MESSAGE);
                                break;
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
                                Client.log.info("Stream Request Denied");
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
                                    client.setOsui(osui);
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
                        break;
                    }
                    case STOP_STREAMING:
                    {

                        try
                        {
                            if (osui.isVisible())
                            {
                                osui.setTerminated(true);
                                client.resetToMainUI();
                            }
                        }

                        catch (NullPointerException e)
                        {
                            if (isui.isVisible())
                            {
                                client.resetToMainUI();
                            }
                        }

                        break;
                    }
                }
            }
            /* Socket exception has to be handled seperately as it is a subclass of IOException
             * so can not be in the same catch block 
             */
            catch (SocketException e) 
            {
                terminated = true;
            }
            catch (IOException | HeadlessException e)
            {
                terminated = true;
            }
        }
        Client.log.info("IncomingConnectionThread Terminated");
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
