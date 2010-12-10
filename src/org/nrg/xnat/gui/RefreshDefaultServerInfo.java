package org.nrg.xnat.gui;

import javax.swing.JLabel;
import org.nrg.xnat.env.GatewayEnvironment;

/**
 * A service class that updates the default XNAT server label with a new server and
 * username
 * @author Aditya Siram
 */
public class RefreshDefaultServerInfo implements UpdateableComponent {
    private final GatewayEnvironment env;
    private final JLabel default_server_user_label;

   public RefreshDefaultServerInfo (JLabel default_server_user_label, GatewayEnvironment env) {
       this.default_server_user_label = default_server_user_label;
       this.env = env;
   }

    @Override
    public void refresh() {
        this.default_server_user_label.setText("(" + env.get_default_server().getName() + "," + env.get_default_server().getUsername() + ")");
    }

}
