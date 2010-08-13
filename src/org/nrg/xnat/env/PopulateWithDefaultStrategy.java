package org.nrg.xnat.env;

import java.io.IOException;

/**
 *
 * @author Aditya Siram
 */
class PopulateWithDefaultStrategy implements PopulateStrategyInterface {
    private final PopulateStrategy ps;
    
    PopulateWithDefaultStrategy () {
        ps = new PopulateStrategy();
    }

    public void execute(InternalNetworkDevices _ds) throws IOException {
        InternalServerDevices ds = (InternalServerDevices) _ds;
        create_shell_devices(ds);
        ps.fill_in_devices(ds);
        ensure_default_exists(ds);
        ps.ensure_unique_hostnames(ds);
        ensure_default_exists(ds);
        set_default_device(ds);
    }

    void ensure_default_exists(InternalNetworkDevices ds) throws IOException {
        if (!has_default_device(ds)) {
            ds.getLog().addFatal("No default device found");
        }
    }

    boolean has_default_device(InternalNetworkDevices ds) {
        String _default = ds.getProperties().getProperty(ds.getName() + ".default");
        return (_default != null && ds.getDevices().containsKey(_default));
    }
    
    void set_default_device(InternalNetworkDevices _ds) {
        XNATServer s = (XNATServer) _ds.getDevices().get(_ds.getProperties().getProperty(_ds.getName() + ".default"));
        s.set_default();
    }

    /**
     * Create empty devices by reading the master list for this device type.
     * @param ds
     * @throws IOException
     */
    void create_shell_devices(InternalNetworkDevices ds) throws IOException {
        String names_value = ds.getProperties().getProperty(ds.getName());
        String[] device_names;
        if (names_value != null) {
            ps.create_shell_devices_helper(names_value, ds);
        } else {
            ds.getLog().addFatal("No XNAT Servers found in the config file");
        }
    }
}
