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
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.logging.Level;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

public class LoginServer implements Runnable
{

    private final Server server;
    private final SSLServerSocket ss;
    private final ServerSocket certExchange;

    public LoginServer(Server server, SSLServerSocket ss) throws IOException
    {
        this.server = server;
        this.ss = ss;
        certExchange = new ServerSocket(123, 50, server.getSERVER_IP());

    }

    public ServerSocket getCertExchange()
    {
        return certExchange;
    }

    public SSLServerSocket getSs()
    {
        return ss;
    }

    private void sendCertificate(DataOutputStream dos) throws IOException
    {
        File certFile = server.getCertificateHandler().getCertificate();
        byte[] cert = Files.readAllBytes(certFile.toPath());

        PacketCertificate pc = new PacketCertificate(cert);
        pc.serialize(dos);
    }

    private void receiveCertificate(DataInputStream dis) throws Exception
    {
        PacketCertificate pc = new PacketCertificate().deserialize(dis);
        File certFile = server.getCertificateHandler().writeCertificateToDisk(pc.getCert());
        server.getCertificateHandler().importCertificate(certFile);
    }

    private void performCertificateExchange() throws IOException, Exception
    {
        try (Socket ce = certExchange.accept())
        {
            ServerMain.LOG.log(Level.INFO, "Incoming connection: {0}", ce.getInetAddress().toString());
            DataInputStream tempDis = new DataInputStream(ce.getInputStream());
            DataOutputStream tempDos = new DataOutputStream(ce.getOutputStream());
            sendCertificate(tempDos);
            receiveCertificate(tempDis);

            ce.close();

            // Restart Server so it can see the new certificate
            server.restartLoginServer();
        }

    }

    @Override
    public void run()
    {
        ServerMain.LOG.info("LoginServer Started");
        while (true)
        {
            try
            {
                SSLSocket s = (SSLSocket) ss.accept();

                s.addHandshakeCompletedListener((HandshakeCompletedEvent hce) ->
                {
                    ServerMain.LOG.info(
                            "\n*********** SECURE CONNECTION ESTABLISHED ***********\n"
                            + "Client IP: " + s.getInetAddress().toString() + "\n"
                            + "Security Protocol: " + s.getSession().getProtocol() + "\n"
                            + "Cipher Suite: " + s.getSession().getCipherSuite() + "\n"
                            + "************ END SECURE CONNECTION STATS ************\n"
                    );
                    try
                    {
                        DataInputStream dis = new DataInputStream(s.getInputStream());
                        DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                        //Handoff client to a LoginSession thread so we can continue listening
                        LoginSession ls = new LoginSession(server, dis, dos, s);
                        Thread t = new Thread(ls);
                        t.start();
                    }
                    catch (IOException e)
                    {
                        ServerMain.LOG.log(Level.SEVERE, "Failure while getting datastreams for socket: {0}", s);
                    }
                });

                s.startHandshake();
            }
            catch (SSLHandshakeException e)
            {
                ServerMain.LOG.info("Attempting a Certificate Exchange");
                try
                {
                    performCertificateExchange();
                    ServerMain.LOG.info("Certificate Exchange Successful");

                    // break because a new LoginServer will be created in the event of a new certificate being added
                    break;
                }
                catch (Exception ex)
                {
                    ServerMain.LOG.warning("Certificate Exchange Failed");
                    ServerMain.LOG.info(ex.toString());
                }
            }
            catch (SocketException e)
            {
                ServerMain.LOG.severe("Socket exception in LoginServer");
                ServerMain.LOG.info(e.toString());
                break;
            }

            catch (IOException e)
            {
                ServerMain.LOG.severe(e.toString());
                break;
            }
        }

        ServerMain.LOG.warning("LoginServer Closed");
    }
}
