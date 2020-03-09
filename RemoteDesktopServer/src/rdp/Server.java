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
import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import javax.net.ssl.SSLServerSocket;
import static rdp.CertificateHandler.Type.SERVER;

public final class Server
{

    // SSL Vars
    private SSLServerSocket ss;
    private InetAddress SERVER_IP;
    private int SERVER_PORT;
    private final String TRUST_STORE_NAME;
    private final char[] TRUST_STORE_PWD;
    private final String KEY_STORE_NAME;
    private final char[] KEY_STORE_PWD;
    private final String CERTIFICATE;

    // Misc Vars
    private Connection con;
    private LoginServer ls;
    protected volatile static Vector<User> users = new Vector<>();
    protected volatile static Vector<ClientHandler> clientHandlers = new Vector<>();
    private CertificateHandler ch;

    // paramaterized constructor
    protected Server() throws ClassNotFoundException, SQLException, UnknownHostException, FileNotFoundException, Exception
    {
        ConfigParser cp;
        try
        {
            cp = new ConfigParser("server.properties").parse();
            ServerMain.LOG.info("Found Config File");
        }
        catch (FileNotFoundException e)
        {
            ServerMain.LOG.warning("Config File not Found");
            ServerMain.LOG.info("Creating Config File...");
            File conf = createConfigFile();
            cp = new ConfigParser(conf.getAbsolutePath()).parse();
            ServerMain.LOG.info("Created Config File");
        }

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
            ServerMain.LOG.info("TLS Certificate Found");
            ch = new CertificateHandler(SERVER, SERVER_IP.getHostAddress(), TRUST_STORE_NAME, TRUST_STORE_PWD, KEY_STORE_NAME, KEY_STORE_PWD, CERTIFICATE);
            ch.setTrustStore(new File("certs/" + TRUST_STORE_NAME + ".jks"));
            ch.setCertificate(new File("certs/" + CERTIFICATE + ".cer"));
            ch.setKeystore(new File("certs/" + KEY_STORE_NAME + ".jks"));
        }
        else
        {
            ServerMain.LOG.warning("TLS Certificate Not Found, Generating...");
            ch = new CertificateHandler(SERVER, SERVER_IP.getHostAddress(), TRUST_STORE_NAME, TRUST_STORE_PWD, KEY_STORE_NAME, KEY_STORE_PWD, CERTIFICATE);
            ch = ch.generate();
            ServerMain.LOG.info("Generated TLS Certificate");
        }

        ServerMain.LOG.info(ch.toString());

