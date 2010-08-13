package org.nrg.xnat.env;

/**
 *
 * @author Aditya Siram
 */
public class IncomingAEFactory implements DeviceFactoryInterface {
    private String group_name;
    public IncomingAEFactory(String group_name) {
        this.group_name = group_name;
    }

    public NetworkDevice make(String name) {
        return new IncomingAE(name, group_name);
    }
}
