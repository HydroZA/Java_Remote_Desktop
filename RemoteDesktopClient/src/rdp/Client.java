/* 
 * AUTHOR: James Legge
 * STUDENT#: 17008250
 * INSTITUTION: London Metropolitan University
 * SUBJECT: CS6P05 Project
 * PROJECT TITLE: Using Asymmetrical Encryption and Digital Signatures to Create a Secure Remote Desktop Environment
 * Project Supervisor: Dr. Qicheng Yu
 */
package rdp;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.imageio.ImageIO;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import org.apache.commons.io.FileUtils;

public class Client
{

    // Parent UI's
    private MainUI mui;
    private IncomingStreamUI isui;
    private OutgoingStreamUI osui;
    private IncomingConnectionThread ict;

    // SSL Vars
    private SSLSocket sock;
    private InetAddress SERVER_IP;
    private int SERVER_PORT;
    private final String TRUST_STORE_NAME;
    private final char[] TRUST_STORE_PWD;
    private final String KEY_STORE_NAME;
    private final char[] KEY_STORE_PWD;
    private final String CERTIFICATE;
    private DataInputStream dis;
    private DataOutputStream dos;

    // Misc Vars
    private User user;
    private boolean loggedIn = false;
    private boolean allowingIncomingConnections = false;
    private static volatile boolean handshakeSuccessful = false;
    private CertificateHandler ch;

    // Create Logger for Log Files
    public static Logger log;

