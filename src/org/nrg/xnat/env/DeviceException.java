package org.nrg.xnat.env;

/**
 * General parent exception of all non-IO exceptions thrown by the environment.
 *
 * @author Aditya Siram
 */
public class DeviceException extends Exception {
      public DeviceException (String message) {
        super(message);
    }
}
