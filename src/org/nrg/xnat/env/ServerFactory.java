
package org.nrg.xnat.env;

/**
 *
 * @author Aditya Siram
 */
public class ServerFactory implements DeviceFactoryInterface {
    private String group_name;
    public ServerFactory(String group_name) {
        this.group_name = group_name;
    }
    
    public NetworkDevice make(String name) {
        return new XNATServer(name, group_name);
    }
}
