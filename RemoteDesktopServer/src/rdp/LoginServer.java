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
import javax.net.ssl.HandshakeCompletedListener;
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
    private Thread thisThread;
    private final ServerSocket certExchange;
    
    public LoginServer(Server server, SSLServerSocket ss) throws IOException
    {
        this.server = server;
        this.ss = ss;
        certExchange = new ServerSocket(123, 50, server.getSERVER_IP());
        
    }

    public Thread getThisThread()
    {
        return thisThread;
    }

    public void setThisThread(Thread thisThread)
    {
        this.thisThread = thisThread;
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
        File certFile = server.getCh().getCertificate();
        byte[] cert = Files.readAllBytes(certFile.toPath());

        PacketCertificate pc = new PacketCertificate(cert);
        pc.serialize(dos);
    }

    private void receiveCertificate(DataInputStream dis) throws Exception
    {
        PacketCertificate pc = new PacketCertificate().deserialize(dis);
        File certFile = server.getCh().writeCertificateToDisk(pc.getCert());
        server.getCh().importCertificate(certFile);
    }

    private void performCertificateExchange() throws IOException, Exception
    {
        try (Socket ce = certExchange.accept())
        {
            System.out.println("Incoming connection: " + ce.getInetAddress().toString());
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
                    System.out.println(
                            "\n*********** SECURE CONNECTION ESTABLISHED ***********\n"
                            + "Client IP: " + s.getInetAddress().toString() + "\n"
                            + "Security Protocol: " + s.getSession().getProtocol() + "\n"
                            + "Cipher Suite: " + s.getSession().getCipherSuite() + "\n"   
                            + "************ END SECURE CONNECTION STATS ************\n"
                    );
                });

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
                            ch.setThisThread(t);
                            t.start();

                            System.out.println("Successful login to account \'" + user.getUsername() + "\' by " + s);
                        }
                        else
                        {
                            ps.setMessage("Invalid Login Credentials");
                            ps.send(dos);
                            System.out.println("Failed login by " + s);
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
                        System.out.println("Login thread got invalid packet type");
                        break;
                    }
                }
            }
            catch (SSLHandshakeException e)
            {
                try
                {
                    performCertificateExchange();
                    System.out.println("Certificate Exchange Successful");
                }
                catch (Exception ex)
                {
                    System.out.println("Connection Failed");
                }
            }
            catch (SocketException e)
            {
                System.out.println("Socket exception in LoginServer");
                ServerMain.setServerRunning(false);
                break;
            }
            catch (IOException | InterruptedException | SQLException e)
            {
                ServerMain.setServerRunning(false);
                e.printStackTrace();
                break;
            }
        }
    }

}
