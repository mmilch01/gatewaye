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
import org.nrg.xnat.repository.XNATRestAdapter;
import com.pixelmed.dicom.InformationEntity;

public class XNATGatewayServer implements Runnable, XNATGatewayServerMBean
{
	private static String m_ver = "May 20, 2010";
	// private static QueryRetrieveScpService m_qrServ;
	private Server m_dcmServer;
	private static boolean m_srvShutdown = false;

	protected long m_RandSeed = 0;
	protected String m_StoreFolder, m_XNATServer, m_XNATUser, m_XNATPass,
			m_AETitle = null;
	protected AEDTO m_localAE;
	protected int m_maxCacheFiles = 50000;
	protected static XNATGatewayServer m_this = null;
	protected AEServer m_ael=new AEServer();

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

	public XNATGatewayServer(Properties props) throws Exception
	{
		if(!test()) return;
		Logger l=Logger.getRootLogger();
		BasicConfigurator.configure(
				new NullAppender());
		
		Appender appender=null;
		SimpleLayout layout=new SimpleLayout();
		try
		{
			if(props.getProperty("Logger.Output").toLowerCase().compareTo("file")==0)
				appender=new FileAppender(layout,"./gateway.log",false);
			else 
				appender=new ConsoleAppender(layout);
//			m_maxCacheFiles=Integer.parseInt(props.getProperty("Application.FilesInCache"));
		}
		catch(Exception e)
		{
			if(appender==null)
				appender=new ConsoleAppender(layout);
		}

		l.addAppender(appender);
		String str;
		if((str=props.getProperty("Dicom.DebugLevel"))!=null)
		{
			l.setLevel(Level.toLevel(str));			
		}
		else l.setLevel(Level.WARN);
		
		l.info(DateFormat.getDateTimeInstance().format(new Date())+" Server started");
				
		QueryRetrieveScpService srv = new QueryRetrieveScpService();
		initServerParams(srv);

		str=props.getProperty("Dicom.CallingAETitle");
		if(str==null)	throw new IOException();
		srv.setCalledAETs(str);
		// srv.setCallingAETs(props.getProperty("Dicom.CalledAETitile"));
		srv.setCoerceRequestPatientIds(true);
		initRemoteAEs(props);
		
		String aets = m_ael.getAETList();
		if(aets!=null)
		{
			aets = aets.replace(' ', '\\');
			srv.setCallingAETs(aets);		
		}
		// srv.setAcceptedStandardSOPClasses(s)			
		// srv.setDcmServerName(new ObjectName("GatewayDcmQRSCP"));
		srv.startService();
		m_dcmServer = ServerFactory.getInstance()
				.newServer(srv.getDcmHandler());
		m_dcmServer.setPort(Integer.valueOf(
				props.getProperty("Dicom.ListeningPort")).intValue());
		m_StoreFolder = props.getProperty("Application.SavedImagesFolderName");
		
		File sf = new File(m_StoreFolder);
		if (!sf.exists())
			sf.mkdir();
		//clean up from the previous run
		try
		{
			for(File f:new File(m_StoreFolder).listFiles()) f.delete();
		}
		finally{}
		
		m_XNATServer = props.getProperty(XnatServerProperties.XNATServerURL);
		m_XNATUser = props.getProperty(XnatServerProperties.XNATUser);
		m_XNATPass = props.getProperty(XnatServerProperties.XNATPass);
		m_AETitle = props.getProperty(XnatServerProperties.AETitle);
		if(m_XNATServer==null || m_XNATUser==null || m_XNATPass==null || m_AETitle==null) 
			throw new IOException();
		
		// clean up from the previous run
		try
		{
			for (File f : new File(m_StoreFolder).listFiles())
				f.delete();
		} finally
		{
		}
		m_this = this;
		// m_dcmServer.start();
		
		m_localAE=new AEDTO(0,
				m_AETitle,
				"localhost",
				Integer.parseInt(props.getProperty("Dicom.ListeningPort")),				
				"", "", "", "", "","", "");
		m_ael.setLocalAE(m_localAE);
//		m_ael=new AEServer(m_localAE);
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

		new Thread(this).start();
		// ?? srv.setPerfMonServiceName(perfMonServiceName)
		// XNATQueryGenerator.LoadVocabulary("./config/vocabulary.xml");
		// new Thread(new SCPServerRunnable(srv)).start();
	}
	private void initRemoteAEs(Properties p)
	{
		try
		{
			String[] aliases = p.getProperty("Dicom.RemoteAEs").split(" ");
			String aet, host;
			int port;
			for (String alias : aliases)
			{
				try
				{
					aet = p.getProperty("Dicom.RemoteAEs." + alias
							+ ".CalledAETitle");
					host = p.getProperty("Dicom.RemoteAEs." + alias
							+ ".HostNameOrIPAddress");
					port = new Integer(p.getProperty("Dicom.RemoteAEs." + alias
							+ ".Port")).intValue();
				} catch (Exception e)
				{
					continue;
				}
				m_ael.addAE(new AEDTO(0, aet, host, port, "", "", "", "", "",
						"", ""));
			}
		} catch (Exception e)
		{
		}
	}
	public void run()
	{
		try
		{
			m_dcmServer.start();
			while (!m_srvShutdown)
			{
				Thread.sleep(100);
			}
		} catch (Exception e)
		{
			System.err.println(e);
		} finally
		{
			try
			{
				m_dcmServer.stop();
			} catch (Exception e)
			{
			}
		}
	}
	private void initServerParams(QueryRetrieveScpService srv)
	{
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		Document bD;
		Document vD;
		try
		{
			mbs.registerMBean(m_ael, new ObjectName(
					"org.nrg.xnat.gateway:type=AEServer"));
			srv.setAEServiceName(new ObjectName(
					"org.nrg.xnat.gateway:type=AEServer"));
			mbs.registerMBean(this, new ObjectName(
					"org.nrg.xnag.gateway:type=GatewayServer"));
			// srv.setDcmServerName(dcmServerName)
			bD = new SAXReader().read(new File(
					"./config/dcm4chee-qrscp-xmbean.xml"));
			vD = new SAXReader().read(new File("./config/qrscp-config.xml"));
		} catch (Exception e)
		{
			return;
		}
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
	}
	public static void main(String arg[])
	{
		try
		{
			Thread.sleep(30000);
		}
		catch(Exception e){}
		
		String propertiesFileName = arg.length > 0
				? arg[0]
				: "./config/gateway.properties";
		// ?? m_qrServ.setAcceptedStandardSOPClasses(s)
		// ?? m_qrServ.setAcceptedTransferSyntax(s)
		try
		{
			Properties props = new Properties();
			try
			{
				FileInputStream in = new FileInputStream(propertiesFileName);
				props.load(in);
				in.close();
			} catch (IOException e)
			{
				Tools.LogException(Priority.ERROR,
						"Unable to read properties file", e);
			}
			new XNATGatewayServer(props);
			System.err.println("XNAT/DICOM gateway, "+ m_ver);
			props.put(XnatServerProperties.XNATPass, "*****");
			System.err.println("properties=" + props);
		} catch (Exception e)
		{
			Tools.LogException(Priority.ERROR, "Initialization exception", e);
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