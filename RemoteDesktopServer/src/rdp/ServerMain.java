/* 
 * AUTHOR: James Legge
 * STUDENT#: 17008250
 * INSTITUTION: London Metropolitan University
 * SUBJECT: CS6P05 Project
 * PROJECT TITLE: Using Asymmetrical Encryption and Digital Signatures to Create a Secure Remote Desktop Environment
 * Project Supervisor: Dr. Qicheng Yu
 */
package rdp;

import java.io.*;
import java.net.*;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.sqlite.SQLiteException;

public class ServerMain
{

    private static Server server;
    private static boolean serverRunning = false;
    public static Logger LOG;

    public static void setServerRunning(boolean serverRunning)
    {
        ServerMain.serverRunning = serverRunning;
    }

    public static boolean isServerRunning()
    {
        return serverRunning;
    }

    public static void main(String[] args) throws IOException
    {

        ServerMain.LOG = Logger.getLogger("Server");
        FileHandler fh = new FileHandler ("logs/server.log");
        SimpleFormatter sf = new SimpleFormatter();
        ServerMain.LOG.addHandler(fh);
        fh.setFormatter(sf);
        
        // Shutdown hook. Code runs when program closed without going through normal exit procedure; ie. when closed using task manager.
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    server.killServer();
                }
                catch (IOException | InterruptedException | SQLException e)
                {
                    LOG.info("Abnormal Server Shutdown. Database may need to be manually updated");
                }
            }
        });

        // Create Server object
        try
        {
            server = new Server();
        }
        catch (SQLiteException e)
        {
            LOG.severe("Failed to create SQLite3 Database! " + e.toString());
            System.exit(1);
        }
        catch (Exception e)
        {
            LOG.info("Failed to create Server object");
            e.printStackTrace();
            System.exit(1);
        }

        while (true)
        {
            LOG.info(
                    "\nSelect an action:\n"
                    + "\t1) Start Server\n"
                    + "\t2) Change IP and Port\n"
                    + "\t3) Restart Server\n"
                    + "\t4) Stop Server\n"
                    + "\t5) Recreate Database\n"
                    + "\t6) Print Active Clients\n"
                    + "\t7) Exit\n"
            );

            int action;
            try
            {
                action = Integer.parseInt(System.console().readLine());
            }
            catch (NumberFormatException e)
            {
                LOG.warning("Please choose a valid option");
                continue;
            }

            switch (action)
            {
                case 1:
                    if (!serverRunning)
                    {
                        LOG.info("Starting Server...");

                        try
                        {
                            server.startLoginServer();
                            serverRunning = true;
                            LOG.info("Server started on IP: " + server.getSERVER_IP().getHostName() + ", Port: " + server.getSERVER_PORT());
                        }
                        catch (BindException | UnknownHostException e)
                        {
                            LOG.severe("Unable to bind to IP: " + server.getSERVER_IP().getHostName() + ", Port: " + server.getSERVER_PORT());
                        }
                        catch (IOException e)
                        {
                            LOG.severe("Unable to bind to IP: " + server.getSERVER_IP().getHostName() + ", Port: " + server.getSERVER_PORT());
                        }
                    }
                    else
                    {
                        LOG.info("Server already running!");
                    }

                    break;
                case 2:
                    System.out.println("New IP: ");
                    String ip = System.console().readLine();
                    try
                    {
                        server.setSERVER_IP(InetAddress.getByName(ip));
                    }
                    catch (UnknownHostException e)
                    {
                        LOG.warning("Invalid IP. Please try again");
                        break;
                    }

                    System.out.println("New Port: ");
                    int port = Integer.parseInt(System.console().readLine());
                    server.setSERVER_PORT(port);

                    LOG.info("Updated IP and Port, restart server for changes to take effect");
                    break;
                case 3:
                    try
                    {
                        server.killServer();
                        LOG.warning("Killed Server");
                        serverRunning = false;
                        server.startLoginServer();
                        serverRunning = true;
                        LOG.log(Level.INFO, "Server started on IP: {0}, Port: {1}", new Object[]{server.getSERVER_IP().getHostName(), server.getSERVER_PORT()});
                    }
                    catch (BindException | UnknownHostException ex)
                    {
                        LOG.log(Level.SEVERE, "Unable to bind to IP: {0}, Port: {1}", new Object[]{server.getSERVER_IP().getHostName(), server.getSERVER_PORT()});
                        LOG.severe(ex.toString());
                    }
                    catch (SQLException | IOException | InterruptedException e)
                    {
                        LOG.severe(e.toString());
                    }

                    break;
                case 4:
                    try
                    {
                        if (serverRunning)
                        {
                            server.killServer();
                            serverRunning = false;
                            LOG.warning("Server killed");
                        }
                        else
                        {
                            LOG.warning("Server is not running");
                        }
                    }
                    catch (IOException | InterruptedException | SQLException e)
                    {
                        LOG.severe("Failed to kill server");
                        LOG.severe(e.toString());
                    }
                    break;
                case 5:
                    if (serverRunning)
                    {
                        LOG.warning("Shutdown Server Before Recreating Database");
                        break;
                    }
                    try
                    {
                        LOG.info("Recreating Database...");
                        server.clearDatabase();
                        server.refreshDatabaseConnection();
                        server.createDatabase();
                        LOG.info("Successfully Recreated Database");
                    }
                    catch (SQLException | ClassNotFoundException e)
                    {
                        LOG.severe("Failed to Recreate Database");
                    }
                    break;
                case 6:
                    LOG.log(Level.INFO, "ACTIVE USERS: {0}", Arrays.toString(server.getLoggedInUsers()));
                    break;
                case 7:
                    LOG.info("Exiting Server...");
                    System.exit(0);
                default:
                    LOG.warning("Please choose a valid option");
                    break;
            }
        }
    }
}
