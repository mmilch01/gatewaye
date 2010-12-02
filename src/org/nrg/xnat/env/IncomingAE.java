package org.nrg.xnat.env;

import java.util.Properties;
import java.util.Vector;

import org.nrg.xnat.env.GatewayEnvironment.Log;
import org.nrg.xnat.util.DuplicateEntryException;
import org.nrg.xnat.util.NonExistentEntryException;
import org.nrg.xnat.util.Utils;

/**
 *
 * @author Aditya Siram
 */
public class IncomingAE extends NetworkDevice implements  PropertiesSynchronizedInterface,
                                                          DeviceCollectionInterface
{
    private String name;
    private String calledAETitle;
    private String hostname;
    private int port = 104;
    private static final String defaultGroupName = "Dicom.RemoteAEs";


    public IncomingAE (String name, String group_name) {
        super(name,group_name);
    }

    public IncomingAE (String name) {
        this(name,getDefaultGroupName());
    }
    
    public static String getDefaultGroupName () {
        return IncomingAE.defaultGroupName;
    }


    /**
     * @return the calledAETitle
     */
    public String getCalledAETitle() {
        return calledAETitle;
    }

    /**
     * @param calledAETitle the calledAETitle to set
     */
    public void setCalledAETitle(String calledAETitle) {
        this.calledAETitle = calledAETitle;
    }

    @Override
    public boolean duplicate_of (NetworkDevice ae) {
        return ae.isValid() &&
               hasHostname(ae.getHostname()) &&
               this.calledAETitle.equals(((IncomingAE) ae).getCalledAETitle());
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * An incoming ae object is only considered valid if it has an ae title and
     * hostname.
     * @return
     */
    @Override
    public boolean isValid() {
        return  (Utils.has_content(getCalledAETitle()) &&
                 Utils.has_content(getHostname()));
    }

    @Override
    public String toString() {
        return super.toString() + "AETitle : " + getCalledAETitle() + "\n" +
                                  "Port :" + Integer.toString(getPort());
    }

     /**
     * Update the properties list with this device's information.
     * @param p
     * @return
     * @throws IncompleteEntryException Thrown if the device does not have minimum
     *                                  required information (see isValid())
     */
    @Override
    public Properties add_to_properties(Properties p) {
        p.setProperty("Dicom.RemoteAEs." + getName() + ".CalledAETitle", getCalledAETitle());
        p.setProperty("Dicom.RemoteAEs." + getName() + ".HostNameOrIPAddress", getHostname());
        p.setProperty("Dicom.RemoteAEs." + getName() + ".Port", Integer.toString(getPort()));
        return p;
    }

    @Override
    public boolean read_from_properties(Properties p, Log log) {
        boolean correct_properties = true;
        String _aetitle = p.getProperty("Dicom.RemoteAEs." + getName() + ".CalledAETitle");
        String _hostname = p.getProperty("Dicom.RemoteAEs." + getName() + ".HostNameOrIPAddress");
        String _port = p.getProperty("Dicom.RemoteAEs." + getName() + ".Port");

        Vector warnings = new Vector();
        if (_aetitle == null || _aetitle.trim().isEmpty()) {
            warnings.add("no AE title specified");
            correct_properties = false;
        }
        if (_hostname == null || _hostname.trim().isEmpty()) {
            warnings.add("no host specified");
            correct_properties = false;
        }

        if (correct_properties) {
            setCalledAETitle(_aetitle.trim());
            setHostname(_hostname.trim());
            try {
                setPort(Integer.parseInt(_port));
            } catch (NumberFormatException e) {
                log.addWarning("Remote device " + getName() + ": could not read port, " +
                               "defaulting to port " + Integer.toString(getPort()));
            }
        }
        else {
            log.addWarning("Remote device " + getName() + " ignored:" +
                            Utils.create_delimited(Utils.toStringArray(warnings.toArray()),","));
        }
        return correct_properties;
    }



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

    @Override
    public Properties remove_from_properties(Properties p) {
        p.remove("Dicom.RemoteAEs." + getName() + ".CalledAETitle");
        p.remove("Dicom.RemoteAEs." + getName() + ".HostNameOrIPAddress");
        p.remove("Dicom.RemoteAEs." + getName() + ".Port");
        return p;
    }

    public void removeFrom (InternalNetworkDevices _i) throws DefaultDeviceDeleteException, IncompleteEntryException, NonExistentEntryException {
       if (!isValid()) {
           throw new IncompleteEntryException("Either the ae title or hostname were not set");
       }
       _i.setProperties(remove_from_properties(_i.getProperties()));
       _i.removeDevice(getName());
    }

    public void addTo (InternalNetworkDevices _i) throws IncompleteEntryException, DuplicateEntryException {
        if (!isValid()) {
            throw new IncompleteEntryException("Either the ae title or hostname were not set");
        }
        _i.setProperties(add_to_properties(_i.getProperties()));
        _i.addDevice(this);
    }

    public void update (InternalNetworkDevices _i) throws IncompleteEntryException, NonExistentEntryException, DuplicateEntryException {
        if (!isValid()) {
            throw new IncompleteEntryException("Either the ae title or hostname were not set");
        }
        _i.setProperties(add_to_properties(_i.getProperties()));
        _i.updateDevice(this);
    }


}