        this.con = getDatabaseConnection();
        if (!databaseExists())
        {
            ServerMain.LOG.info("Creating Database...");
            createDatabase();
            ServerMain.LOG.info("Database Created");
        }
    }

    protected CertificateHandler getCertificateHandler()
    {
        return ch;
    }

    // Mutators
    protected void setSERVER_IP(InetAddress SERVER_IP)
    {
        this.SERVER_IP = SERVER_IP;
    }

    protected void setSERVER_PORT(int SERVER_PORT)
    {
        this.SERVER_PORT = SERVER_PORT;
    }

    // Accessors
    protected InetAddress getSERVER_IP()
    {
        return SERVER_IP;
    }

    protected int getSERVER_PORT()
    {
        return SERVER_PORT;
    }

    // Methods
    protected Connection getDatabaseConnection() throws ClassNotFoundException, SQLException
    {
        Connection dbCon;

        Class.forName("org.sqlite.JDBC");
        dbCon = DriverManager.getConnection("jdbc:sqlite:rdp.sqlite");

        return dbCon;
    }

    protected void refreshDatabaseConnection() throws ClassNotFoundException, SQLException
    {
        this.con = getDatabaseConnection();
    }

    protected boolean databaseExists()
    {
        try
        {
            String query = "SELECT * FROM friends";
            PreparedStatement stmt = con.prepareStatement(query);
            return true;
        }
        catch (SQLException e)
        {
            return false;
        }
    }

    protected void createDatabase() throws SQLException
    {
        // Define SQL
        String usersTable = "CREATE TABLE IF NOT EXISTS USERS (\n"
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "username TEXT NOT NULL,\n"
                + "pword TEXT NOT NULL,\n"
                + "loggedin integer"
                + ");";
        String friendsTable = "CREATE TABLE IF NOT EXISTS friends (\n"
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "userid INTEGER NOT NULL,\n"
                + "friendid INTEGER NOT NULL"
                + ");";

        String usersData = "INSERT INTO USERS(\n"
                + "username,\n"
                + "pword)\n"
                + "VALUES\n"
                + "(\'hydro\',"
                + "\'26350AA738773C07525F1C0CBA0426B2D27EF51977F4AEC2731A7DE4D7B5FA79\'),"
                + "(\'yahmes\',"
                + "\'C322D8899DFDF70FEC08F4629BBB1E84BBF9291DDE484175DA52428ADDFCFA9F\');";

        String friendsData = "INSERT INTO friends (\n"
                + "userid,\n"
                + "friendid)\n"
                + "VALUES\n"
                + "(1,\n"
                + "2),\n"
                + "(2,\n"
                + "1);";
        // Get statements
        Statement usersTableStatement = con.createStatement();
        Statement friendsTableStatement = con.createStatement();
        Statement usersDataStatement = con.createStatement();
        Statement friendsDataStatement = con.createStatement();

        // Execute Table Creation SQL
        usersTableStatement.execute(usersTable);
        friendsTableStatement.execute(friendsTable);

        // Execute Data Statements
        usersDataStatement.execute(usersData);
        friendsDataStatement.execute(friendsData);

        // Close Everything
        usersTableStatement.close();
        friendsTableStatement.close();
        usersDataStatement.close();
        friendsDataStatement.close();

    }

    protected void clearDatabase() throws SQLException
    {
        String removeUsersTableSQL = "DROP TABLE IF EXISTS USERS";
        String removeFriendsTableSQL = "DROP TABLE IF EXISTS friends";

        Statement stmtRemoveUsersTable = con.createStatement();
        Statement stmtRemoveFriendsTable = con.createStatement();

        stmtRemoveUsersTable.execute(removeUsersTableSQL);
        stmtRemoveFriendsTable.execute(removeFriendsTableSQL);

        stmtRemoveUsersTable.close();
        stmtRemoveFriendsTable.close();
        con.close();
    }

    protected boolean register(User user) throws SQLException
    {
        String username = user.getUsername();
        String pword = user.getPassword();

        String query = "SELECT EXISTS ( SELECT 1 FROM USERS WHERE username=\'" + username + "\')";
        PreparedStatement stmt = con.prepareStatement(query);

        //find if username already exists
        ResultSet rs = stmt.executeQuery();

        if (rs.getInt(1) == 0)
        {
            // Username does not exist, add user to db
            rs.close();
            stmt.close();

            query = "INSERT INTO USERS (username, pword) VALUES (\'" + username + "\', \'" + pword + "\')";
            try (PreparedStatement stmt2 = con.prepareStatement(query))
            {
                stmt2.execute();
            }
            ServerMain.LOG.log(Level.INFO, "Created new user: {0}", username);
            return true;
        }
        else
        {
            // Username already exists
            ServerMain.LOG.log(Level.SEVERE, ": Failed to create new user using name: {0}", username);
            return false;
        }

    }

    protected boolean login(User user) throws SQLException, InterruptedException
    {
        String query = "SELECT 1 FROM USERS WHERE EXISTS (SELECT * FROM USERS WHERE username=\'" + user.getUsername() + "\' AND pword=\'" + user.getPassword() + "\');";
        PreparedStatement stmt = con.prepareStatement(query);

        ResultSet rs = stmt.executeQuery();
        if (!rs.next())
        {
            return false;
        }
        if (rs.getInt(1) == 1)
        {
            // Requested user exists
            // find out if user already logged in
            query = "SELECT loggedin FROM USERS WHERE username=\'" + user.getUsername() + "\'";
            stmt = con.prepareStatement(query);
            ResultSet rs2 = stmt.executeQuery();

            if (rs2.getInt(1) == 0)
            {
                // User not logged in 
                rs2.close();

                query = "UPDATE USERS SET loggedin=1 WHERE username=\'" + user.getUsername() + "\'";
                stmt = con.prepareStatement(query);
                stmt.execute();

                stmt.close();
                rs.close();

                return true;
            }
            else
            {
                // user is already logged in
                rs.close();
                rs2.close();
                stmt.close();

                return false;
            }
        }
        else
        {
            rs.close();

            return false;
        }
    }

    private File createConfigFile()
    {
        File conf = new File("server.properties");
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
                    + "CERTIFICATE:server\n"
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

    protected void logout(User user) throws SQLException, InterruptedException, IOException
    {
        // Update DB to show logged out user
        String query = "UPDATE USERS SET loggedin=0 WHERE username=\'" + user.getUsername() + "\'";
        try (PreparedStatement stmt = con.prepareStatement(query))
        {
            stmt.execute();
        }

        // Remove user from Users vector
        users.remove(user);

        // Very hacky and slow, look into improvements
        for (ClientHandler c : clientHandlers)
        {
            if (c.getUser().getUsername().equals(user.getUsername()))
            {
                c.getSocket().close();
                clientHandlers.remove(c);

                break;
            }
        }
        ServerMain.LOG.log(Level.INFO, "{0} logged out", user.getUsername());
    }

    protected String[] getLoggedInUsers()
    {
        String[] usernames = new String[clientHandlers.size()];

        int i = 0;
        for (ClientHandler c : clientHandlers)
        {
            usernames[i] = c.getUser().getUsername();
            i++;
        }

        return usernames;
    }

    protected void logoutAllUsers() throws SQLException, IOException
    {
        // Close all the sockets for the active users
        for (ClientHandler c : clientHandlers)
        {
            c.getSocket().close();

            String query = "UPDATE USERS SET loggedin=0 WHERE username=\'" + c.getUser().getUsername() + "\'";
            try (PreparedStatement stmt = con.prepareStatement(query))
            {
                stmt.execute();
            }
        }

        // Clear Vectors
        users.clear();
        clientHandlers.clear();
    }

    protected String[] getFriends(User user) throws SQLException
    {
        String query = "SELECT id FROM USERS WHERE username=\'" + user.getUsername() + "\'";
        PreparedStatement stmt = con.prepareStatement(query);

        ResultSet rs = stmt.executeQuery();
        int userid = rs.getInt(1);
        stmt.close();
        rs.close();
        query = "SELECT friendid FROM friends WHERE userid=\'" + userid + "\'";
        PreparedStatement stmt2 = con.prepareStatement(query);

        ResultSet rs2 = stmt2.executeQuery();

        ArrayList<Integer> friendIDs = new ArrayList<>();

        while (rs2.next())
        {
            friendIDs.add(rs2.getInt(1));
        }

        int rowCount = friendIDs.size();

        rs2.close();
        stmt2.close();

        ArrayList<String> friendsAL = new ArrayList<>();

        for (int i = 0; i < rowCount; i++)
        {
            query = "SELECT username FROM USERS WHERE id=\'" + friendIDs.get(i) + "\'";
            PreparedStatement stmt3 = con.prepareStatement(query);

            ResultSet rs3 = stmt3.executeQuery();
            friendsAL.add(rs3.getString(1));
        }

        String[] friends = friendsAL.toArray(new String[friendsAL.size()]);
        return friends;
    }

    protected void killServer() throws IOException, SQLException, InterruptedException
    {
        // logout all users
        logoutAllUsers();

        try
        {
            ls.getSs().close();
            ls.getCertExchange().close();
            ss.close();
        }
        catch (NullPointerException e)
        {
            ServerMain.LOG.severe(e.toString());
        }

        ServerMain.LOG.info("Server Shutdown");
    }

    protected boolean addFriend(String username, String friendName)
    {
        try
        {
            int userid;
            String query = "SELECT id FROM USERS WHERE username=\'" + username + "\'";
            PreparedStatement stmt5 = con.prepareStatement(query);

            ResultSet rs4 = stmt5.executeQuery();

            userid = rs4.getInt(1);

            query = "SELECT EXISTS ( SELECT 1 FROM USERS WHERE username=\'" + friendName + "\')";
            PreparedStatement stmt = con.prepareStatement(query);

            ResultSet rs = stmt.executeQuery();

            if (rs.getInt(1) == 1)
            {
                //get userid from friend
                query = "SELECT id FROM USERS WHERE username=\'" + friendName + "\'";
                PreparedStatement stmt2 = con.prepareStatement(query);

                ResultSet rs2 = stmt2.executeQuery();

                int friendid = rs2.getInt(1);

                stmt2.close();
                rs2.close();

                // Check if users are already friends
                query = "SELECT EXISTS (SELECT 1 FROM friends WHERE userid=" + userid + " AND friendid=" + friendid + ")";
                PreparedStatement stmt3 = con.prepareStatement(query);

                ResultSet rs3 = stmt3.executeQuery();

                if (rs3.getInt(1) == 1)
                {
                    stmt3.close();
                    rs3.close();
                    return false;
                }
                else
                {
                    // create friend link
                    query = "INSERT INTO friends (userid, friendid) VALUES (" + userid + ", " + friendid + ")";
                    PreparedStatement stmt4 = con.prepareStatement(query);

                    stmt4.execute();

                    stmt4.close();

                    ServerMain.LOG.info("\'" + username + "\' added \'" + friendName + "\' as a friend");
                    return true;
                }
            }
            else
            {
                ServerMain.LOG.info("Requested friend not found");

                return false;
            }
        }
        catch (SQLException e)
        {
            ServerMain.LOG.severe(e.toString());
            return false;
        }
    }

    protected boolean removeFriend(String user, String friendName) throws SQLException
    {
        // get id of user and friend
        String query = "SELECT id FROM USERS WHERE username=\'" + user + "\' OR username=\'" + friendName + "\'";
        PreparedStatement stmt = con.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int userID = rs.getInt(1);
        rs.next();
        int friendID = rs.getInt(1);
        rs.close();
        stmt.close();

        // find if entry exists in DB
        query = "SELECT id FROM friends WHERE userid=" + userID + " AND friendid=" + friendID;
        PreparedStatement stmt2 = con.prepareStatement(query);
        ResultSet rs2 = stmt2.executeQuery();

        if (rs2.next() == false)
        {
            // Users are not friends, return false
            ServerMain.LOG.log(Level.WARNING, "''{0}'' attempted to remove friend: ''{1}'' but they are not friends", new Object[]
            {
                user, friendName
            });
            return false;
        }
        else
        {
            // users are friends, remove from DB 
            int friendEntryID = rs2.getInt(1);
            query = "DELETE FROM friends WHERE id=" + friendEntryID;
            PreparedStatement stmt3 = con.prepareStatement(query);
            stmt3.execute();

            ServerMain.LOG.log(Level.INFO, "''{0}'' removed ''{1}'' as a friend", new Object[]{user, friendName});
            return true;
        }
    }

    protected void restartLoginServer() throws BindException, UnknownHostException, IOException, SQLException, InterruptedException
    {
        logoutAllUsers();

        if (ls != null)
        {
            ls.getSs().close();
            ls.getCertExchange().close();
        }

        startLoginServer();
    }

    protected void startLoginServer() throws BindException, UnknownHostException, IOException
    {
        try
        {
            ss = SSLSocketCreator.getSecureServerSocket(ch);
        }

        catch (Exception e)
        {
            ServerMain.LOG.log(Level.SEVERE, "Failed to get SSL Server Socket{0}", e.toString());
            return;
        }

        // This thread handles incoming connections, gets the users friends and then hands the client off to a ClientHandler thread
        ls = new LoginServer(this, ss);
        Thread t = new Thread(ls);
        t.start();

    }
}
