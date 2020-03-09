/*
 * AUTHOR: James Legge
 * STUDENT#: 17008250
 * INSTITUTION: London Metropolitan University
 * SUBJECT: CS6P05 Project
 * PROJECT TITLE: Using Asymmetrical Encryption and Digital Signatures to Create a Secure Remote Desktop Environment
 * Project Supervisor: Dr. Qicheng Yu
 */
package rdp;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author james
 */
public class UserTest
{
    
    public UserTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of getFriends method, of class User.
     */
    @Test
    public void testGetFriends()
    {
        System.out.println("setFriends");
        String[] friends = new String[]{"james", "matt", "josh"};
        User instance = new User();
        instance.setFriends(friends);
        assertArrayEquals(friends, instance.getFriends());
    }

    /**
     * Test of setUsername method, of class User.
     */
    @Test
    public void testSetUsername()
    {
        System.out.println("setUsername");
        String username = "Hydro";
        User instance = new User();
        instance.setUsername(username);
        assertEquals("Hydro", instance.getUsername());
    }

    /**
     * Test of setPassword method, of class User.
     */
    @Test
    public void testSetPassword()
    {
        System.out.println("setPassword");
        String password = "1234";
        User instance = new User();
        instance.setPassword(password);
        assertEquals("1234", instance.getPassword());
    }

    /**
     * Test of setFriends method, of class User.
     */
    @Test
    public void testSetFriends()
    {
        System.out.println("setFriends");
        String[] friends = new String[]{"james", "matt", "josh"};
        User instance = new User();
        instance.setFriends(friends);
        assertArrayEquals(friends, instance.getFriends());
    }

    /**
     * Test of toString method, of class User.
     */
    @Test
    public void testToString()
    {
        User instance = new User();
        instance.setUsername("Hydro");
        instance.setPassword("1234");
        String[] friends = new String[]{"james", "matt", "josh"};
        instance.setFriends(friends);
        String expResult = "Username: Hydro\nPassword: 1234\nFriends: james\nmatt\njosh\n";
        String result = instance.toString();
        assertEquals(expResult, result);
        
    }
    
}
