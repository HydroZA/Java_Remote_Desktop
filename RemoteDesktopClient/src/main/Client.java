package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.io.*;
import java.net.UnknownHostException;
import javax.imageio.ImageIO;
import javax.net.ssl.SSLSocket;

public class Client
{

    // Parent UI's
    private MainUI mui;
    private IncomingStreamUI isui;
    private OutgoingStreamUI osui;
    private IncomingConnectionThread ict;

    // SSL Server Vars
    private SSLSocket sock;
    private InetAddress SERVER_IP;
    private int SERVER_PORT;
    private final String TLS_VERSION = "TLSv1.2";
    private final String TRUST_STORE_NAME;
    private final char[] TRUST_STORE_PWD;
    private final String KEY_STORE_NAME;
    private final char[] KEY_STORE_PWD;
    private DataInputStream dis;
    private DataOutputStream dos;

    // Misc Vars
    private User user;
    private boolean loggedIn = false;
    private boolean allowingIncomingConnections = false;

    // Parameterized Constructors
    public Client(User user, MainUI mui) throws UnknownHostException, FileNotFoundException
    {
        this.user = user;
        this.mui = mui;

        ConfigParser cp;
        try
        {
            System.out.println("Found Config File");
            cp = new ConfigParser("client.properties").parse();
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Creating Config File...");
            File conf = createConfigFile();
            cp = new ConfigParser(conf.getAbsolutePath()).parse();
            System.out.println("Created Config File");
        }
        this.SERVER_IP = cp.getSERVER_IP();
        this.SERVER_PORT = cp.getSERVER_PORT();
        this.KEY_STORE_NAME = cp.getKEY_STORE_NAME();
        this.KEY_STORE_PWD = cp.getKEY_STORE_PWD();
        this.TRUST_STORE_NAME = cp.getTRUST_STORE_NAME();
        this.TRUST_STORE_PWD = cp.getTRUST_STORE_PWD();
    }

    public Client(User user) throws FileNotFoundException, UnknownHostException
    {
        this.user = user;

                ConfigParser cp;
        try
        {
            System.out.println("Found Config File");
            cp = new ConfigParser("client.properties").parse();
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Creating Config File...");
            File conf = createConfigFile();
            cp = new ConfigParser(conf.getAbsolutePath()).parse();
            System.out.println("Created Config File");
        }
        this.SERVER_IP = cp.getSERVER_IP();
        this.SERVER_PORT = cp.getSERVER_PORT();
        this.KEY_STORE_NAME = cp.getKEY_STORE_NAME();
        this.KEY_STORE_PWD = cp.getKEY_STORE_PWD();
        this.TRUST_STORE_NAME = cp.getTRUST_STORE_NAME();
        this.TRUST_STORE_PWD = cp.getTRUST_STORE_PWD();
    }

    // No-Params Constructor
    public Client() throws FileNotFoundException, UnknownHostException
    {
        ConfigParser cp;
        try
        {
            cp = new ConfigParser("client.properties").parse();
            System.out.println("Found Config File");
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Creating Config File...");
            File conf = createConfigFile();
            cp = new ConfigParser(conf.getAbsolutePath()).parse();
            System.out.println("Created Config File");
        }
        this.SERVER_IP = cp.getSERVER_IP();
        this.SERVER_PORT = cp.getSERVER_PORT();
        this.KEY_STORE_NAME = cp.getKEY_STORE_NAME();
        this.KEY_STORE_PWD = cp.getKEY_STORE_PWD();
        this.TRUST_STORE_NAME = cp.getTRUST_STORE_NAME();
        this.TRUST_STORE_PWD = cp.getTRUST_STORE_PWD();
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
    public void setServerIP(InetAddress serverIP)
    {
        this.SERVER_IP = serverIP;
    }

    public void setPort(int port)
    {
        this.SERVER_PORT = port;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public void setIct(IncomingConnectionThread ict)
    {
        this.ict = ict;
    }

    // Accessors
    public InetAddress getServerIP()
    {
        return this.SERVER_IP;
    }

    public int getServerPort()
    {
        return this.SERVER_PORT;
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

    public void connect() throws Exception
    {
        // Connect to server
        SSLSocketConnector ssc = new SSLSocketConnector();
        
        sock = ssc.connect(SERVER_IP,
                SERVER_PORT,
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
    
    
    private File createConfigFile()
    {
        File conf = new File("client.properties");
        try
        {
            BufferedWriter bw = new BufferedWriter(new FileWriter(conf));
            bw.write(
                              "#\n"
                            + "# Config File for Java_Remote_Desktop\n"
                            + "# DO NOT EDIT UNLESS YOU KNOW WHAT YOU ARE DOING\n"
                            + "#\n"
                            + "SERVER_IP:127.0.0.1\n"
                            + "SERVER_PORT:1234\n"
                            + "TRUST_STORE_NAME:certs/localhost-clientcert.p12\n"
                            + "TRUST_STORE_PWD:abc123\n"
                            + "KEY_STORE_NAME:certs/localhost-clientcert.p12\n"
                            + "KEY_STORE_PWD:abc123\n"
                            + "#TLS_VERSION:TLSv1.2"
            );
            bw.close();
        }
        catch (IOException e)
        {
            return null;
        }
        return conf;
    }

    public void disconnect() throws IOException
    {
        sock.close();
    }

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

    public OutgoingStreamUI getOsui()
    {
        return osui;
    }

    public void setOsui(OutgoingStreamUI osui)
    {
        this.osui = osui;
    }

    public void stopStream() throws IOException
    {
        PacketStopStreaming pss = new PacketStopStreaming();
        pss.sendOnlyType(dos);
    }

    public void resetToMainUI()
    {
        if (osui.isVisible())
        {
            osui.setTerminated(true);
            osui.dispose();
            mui.setVisible(true);
        }
        else if (isui.isVisible())
        {
            isui.dispose();
            mui.setVisible(true);
        }
    }

    public void streamRequestAccepted(PacketConnectRequest pcr)
    {
        // Do stuff if the stream is accepted
        //JOptionPane.showMessageDialog(mui, "Your Stream Request was Granted");

        isui = new IncomingStreamUI(mui, pcr);
        isui.setVisible(true);
        ict.setIsui(isui);
        this.mui.setVisible(false);
    }
}
