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
import java.sql.SQLException;
import java.util.logging.Level;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import static rdp.Server.clientHandlers;
import static rdp.Server.users;

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
                });
                
                s.startHandshake();
                
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

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
                            clientHandlers.add(ch);
                            t.start();

                            ServerMain.LOG.info("Successful login to account \'" + user.getUsername() + "\' by " + s);
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
                    default:
                    {
                        // Ignore the packet as it's not meant for this thread
                        ServerMain.LOG.warning("Login thread got invalid packet type");
                        break;
                    }
                }
            }
            catch (SSLHandshakeException e)
            {
                try
                {
                    performCertificateExchange();
                    ServerMain.LOG.info("Certificate Exchange Successful");

                    // break because a new LoginServer will be created in the event of a new certificate being added
                    break;
                }
                catch (Exception ex)
                {
                    ServerMain.LOG.severe("Connection Failed");
                }
            }
            catch (SocketException e)
            {
                ServerMain.LOG.severe("Socket exception in LoginServer");
                ServerMain.setServerRunning(false);
                
            }

            catch (IOException | InterruptedException | SQLException e)
            {
                ServerMain.setServerRunning(false);
                ServerMain.LOG.severe(e.toString());
                break;
            }

        }
    }

}
