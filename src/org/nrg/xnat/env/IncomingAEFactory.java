package org.nrg.xnat.env;

import org.nrg.xnat.env.GatewayEnvironment.Log;

/**
 *
 * @author Aditya Siram
 */
public class IncomingAEFactory implements DeviceFactoryInterface {
    private String group_name;
    public IncomingAEFactory(String group_name, Log log) {
        this.group_name = group_name;
    }

    public NetworkDevice make(String name, Log log) {
        return new IncomingAE(name, group_name, log);
    }
}
