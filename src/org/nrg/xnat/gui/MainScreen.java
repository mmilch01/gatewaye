/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nrg.xnat.gui;

import org.nrg.xnat.gateway.XNATGatewayServer;
import org.nrg.xnat.gui.About;
import org.nrg.xnat.gui.Connect;
import org.nrg.xnat.gui.XNATServers;
import org.nrg.xnat.gui.Log;
import org.nrg.xnat.gui.XNATServerProperties;
import org.nrg.xnat.gui.About;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.AWTException;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.nrg.xnat.env.GatewayEnvironment;
/**
 *
 * @author deech
 */
public class MainScreen {
    private boolean system_tray_supported;
    private java.awt.MenuItem menuItem1;
    private java.awt.MenuItem menuItem2;
    private java.awt.MenuItem connect_info;
    private java.awt.PopupMenu popup;

    private About about;

    private void makePopupMenu () {
        popup = new java.awt.PopupMenu();
        java.awt.MenuItem about_item = new java.awt.MenuItem();
        java.awt.MenuItem connect_item = new java.awt.MenuItem();
        java.awt.MenuItem log_item = new java.awt.MenuItem();
        java.awt.MenuItem edit_xnat_servers_item = new java.awt.MenuItem();

        about_item.setLabel("About");
        connect_item.setLabel("Connect");
        log_item.setLabel("Log");
        edit_xnat_servers_item.setLabel("Properties");

        popup.add(about_item);
        popup.add(connect_item);
        popup.add(log_item);
        popup.add(edit_xnat_servers_item);

        ActionListener about_listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                about.setVisible(true);
            }
        };

        about_item.addActionListener(about_listener);
    }

    private void makeSystemTrayMenu () {
        // get the SystemTray instance
         SystemTray tray = SystemTray.getSystemTray();
         // load an image
         Image image = Toolkit.getDefaultToolkit().getImage("./TestGatewayIcon.jpg");
         
         this.makePopupMenu();
         // create a action listener to listen for default action executed on the tray icon
         // construct a TrayIcon

 

         TrayIcon trayIcon = new TrayIcon(image, "Tray Demo", popup);
         // add the tray image
         try {
             tray.add(trayIcon);
         } catch (AWTException e) {
             System.err.println(e);
         }
    }
    public void initGUI(final GatewayEnvironment env, final XNATGatewayServer s) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Status(env,s).setVisible(true);
            }
        });
//        this.system_tray_supported = SystemTray.isSupported();
//        if (this.system_tray_supported) this.makeSystemTrayMenu();
    }
}
