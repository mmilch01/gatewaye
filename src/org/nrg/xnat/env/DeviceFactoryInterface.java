
package org.nrg.xnat.env;

/**
 * A factory to create new device instances.
 * @author Aditya Siram
 */
public interface DeviceFactoryInterface {
    NetworkDevice make(String name);
}
