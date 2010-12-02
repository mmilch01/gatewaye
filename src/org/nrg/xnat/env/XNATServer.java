package org.nrg.xnat.env;

import java.util.Properties;
import java.util.Vector;

import org.nrg.xnat.env.GatewayEnvironment.Log;
import org.nrg.xnat.util.DuplicateEntryException;
import org.nrg.xnat.util.NonExistentEntryException;
import org.nrg.xnat.util.Utils;


/**
 *
 * @author Aditya Siram
 */
public class XNATServer extends NetworkDevice implements AuthenticatedDeviceInterface,
                                                         DefaultDeviceInterface,
                                                         PropertiesSynchronizedInterface,
                                                         DeviceCollectionInterface
{
    private AuthenticatedDevice auth_info;
    private DefaultableDevice default_info;
    private static final String defaultGroupName = "XNATServers";

    public XNATServer(String name, String group_name) {
        super(name,group_name);
        auth_info = new AuthenticatedDevice(name,group_name);
        default_info = new DefaultableDevice(name, group_name);
    }
    
    public XNATServer(String name) {
        this(name, getDefaultGroupName());
    }

    public static String getDefaultGroupName () {
        return XNATServer.defaultGroupName;
    }

    /**
     * Two xnat server entries are considered duplicates if they have the same hostname
     */
    @Override
    public boolean duplicate_of(NetworkDevice d) {
        return d.isValid() && super.hasHostname(d.getHostname());
    }

    /**
     * @return the username
     */
    @Override
    public String getUsername() {
        return this.auth_info.getUsername();
    }

    @Override
    public boolean isValid() {
        return super.isValid() && this.auth_info.isValid();
    }

    /**
     * @param username the username to set
     */
    @Override
    public void setUsername(String username) {
        this.auth_info.setUsername(username);
    }

    /**
     * @return the password
     */
    @Override
    public String getPassword() {
        return this.auth_info.getPassword();
    }

    /**
     * @param password the password to set
     */
    @Override
    public void setPassword(String password) {
        this.auth_info.setPassword(password);
    }



    @Override
    public Properties add_to_properties (Properties p) {
        p.setProperty(getGroupName() + "." + getName() + ".ServerURL", getHostname());
        p = this.auth_info.add_to_properties(p);
        p = this.default_info.add_to_properties(p);
        return p;
    }
    

    @Override
    public Properties remove_from_properties (Properties p) {
        p.remove(getGroupName() + "." +  getName() + ".ServerURL");
        p = this.auth_info.remove_from_properties(p);
        return p;
    }

    @Override
    public boolean read_from_properties(Properties p, Log log) {
        boolean correct_properties = true;
        Vector warnings = read_hostname_prop(p, log, "ServerURL");
        if (!warnings.isEmpty()) {
           correct_properties = false;
        }
        Vector auth_warnings = this.auth_info.read_from_properties(p);
        if (!auth_warnings.isEmpty()) {
            correct_properties = false;
            warnings.addAll(auth_warnings);
        }
        if (!correct_properties) {
            log.addWarning("Ignoring server " + getName() + ":" +
                           Utils.create_delimited(Utils.toStringArray(warnings.toArray()),","));
        }
        return correct_properties;
    }

    @Override
    public void removeFrom (InternalNetworkDevices _i) throws DefaultDeviceDeleteException, IncompleteEntryException, NonExistentEntryException {
        if (!isValid()) {
            throw new IncompleteEntryException("Either name, hostname, username or password are missing");
        }
        if (is_default()) {
            throw new DefaultDeviceDeleteException("Server : " + getName() + " is the default XNAT server. " +
                                                   " Please make another XNAT server the default before deleting this one.");
        }
        _i.setProperties(remove_from_properties(_i.getProperties()));
        _i.removeDevice(getName());
    }

    @Override
    public void addTo (InternalNetworkDevices _i) throws IncompleteEntryException, DuplicateEntryException {
        if (!isValid()) {
            throw new IncompleteEntryException("Either name, hostname, username or password are missing");
        }
        if (is_default()) {
            this.default_info.set_default();
        }
        _i.setProperties(add_to_properties(_i.getProperties()));
        _i.addDevice(this);
    }


    @Override
    public void update (InternalNetworkDevices _i) throws IncompleteEntryException, NonExistentEntryException, DuplicateEntryException {
        if (!isValid()) {
            throw new IncompleteEntryException("Either name, hostname, username or password are missing");
        }
        if (is_default()) {
            this.default_info.set_default();
        }
        _i.setProperties(add_to_properties(_i.getProperties()));
        _i.updateDevice(this);
    }

    public boolean is_default() {
        return this.default_info.is_default();
    }

    public void set_default() {
        this.default_info.set_default();
    }

    public void unset_default () {
        this.default_info.unset_default();
    }

    @Override
    public String toString () {
        return super.toString() + auth_info.toString() + default_info.toString();
    }
}
