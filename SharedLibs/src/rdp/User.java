/* 
 * AUTHOR: James Legge
 * STUDENT#: 17008250
 * INSTITUTION: London Metropolitan University
 * SUBJECT: CS6P05 Project
 * PROJECT TITLE: Using Asymmetrical Encryption and Digital Signatures to Create a Secure Remote Desktop Environment
 * Project Supervisor: Dr. Qicheng Yu
 */
package rdp;

public class User
{
    // Fields
    private String username, password;
    private String[] friends;
            
    // Paramaterized Constructor
    public User (String username, String password)
    {
        this.username = username;
        this.password = password;
    }
    
    // No-Params Constructor
    public User()
    {
    }
    
    // Accessors
    public String getUsername()
    {
        return this.username;
    }
    public String getPassword()
    {
        return this.password;
    }
    public String[] getFriends()
    {
        return this.friends;
    }
    
    // Mutators
    public void setUsername(String username)
    {
        this.username = username;
    }
    public void setPassword (String password)
    {
        this.password = password;
    }
    public void setFriends(String[] friends)
    {
        this.friends = friends;
    }
}
