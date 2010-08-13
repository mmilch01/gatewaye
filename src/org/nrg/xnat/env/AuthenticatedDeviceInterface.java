/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nrg.xnat.env;

/**
 * Any device that has an associated username and password
 * @author deech
 */
public interface AuthenticatedDeviceInterface extends DeviceInterface {
    String getUsername();
    String getPassword();

    void setUsername (String username);
    void setPassword (String password);

    boolean isValid();
}
