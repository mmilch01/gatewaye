
package org.nrg.xnat.gui;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.nrg.xnat.env.GatewayEnvironment;
import org.nrg.xnat.gateway.XNATGatewayServer;

/**
 *
 * @author Aditya Siram
 */
public class InitialProperties {
    GatewayEnvironment env;
    InitialSetupGUI gui;
    Properties p;
    private final String filename = "./config/gateway.properties.test";
    public InitialProperties(File f) throws IOException {
        if (f.exists()) {
            this.env = new GatewayEnvironment(f);
            final XNATGatewayServer s = XNATGatewayServer.start(p,env);
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new Status(env, s).setVisible(true);
                }
            });
        }
        else {
            run_gui();
        }
    }

    public InitialProperties() {
        run_gui();
    }


    public void run_gui () {
        final InitialProperties that = this;
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new InitialSetupGUI(that).setVisible(true);
            }
        });
    }

    public void initializeEnvironment (Properties p) throws IOException {
        p.setProperty("Logger.Output","file");
        p.setProperty("Application.SavedImagesFolderName","./tmp/");
        this.env = new GatewayEnvironment(p, filename);
        final XNATGatewayServer s = XNATGatewayServer.start(p,env);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Status(env,s).setVisible(true);
            }
        });
    }

    public void cancelled_initial_setup () {
        int exit_confirm = JOptionPane.showConfirmDialog(null, "Cannot continue without initial setup information.\n Exit program?", "Initialization error", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (exit_confirm == JOptionPane.YES_OPTION){
            System.exit(0);
        }
    }
}
