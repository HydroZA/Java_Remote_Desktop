/*
 * AUTHOR: James Legge
 * STUDENT#: 17008250
 * INSTITUTION: London Metropolitan University
 * SUBJECT: CS6P05 Project
 * PROJECT TITLE: Using Asymmetrical Encryption and Digital Signatures to Create a Secure Remote Desktop Environment
 * Project Supervisor: Dr. Qicheng Yu
 */
package rdp;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;

/**
 *
 * @author James Legge
 */
public class ClientMain
{

    public static void main(String args[]) throws Exception
    {  
        // Create our client object
        Client client = new Client();
        LoginUI lui = new LoginUI(client);

        try
        {
            // Connect to the server
            client.connect(lui);

            Client.log.info("Waiting for handshake with server to complete...");
            
            // Show the login interface
            lui.setVisible(true);
        }
        catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException ex)
        {
            Client.log.log(Level.SEVERE, "Failed to connect to server {0}", ex.toString());
            System.exit(1);
        }               
        
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    client.disconnect();
                }
                catch(Exception e)
                {
                    
                }
            }
        });
    }
}
