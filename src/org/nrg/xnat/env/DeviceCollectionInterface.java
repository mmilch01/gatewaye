/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nrg.xnat.env;

import org.nrg.xnat.util.DuplicateEntryException;
import org.nrg.xnat.util.NonExistentEntryException;

/**
 * Functions a child device can use to modify it's parent collection
 * @author Aditya Siram
 */

public interface DeviceCollectionInterface extends DeviceInterface {
    /**
     * Adds this device to the parent collection.
     * @param d
     * @throws IncompleteEntryException Thrown if the device is invalid.
     * @throws DuplicateEntryException  Thrown if the device already exists in the collection.
     */
    void addTo(InternalNetworkDevices d) throws IncompleteEntryException, DuplicateEntryException;
    /**
     * Remove this device from the parent collection
     * @param d
     * @throws DefaultDeviceDeleteException Thrown if this device is the default device
     * @throws IncompleteEntryException     Thrown if the device is invalid.
     * @throws NonExistentEntryException    Thrown if this device does not exist in the collection.
     */
    void removeFrom(InternalNetworkDevices d) throws DefaultDeviceDeleteException, IncompleteEntryException, NonExistentEntryException;
    /**
     * Modify an existing device. Everything can be modified but the unique name.
     * To modify the name the device has to be completely removed and added back
     * to the collection
     *
     * @param d
     * @throws IncompleteEntryException   Thrown if the device is invalid.
     * @throws NonExistentEntryException  Thrown if this device does not exist in the collection.
     * @throws DuplicateEntryException    Thrown if there is a duplicate of this device in the collection.
     *                                    Two devices with different names can still be duplicates if, for
     *                                    example, the hostnames are the same. All devices in the collection
     *                                    must have unique names and hostnames.
     */
    void update(InternalNetworkDevices d) throws IncompleteEntryException, NonExistentEntryException, DuplicateEntryException;
}
