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
    private String CERTIFICATE;
    private String TLS_VERSION = "TLSv1.2";

    // Misc Vars
    private Connection con;
    private LoginServer ls;
    public static Vector<User> users = new Vector<>();
    public static Vector<ClientHandler> clientHandlers = new Vector<>();
    private CertificateHandler ch;

    // paramaterized constructor
    public Server() throws ClassNotFoundException, SQLException, UnknownHostException, FileNotFoundException, Exception
    {
        ConfigParser cp;
        try
        {
            cp = new ConfigParser("server.properties").parse();
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
        this.CERTIFICATE = cp.getCERTIFICATE();

        //Generate KeyStore and Certificates if not found, and assign values to CertificateHandler
        if (new File("certs/" + KEY_STORE_NAME + ".jks").exists())
        {
            System.out.println("TLS Certificate Found");
            ch = new CertificateHandler(SERVER, SERVER_IP.getHostAddress(), TRUST_STORE_NAME, TRUST_STORE_PWD, KEY_STORE_NAME, KEY_STORE_PWD, CERTIFICATE);
            ch.setTrustStore(new File("certs/" + TRUST_STORE_NAME + ".jks"));
            ch.setCertificate(new File("certs/" + CERTIFICATE + ".cer"));
            ch.setKeystore(new File("certs/" + KEY_STORE_NAME + ".jks"));
        }
        else
        {
            System.out.println("TLS Certificate Not Found, Generating...");
            System.out.println("CERTIFCATE NAME: " + CERTIFICATE);
            ch = new CertificateHandler(SERVER, SERVER_IP.getHostAddress(), TRUST_STORE_NAME, TRUST_STORE_PWD, KEY_STORE_NAME, KEY_STORE_PWD, CERTIFICATE);
            ch = ch.generate();
            System.out.println("Generated TLS Certificate");
        }

        this.con = getDatabaseConnection();
        if (!databaseExists())
        {
            System.out.println("Creating Database...");
            createDatabase();
            System.out.println("Database Created");
        }
    }

    public CertificateHandler getCh()
    {
        return ch;
    }

    // Mutators
    public void setSERVER_IP(InetAddress SERVER_IP)
    {
        this.SERVER_IP = SERVER_IP;
    }

    public void setSERVER_PORT(int SERVER_PORT)
    {
        this.SERVER_PORT = SERVER_PORT;
    }

    // Accessors
    public InetAddress getSERVER_IP()
    {
        return SERVER_IP;
    }

    public int getSERVER_PORT()
    {
        return SERVER_PORT;
    }

    // Methods
    public Connection getDatabaseConnection() throws ClassNotFoundException, SQLException
    {
        Connection dbCon;

        Class.forName("org.sqlite.JDBC");
        dbCon = DriverManager.getConnection("jdbc:sqlite:rdp.sqlite");

        return dbCon;
    }

    public void refreshDatabaseConnection() throws ClassNotFoundException, SQLException
    {
        this.con = getDatabaseConnection();
    }

    public boolean databaseExists()
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

    public void createDatabase() throws SQLException
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

    public void clearDatabase() throws SQLException
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

    public boolean login(User user) throws SQLException, InterruptedException
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

    public void logout(User user) throws SQLException, InterruptedException
    {
        // Update DB to show logged out user
        String query = "UPDATE USERS SET loggedin=0 WHERE username=\'" + user.getUsername() + "\'";
        PreparedStatement stmt = con.prepareStatement(query);

        stmt.execute();
        stmt.close();

        // Remove user from Users vector
        users.remove(user);

        // Very hacky and slow, look into improvements
        for (ClientHandler ch : clientHandlers)
        {
            if (ch.getUser().getUsername().equals(user.getUsername()))
            {
                ch.getThisThread().stop();
                clientHandlers.remove(ch);

                break;
            }
        }
        System.out.println(user.getUsername() + " logged out");
    }

    public String[] getLoggedInUsers()
    {
        String[] usernames = new String[clientHandlers.size()];

        int i = 0;
        for (ClientHandler ch : clientHandlers)
        {
            usernames[i] = ch.getUser().getUsername();
            i++;
        }

        return usernames;
    }

    public void logoutAllUsers() throws SQLException, InterruptedException
    {
        for (User ch : users)
        {
            logout(ch);
        }
        users.clear();
        clientHandlers.clear();
    }

    public boolean register(User user)
    {
        // add user to DB
        return false;
    }

    public String[] getFriends(User user) throws SQLException
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

    public void killServer() throws IOException, SQLException, InterruptedException
    {
        try
        {
            ls.getSs().close();
            ls.getCertExchange().close();
            ss.close();
        }
        catch (NullPointerException e)
        {
        }

        // logout all users
        logoutAllUsers();

        System.out.println("Server Shutdown");
    }

    public boolean addFriend(String username, String friendName)
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
                    System.out.println("Users are already friends");

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

                    System.out.println("\'" + username + "\' added \'" + friendName + "\' as a friend");
                    return true;
                }
            }
            else
            {
                System.out.println("Requested friend not found");

                return false;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeFriend(String user, String friendName) throws SQLException
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
            System.out.println("\'" + user + "\' attempted to remove friend: \'" + friendName + "\' but they are not friends");
            return false;
        }
        else
        {
            // users are friends, remove from DB 
            int friendEntryID = rs2.getInt(1);
            query = "DELETE FROM friends WHERE id=" + friendEntryID;
            PreparedStatement stmt3 = con.prepareStatement(query);
            stmt3.execute();

            System.out.println("\'" + user + "\' removed \'" + friendName + "\' as a friend");
            return true;
        }
    }

    public void restartLoginServer() throws BindException, UnknownHostException, IOException, SQLException, InterruptedException
    {
        logoutAllUsers();

        if (ls != null)
        {
            ls.getSs().close();
            ls.getCertExchange().close();
        }

        startLoginServer();
    }

    public void startLoginServer() throws BindException, UnknownHostException, IOException
    {
        SSLSocketCreator tlsS = new SSLSocketCreator();

        try
        {
            ss = tlsS.getSecureServerSocket(
                    SERVER_IP,
                    SERVER_PORT,
                    TLS_VERSION,
                    TRUST_STORE_NAME,
                    TRUST_STORE_PWD,
                    KEY_STORE_NAME,
                    KEY_STORE_PWD);
        }

        catch (Exception e)
        {
            System.out.println("Failed to get SSL Server Socket");
            e.printStackTrace();
            return;
        }

        // This thread handles incoming connections, gets the users friends and then hands the client off to a ClientHandler thread
        ls = new LoginServer(this, ss);
        Thread t = new Thread(ls);
        ls.setThisThread(t);
        t.start();

    }
}
