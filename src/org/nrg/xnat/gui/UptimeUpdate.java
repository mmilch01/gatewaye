package org.nrg.xnat.gui;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;

import org.nrg.xnat.gateway.XNATGatewayServer;

/**
 *
 * @author Aditya Siram
 */
public class UptimeUpdate implements Runnable {
    private JLabel l;
    private XNATGatewayServer s;
    private Thread running;
    public boolean stop_flag;
    public UptimeUpdate(JLabel l, XNATGatewayServer s) {
        this.l = l;
        this.s = s;
    }

    public void start() {
        this.stop_flag = false;
        running = new Thread(this);
        running.start();
    }

    public void run() {
        while (!stop_flag) {
            if (s.is_running()) {
                System.out.println(s.get_start_time());
                l.setText(org.nrg.xnat.util.Utils.print_elapsed_time(new Date().getTime() - s.get_start_time()));
            } else {
                l.setText("no uptime data available");
            }
            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(UptimeUpdate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        l.setText("no uptime data available");
    }

    public void set_server (XNATGatewayServer s) {
        this.s = s;
    }
  
    public void stop () {
        this.stop_flag = true;
        running.interrupt();
    }
}
