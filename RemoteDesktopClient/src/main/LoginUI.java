/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;

/**
 *
 * @author James Legge
 */
public class LoginUI extends javax.swing.JFrame
{

    /**
     * Creates new form LoginUI
     */
    private Client client;
    
    public LoginUI()
    {
        initComponents();
        this.getRootPane().setDefaultButton(btnLogin);
        client = new Client();    
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

        jLabel1.setText("Username:");

        jLabel2.setText("Password:");

        btnRegister.setText("Register");

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
    
    private String convertToSHA256(String plainText)
    {
        byte[] bytesText = null;
        
        //Hash password
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            bytesText = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException e)
        {
            System.out.println("Requested hashing algorithm not found");  
        }
        
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
    public Thread getThread()
    {
        return Thread.currentThread();
    }
    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnLoginActionPerformed
    {//GEN-HEADEREND:event_btnLoginActionPerformed
        String username = txfUsername.getText();
        String password = pwfPassword.getText();

        // Turn password into a SHA-256 Hash
        password = convertToSHA256(password);
                
        User user = new User (username, password);
        client.setUser(user);
        client.setServerIP("127.0.0.1");
        client.setPort(1234);

        try
        {
            client.connect();

            IncomingConnectionThread ict = new IncomingConnectionThread(client.getDis(), this);
            Thread t = new Thread(ict);
            ict.setClient(client);
            
            MainUI mui = new MainUI(user, client);
            ict.setMainUI(mui);
            mui.setIncomingConnectionThread(ict);
            client.setMui(mui);
            
            // Start IncomingConnectionThread
            t.start();

            client.login();

            // Wait so the IncomingConnectionThread can hear back if the login was successful
            ict.setLoginThread(getThread());
            getThread().suspend();
            
            if (client.isLoggedIn())
            {
                System.out.println("Login Succeeded");
                JOptionPane.showMessageDialog(this, "Logged In Successfully", "Login Attempt", INFORMATION_MESSAGE);
                               
                // Spawn MainUI and dispose this       
                mui.setVisible(true);
                this.dispose();
            }
            else
            {
                JOptionPane.showMessageDialog(this, "Login Failed", "Login Attempt", ERROR_MESSAGE);
                System.out.println("Login Failed");
                ict.getDis().close();
            }
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Failed to login - Exception");
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnLoginActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(LoginUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(LoginUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(LoginUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(LoginUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        
        
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                new LoginUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnRegister;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPasswordField pwfPassword;
    private javax.swing.JTextField txfUsername;
    // End of variables declaration//GEN-END:variables
}
