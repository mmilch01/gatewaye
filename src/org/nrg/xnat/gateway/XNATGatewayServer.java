package org.nrg.xnat.gateway;
import java.lang.System;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.swing.JOptionPane;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.dcm4che.data.Dataset;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.dcm4chex.archive.dcm.qrscp.AEManager;
import org.dcm4chex.archive.dcm.qrscp.QueryRetrieveScpService;
import org.dcm4chex.archive.ejb.interfaces.AEDTO;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.nrg.xnat.env.GatewayEnvironment;
import org.nrg.xnat.env.IncomingAE;
import org.nrg.xnat.env.XNATServer;
import org.nrg.xnat.gui.GUIUtils;
import org.nrg.xnat.gui.InitialProperties;

public class XNATGatewayServer implements Runnable, XNATGatewayServerMBean
{
	// private static QueryRetrieveScpService m_qrServ;
	private Server m_dcmServer;	
	private boolean m_srvShutdown = false;

	protected long m_RandSeed = 0;
	protected String m_StoreFolder, m_XNATServer, m_XNATUser, m_XNATPass,
			m_AETitle = null;
	protected AEDTO m_localAE;
	protected int m_maxCacheFiles = 50000;
	protected static XNATGatewayServer m_this = null;
	protected AEServer m_ael=new AEServer();
	private GatewayEnvironment m_env=null;
	
	public static boolean bUseDICOMUIDs=true;
	private static boolean bConsole=false;
		
	private boolean m_bStartFlag=false;
	private long start_time = 0;
        private Logger l;
	
    private void set_start_flag (boolean b)
    {
        this.m_bStartFlag = b;
    }
    public Boolean isAnonymousAEAllowed()
    {
    	if(m_env==null) return false;
    	return m_env.get_anoymous_ae_allowed();
    }
    public boolean is_running () 
    {
        return this.m_bStartFlag;
    }

    public String uptime () 
    {
        long now = new Date().getTime();
        return org.nrg.xnat.util.Utils.print_elapsed_time(now - start_time);
    }
    public static long get_start_time () {
        return getInstance().start_time;
    }

	public static boolean isDICOMUID(){return true; /*bUseDICOMUIDs;*/}
	
	public boolean test()
	{
		return true;
/*		
		String s0="CNDA_E01841",s1="CNDA_E01841_1";
		String s01=Utils.String2UID(s0), s11=Utils.String2UID(s1);
		System.out.println(s0);
		System.out.println(s01);
		System.out.println(s1);
		System.out.println(s11);
		
		System.out.println(Utils.UID2String(s01));
		System.out.println(Utils.UID2String(s11));
		return false;
*/
	}
	public void instancesSent(ArrayList fileInfos)
	{
		//delete files that were sent.
		for(Object fi:fileInfos)
		{
			new File(((FileInfo)fi).fileID).delete();
		}
	}
	
	public String getCalledAET(){return m_AETitle;}
	public static XNATGatewayServer getInstance()
	{
		return m_this;
	}
	
	//retrieve the entire set directly (slow)
	public FileInfo[][] retrieveFiles(Dataset query)
	{
		return new XNATCMoveRsp(m_XNATServer, m_XNATUser, m_XNATPass,
		m_StoreFolder).performRetrieve(query);		
	}
	
	//retrieve series, has to be preceded by call to getSeriesRequests (faster, doesn't work for C-GET).
	public FileInfo[][] retrieveSeries(Dataset query, TreeMap scanMap)
	{
		return new XNATCMoveRsp(m_XNATServer, m_XNATUser, m_XNATPass,
				m_StoreFolder).retrieveSeries(query,scanMap);
	}
	public LinkedList<Object> getSeriesRequests(Dataset query)
	{
		return new XNATCMoveRsp(m_XNATServer, m_XNATUser, m_XNATPass,
				m_StoreFolder).getSeriesRequests(query);		
	}
	public XNATCFindRsp getMultiCFindRsp(Dataset query)
	{
		XNATCFindRsp rsp = new XNATCFindRsp(query, m_XNATServer, m_XNATUser,
				m_XNATPass);
		rsp.executeQuery();
		return rsp;
	}

