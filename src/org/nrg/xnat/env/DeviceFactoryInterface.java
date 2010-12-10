
package org.nrg.xnat.env;

import java.io.IOException;
import org.nrg.xnat.env.GatewayEnvironment.Log;

/**
 * A factory to create new device instances.
 * @author Aditya Siram
 */
public interface DeviceFactoryInterface {
    NetworkDevice make(String name, Log log) throws IOException;
}
