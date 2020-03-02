/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author James Legge
 */
public class ConfigParser
{

    private String path;
    private InetAddress SERVER_IP;
    private int SERVER_PORT;
    private String TRUST_STORE_NAME;
    private char[] TRUST_STORE_PWD;
    private String KEY_STORE_NAME;
    private char[] KEY_STORE_PWD;

    public ConfigParser(String path)
    {
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public InetAddress getSERVER_IP()
    {
        return SERVER_IP;
    }

    public int getSERVER_PORT()
    {
        return SERVER_PORT;
    }

    public String getTRUST_STORE_NAME()
    {
        return TRUST_STORE_NAME;
    }

    public char[] getTRUST_STORE_PWD()
    {
        return TRUST_STORE_PWD;
    }

    public String getKEY_STORE_NAME()
    {
        return KEY_STORE_NAME;
    }

    public char[] getKEY_STORE_PWD()
    {
        return KEY_STORE_PWD;
    }
    
    public ConfigParser parse() throws FileNotFoundException, UnknownHostException
    {
        File f = new File(this.path);
        Scanner s = new Scanner(f);
        
        while (s.hasNextLine())
        {
            String[] line = s.nextLine().split(":");
           
            // Ignore lines that are commented, blank or too short
            if (line[0].startsWith("#") || line.length != 2)
            {
                continue;
            }
            
            String key = line[0];
            String value = line[1];
            
            switch (key)
            {
                case "SERVER_IP":
                    this.SERVER_IP = InetAddress.getByName(value);
                    break;
                case "SERVER_PORT":
                    this.SERVER_PORT = Integer.parseInt(value);
                    break;
                case "TRUST_STORE_NAME":
                    this.TRUST_STORE_NAME = value;
                    break;
                case "TRUST_STORE_PWD":
                    this.TRUST_STORE_PWD = value.toCharArray();
                    break;
                case "KEY_STORE_NAME":
                    this.KEY_STORE_NAME = value;
                    break;
                case "KEY_STORE_PWD":
                    this.KEY_STORE_PWD = value.toCharArray();
                    break;
                default:
                    break;
            }
        }
        
       return this;
    }
    
    @Override
    public String toString()
    {
        return 
              "SERVER_IP: " + SERVER_IP + "\n"
            + "SERVER_PORT: " + SERVER_PORT + "\n"
            + "TRUST_STORE_NAME: " + TRUST_STORE_NAME + "\n"
            + "TRUST_STORE_PWD: " + Arrays.toString(KEY_STORE_PWD) + "\n"
            + "KEY_STORE_NAME: " + KEY_STORE_NAME + "\n"
            + "KEY_STORE_PWD: " + Arrays.toString(KEY_STORE_PWD);
    }

}
