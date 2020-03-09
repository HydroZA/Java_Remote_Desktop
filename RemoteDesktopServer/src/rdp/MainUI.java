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
import java.net.UnknownHostException;
import java.util.logging.Level;
import javax.swing.JOptionPane;

/**
 *
 * @author James Legge
 */
public class MainUI extends javax.swing.JFrame
{

    private final User user;
    private final Client client;
    private IncomingDataHandler ict;

    public MainUI(User user, Client client)
    {
        initComponents();
        this.setLocationRelativeTo(null);

        this.user = user;
        this.client = client;

        // Shutdown hook. Code runs when program closed without going through normal exit procedure; ie. when closed using task manager.
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                try
                {

                    client.logout();

                }
                catch (IOException e)
                {
                    Client.log.severe("Exception while attempting to logout");
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnAddFriend = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstFriends = new javax.swing.JList<>();
        btnConnect = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        tglBtnAllowConnections = new javax.swing.JToggleButton();
        btnRemoveFriend = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        mnuLogout = new javax.swing.JMenuItem();
        mnuExit = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Remote Desktop Client");
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel1.setText("Welcome to the Remote Desktop Client!");

        jLabel2.setText("Friends:");

        btnAddFriend.setText("Add Friend");
        btnAddFriend.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnAddFriendActionPerformed(evt);
            }
        });

        lstFriends.setModel(new javax.swing.AbstractListModel<String>()
        {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(lstFriends);

        btnConnect.setText("Connect");
        btnConnect.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnConnectActionPerformed(evt);
            }
        });

        jLabel3.setText("Allow Incoming Connections:");

        tglBtnAllowConnections.setText("OFF");
        tglBtnAllowConnections.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(java.awt.event.ItemEvent evt)
            {
                tglBtnAllowConnectionsItemStateChanged(evt);
            }
        });
        tglBtnAllowConnections.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                tglBtnAllowConnectionsActionPerformed(evt);
            }
        });

        btnRemoveFriend.setText("Remove Friend");
        btnRemoveFriend.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnRemoveFriendActionPerformed(evt);
            }
        });

        jMenu1.setText("File");

        mnuLogout.setText("Logout");
        mnuLogout.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mnuLogoutActionPerformed(evt);
            }
        });
        jMenu1.add(mnuLogout);

        mnuExit.setText("Exit");
        mnuExit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mnuExitActionPerformed(evt);
            }
        });
        jMenu1.add(mnuExit);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddFriend)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveFriend))
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnConnect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tglBtnAllowConnections)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(btnAddFriend, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoveFriend, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnConnect)
                    .addComponent(jLabel3)
                    .addComponent(tglBtnAllowConnections))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public Client getClient()
    {
        return this.client;
    }

    public IncomingDataHandler getIncomingConnectionThread()
    {
        return this.ict;
    }

    public void setIncomingConnectionThread(IncomingDataHandler ict)
    {
        this.ict = ict;
    }

    private void btnAddFriendActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnAddFriendActionPerformed
    {//GEN-HEADEREND:event_btnAddFriendActionPerformed
        String friendName = JOptionPane.showInputDialog(this, "Enter the name of the friend to add");
        if (friendName == null || friendName.equals(""))
        {
            return;
        }

        try
        {
            client.addFriend(friendName);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this, "Failed to add friend");
        }
    }//GEN-LAST:event_btnAddFriendActionPerformed

    public void updateFriendsList()
    {
        lstFriends.setListData(user.getFriends());
    }

    public void setFriendsList(String[] friends)
    {
        lstFriends.setListData(friends);
    }

    private void btnRemoveFriendActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnRemoveFriendActionPerformed
    {//GEN-HEADEREND:event_btnRemoveFriendActionPerformed
        String friendName = lstFriends.getSelectedValue();
        if (friendName == null || friendName.equals(""))
        {
            return;
        }

        try
        {
            client.removeFriend(friendName);
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this, "Failed to remove friend");
            Client.log.severe("Failed to remove friend");
        }
    }//GEN-LAST:event_btnRemoveFriendActionPerformed

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnConnectActionPerformed
    {//GEN-HEADEREND:event_btnConnectActionPerformed
        try
        {
            client.askPermissionToConnect(lstFriends.getSelectedValue());
        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(this, "Unable to connect");
            Client.log.severe("Failed to ask permission to connect");
        }
        catch (NullPointerException e)
        {
            Client.log.warning("No user selected");
        }
    }//GEN-LAST:event_btnConnectActionPerformed

    private void mnuLogoutActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mnuLogoutActionPerformed
    {//GEN-HEADEREND:event_mnuLogoutActionPerformed
        try
        {
            client.disconnect();
            LoginUI lui = new LoginUI(client);
            lui.setVisible(true);
            client.connect(lui);
            this.dispose();
        }
        catch (UnknownHostException ex)
        {
            Client.log.log(Level.SEVERE, "Error during logout: {0}", ex.toString());
        }
        catch (Exception ex)
        {
            Client.log.log(Level.SEVERE, "Error during logout: {0}", ex.toString());
        }
    }//GEN-LAST:event_mnuLogoutActionPerformed

    private void mnuExitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mnuExitActionPerformed
    {//GEN-HEADEREND:event_mnuExitActionPerformed
        try
        {
            if (client.isLoggedIn())
            {
                client.logout();
                Client.log.info("Logout Succeeded");
            }
        }
        catch (IOException e)
        {
            Client.log.severe("Exception while attempting to Logout");
        }
        System.exit(0);
    }//GEN-LAST:event_mnuExitActionPerformed

    private void tglBtnAllowConnectionsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_tglBtnAllowConnectionsActionPerformed
    {//GEN-HEADEREND:event_tglBtnAllowConnectionsActionPerformed

    }//GEN-LAST:event_tglBtnAllowConnectionsActionPerformed

    private void tglBtnAllowConnectionsItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_tglBtnAllowConnectionsItemStateChanged
    {//GEN-HEADEREND:event_tglBtnAllowConnectionsItemStateChanged
        // Client.log.info(tglBtnAllowConnections.isSelected());
        if (tglBtnAllowConnections.isSelected())
        {
            client.setAllowingIncomingConnections(true);
            tglBtnAllowConnections.setText("ON");
            Client.log.info("Now allowing incoming connections");
        }
        else
        {
            client.setAllowingIncomingConnections(false);
            tglBtnAllowConnections.setText("OFF");
            Client.log.info("Disallowing Incoming Connections");
        }
    }//GEN-LAST:event_tglBtnAllowConnectionsItemStateChanged

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddFriend;
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnRemoveFriend;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<String> lstFriends;
    private javax.swing.JMenuItem mnuExit;
    private javax.swing.JMenuItem mnuLogout;
    private javax.swing.JToggleButton tglBtnAllowConnections;
    // End of variables declaration//GEN-END:variables
}
