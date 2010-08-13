/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nrg.xnat.env;

import org.nrg.xnat.util.NonExistentEntryException;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;
import org.nrg.xnat.util.DuplicateEntryException;

/**
 * A collection of network devices and properties. Each collection can only have one
 * type of device, so it is bad for example to mix XNATServers and Remote AE's into
 * the same collection even though they are both network devices.
 *
 * @author Aditya Siram
 */

class NetworkDevices {
    protected Hashtable<String, NetworkDevice>  devices;
    protected Properties p;
    protected File f;
    protected String name;

    NetworkDevices (Properties p, File f, String name) {
        this.p = p;
        this.f = f;
        this.name = name;
    }

    NetworkDevices (Properties p, File f, Hashtable<String,NetworkDevice> h, String name) {
        this.p = p;
        this.f = f;
        this.devices = h;
        this.name = name;
    }

    /**
     * Isolate the current state of the devices so function can rollback a modification
     * if it errors.
     *
     * @return
     */
    InternalNetworkDevices sandbox_devices () {
        return new InternalNetworkDevices((Hashtable<String,NetworkDevice>) this.devices.clone(),
                                          (Properties) this.p.clone(),
                                          this.name);
    }

    void syncWith (InternalNetworkDevices ds) throws IOException {
        Writer.write_properties(ds.getProperties(), this.f);
        this.devices = ds.getDevices();
        this.p = ds.getProperties();
    }

    void remove (NetworkDevice d) throws IOException, NonExistentEntryException, DefaultDeviceDeleteException, IncompleteEntryException {
        InternalNetworkDevices _i = sandbox_devices();
        d.removeFrom(_i);
        syncWith(_i);
    }

    void add (NetworkDevice d) throws IncompleteEntryException, IOException, DuplicateEntryException {
        InternalNetworkDevices _i = sandbox_devices();
        d.addTo(_i);
        syncWith(_i);
    }

    void update (NetworkDevice d) throws IncompleteEntryException,  IOException, NonExistentEntryException, DuplicateEntryException {
        InternalNetworkDevices _i = sandbox_devices();
        d.update(_i);
        syncWith(_i);
    }

    NetworkDevice get (String name) {
        return (NetworkDevice) this.devices.get(name);
    }
}