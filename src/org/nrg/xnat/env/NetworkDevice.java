package org.nrg.xnat.env;

import java.util.Properties;
import java.util.Vector;

import org.nrg.xnat.env.GatewayEnvironment.Log;
import org.nrg.xnat.util.Utils;

/**
 *
 * @author Aditya Siram
 */
public abstract class NetworkDevice implements NetworkDeviceInterface,
                                               PropertiesSynchronizedInterface,
                                               DeviceCollectionInterface
{
    private String hostname;
    private String name;
    private String group_name;

    public NetworkDevice (String name, String group_name) {
        this.name = name;
        this.group_name = group_name;
    }

    /**
     * @param hostname the hostname to set
     */
    @Override
    public void setHostname(String hostname) {
        if (hostname != null && !hasHostname(hostname) && !hostname.trim().isEmpty()) {
            this.hostname = hostname;
        }
    }

    @Override
    public String getName () {
        return this.name;
    }

    public String getGroupName () {
        return this.group_name;
    }


    @Override
    public boolean hasHostname(String hostname) {
        return this.hostname != null && Utils.normalizeURL(this.hostname).equals(Utils.normalizeURL(hostname));
    }

    @Override
    public String getHostname() {
        return this.hostname;
    }

    @Override
    public boolean isValid() {
        return (Utils.has_content(this.hostname) &&
                Utils.has_content(this.name));
    }

    @Override
    public String toString() {
        return "\n" +
               "Name : " + this.getName() + "\n" +
               "Hostname : " + this.getHostname() + "\n";
    }

    public Vector read_hostname_prop (Properties p, Log log, String key) {
        String _hostname = p.getProperty(getGroupName() + "." +  getName() + "." + key);

        Vector warnings = new Vector();
        if (_hostname == null) {
            warnings.add("no host specified");
        }
        else {
            setHostname(_hostname.trim());
        }
        return warnings;
    }

    public abstract boolean duplicate_of(NetworkDevice d);
}