	public XNATGatewayServer(GatewayEnvironment env) throws Exception
	{            
		if(!test()) return;
		m_env=env;
		this.l=env.make_logger();
		
		bUseDICOMUIDs=true;
//			env.isdcmuid();
		
		l.info(DateFormat.getDateTimeInstance().format(new Date())+" XNAT Gateway server v. "+env.version+" started");

		QueryRetrieveScpService srv = new QueryRetrieveScpService();
		if(!initServerParams(srv)) throw new IOException();

		String str=env.get_callingaetitle();
		if(str==null)	throw new IOException();
		srv.setCalledAETs(str);		
		
		srv.setCoerceRequestPatientIds(true);
		initRemoteAEs(env);
		
		String aets = m_ael.getAETList();
		if(aets!=null)
		{
			aets = aets.replace(' ', '\\');
			srv.setCallingAETs(aets);
			String perm=srv.getUnrestrictedQueryPermissionsToAETitles();
			
			if (perm.compareTo("ANY")!=0)
			{
				srv.setUnrestrictedQueryPermissionsToAETitles(aets);		
				System.out.println("Unrestricted query permissions for all AEs are disabled");
			}
			else
				System.out.println("Unrestricted query permissions for all AEs are enabled");
		}
		else 
		{
			if(!bConsole)
				JOptionPane.showMessageDialog(null, "No remote AEs are configured.\nPlease configure every DICOM AE you plan to use with Gateway.");
			else
			{
				l.error("Error: No remote AEs are configured");
				throw new IOException("No remote AEs are configured");				
			}
		}			
		srv.startService();
		
		m_dcmServer = ServerFactory.getInstance()
				.newServer(srv.getDcmHandler());
		m_dcmServer.setPort(env.get_listening_port());
		m_StoreFolder = env.get_cache_folder();
		
		File sf = new File(m_StoreFolder);
		if (!sf.exists())
			sf.mkdir();
		//clean up from the previous run
		try
		{
			for(File f:new File(m_StoreFolder).listFiles()) f.delete();
		}
		finally{}
		XNATServer xs=env.get_default_server();
		if(xs==null || !xs.isValid())
			throw new IOException ("Incorrect XNAT server configuration");
		
		m_XNATServer = xs.getHostname();		
		m_XNATUser = xs.getUsername();
		m_XNATPass = xs.getPassword();
		m_AETitle = env.get_calledae_title();		
		m_this = this;
		// m_dcmServer.start();
		
		m_localAE=new AEDTO(0, m_AETitle, "localhost", env.get_listening_port(),				
				"", "", "", "", "","", "");
		m_ael.setLocalAE(m_localAE);
		srv.setAEManager(
				new AEManager()
				{
					public AEDTO findByPrimaryKey(long l)
					{
						return null;
					}
					public AEDTO findByAET(String s)
					{
						if(s.compareTo(m_AETitle)!=0) return null;
						return m_localAE;
					}
					public List<AEDTO> findAll()
					{
						LinkedList<AEDTO> llae=new LinkedList<AEDTO>();
						llae.add(m_localAE);
						return llae;
					}
				}
		);

		XNATQueryGenerator.LoadVocabulary("./config/vocabulary.xml");
		//this attribute does not affect the behavior and the statement below is therefore misleading.
//		System.out.println("Anonymous AE allowed: " + env.get_anoymous_ae_allowed());
		start_time = new Date().getTime();		
		new Thread(this).start();		
	}
	private void initRemoteAEs(GatewayEnvironment env)
	{
		IncomingAE [] aes=env.get_all_incomingaes();
		for(IncomingAE ae: aes)
			m_ael.addAE(new AEDTO(0, ae.getCalledAETitle(), ae.getHostname(), ae.getPort(),
					"", "", "", "", "",	"", ""));
	}
	public void run()
	{
		try
		{
 			m_dcmServer.start();
                        this.set_start_flag(true);
			while (!m_srvShutdown)
				Thread.sleep(100);
		}
                catch (Exception e)
		{
                    this.set_start_flag(false);
                    l.log(Level.FATAL, e.getMessage());
                    if (bConsole) {
                        System.err.println("Server startup error! \n" + e.getMessage());
                        System.exit(1);
                    }
                    else{
                        GUIUtils.warn("Server startup error! \n" + e.getMessage(), "Startup error");
                    }
		}
		finally
		{
			m_dcmServer.stop();
			m_bStartFlag=false;
			m_srvShutdown=false;
		}
	}
	private void unregisterMBean(String s)
	{
		try
		{
			ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(s));
		}
		catch(Exception e){}
	}
	
	private boolean initServerParams(QueryRetrieveScpService srv)
	throws Exception 
	{
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		Document bD;
		Document vD;
		
		//unregister previously registered MBean's
		unregisterMBean("org.nrg.xnat.gateway:type=AEServer");
		unregisterMBean("org.nrg.xnag.gateway:type=GatewayServer");
		
		mbs.registerMBean(m_ael, new ObjectName(
				"org.nrg.xnat.gateway:type=AEServer"));
		srv.setAEServiceName(new ObjectName(
				"org.nrg.xnat.gateway:type=AEServer"));
		
		mbs.registerMBean(XNATGatewayServer.this, new ObjectName(
				"org.nrg.xnag.gateway:type=GatewayServer"));
			// srv.setDcmServerName(dcmServerName)
		bD = new SAXReader().read(new File(
				"./config/dcm4chee-qrscp-xmbean.xml"));
		vD = new SAXReader().read(new File("./config/qrscp-config.xml"));
		
		final class MAttr
		{
			String name, getMethod, setMethod, type;
			public MAttr(Element el)
			{
				name = el.element("name").getText();
				type = el.element("type").getText();
				getMethod = el.attributeValue("getMethod");
				setMethod = el.attributeValue("setMethod");
			}
		};
		TreeMap<String, MAttr> attMap = new TreeMap<String, MAttr>();
		Element element = bD.getRootElement();
		// elementByID("mbean");
		for (Iterator<Element> it = element.elementIterator(); it.hasNext();)
		{
			Element eln = it.next();
			if (eln.getName().compareTo("attribute") != 0)
				continue;
			MAttr attr = new MAttr(eln);
			attMap.put(attr.name, attr);
		}
		for (Iterator<Element> it = vD.getRootElement().elementIterator(); it
				.hasNext();)
		{
			Element eln = it.next();
			try
			{
				MAttr att = attMap.get(eln.attribute("name").getText());
				Class type = Class.forName(att.type);
				Method method = srv.getClass().getMethod(att.setMethod, type);
				String param = eln.getText();
				if (att.type.compareTo("java.lang.String") != 0)
				{
					method.invoke(srv, type.getMethod("valueOf",
							Class.forName("java.lang.String")).invoke(null,
							param));
				} else
				{
					method.invoke(srv, param);
				}
			} catch (Exception e)
			{
			}
		}
		return true;
	}
	public static void start(Properties p, final GatewayEnvironment env)
	{
		try
		{
			m_this = new XNATGatewayServer(env);
                        
			if(bConsole)
			{
				System.err.println("XNAT/DICOM gateway, "+ GatewayEnvironment.version);
				p.put(XnatServerProperties.XNATPass, "*****");
				System.err.println("properties=" + p);
			}
		}
		catch (Exception e)
		{
			Result r = Result.INITIALIZATION_EXCEPTION;
			Tools.LogException(Priority.ERROR, 
					r.toString(), e);
		}	
	}
        public static boolean isRunning(){
            return (getInstance() != null && getInstance().is_running());
        }

	public static Result start(String prop)
	{
		GatewayEnvironment env;
		try
		{
			env=new GatewayEnvironment(new File(prop));
		}
		catch(Exception e)
		{
			System.out.println ("Error reading properties file "+prop);
			e.printStackTrace();
			return Result.PROPERTIES_FILE_ERROR;
		}
		XNATGatewayServer srv;
		try
		{
			srv=new XNATGatewayServer(env);
		}
		catch(Exception e)
		{
			System.out.println("Error initializing environment from properties file "+prop);
			return Result.INITIALIZATION_EXCEPTION;
		}
		srv.m_bStartFlag=true;
		return Result.SERVER_STARTED;
	}
	
	public static Result stop()
	{
            try {
                getInstance().m_srvShutdown = true;
                try {
                    Thread.sleep(1000);
		}catch(Exception e){
                    System.out.println("Could not stop");
                }
                m_this = null;
            }
            catch (NullPointerException e) {}
            return Result.SERVER_STOPPED;
	}
	
	public static void main(String arg[]) throws IOException, SecurityException
	{
		if((arg.length>0) && (arg[0].toLowerCase().compareTo("console")==0 
				|| arg[0].toLowerCase().compareTo("c")==0))
			bConsole=true;
		else
			bConsole=false;
		new File(System.getProperty("user.home")+"/.xnatgateway/tmp").mkdirs();
		String propFile=System.getProperty("user.home")+"/.xnatgateway/gateway.properties.test";
		System.out.println ("using properties file "+propFile);			
		if(bConsole)			
		{			
			System.err.println(start(propFile));
		}
		else
		{			
			InitialProperties i = new InitialProperties(new File(propFile));
		}		
	}
	@Override
	protected void finalize() throws Throwable
	{
		// if(m_dcmServer!=null)
		// m_dcmServer.stop();
		super.finalize();
	}
	/*
	 * public class AEServer implements AEServerMBean { private
	 * TreeMap<String,AEDTO> m_AEs=new TreeMap<String,AEDTO>(); public AEDTO
	 * findAE(String aet, InetAddress addr) { return m_AEs.get(aet); } public
	 * void addAE(AEDTO ae) { m_AEs.put(ae.getTitle(), ae); } }
	 */
}