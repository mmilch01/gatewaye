package org.nrg.xnat.env;

import java.util.Properties;
import java.util.Vector;

import org.nrg.xnat.util.Utils;

/**
 *
 * @author Aditya Siram
 */
class DefaultableDevice implements DefaultDeviceInterface {
    private boolean default_device;
    private String name;
    private String group_name;

     DefaultableDevice (String name, String group_name) {
        this.name = name;
        this.group_name = group_name;
        this.default_device = false;
    }

    /**
     * @return the default_server
     */
    public boolean is_default() {
        return default_device;
    }

    /**
     * @param default_server the default_server to set
     */
    public void set_default() {
        if (!is_default()) {
            this.default_device = true;
        }
    }

    public Properties add_to_properties (Properties p) {
        if (is_default()) {
            p.setProperty(this.group_name + ".default", this.name);
        }
        return p;
    }

    Vector read_from_properties (Properties p) {
        boolean correct_properties = true;
        String _d = p.getProperty(this.group_name + ".default");

        Vector warnings = new Vector();
        if (!Utils.has_content(_d)) {
            warnings.add(" no default server specified");
            correct_properties = false;
        }
        if (correct_properties) {
            if (_d.equals(this.name)) {
                set_default();
            }
        }
        return warnings;
    }

    public void unset_default () {
        if (is_default()) {this.default_device = false;}
    }

    @Override
    public String toString () {
        return "Default : " + is_default() + "\n";
    }

    public String getName() {
        return this.name;
    }
}
