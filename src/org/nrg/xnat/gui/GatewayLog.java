/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nrg.xnat.gui;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.nrg.xnat.env.GatewayEnvironment;

/**
 *
 * @author Aditya Siram
 */
public class GatewayLog extends javax.swing.JFrame {
    private final ChildManager c;
    private final GatewayEnvironment env;
    /** Creates new form GatewayLog */
    public GatewayLog(ChildManager c, GatewayEnvironment env) {
        this.c = c;
        this.env = env;
        initComponents();
        fill_textarea();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        gateway_log_text_area = new javax.swing.JTextArea();
        gateway_log_close_button = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        gateway_log_refresh_button = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        gateway_log_text_area.setColumns(20);
        gateway_log_text_area.setRows(5);
        jScrollPane1.setViewportView(gateway_log_text_area);

        gateway_log_close_button.setText("Close");
        gateway_log_close_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                gateway_log_close_buttonMousePressed(evt);
            }
        });

        jLabel1.setText("Gateway Log");

        gateway_log_refresh_button.setText("Refresh");
        gateway_log_refresh_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                gateway_log_refresh_buttonMousePressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(gateway_log_refresh_button, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 198, Short.MAX_VALUE)
                        .addComponent(gateway_log_close_button, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(151, 151, 151)
                        .addComponent(jLabel1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(17, 17, 17)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gateway_log_close_button)
                    .addComponent(gateway_log_refresh_button))
                .addContainerGap())
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-410)/2, (screenSize.height-334)/2, 410, 334);
    }// </editor-fold>//GEN-END:initComponents

    private void gateway_log_close_buttonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gateway_log_close_buttonMousePressed
        this.dispose();
    }//GEN-LAST:event_gateway_log_close_buttonMousePressed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        this.c.remove_from_child_count();
    }//GEN-LAST:event_formWindowClosed

    private void gateway_log_refresh_buttonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gateway_log_refresh_buttonMousePressed
        this.fill_textarea();
    }//GEN-LAST:event_gateway_log_refresh_buttonMousePressed

    private void empty_textarea() {
        this.gateway_log_text_area.setText("");
    }

    private void fill_textarea () {
        try {
            this.empty_textarea();
            List messages = env.get_log_messages();
            if (messages.size() == 0) {
                this.gateway_log_text_area.setText("(no log messages)");
            }
            else {
                String msg = null;
                for (Iterator i = messages.iterator(); i.hasNext();) {
                    String message = (String) i.next();
                    msg = msg + "\n" + message;
                    this.gateway_log_text_area.setText(this.gateway_log_text_area.getText() + "\n" + message);
                }
                this.gateway_log_text_area.setText(msg);
                this.gateway_log_text_area.setText(this.gateway_log_text_area.getText() + "\n");
            }
        } catch (IOException e) {
            GUIUtils.warn("Error reading log file : \n" + e.toString(), "Read error");
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton gateway_log_close_button;
    private javax.swing.JButton gateway_log_refresh_button;
    private javax.swing.JTextArea gateway_log_text_area;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}
