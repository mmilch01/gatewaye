package org.nrg.xnat.gui;

import java.io.IOException;
import org.nrg.xnat.env.GatewayEnvironment;
import javax.swing.JLabel;


/**
 *
 * @author Aditya Siram
 */
public class UpdateGateway implements UpdateableComponent {
    private  JLabel ae_label;
    private  JLabel port_label;
    private final GatewayEnvironment env;

    public UpdateGateway (JLabel ae_label, JLabel port_label, GatewayEnvironment env) {
        this.env = env;
        this.ae_label = ae_label;
        this.port_label = port_label;
    }

    public boolean update_gateway (String ae, String _port) {
        try {
            int port = Integer.parseInt(_port);
            env.set_callingaetitle(ae);
            env.set_listening_port(port);
            refresh();
        }
        catch (NumberFormatException e ) {
            GUIUtils.warn("Could not read the port number", "Update error");
            return false;
        }
        catch (IOException e) {
            GUIUtils.application_stop(e.toString(), "Update error");
            return false;
        }
        return true;
    }

    public void refresh() {
        this.ae_label.setText(env.get_callingaetitle());
        this.port_label.setText(Integer.toString(env.get_listening_port()));
    }
}
