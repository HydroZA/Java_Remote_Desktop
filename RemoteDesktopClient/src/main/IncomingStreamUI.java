/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.Image;
import java.io.IOException;
import javax.swing.ImageIcon;

/**
 *
 * @author James Legge
 */
public class IncomingStreamUI extends javax.swing.JFrame
{
    private final Client client;
    
    public IncomingStreamUI(MainUI mui, PacketConnectRequest pcr)
    {
        this.client = mui.getClient();
        this.setTitle(pcr.getPartner());
        initComponents();
    }

    public void updateFrame(byte[] frame)
    {
        Thread UpdateUI = new Thread(() ->
        {
            // Resize to fit window and display
            lblFrameViewer.setIcon(new ImageIcon(
                    new ImageIcon(frame)
                            .getImage()
                                .getScaledInstance(
                                        lblFrameViewer.getWidth(), 
                                        lblFrameViewer.getHeight(), 
                                        Image.SCALE_DEFAULT)));
        });
        UpdateUI.start();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        lblFrameViewer = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        mnuDisconnect = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jMenu1.setText("File");

        mnuDisconnect.setText("Disconnect");
        mnuDisconnect.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                mnuDisconnectActionPerformed(evt);
            }
        });
        jMenu1.add(mnuDisconnect);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblFrameViewer, javax.swing.GroupLayout.DEFAULT_SIZE, 854, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblFrameViewer, javax.swing.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mnuDisconnectActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mnuDisconnectActionPerformed
    {//GEN-HEADEREND:event_mnuDisconnectActionPerformed
        try
        {
            client.stopStream();
            client.resetToMainUI();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }//GEN-LAST:event_mnuDisconnectActionPerformed

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JLabel lblFrameViewer;
    private javax.swing.JMenuItem mnuDisconnect;
    // End of variables declaration//GEN-END:variables
}
