
package org.nrg.xnat.env;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.nrg.xnat.env.GatewayEnvironment.Log;

/**
 * Makes a new XNAT server object. See the documentation for DeviceFactoryInterface
 * for more information.
 * @author Aditya Siram
 */
public class ServerFactory implements DeviceFactoryInterface {
    private String group_name;
    public ServerFactory(String group_name, Log log) {
        this.group_name = group_name;
    }
    
    @Override
    public NetworkDevice make(String name, Log log) throws IOException {
        XNATServer s = null;
        try {
            s = new XNATServer(name, group_name, log);
        } catch (NoSuchAlgorithmException ex) {
            log.addFatal(ex.toString());
        } catch (IOException ex) {
            log.addFatal(ex.toString());
        }
        return s;
    }
}
