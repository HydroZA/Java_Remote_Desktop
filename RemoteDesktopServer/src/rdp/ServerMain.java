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

public class ServerMain
{

    private static Server server;
    private static boolean serverRunning = false;

    public static boolean isServerRunning()
    {
        return serverRunning;
    }

    public static void setServerRunning(boolean serverRunning)
    {
        serverRunning = serverRunning;
    }

    public static void main(String[] args)
    {

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
                    System.out.println("Abnormal Server Shutdown. Database may need to be manually updated");
                }
            }
        });

        // Create Server object
        try
        {
            server = new Server();
        }
        catch (Exception e)
        {
            System.out.println("Failed to create Server object");
        }

        while (true)
        {
            System.out.println(
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
                System.out.println("Please choose a valid option");
                continue;
            }

            switch (action)
            {
                case 1:
                    if (!serverRunning)
                    {
                        System.out.println("Starting Server...");

                        try
                        {
                            server.startLoginServer();
                            serverRunning = true;
                            System.out.println("Server started on IP: " + server.getSERVER_IP().getHostName() + ", Port: " + server.getSERVER_PORT());
                        }
                        catch (BindException | UnknownHostException e)
                        {
                            System.out.println("Unable to bind to IP: " + server.getSERVER_IP().getHostName() + ", Port: " + server.getSERVER_PORT());
                            System.out.println(e.getMessage());
                        }
                        catch (IOException e)
                        {
                            System.out.println("Unable to bind to IP: " + server.getSERVER_IP().getHostName() + ", Port: " + server.getSERVER_PORT());
                            System.out.println(e.getMessage());
                        }
                    }
                    else
                    {
                        System.out.println("Server already running!");
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
                        System.out.println("Invalid IP. Please try again");
                        System.out.println(e.getMessage());
                        break;
                    }

                    System.out.println("New Port: ");
                    int port = Integer.parseInt(System.console().readLine());
                    server.setSERVER_PORT(port);

                    System.out.println("Updated IP and Port, restart server for changes to take effect");
                    break;
                case 3:
                    try
                    {
                        server.killServer();
                        System.out.println("Killed Server");
                        serverRunning = false;
                        server.startLoginServer();
                        serverRunning = true;
                        System.out.println("Server started on IP: " + server.getSERVER_IP().getHostName() + ", Port: " + server.getSERVER_PORT());
                    }
                    catch (BindException | UnknownHostException ex)
                    {
                        System.out.println("Unable to bind to IP: " + server.getSERVER_IP().getHostName() + ", Port: " + server.getSERVER_PORT());
                        System.out.println(ex.getMessage());
                    }
                    catch (SQLException | IOException | InterruptedException e)
                    {
                        System.out.println(e.getMessage());
                    }

                    break;
                case 4:
                    try
                    {
                        if (serverRunning)
                        {
                            server.killServer();
                            serverRunning = false;
                            System.out.println("Server killed");
                        }
                        else
                        {
                            System.out.println("Server is not running");
                        }
                    }
                    catch (IOException | InterruptedException | SQLException e)
                    {
                        System.out.println("Failed to kill server");
                        System.out.println(e.getMessage());
                    }
                    break;
                case 5:
                    if (serverRunning)
                    {
                        System.out.println("Shutdown Server Before Recreating Database");
                        break;
                    }
                    try
                    {
                        System.out.println("Recreating Database...");
                        server.clearDatabase();
                        server.refreshDatabaseConnection();
                        server.createDatabase();
                        System.out.println("Successfully Recreated Database");
                    }
                    catch (SQLException | ClassNotFoundException e)
                    {
                        System.out.println("Failed to Recreate Database");
                    }
                    break;
                case 6:
                    String[] activeUsers = server.getLoggedInUsers();

                    System.out.println("ACTIVE USERS:");
                    for (String user : activeUsers)
                    {
                        System.out.println(user);
                    }
                    break;
                case 7:
                    System.out.println("Exiting Server...");
                    System.exit(0);
                default:
                    System.out.println("Please choose a valid option");
                    break;
            }
        }

    }
}
