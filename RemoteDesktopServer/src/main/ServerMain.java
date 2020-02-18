package main;

import java.io.*;
import java.net.*;
import java.sql.SQLException;

public class ServerMain
{
    private static Server server;
    
    public static void main(String[] args)
    {
        boolean serverRunning = false;
        
        // Shutdown hook. Code runs when program closed without going through normal exit procedure; ie. when closed using task manager.
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            public void run()
            {
                try
                {
                    server.killServer();
                }
                catch (Exception e)
                {
                    System.out.println("Abnormal Server Shutdown. Database may need to be manually updated");
                }
            }
        });
        
        // get IP from args or use localhost
        String strIP = "127.0.0.1";
        try
        {
            strIP = args[0];
        }
        catch (IndexOutOfBoundsException e)
        {
            System.out.println("No IP given, using localhost");
        }
        
        // Create Server object
        try
        {
            server = new Server(strIP, 1234);
        }
        catch (Exception e)
        {
            System.out.println("Failed to create Server object");
        }
        
        while (true)
        {
            System.out.println(
                "\nSelect an action:\n" +
                    "\t1) Start Server\n" +
                    "\t2) Change IP and Port\n" +
                    "\t3) Restart Server\n" +
                    "\t4) Stop Server\n" +
                    "\t5) Recreate Database\n" +
                    "\t6) Print Active Clients\n" +
                    "\t7) Exit\n"
            );
            
            int action;
            try
            {
                action = Integer.parseInt(System.console().readLine());
            }
            catch (NumberFormatException e)
            {
                System.out.println ("Please choose a valid option");
                continue;
            }
            
            switch (action)
            {
                case 1:                   
                    try
                    {
                        if (!serverRunning)
                        {
                            System.out.println("Starting Server...");
                            server.startServer();
                            serverRunning = true;
                            System.out.println("Server started on IP: " + server.getIP() + ", Port: " + server.getPort());
                        }
                        else
                        {
                            System.out.println("Server already running!");
                        }
                    }
                    catch (BindException e)
                    {
                        System.out.println("Unable to bind to IP: " + server.getIP() + ", Port: " + server.getPort());
                    }
                    catch (UnknownHostException e)
                    {
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    System.out.println("Enter the IP");
                    strIP = System.console().readLine();
                    server.setIP(strIP);

                    System.out.println("Enter the Port");
                    int port = Integer.parseInt(System.console().readLine());
                    server.setPort(port);

                    System.out.println("Updated IP and Port, restart server for changes to take effect");
                    break;
                case 3:
                    try
                    {
                        server.killServer();
                        System.out.println("Killed Server");
                        serverRunning = false;
                        server.startServer();
                        serverRunning = true;
                        System.out.println("Server started on IP: " + server.getIP() + ", Port: " + server.getPort());
                    }
                    catch (BindException e)
                    {
                        System.out.println("Unable to bind to IP: " + server.getIP() + ", Port: " + server.getPort());
                    }
                    catch (SQLException e)
                    {
                        System.out.println ("An SQL Exception occurred");
                        e.printStackTrace();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
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
                    catch (Exception e)
                    {
                        System.out.println("Failed to kill server");
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
                        server.createDatabase();
                        System.out.println("Successfully Recreated Database");
                    }
                    catch (SQLException e)
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
                    break;
            }
        }

    }
}
