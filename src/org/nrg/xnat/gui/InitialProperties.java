
package org.nrg.xnat.gui;

import org.nrg.xnat.gateway.XNATGatewayServer;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.nrg.xnat.env.GatewayEnvironment;

/**
 *
 * @author Aditya Siram
 */
public class InitialProperties {
    GatewayEnvironment env;
    InitialSetupGUI gui;
    Properties p;
    private final String filename = System.getProperty("user.home")+"/.xnatgateway/gateway.properties.test";
    public InitialProperties(File f) throws IOException {
        if (f.exists()) {
            this.env = new GatewayEnvironment(f);
            XNATGatewayServer.start(p,env);
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new Status(env).setVisible(true);
                }
            });
        }
        else {
            runInitialSetupScreen();
        }
    }

    public InitialProperties() {
        runInitialSetupScreen();
    }


    private void runInitialSetupScreen () {
        final InitialProperties that = this;
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new InitialSetupGUI(that).setVisible(true);
            }
        });
    }
    private void runInitialRemoteAEScreen () {
        final InitialProperties that = this;
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new InitialRemoteAESetupGUI(that).setVisible(true);
            }
        });
    }

    public void initialSetupDone(Properties p) throws IOException {
        p.setProperty("Logger.Output","file");
        p.setProperty("Application.SavedImagesFolderName",System.getProperty("user.home")+"/.xnatgateway/tmp/");
        this.p = p;        
        runInitialRemoteAEScreen ();        
        initializeEnvironment(p);
    }

    public void initialRemoteAESetupDone (Properties p) throws IOException {
        this.p.putAll(p);
        initializeEnvironment(p);
    }

    public void initializeEnvironment (Properties p) throws IOException {
        this.env = new GatewayEnvironment(p, filename);
        XNATGatewayServer.start(p,env);
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Status(env).setVisible(true);
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
