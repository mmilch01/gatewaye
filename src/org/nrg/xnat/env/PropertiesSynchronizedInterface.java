/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nrg.xnat.env;

import java.util.Properties;
import org.nrg.xnat.env.GatewayEnvironment.Log;

/**
 * A device that implements this interface is able to write its
 * current state to and remove itself from a set of properties.
 * @author Aditya Siram
 */
interface PropertiesSynchronizedInterface {
    /**
     * Update the properties list with this device's information.
     * @param p
     * @return
     * @throws IncompleteEntryException Thrown if the device does not have minimum
     *                                  required information (see isValid())
     */
    Properties add_to_properties(Properties p);
    /**
     * Remove this device's information from the properties. Expects that the
     * device has minimum information and ensures that the one default device (if it exists)
     * is not deleted.
     *
     * @param p
     * @return
     * @throws IncompleteEntryException
     * @throws DefaultDeviceDeleteException
     */
    Properties remove_from_properties(Properties p);
    /**
     * Populate attributes about this device from the given properties. Warnings/errors
     * if any are added to the given log.
     * @param p
     * @param log
     * @return true if the read was successful and false otherwise.
     */
    boolean read_from_properties(Properties p, Log log);
}
