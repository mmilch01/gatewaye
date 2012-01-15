package org.nrg.xnat.env;

import java.security.NoSuchAlgorithmException;
import org.nrg.xnat.util.NonExistentEntryException;
import org.nrg.xnat.util.DuplicateEntryException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.nrg.xnat.gateway.Tools;
import org.nrg.xnat.util.Utils;

/**
 * A class that holds the current state of the devices and properties.
 * This class should only be instantiated once and is the public facing
 * class for all users outside this package.
 * @author Aditya Siram
 */

public class GatewayEnvironment {
    private int listening_port;
    private String ae_title;

    private ServerDevices servers;       // collection of XNAT servers
    private NetworkDevices incomingAEs;  // collection of remote AEs
    private static boolean initialized;
    private Log global_log;              // a log of warnings and errors
    private File log_file;

    // Properties and the associated properties file. These are kept in sync.
    private Properties p;
    private File f;

    private GatewayDevice settings;

    public GatewayEnvironment(File f) throws IOException {
        if (!initialized) {
            this.f = f;
            //??
            //System.out.println(System.getProperty("user.home"));
            System.out.println("GatewayEnvironment.line 57: " + f.toString());
            this.p = read_props(this.f);
            initialize(this.p);
        }
        else {
            throw new IOException("Cannot initialize more than one GatewayEnvironment");
        }
        this.log_file = new File(System.getProperty("user.home")+"/.xnatgateway/gateway.log");
    }

    public GatewayEnvironment(Properties p, String filename) throws IOException {
        if (!initialized) {
            this.p = p;
            this.f = new File(filename);
            Writer.write_properties(p, f);
            initialize(this.p);
        }
        else {
            throw new IOException("Cannot initialize more than one GatewayEnvironment");
        }
        this.log_file = new File(System.getProperty("user.home")+"/.xnatgateway/gateway.log");
    }

    private void initialize (Properties p) throws IOException {
        this.global_log = new Log();
        InternalNetworkDevices internal_servers = new InternalServerDevices(new Hashtable<String, NetworkDevice>(),
                                                                             this.p,
                                                                             "XNATServers",
                                                                             this.global_log,
                                                                             new PopulateWithDefaultStrategy(),
                                                                             new ServerFactory("XNATServers", this.global_log));

        InternalNetworkDevices internal_incomingAEs = new InternalNetworkDevices(new Hashtable<String, NetworkDevice>(),
                                                                                 this.p,
                                                                                 "Dicom.RemoteAEs",
                                                                                 this.global_log,
                                                                                 new PopulateStrategy(),
                                                                                 new IncomingAEFactory("Dicom.RemoteAEs", this.global_log));

        this.settings = new GatewayDevice("Dicom",this.p,this.f,this.global_log);


        Writer.backup_props_if_necessary(this.f, this.p, this.global_log);
        
        servers = new ServerDevices(internal_servers.getProperties(),
                                     this.f,
                                     internal_servers.getDevices(),
                                     internal_servers.getName());
        incomingAEs = new NetworkDevices(internal_servers.getProperties(),
                                         this.f,
                                         internal_incomingAEs.getDevices(),
                                         internal_incomingAEs.getName());
        initialized = true;
    }

    /**
     * Load the properties listed in the gateway properties file.
     * @param f
     * @return
     * @throws IOException
     */
    private Properties read_props(File f) throws IOException {
        boolean successful = false;
        Properties props = new Properties();
        FileInputStream in = new FileInputStream(f);
        try {
            props.load(in);
            successful = true;
            return props;
        }
        finally {
            try {
                in.close();
            }
            catch (IOException e) {
                if (successful) throw e;
                else Tools.LogException(Priority.ERROR,"Could not load properties file ", e);
            }
        }
    }

    public boolean isInitialized () {return initialized;}

    /**
     * Log holds a list of messages the user can retrieve and append. If the user
     * appends a FATAL message an exception holding all the messages is thrown.
     **/
    protected static class Log {
        Vector messages;
        Log () {messages = new Vector();}

        void addWarning(String warning) {
            this.messages.add(new Message(new Warning(),warning));
        }
        void addFatal(String fatal) throws IOException {
            this.messages.add(new Message(new Fatal(), fatal));
            throw new IOException(Arrays.toString(messages.toArray()));
        }

        Vector getMessages () {return messages;}
        boolean hasMessages () {return this.messages.size() > 0;}

