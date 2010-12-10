package org.nrg.xnat.gui;

import org.nrg.xnat.gateway.XNATGatewayServer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;

/**
 * Monitor the current Gateway's uptime. A separate thread is launched that polls
 * the server every minute and updates a label in the main window
 * @author Aditya Siram
 */
public class UptimeUpdate implements Runnable, UpdateableComponent {
    // label to be updated
    private JLabel l;
    // the thread on which monitor runs
    private Thread running;
    // flag that tells the monitor to stop polling the server
    public boolean stop_flag;

    public UptimeUpdate(JLabel l) {
        this.l = l;
    }

    // start a new monitor thread
    public void start() {
        this.stop_flag = false;
        running = new Thread(this);
        running.start();
    }

    /*
     * The monitor runs as long as the stop flag is false. It updates the label and
     * blocks for 60 seconds.
     */
    @Override
    public void run() {
        while (!stop_flag) {
            if (XNATGatewayServer.isRunning()) {
                this.refresh();
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

    /*
     * Stop the monitor thread
     */
    public void stop () {
        this.stop_flag = true;
        running.interrupt();
    }

    /*
     * Refresh the label with the new uptime.
     */
    @Override
    public void refresh() {
        l.setText(org.nrg.xnat.util.Utils.print_elapsed_time(new Date().getTime() - XNATGatewayServer.get_start_time()));
    }
}
