package org.nrg.xnat.env;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.nrg.xnat.env.GatewayEnvironment.Log;
import org.nrg.xnat.util.Utils;

/**
 *
 * @author Aditya Siram
 */

class PopulateStrategy implements PopulateStrategyInterface {
    public void execute(InternalNetworkDevices ds) throws IOException {
        common_setup(ds);
        common_finish(ds);
    }

    void common_setup(InternalNetworkDevices ds) throws IOException {
        create_shell_devices(ds);
        fill_in_devices(ds);
        ensure_unique_hostnames(ds);
    }

    void common_finish(InternalNetworkDevices ds) {
        ds.rewrite_properties_device_list();
    }

    /**
     * Fills the empty devices in the collection with other properties.
     * If essential attributes are not present in the properties this device
     * is ignored.
     *
     * @param ds
     * @throws IOException
     */
    void fill_in_devices(InternalNetworkDevices ds) throws IOException {
        for (Enumeration e = ds.getDevices().keys(); e.hasMoreElements();) {
            String device_name = (String) e.nextElement();
            NetworkDevice d = ds.getDevices().get(device_name);
            if (!d.read_from_properties(ds.getProperties(), ds.getLog())) {
                ds.setProperties(d.remove_from_properties(ds.getProperties()));
                ds.getDevices().remove(device_name);
            }
        }
    }

    /**
     * Ensure that all devices have unique hostnames. All duplicates and the original are ignored.
     * For instance, if devices A, B and C have "localhost" has a hostname, they are
     * all removed from the collection.
     *
     * @param ds
     */
    void ensure_unique_hostnames(InternalNetworkDevices ds) {
        for (Enumeration e = ds.getDevices().keys(); e.hasMoreElements();) {
            boolean marked_for_deletion = false;
            String device_name = (String) e.nextElement();
            NetworkDevice d = ds.getDevices().get(device_name);
            Hashtable<String, NetworkDevice> temp = (Hashtable<String, NetworkDevice>) ds.getDevices().clone();
            temp.remove(device_name);
            for (Enumeration _e = temp.keys(); _e.hasMoreElements();) {
                String temp_device_name = (String) _e.nextElement();
                NetworkDevice temp_device = temp.get(temp_device_name);
                if (temp_device.duplicate_of(d)) {
                    marked_for_deletion = true;
                    ds.getDevices().remove(temp_device_name);
                    temp.remove(temp_device_name);
                    ds.getLog().addWarning("Ignoring " + temp_device_name + ": duplicate of " + device_name);
                }
            }
            if (marked_for_deletion) {
                ds.getDevices().remove(device_name);
                ds.getLog().addWarning("Ignoring " + device_name + ": duplicates were found");
                marked_for_deletion = false;
            }
        }
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
            create_shell_devices_helper(names_value, ds);
        } else {
            ds.getLog().addWarning("No remote devices found in the config file");
        }
    }

    void create_shell_devices_helper (String names_value, InternalNetworkDevices ds) throws IOException {
        names_value = names_value.trim();
        String [] device_names = read_names(names_value, ds.getLog());
        ds.getProperties().setProperty(ds.getName(), Utils.create_delimited(device_names, " "));
        for (String device_name : device_names) {
            NetworkDevice d = ds.getFactory().make(device_name, ds.getLog());
            ds.getDevices().put(device_name, d);
        }
    }

    void rewrite_properties_device_list(InternalNetworkDevices ds) {
        Vector _name_list = new Vector();
        for (Enumeration e = ds.getDevices().keys(); e.hasMoreElements();) {
            String device_name = (String) e.nextElement();
            _name_list.add(device_name);
        }
        String names = Utils.create_delimited(Utils.toStringArray(_name_list.toArray()), " ");
        ds.getProperties().setProperty(ds.getName(), names);
    }

    private String[] read_names(String names, Log log) {
        String[] name_list = Utils.break_up_value(names);
        //remove duplicate entries
        String[] duplicates = Utils.toStringArray(Utils.duplicates(name_list));
        if (duplicates.length != 0) {
            log.addWarning("Ignoring duplicate entries: " + Arrays.toString(duplicates));
        }
        return Utils.toStringArray(Utils.remove_elements(duplicates, name_list));
    }
}
