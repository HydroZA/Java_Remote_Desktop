/* 
 * AUTHOR: James Legge
 * STUDENT#: 17008250
 * INSTITUTION: London Metropolitan University
 * SUBJECT: CS6P05 Project
 * PROJECT TITLE: Using Asymmetrical Encryption and Digital Signatures to Create a Secure Remote Desktop Environment
 * Project Supervisor: Dr. Qicheng Yu
 */
package rdp;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import javax.swing.JOptionPane;

/**
 *
 * @author James Legge
 */
public class LoginUI extends javax.swing.JFrame
{

    /**
     * Creates new form LoginUI
     */
    private final Client client;

    public LoginUI(Client c)
    {
        this.client = c;

        initComponents();
        extraInitComponents();
    }

    /**
     * Netbeans does not allow editing of the existing initComponenets(), so I
     * created my own extra code that is run after it
     */
    private void extraInitComponents()
    {
        // Center the window on the screen and set the default button to Longin
        this.setLocationRelativeTo(null);
        this.getRootPane().setDefaultButton(btnLogin);

        // Tell the server we wish to cancel the login attempt if the window is closed
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if (!client.isLoggedIn())
                {
                    try
                    {
                        new PacketCancelLogin().sendOnlyType(client.getDos());
                        Client.log.info("Disconnected Safely");
                    }
                    catch (Exception ex)
                    {
                        Client.log.log(Level.SEVERE, "Unable to negotiate cancel login{0}", ex.toString());
                    }
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        pwfPassword = new javax.swing.JPasswordField();
        txfUsername = new javax.swing.JTextField();
        btnRegister = new javax.swing.JButton();
        btnLogin = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Remote Desktop Login");
        setResizable(false);

        jLabel1.setText("Username:");

        jLabel2.setText("Password:");

        btnRegister.setText("Register");
        btnRegister.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnRegisterActionPerformed(evt);
            }
        });

        btnLogin.setText("Login");
        btnLogin.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnLoginActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txfUsername))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnRegister)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnLogin, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE))
                            .addComponent(pwfPassword))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(txfUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(pwfPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRegister)
                    .addComponent(btnLogin))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private String convertToSHA256(String plainText) throws NoSuchAlgorithmException
    {
        //Hash password
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytesText = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));

        // Convert to string format
        StringBuilder encText = new StringBuilder();
        for (int i = 0; i < bytesText.length; i++)
        {
            String hex = Integer.toHexString(0xff & bytesText[i]);
            if (hex.length() == 1)
            {
                encText.append('0');
            }
            encText.append(hex);
        }
        return encText.toString().toUpperCase();
    }

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnLoginActionPerformed
    {//GEN-HEADEREND:event_btnLoginActionPerformed
        String username = txfUsername.getText();
        String plaintext = String.valueOf(pwfPassword.getPassword());

        // Turn password into a SHA-256 Hash
        String password;
        try
        {
            password = convertToSHA256(plaintext);
            
        }
        catch (NoSuchAlgorithmException e)
        {
            Client.log.severe("Requested Hashing Algorithm Not Found!");
            return;
        }

        User user = new User(username, password);
        client.setUser(user);

        try
        {
            client.login();
        }

        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this, "Failed to login - Exception");
            Client.log.log(Level.WARNING, "LOGIN FAILED!{0}", e.toString());
        }
    }//GEN-LAST:event_btnLoginActionPerformed

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnRegisterActionPerformed
    {//GEN-HEADEREND:event_btnRegisterActionPerformed
        String plaintext = String.valueOf(pwfPassword.getPassword());
        
        // Turn password into a SHA-256 Hash
        String password;
        try
        {
            password = convertToSHA256(plaintext);
        }
        catch (NoSuchAlgorithmException e)
        {
            Client.log.severe("Requested Hashing Algorithm Not Found!");
            return;
        }
        
        User user = new User(txfUsername.getText(), password);
        client.setUser(user);

        try
        {
            client.register();
        }
        catch (IOException e)
        {

            JOptionPane.showMessageDialog(this, "Failed to register - Exception");
            Client.log.log(Level.SEVERE, "Failed to register due to server error {0}", e.toString());
        }
    }//GEN-LAST:event_btnRegisterActionPerformed

    /**
     * @param args the command line arguments
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.IOException
     * @throws java.io.FileNotFoundException
     * @throws java.security.KeyStoreException
     * @throws java.security.cert.CertificateException
     * @throws java.security.UnrecoverableKeyException
     * @throws java.security.KeyManagementException
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnRegister;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPasswordField pwfPassword;
    private javax.swing.JTextField txfUsername;
    // End of variables declaration//GEN-END:variables
}
