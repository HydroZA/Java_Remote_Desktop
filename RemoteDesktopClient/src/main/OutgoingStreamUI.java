/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.AWTException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;

/**
 *
 * @author James Legge
 */
public class OutgoingStreamUI extends javax.swing.JFrame
{
    private volatile int frames;
    private volatile StopWatch sw;
    private final String partnerName;
    private final MainUI mui;
    private final Client client;
    private final IncomingConnectionThread ict;
    private final DataOutputStream dos;
    private volatile boolean terminated;
    
    public OutgoingStreamUI(MainUI mui, PacketConnectRequest pcr)
    {
        this.partnerName = pcr.getRequester();
        this.mui = mui;
        this.client = mui.getClient();
        this.ict = mui.getIncomingConnectionThread();
        this.dos = mui.getClient().getDos();
        this.terminated = false;
        this.sw = new StopWatch();
        initComponents();
        
        startSendingFrames();
        startUpdateUI();
    }

    private void endStream()
    {
        terminated = true;
        mui.setVisible(true);
        this.dispose();
    }
    
    private void startUpdateUI()
    {
        Thread UpdateUI = new Thread(() ->
        {    
            while (!terminated)
            {
                lblSentFrames.setText(String.valueOf(frames));
                lblElapsedTime.setText(String.valueOf(sw.getTime(TimeUnit.SECONDS)));
            }
        });
        UpdateUI.start();     
    }
    
    private void startSendingFrames()
    {
        Thread SendFrames = new Thread(() ->
        {
            sw.start(); // Start the stop watch to get elapsed time
            while (!terminated)
            {
                try
                {
                    byte[] img = client.takeScreenshot();
                    PacketFrame pf = new PacketFrame(img, img.length);
                    pf.send(dos);
                    
                    frames++;
                }
                catch (IOException e)
                {
                    System.out.println("IOException while attempting to send a frame");
                    e.printStackTrace();
                    terminated = true;
                }
                catch (AWTException ex)
                {
                    System.out.println("AWTException while attempting to send a frame");
                    terminated = true;
                }
            }
            endStream();
            sw.stop();
        });
        SendFrames.start();
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
        lblStreamingTo = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblSentFrames = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblElapsedTime = new javax.swing.JLabel();
        btnEndStream = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Streaming to:");

        lblStreamingTo.setText(partnerName);

        jLabel2.setText("Frames Sent:");

        lblSentFrames.setText("0");

        jLabel3.setText("Elapsed Time:");

        lblElapsedTime.setText("0");

        btnEndStream.setText("End Stream");
        btnEndStream.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnEndStreamActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel2)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblSentFrames))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel3)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 66, Short.MAX_VALUE)
                                    .addComponent(lblElapsedTime)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(69, 69, 69)
                                .addComponent(lblStreamingTo)))
                        .addGap(42, 42, 42))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnEndStream, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblStreamingTo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lblSentFrames))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblElapsedTime))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEndStream, javax.swing.GroupLayout.PREFERRED_SIZE, 31, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnEndStreamActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnEndStreamActionPerformed
    {//GEN-HEADEREND:event_btnEndStreamActionPerformed
        terminated = true;
        long time = sw.getTime(TimeUnit.SECONDS);
        long fps = frames/time;
        
        System.out.println(
                  "*********** STREAM STATS ***********\n"
                + "Total Frames Sent: " + frames + "\n"
                + "Elapsed Time: " + time + " Seconds\n"
                + "Average Frame Rate: " + fps + " FPS\n"
                + "*********** END STREAM STATS ***********");
        
        endStream();
    }//GEN-LAST:event_btnEndStreamActionPerformed

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEndStream;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel lblElapsedTime;
    private javax.swing.JLabel lblSentFrames;
    private javax.swing.JLabel lblStreamingTo;
    // End of variables declaration//GEN-END:variables
}
