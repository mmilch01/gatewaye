package org.nrg.xnat.env;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.nrg.xnat.env.GatewayEnvironment.Log;
import org.nrg.xnat.util.NonExistentEntryException;
import org.nrg.xnat.util.Utils;

/**
 * A sandboxed environment specifically for XNAT servers which are different
 * from other devices because we ensure that there is a valid default server
 * present at all times.
 *
 * @author Aditya Siram
 */
 class InternalServerDevices extends InternalNetworkDevices {
    InternalServerDevices (Hashtable<String,NetworkDevice> h,
                           Properties p,
                           String name,
                           Log log,
                           PopulateStrategyInterface ps,
                           DeviceFactoryInterface factory) throws IOException {
        super (h,p,name,log,ps,factory);
    }

    InternalServerDevices(Hashtable<String, NetworkDevice> h, Properties p, String name) {
       super(h,p,name);
    }

    public void set_default_server (String name) throws NonExistentEntryException, IOException {
        XNATServer d = (XNATServer) Utils.try_get(name,getDevices());
        if (!d.is_default()) {
            for (Enumeration e = getDevices().keys(); e.hasMoreElements();) {
                String device_name = (String) e.nextElement();
                XNATServer s = (XNATServer) getDevices().get(device_name);
                if (s.is_default()) {
                    s.unset_default();
                }
            }
            d.set_default();
            d.add_to_properties(this.p, this.log);
        }
    }

    @Override
    public void removeDevice (String name) throws NonExistentEntryException, IncompleteEntryException, DefaultDeviceDeleteException {
        XNATServer d = (XNATServer) Utils.try_get(name,getDevices());
        if (!d.isValid()) {
                throw new IncompleteEntryException("Either name, hostname, username or password are missing");
        }
        if (d.is_default()) {
                throw new DefaultDeviceDeleteException("Server : " + getName() + " is the default XNAT server. " + " Please make another XNAT server the default before deleting this one.");

        }
        d.remove_from_properties(this.p);
        this.devices.remove(d.getName());
        rewrite_properties_device_list();
    }

    public void updateDevice (XNATServer s) throws NonExistentEntryException, IncompleteEntryException, IOException {
        if (!s.isValid()) {
            throw new IncompleteEntryException("Either name, hostname, username or password are missing");
        }
        Utils.try_get(s.getName(),getDevices());
        addDevice_helper(s);
    }

    private void addDevice_helper (XNATServer s) throws NonExistentEntryException, IOException {
        s.add_to_properties(this.p, this.log);
        getDevices().put(s.getName(),s);
        if (s.is_default()) {
            set_default_server(s.getName());
        }
        rewrite_properties_device_list();
    }

    public void addDevice (XNATServer s) throws IncompleteEntryException, NonExistentEntryException, IOException  {
        if (!s.isValid()) {
            throw new IncompleteEntryException("Either name, hostname, username or password are missing");
        }
        addDevice_helper(s);
    }

}