    // No-Params Constructor
    public Client() throws FileNotFoundException, UnknownHostException, Exception
    {
        Client.log = Logger.getLogger("Client");
        FileHandler fh = new FileHandler("logs/client.log");
        SimpleFormatter sf = new SimpleFormatter();
        log.addHandler(fh);
        fh.setFormatter(sf);

        ConfigParser cp;
        try
        {
            cp = new ConfigParser("client.properties").parse();
            log.info("Config File Found");
        }
        catch (FileNotFoundException e)
        {
            log.warning("Config file not found");
            log.info("Creating Config File...");
            File conf = createConfigFile();
            cp = new ConfigParser(conf.getAbsolutePath()).parse();
            log.info("Created Config File");
        }
        log.log(Level.INFO, "Loaded following params from config file:\n{0}", cp.toString());

        this.SERVER_IP = cp.getSERVER_IP();
        this.SERVER_PORT = cp.getSERVER_PORT();
        this.KEY_STORE_NAME = cp.getKEY_STORE_NAME();
        this.KEY_STORE_PWD = cp.getKEY_STORE_PWD();
        this.TRUST_STORE_NAME = cp.getTRUST_STORE_NAME();
        this.TRUST_STORE_PWD = cp.getTRUST_STORE_PWD();
        this.CERTIFICATE = cp.getCERTIFICATE();

        //Generate KeyStore and Certificates if not found, and assign values to CertificateHandler
        if (new File("certs/" + KEY_STORE_NAME + ".jks").exists())
        {
            log.info("TLS Certificate Found");
            ch = new CertificateHandler(CertificateHandler.Type.CLIENT, SERVER_IP.getHostAddress(), TRUST_STORE_NAME, TRUST_STORE_PWD, KEY_STORE_NAME, KEY_STORE_PWD, CERTIFICATE);
            ch.setTrustStore(new File("certs/" + TRUST_STORE_NAME + ".jks"));
            ch.setCertificate(new File("certs/" + CERTIFICATE + ".cer"));
            ch.setKeystore(new File("certs/" + KEY_STORE_NAME + ".jks"));
        }
        else
        {
            log.warning("TLS Certificate Not Found, Generating...");
            ch = new CertificateHandler(CertificateHandler.Type.CLIENT, SERVER_IP.getHostAddress(), TRUST_STORE_NAME, TRUST_STORE_PWD, KEY_STORE_NAME, KEY_STORE_PWD, CERTIFICATE);
            ch = ch.generate();
            log.info("Generated TLS Certificate");
        }

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

    public static boolean isHandshakeSuccessful()
    {
        return handshakeSuccessful;
    }

    public void connect(LoginUI lui) throws Exception
    {
        // Connect to server
        sock = SSLSocketConnector.connect(ch);

        dis = new DataInputStream(sock.getInputStream());
        dos = new DataOutputStream(sock.getOutputStream());

        sock.addHandshakeCompletedListener((HandshakeCompletedEvent hce) ->
        {
            handshakeSuccessful = true;
            Client.log.info(
                    "\n*********** SECURE CONNECTION ESTABLISHED ***********\n"
                    + "Server IP: " + sock.getInetAddress().toString() + "\n"
                    + "Security Protocol: " + sock.getEnabledProtocols()[0] + "\n"
                    + "Cipher Suite: " + sock.getSession().getCipherSuite() + "\n"
                    //+ "Certificate: " + sock.getSession().getLocalCertificates()[0] + "\n"
                    + "************ END SECURE CONNECTION STATS ************\n"
            );
            ict = new IncomingConnectionThread(dis, lui);
            Thread t = new Thread(ict);

            // Let everyone get to know each other
            ict.setClient(this);
            this.setIct(ict);

            // Start IncomingConnectionThread
            t.start();
        });
        try
        {
            sock.startHandshake();
        }
        catch (SSLHandshakeException e)
        {
            Client.log.log(Level.WARNING, "Handshake Failed, Performing Certificate Exchange...");
            try
            {
                performCertificateExchange();

                // Wait for server to restart, then try again, will attempt 5 times
                for (int i = 0; i <= 4; i++)
                {
                    Thread.sleep(100);
                    connect(lui);
                    if (isHandshakeSuccessful())
                    {
                        return;
                    }

                    Client.log.warning("Connection failed, retrying in 100ms...");
                }
                Client.log.severe("Unable to connect to server");
                throw new Exception("Connection to server could not be made");
            }
            catch (Exception ex)
            {
                Client.log.log(Level.SEVERE, "Failed to connect to server {0}", ex.toString());
                System.exit(1);
            }
        }
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
                    + "TRUST_STORE_NAME:truststore\n"
                    + "TRUST_STORE_PWD:abc123\n"
                    + "KEY_STORE_NAME:keystore\n"
                    + "KEY_STORE_PWD:abc123\n"
                    + "CERTIFICATE:client\n"
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

    public void performCertificateExchange() throws Exception
    {
        try
        {
            Socket ce = new Socket(SERVER_IP, 123);
            DataInputStream tempDis = new DataInputStream(ce.getInputStream());
            DataOutputStream tempDos = new DataOutputStream(ce.getOutputStream());

            PacketCertificate pc = new PacketCertificate().deserialize(tempDis);
            File certFile = ch.writeCertificateToDisk(pc.getCert());
            ch.importCertificate(certFile);

            pc = new PacketCertificate(FileUtils.readFileToByteArray(ch.getCertificate()));
            pc.serialize(tempDos);

            ce.close();
        }
        catch (IOException e)
        {
            log.info("Error During Certificate Exchange");
        }
    }

    public void disconnect() throws IOException
    {
        logout();
        sock.close();
    }

    public void login() throws IOException
    {
        PacketLogin pl = new PacketLogin(this.user.getUsername(), this.user.getPassword());
        pl.send(dos);
    }

    public void register() throws IOException
    {
        PacketRegister pr = new PacketRegister(user.getUsername(), user.getPassword());
        pr.send(dos);
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

    public void loginSuccessful(LoginUI lui)
    {
        log.info("Login Succeeded");
        JOptionPane.showMessageDialog(lui, "Logged In Successfully", "Login Attempt", INFORMATION_MESSAGE);

        // Spawn MainUI and dispose LoginUI
        mui = new MainUI(user, this);
        ict.setMainUI(mui);
        mui.setIncomingConnectionThread(ict);
        setMui(mui);

        mui.setVisible(true);
        lui.dispose();
    }

    public void loginFailed(LoginUI lui) throws IOException
    {
        JOptionPane.showMessageDialog(lui, "Login Failed", "Login Attempt", ERROR_MESSAGE);
        log.info("Login Failed");
        ict.getDis().close();
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
        if (osui != null)
        {
            osui.setTerminated(true);
            osui.dispose();
            mui.setVisible(true);
        }
        else if (isui != null)
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
        mui.setVisible(false);
    }
}
