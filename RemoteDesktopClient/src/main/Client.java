package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.io.*;
import javax.imageio.ImageIO;
import javax.net.ssl.SSLSocket;
import javax.swing.JOptionPane;

public class Client
{
    private String serverIP;
    private User user;
    private int port;
    private SSLSocket sock;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean loggedIn = false;
    private boolean allowingIncomingConnections = false;
    private MainUI mui;
    private IncomingStreamUI isui;
    private static final String TLS_VERSION = "TLSv1.2";
    private static final String TRUST_STORE_NAME = "servercert.p12";
    private static final char[] TRUST_STORE_PWD = new char[]
    {
        'a', 'b', 'c', '1',
        '2', '3'
    };
    private static final String KEY_STORE_NAME = "servercert.p12";
    private static final char[] KEY_STORE_PWD = new char[]
    {
        'a', 'b', 'c', '1',
        '2', '3'
    };


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

    public void connect() throws Exception
    {
        // Connect to server
        InetAddress SERVER_IP = InetAddress.getByName(serverIP);
        SSLSocketConnector ssc = new SSLSocketConnector();
        
        //System.setProperty("javax.net.debug", "ssl");
        //System.setProperty("jdk.tls.client.cipherSuites", "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384");
        //System.setProperty("jdk.tls.server.cipherSuites", "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384");

        sock = ssc.connect(
                SERVER_IP,
                port,
                TLS_VERSION,
                TRUST_STORE_NAME,
                TRUST_STORE_PWD,
                KEY_STORE_NAME,
                KEY_STORE_PWD
        );

        dis = new DataInputStream(sock.getInputStream());
        dos = new DataOutputStream(sock.getOutputStream());

        System.out.println(
                "\n*********** SECURE CONNECTION ESTABLISHED ***********\n"
                + "Server IP: " + sock.getInetAddress().toString() + "\n"
                + "Username: " + user.getUsername() + "\n"
                + "Security Protocol: " + sock.getEnabledProtocols()[0] + "\n"
                + "Cipher Suite: " + sock.getSession().getCipherSuite() + "\n"
                //+ "Certificate: " + sock.getSession().getLocalCertificates()[0] + "\n"
                + "************ END SECURE CONNECTION STATS ************\n"
        );
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
