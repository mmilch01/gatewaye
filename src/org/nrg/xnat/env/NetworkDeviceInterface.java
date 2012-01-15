package org.nrg.xnat.env;

/**
 *
 * @author Aditya Siram
 */
public interface NetworkDeviceInterface extends DeviceInterface {

    /**
     * Every network device has a hostname
     * @return
     */
    void setHostname(String hostname);
    String getHostname();

    /**
     * Check if the input hostname is equal to this hostname.
     * It works on differently formatted hostnames, eg
     *  "google.com" == "www.google.com" == "http://google.com" == "http://www.google.com"
     * @param hostname
     * @return
     */
    boolean hasHostname(String hostname);

    /**
     * The user is not required to enter NetworkDeviceInterface information all at once so
     * this method is necessary to ensure that the minimum information required
     * (for example, the name) is available.
     *
     * @return
     */
    boolean isValid();

 //   @Override
    String toString();
}
