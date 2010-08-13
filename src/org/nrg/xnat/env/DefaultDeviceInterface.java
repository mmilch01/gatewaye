package org.nrg.xnat.env;

/**
 * Any device in a collection that can be the default.
 * @author Aditya Siram
 */
public interface DefaultDeviceInterface extends DeviceInterface {
    boolean is_default();
    void set_default();
    void unset_default();
}