        @Override
        public String toString () {
            Iterator i = messages.iterator();
            String [] _messages = new String [0];
            while (i.hasNext()) {
                String _m = ((Message) i.next()).toString();
                _messages = (String []) ArrayUtils.add(_messages, _m);
            }
            return Utils.create_delimited(_messages, System.getProperty("line.separator"));
        }
    }
    protected static class Message {
        private MessageType type;
        private String message;

        Message (MessageType type, String message) {
            this.type = type;
            this.message = message;
        }

        String getMessage() {return this.message;}
        void setMessage(String m) {this.message = m;}

        MessageType getMessageType () {return this.type;}
        void setMessageType (MessageType t) {this.type = t;}

        @Override
        public String toString () {
            return this.type.toString() + ":" + this.message;
        }
    }
    protected static interface MessageType {
//        @Override
        String toString();
    }
    protected static class Fatal implements MessageType {
        @Override
        public String toString() {
            return "Fatal";
        }
    }
    protected static class Warning implements MessageType {
        @Override
        public String toString() {
            return "Warning";
        }
    }

    //------------------------------------------------------------------------
    // Environment modifying functions (for public use).
    //------------------------------------------------------------------------

    /**
     * Remove an XNAT server from the master list. If the server is the default
     * XNAT server it cannot be deleted. Use set_default_server to make some
     * other server the default first.
     *
     * This method also updates the properties file.
     *
     * @param server
     * @throws IOException
     * @throws NonExistentEntryException    Thrown if the given server does not exist.
     * @throws DefaultDeviceDeleteException Thrown if the given server is the default.
     * @throws IncompleteEntryException     This should never be thrown because it indicates that
     *                                      the given server does not have either a name,
     *                                      username, password or hostname.
     */

    public void remove_server(NetworkDevice server) throws IOException, NonExistentEntryException, DefaultDeviceDeleteException, IncompleteEntryException {
        servers.remove(server);
    }

    /**
     * Remove a remote device from the master list.
     *
     * This method also updates the properties file.
     *
     * @param incomingae
     * @throws IOException
     * @throws NonExistentEntryException Thrown if the device does not exist.
     * @throws IncompleteEntryException  This should never be thrown because it indicates that
     *                                   the given server does not have either an ae title or
     *                                   hostname.
     */
    public void remove_incomingae(IncomingAE incomingae) throws IOException, NonExistentEntryException, IncompleteEntryException, DefaultDeviceDeleteException {
        incomingAEs.remove(incomingae);
    }


    /**
     * Add an XNAT server to the master list of servers. It expects a complete
     * XNATServer object meaning that the object must come loaded with a name, username, password and hostname
     * otherwise an exception is thrown.
     *
     * This method also updates the properties file.
     *
     * @param s
     * @throws DuplicateEntryException  Thrown if an attempt is made to add a server already in the list
     * @throws IOException
     * @throws IncompleteEntryException Thrown if the server does not have a name,username,password or hostname.
     */
    public void add_server(NetworkDevice s) throws DuplicateEntryException, IOException, IncompleteEntryException {
        servers.add(s);
    }

    /**
     * Change the definition of an existing XNATServer. The name of the device
     * should match an existing server in the environment or it is rejected.
     *
     * This method also updates the properties file.
     *
     * @param s
     * @throws NonExistentEntryException
     * @throws IncompleteEntryException
     * @throws IOException
     * @throws DuplicateEntryException
     */
    public void update_server (NetworkDevice s) throws NonExistentEntryException, IncompleteEntryException, IOException, DuplicateEntryException {
        servers.update(s);
    }

    /**
     * Change the definition of an exising Remote AE. The name of the device
     * should match an existing server in the environment or it is rejected.
     *
     * This method also updates the properties file
     * @param ae
     * @throws IncompleteEntryException
     * @throws NonExistentEntryException
     * @throws IOException
     * @throws DuplicateEntryException
     */
    public void update_incoming_ae(IncomingAE ae) throws IncompleteEntryException, NonExistentEntryException, IOException, DuplicateEntryException {
        incomingAEs.update(ae);
    }

    /**
     * Make the given server the default server. The current default server is modified,
     * and the properties file is updated.
     * @param server_name
     * @throws NonExistentEntryException
     * @throws IOException
     */

    public void set_default_server(String server_name) throws NonExistentEntryException, IOException {
        servers.set_default_server(server_name);
    }

