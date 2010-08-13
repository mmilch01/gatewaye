/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nrg.xnat.env;

import java.util.Properties;
import java.util.Vector;
import org.nrg.xnat.util.Utils;

/**
 *
 * @author Aditya Siram
 */
 class AuthenticatedDevice {
    private String username;
    private String password;
    private String group_name;
    private String name;

    AuthenticatedDevice (String name, String group_name) {
        this.name = name;
        this.group_name = group_name;
    }

    String getUsername() {return this.username;}
    String getPassword() {return this.password;}

    boolean hasUsername() {return this.username != null;}
    boolean hasPassword() {return this.password != null;}

    boolean isValid() {return Utils.has_content(getUsername()) &&
                              Utils.has_content(getPassword());}

    void setUsername (String username) {this.username = username;}
    void setPassword (String password) {this.password = password;}

    Vector read_from_properties (Properties p) {
        boolean correct_properties = true;
        String _username = p.getProperty(group_name + "." + this.name + ".User");
        String _password = p.getProperty(group_name + "." + this.name + ".Pass");

        Vector warnings = new Vector();
        if (!Utils.has_content(_username)) {
            warnings.add(" no username specified");
            correct_properties = false;
        }
        if (!Utils.has_content(_password)) {
            warnings.add("no password specified");
            correct_properties = false;
        }
        if (correct_properties) {
            setUsername(_username.trim());
            setPassword(_password.trim());
        }
        return warnings;
    }

    Properties add_to_properties(Properties p) {
        p.setProperty(this.group_name + "." + this.name + ".Pass", getPassword());
        p.setProperty(this.group_name + "." + this.name + ".User", getUsername());
        return p;
    }
    Properties remove_from_properties (Properties p) {
        p.remove(this.group_name + "." + this.name + ".User");
        p.remove(this.group_name + "." + this.name + ".Pass");
        return p;
    }

    @Override
   public String toString (){
        return "Username : " + getUsername() + "\n" +
               "Password : " + getPassword() + "\n";
    }
}
