package org.nrg.xnat.gateway;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.rmi.CORBA.Util;
import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.varia.NullAppender;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.DcmServiceBase.MultiDimseRsp;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.dcm4chex.archive.dcm.qrscp.AEManager;
import org.dcm4chex.archive.dcm.qrscp.QueryRetrieveScpService;
import org.dcm4chex.archive.ejb.interfaces.AEDTO;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.nrg.xnat.desktop.tools.XNATTableParser;
import org.nrg.xnat.desktop.utils.Utils;
import org.nrg.xnat.env.GatewayEnvironment;
import org.nrg.xnat.env.IncomingAE;
import org.nrg.xnat.env.XNATServer;
import org.nrg.xnat.gui.GUIUtils;
import org.nrg.xnat.gui.InitialProperties;
import org.nrg.xnat.repository.XNATRestAdapter;
import com.pixelmed.dicom.InformationEntity;

public class XNATGatewayServer implements Runnable, XNATGatewayServerMBean
{
	private static String m_ver = "Aug 18, 2010";
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
	
	public static boolean bUseDICOMUIDs=true;
	private static boolean bConsole=false;
	
	private boolean m_bStartFlag=false;
	private long start_time = 0;
	
    public void set_start_flag (boolean b) 
    {
        this.m_bStartFlag = b;
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

    public long get_start_time () {
        return this.start_time;
    }

	public static boolean isDICOMUID(){return bUseDICOMUIDs;}
	
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

	public FileInfo[][] retrieveFiles(Dataset query)
	{
		return new XNATCMoveRsp(m_XNATServer, m_XNATUser, m_XNATPass,
				m_StoreFolder).performRetrieve(query);
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
		Logger l=env.make_logger();
		
		bUseDICOMUIDs=env.isdcmuid();		
		
		l.info(DateFormat.getDateTimeInstance().format(new Date())+" Server started");

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
			while (!m_srvShutdown)
				Thread.sleep(100);
		} catch (Exception e)
		{
			if(bConsole) 
	            GUIUtils.warn("Server startup error! \n" + e.getMessage(), "Startup error");
			else 
				System.err.println("Server startup error! \n" + e.getMessage());
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
	public static XNATGatewayServer start(Properties p, final GatewayEnvironment env)
	{
		XNATGatewayServer s = null;		
		try
		{
			s = new XNATGatewayServer(env);
			if(bConsole)
			{
				System.err.println("XNAT/DICOM gateway, "+ m_ver);
				p.put(XnatServerProperties.XNATPass, "*****");
				System.err.println("properties=" + p);
			}
		}
		catch (Exception e)
		{
			Result r = Result.INITIALIZATION_EXCEPTION;
			Tools.LogException(Priority.ERROR, 
					r.toString(), e);
			return null;
		}
		s.m_bStartFlag=true;
		return s;		
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
			return Result.PROPERTIES_FILE_ERROR;
		}
		XNATGatewayServer srv;
		try
		{
			srv=new XNATGatewayServer(env);
		}
		catch(Exception e)
		{
			return Result.INITIALIZATION_EXCEPTION;
		}
		srv.m_bStartFlag=true;
		return Result.SERVER_STARTED;
	}
	
	public static Result stop()
	{
		getInstance().m_srvShutdown=true;
		try
		{
			Thread.sleep(1000);
		}
		catch(Exception e){}
		
		m_this=null;
		return Result.SERVER_STOPPED;
	}
	
	public static void main(String arg[]) throws IOException
	{
		String propFile="/config/gateway.properties";
		if((arg.length>0) && (arg[0].toLowerCase().compareTo("console")==0 
				|| arg[0].toLowerCase().compareTo("c")==0))
			bConsole=true;
		else
			bConsole=false;
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