    /**
     * Get the default server. There should *always* be a default server, if one isn't found
     * null is returned.
     * @param server_name
     */
    public XNATServer get_default_server() {
        return this.servers.get_default_server();
    }

    /**
     * Add a remote device to the master list. It fails unless the
     * device has an aetile and hostname. The port defaults to 104.
     *
     * This method also updates the properties file.
     *
     * @param ae
     * @throws DuplicateEntryException
     * @throws IOException
     * @throws IncompleteEntryException
     */
    public void add_incomingae(IncomingAE ae) throws DuplicateEntryException, IOException, IncompleteEntryException {
        incomingAEs.add(ae);
    }

    //--------------------------------------------------
    // Query the environment
    //--------------------------------------------------
    public boolean has_server (String name) {
        return get_server(name) != null;
    }
    public boolean has_incoming_ae (String name) {
        return get_incomingae(name) != null;
    }

    //-------------------------------------------------
    // Retrieve devices from the environment
    //-------------------------------------------------
    public XNATServer get_server(String name) {
        return (XNATServer) this.servers.get(name);
    }

    public IncomingAE get_incomingae(String name) {
        return (IncomingAE) this.incomingAEs.get(name);
    }

    public XNATServer [] get_all_servers() {
        Object [] os = get_all_devices(this.servers);
        XNATServer [] ss = new XNATServer[os.length];
        for (int i = 0; i < os.length ; i++) {
            ss[i] = (XNATServer) os[i]; 
        }
        return ss;
    }
    
    public IncomingAE[] get_all_incomingaes() {
        Object [] os = get_all_devices(this.incomingAEs);
        IncomingAE [] ss = new IncomingAE[os.length];
        for (int i = 0; i < os.length ; i++) {
            ss[i] = (IncomingAE) os[i]; 
        }
        return ss;
    }

    private Object [] get_all_devices (NetworkDevices ds) {
        Vector<NetworkDevice> _ss = new Vector<NetworkDevice>();
        for (Enumeration e = ds.devices.keys(); e.hasMoreElements();) {
            String device_name = (String) e.nextElement();
            NetworkDevice _d = (NetworkDevice) ds.get(device_name);
            _ss.add(_d);
        }
        return  _ss.toArray();
    }

    public Logger make_logger() {
        Logger l = Logger.getRootLogger();
        BasicConfigurator.configure(new NullAppender());
        Appender appender = null;
        SimpleLayout layout = new SimpleLayout();
        try {
            if (this.p.getProperty("Logger.Output").toLowerCase().compareTo("file") == 0) {
                appender = new FileAppender(layout, System.getProperty("user.home")+"/.xnatgateway/gateway.log", false);
            } else {
                appender = new ConsoleAppender(layout);
            }
        } catch (Exception e) {
            if (appender == null) {
                appender = new ConsoleAppender(layout);
            }
        }

        l.addAppender(appender);
        String str;
        if ((str = this.p.getProperty("Dicom.DebugLevel")) != null) {
            l.setLevel(Level.toLevel(str));
        } else {
            l.setLevel(Level.INFO);
        }

        return l;
    }


    public void set_callingaetitle (String name) throws IOException {
        this.settings.setName(name);
    }

    public void set_listening_port (int port) throws IOException {
        this.settings.setPort(port);
    }

    public String get_callingaetitle () {
        return this.settings.getName();
    }
    
    public boolean isdcmuid(){return settings.useDCMUIDs();}
    
    public int get_listening_port () {
        return this.settings.getPort();
    }
    public Properties get_properties() {
        return this.p;
    }

    public String get_cache_folder () {
        return this.settings.getCacheFolder();
    }

    public void set_cache_folder (String name) throws IOException {
        this.settings.setCacheFolder(name);
    }

    public String get_calledae_title () {
        return this.settings.getName();
    }

    public List get_log_messages () throws IOException {
        return FileUtils.readLines(this.log_file);
    }

    public IncomingAE make_ae (String name) {
        return new IncomingAE(name, this.global_log);
    }

    public XNATServer make_xnat (String name) throws IOException {
        XNATServer s = null;
        try {
            s = new XNATServer(name, this.global_log);
        } catch (NoSuchAlgorithmException ex) {
            this.global_log.addFatal(ex.toString());
            java.util.logging.Logger.getLogger(GatewayEnvironment.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IOException ex) {
            this.global_log.addFatal(ex.toString());
        }
        return s;
    }
}