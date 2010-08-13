/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nrg.xnat.env;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import org.nrg.xnat.util.NonExistentEntryException;

/**
 * XNAT Servers are a special case of network devices because the collection needs to have
 * a way of setting and getting the default server.
 * @author Aditya Siram
 */
class ServerDevices extends NetworkDevices {

    ServerDevices(Properties p, File f, String name) {
        super(p,f,name);
    }

    ServerDevices (Properties p, File f, Hashtable<String,NetworkDevice> h, String name) {
        super(p, f, h, name);
    }

    @Override
    InternalNetworkDevices sandbox_devices () {
        return new InternalServerDevices(this.devices, this.p,this.name);
    }

    void set_default_server (String name) throws NonExistentEntryException, IOException {
        InternalServerDevices d = (InternalServerDevices) sandbox_devices();
        d.set_default_server(name);
        syncWith(d);
    }

    XNATServer get_default_server () {
        XNATServer d = null;
        for (Enumeration e = this.devices.keys(); e.hasMoreElements();) {
            String device_name = (String) e.nextElement();
            XNATServer _d = (XNATServer) this.devices.get(device_name);
            if (_d.is_default()) {d = _d;}
        }
        return d;
     }
}
