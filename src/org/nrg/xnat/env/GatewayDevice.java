package org.nrg.xnat.env;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.nrg.xnat.env.GatewayEnvironment.Log;
import org.nrg.xnat.util.Utils;

/**
 *
 * @author Aditya Siram
 */
class GatewayDevice  {
    private String name;
    private int port = 104;
    private final String hostname = "127.0.0.1";
    private final String group_name = "Dicom";
    private boolean usedcmuid=true;
    private Properties p;
    private File f;
    private Log l;
    private String cache_folder;
    
    GatewayDevice (String name, Properties p, File f, Log l) throws IOException {
        this.name = name;
        this.f = f;
        this.p = p;
        this.l = l;
        populate();
    }

    
    public void setHostname(String hostname) {
        throw new UnsupportedOperationException("Cannot change the hostname of the gateway. It always runs locally.");
    }

    public boolean hasHostname(String hostname) {
        return this.hostname.equals(hostname);
    }
    
    public String getHostname () {
        return this.hostname;
    }
    
    public String getGroupName () {
        return this.group_name;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public boolean useDCMUIDs(){return usedcmuid;}

    public void sync (Properties _p) throws IOException {
        Writer.write_properties(_p, this.f);
    }

    public Properties sandbox () {
        Properties _p = this.p;
        return _p;
    }

    private void write_property (String _name, String _value) throws IOException {
        Properties _p = sandbox();
        _p.setProperty(this.group_name + "." + _name, _value);
        sync(_p);
        this.p = _p;
    }

    public void setPort(int port) throws IOException {
        write_property("ListeningPort", Integer.toString(port));
        this.port = port;
    }

    public void setName(String name) throws IOException {
        if (!Utils.has_content(name)) {
            write_property("CallingAETitle",name);
            this.name = name;
        }
        else {
            this.l.addWarning("Bad AE Title : Cannot give the gateway an empty AE title");
        }
    }

    public void setCacheFolder (String name) throws IOException {
        if (!Utils.has_content(name)) {
            write_property("Application.SavedImagesFolderName",name);
            this.cache_folder = name;
        }
        else {
            this.l.addWarning("Bad Cache Folder : Cannot give the gateway an empty folder name");
        }
    }

    public String getCacheFolder () {
        return this.cache_folder;
    }

    public String getName () {
        return this.name;
    }

    public boolean isValid() {
        return this.name != null;
    }

    public void populate() throws IOException {
        String _name = this.p.getProperty(this.group_name + "." + "CallingAETitle");
        String _port = this.p.getProperty(this.group_name + "." + "ListeningPort");
        String _cache = this.p.getProperty("Application.SavedImagesFolderName");

        if (!Utils.has_content(_name)) {
            this.l.addFatal("Gateway AE title is missing.");
        }

        if (!Utils.has_content(_cache)) {
            this.l.addFatal("No cache folder specified");
        }

        if (!Utils.has_content(_port)) {
            this.l.addWarning("Gateway port is missing, defaulting to port " + Integer.toString(this.port));
        }
        
        String usedcm=p.getProperty("Xnat.UseDICOMUIDs");
        if(usedcm==null || usedcm.compareTo("1")!=0) 
        	usedcmuid=false; 
        	
        
        this.name = _name;
        this.cache_folder = _cache;
        try {
            setPort(Integer.parseInt(_port));
        } catch (NumberFormatException e) {
            this.l.addWarning("Could not read the Gateway port, " +
                              "defaulting to port " + Integer.toString(getPort()));
            setPort(this.port);
        }
    }
}
