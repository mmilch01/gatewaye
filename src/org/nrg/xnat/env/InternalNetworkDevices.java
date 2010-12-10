package org.nrg.xnat.env;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import org.nrg.xnat.env.GatewayEnvironment.Log;
import org.nrg.xnat.util.DuplicateEntryException;
import org.nrg.xnat.util.NonExistentEntryException;
import org.nrg.xnat.util.Utils;

/**
 * A sandboxed gateway environment that essentially mirrors the
 * real environment without writing anything to disk. This allows the user to
 * try modifying the environment and isolate transactions and only commit them
 * if there are no errors.
 * @author Aditya Siram
 */

 class InternalNetworkDevices {
    protected Hashtable<String, NetworkDevice> devices;
    protected Properties p;
    protected String name;
    protected Log log;
    protected DeviceFactoryInterface factory;
    protected PopulateStrategyInterface ps;

    InternalNetworkDevices(Hashtable<String, NetworkDevice> h, 
                           Properties p,
                           String name,
                           Log log,
                           PopulateStrategyInterface ps,
            DeviceFactoryInterface factory) throws IOException {
        this(h,p,name);
        this.log = log;
        this.factory = factory;
        this.ps = ps;
        populate();
    }

    InternalNetworkDevices(Hashtable<String, NetworkDevice> h, Properties p, String name) {
        this.devices = h;
        this.p = p;
        this.name = name;
    }

    Hashtable<String, NetworkDevice> getDevices() {
        return devices;
    }

    Properties getProperties() {
        return p;
    }

    Log getLog () {
        return this.log;
    }

    DeviceFactoryInterface getFactory() {
        return this.factory;
    }

    void setProperties (Properties p) {this.p = p;}

    String getName () {return this.name;}

    void setDevices(Hashtable<String, NetworkDevice> h) {
        this.devices = h;
    }

    void populate () throws IOException {
        this.ps.execute(this);
    }

    void removeDevice(String device_name) throws NonExistentEntryException, IncompleteEntryException, DefaultDeviceDeleteException, IOException {
        NetworkDevice d = (NetworkDevice) Utils.try_get(device_name,this.devices);
        d.remove_from_properties(this.p);
        this.devices.remove(device_name);
        rewrite_properties_device_list();
    }

    void addDevice (NetworkDevice d) throws DuplicateEntryException, IOException {
        NetworkDevice dup = find_duplicate(d);
        if (dup != null) {
            throw new DuplicateEntryException(dup.getName() + " is a duplicate device");
        }
        this.devices = Utils.try_put(d.getName(), d, this.devices);
        d.add_to_properties(this.p, this.log);
        rewrite_properties_device_list();
    }

    void updateDevice (NetworkDevice d) throws NonExistentEntryException, DuplicateEntryException, IOException {
        Utils.try_get(d.getName(), this.devices);
        NetworkDevice dup = find_duplicate(d);
        if (dup != null) {
            throw new DuplicateEntryException(dup.getName() + " is a duplicate device");
        }
        this.devices.put(d.getName(), d);
        d.add_to_properties(this.p, this.log);
        rewrite_properties_device_list();
    }

    /**
     * Find a duplicate of the given device in the current environment.
     *
     * @param in
     * @return
     */
    NetworkDevice find_duplicate (NetworkDevice in) {
        Hashtable<String, NetworkDevice> temp = (Hashtable<String, NetworkDevice>) getDevices().clone();
        temp.remove(in.getName());
        for (Enumeration e = temp.keys(); e.hasMoreElements(); ) {
            String device_name = (String) e.nextElement();
            NetworkDevice d = temp.get(device_name);
            if (d.duplicate_of(in)) {
                return d;
            }
        }
        return null;
    }

    void rewrite_properties_device_list() {
        Vector _name_list = new Vector();
        for (Enumeration e = this.devices.keys(); e.hasMoreElements();) {
            String device_name = (String) e.nextElement();
            _name_list.add(device_name);
        }
        if (this.devices.size() != 0) {
            String names = Utils.create_delimited(Utils.toStringArray(_name_list.toArray()), " ");
            this.p.setProperty(this.name, names);
        }
    }
}
