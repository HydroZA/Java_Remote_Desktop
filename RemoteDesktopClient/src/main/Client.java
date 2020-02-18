package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.Socket;
import java.io.*;
import java.net.UnknownHostException;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class Client
{
    private String serverIP;
    private User user;
    private int port;
    private Socket sock;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean loggedIn = false;
    private boolean allowingIncomingConnections = false;
    private MainUI mui;
    private IncomingStreamUI isui;
    
    // Parameterized Constructors
    public Client(User user, MainUI mui)
    {
        this.user = user;
        this.mui = mui;
    }

    public Client(User user)
    {
        this.user = user;
    }

    // No-Params Constructor
    public Client()
    {

    }

    public boolean isAllowingIncomingConnections()
    {
        return allowingIncomingConnections;
    }

    public void setAllowingIncomingConnections(boolean allowingIncomingConnections)
    {
        this.allowingIncomingConnections = allowingIncomingConnections;
    }

    public MainUI getMui()
    {
        return mui;
    }

    public void setMui(MainUI mui)
    {
        this.mui = mui;
    }

    public boolean isLoggedIn()
    {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn)
    {
        this.loggedIn = loggedIn;
    }

    // Mutators
    public void setServerIP(String serverIP)
    {
        this.serverIP = serverIP;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    // Accessors
    public String getServerIP()
    {
        return this.serverIP;
    }

    public int getServerPort()
    {
        return this.port;
    }

    public User getUser()
    {
        return this.user;
    }

    public DataInputStream getDis()
    {
        return this.dis;
    }

    public DataOutputStream getDos()
    {
        return this.dos;
    }

    // Private Methods
    private boolean intToBoolean(int i)
    {
        return i == 1;
    }

    public void connect() throws UnknownHostException, IOException
    {
        // Connect to server
        InetAddress ip = InetAddress.getByName(serverIP);
        sock = new Socket(ip, port);
        dis = new DataInputStream(sock.getInputStream());
        dos = new DataOutputStream(sock.getOutputStream());
    }

    public void disconnect() throws IOException
    {
        sock.close();
    }

    // Public Methods
    public void login() throws IOException
    {
        PacketLogin pl = new PacketLogin(this.user.getUsername(), this.user.getPassword());
        pl.send(dos);
    }

    public void logout() throws IOException
    {
        PacketLogout pl = new PacketLogout();
        pl.sendOnlyType(dos);

        this.loggedIn = false;
    }

    public void addFriend(String friendName) throws IOException
    {
        PacketAddFriend paf = new PacketAddFriend(user.getUsername(), friendName);
        paf.send(dos);
    }

    public void removeFriend(String friendName) throws IOException
    {
        PacketRemoveFriend prf = new PacketRemoveFriend(user.getUsername(), friendName);
        prf.send(dos);
    }

    public byte[] takeScreenshot() throws AWTException, IOException
    {
        BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }

    public void askPermissionToConnect(String streamPartner) throws IOException
    {
        PacketConnectRequest pc = new PacketConnectRequest(user.getUsername(), streamPartner);
        pc.send(dos);
    }

    public void streamRequestAccepted(PacketConnectRequest pcr)
    {
        // Do stuff if the stream is accepted
        JOptionPane.showMessageDialog(mui, "Your Stream Request was Granted");
        
        IncomingStreamUI isui = new IncomingStreamUI(mui, pcr);
        isui.setVisible(true);
        this.mui.setVisible(false);
    }

    public void incomingFrame(byte[] frame)
    {
        
    }
    
    public void startOutgoingConnection(String streamPartner) throws IOException
    {

    }
}
