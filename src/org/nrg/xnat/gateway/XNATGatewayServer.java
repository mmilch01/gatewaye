package org.nrg.xnat.gateway;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.TreeMap;

import javax.management.ObjectName;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.log4j.Priority;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.DcmServiceBase.MultiDimseRsp;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.dcm4chex.archive.dcm.qrscp.QueryRetrieveScpService;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.nrg.xnat.desktop.tools.XNATTableParser;
import org.nrg.xnat.repository.XNATRestAdapter;


import com.pixelmed.dicom.InformationEntity;

public class XNATGatewayServer implements Runnable
{
	private static String m_ver="April 26, 2010";
//	private static QueryRetrieveScpService m_qrServ;
	private Server m_dcmServer;
	private static boolean m_srvShutdown=false;
	
	protected long m_RandSeed=0;
	protected String m_StoreFolder, m_XNATServer, m_XNATUser, m_XNATPass, m_AETitle=null;
	protected int m_maxCacheFiles=50000;
	protected static XNATGatewayServer m_this=null;
	public static XNATGatewayServer getInstance()
	{
		return m_this;
	}
	public XNATFindRsp getMultiCFindRsp(Dataset query)
	{
		return new XNATFindRsp(query,m_XNATServer,m_XNATUser,m_XNATPass);
	}
	
	public XNATGatewayServer(Properties props)
	throws Exception
	{
		QueryRetrieveScpService srv=new QueryRetrieveScpService();
		initServerParams(srv);
		
		srv.setCalledAETs(props.getProperty("Dicom.CallingAETitle"));
//		srv.setCallingAETs(props.getProperty("Dicom.CalledAETitile"));
		srv.setCoerceRequestPatientIds(true);
		String aets=props.getProperty("Dicom.RemoteAEs");
		aets=aets.replace(' ', '\\');
		srv.setCallingAETs(aets);
//		srv.setAcceptedStandardSOPClasses(s)
//		srv.setDcmServerName(new ObjectName("GatewayDcmQRSCP"));
		srv.startService();
		m_dcmServer=ServerFactory.getInstance().newServer(srv.getDcmHandler());		
		m_dcmServer.setPort(Integer.valueOf(props.getProperty("Dicom.ListeningPort")).intValue());
		
		m_StoreFolder=props.getProperty("Application.SavedImagesFolderName");
		File sf=new File(m_StoreFolder);
		if(!sf.exists()) sf.mkdir();
		
		m_XNATServer=props.getProperty(XnatServerProperties.XNATServerURL);
		m_XNATUser=props.getProperty(XnatServerProperties.XNATUser);
		m_XNATPass=props.getProperty(XnatServerProperties.XNATPass);
		m_AETitle=props.getProperty(XnatServerProperties.AETitle);
		//clean up from the previous run
		try
		{
			for(File f:new File(m_StoreFolder).listFiles()) f.delete();
		}
		finally{}
		m_this=this;
//		m_dcmServer.start();
		new Thread(this).start();
//??	srv.setPerfMonServiceName(perfMonServiceName)
//		XNATQueryGenerator.LoadVocabulary("./config/vocabulary.xml");
//		new Thread(new SCPServerRunnable(srv)).start();
	}
	public void run()
	{
		try
		{
			m_dcmServer.start();
			while(!m_srvShutdown)
			{
				Thread.sleep(100);
			}
		}
		catch(Exception e)
		{
			System.err.println(e);
		}
		finally
		{
			try
			{
				m_dcmServer.stop();
			}
			catch(Exception e){}
		}
	}	
	private void initServerParams(QueryRetrieveScpService srv)
	{
		Document bD;
		Document vD;		
		try
		{
			bD=new SAXReader().read(new File("config/dcm4chee-qrscp-xmbean.xml"));
			vD=new SAXReader().read(new File("config/qrscp-config.xml"));
		}
		catch(Exception e)
		{
			return;
		}
		final class MAttr{
			String name,getMethod,setMethod,type;
			public MAttr(Element el)
			{
				name=el.element("name").getText();	type=el.element("type").getText();
				getMethod=el.attributeValue("getMethod"); setMethod=el.attributeValue("setMethod");
			}
		};
		TreeMap<String,MAttr> attMap=new TreeMap<String,MAttr>();
		Element element=bD.getRootElement();
		//elementByID("mbean");
		for(Iterator<Element> it=element.elementIterator(); it.hasNext();)
		{
			Element eln=it.next();
			if(eln.getName().compareTo("attribute")!=0) continue;
			MAttr attr=new MAttr(eln);
			attMap.put(attr.name, attr);
		}
		for(Iterator<Element> it=vD.getRootElement().elementIterator(); it.hasNext();)
		{
			Element eln=it.next();
			try
			{

				MAttr att=attMap.get(eln.attribute("name").getText());			
				Class type=Class.forName(att.type);
				Method method=srv.getClass().getMethod(att.setMethod, type);
				String param=eln.getText();
				if(att.type.compareTo("java.lang.String")!=0)
				{
					method.invoke(srv, type.getMethod("valueOf", Class.forName("java.lang.String")).invoke(null, param));
				}
				else
				{
					method.invoke(srv,param);
				}
			}
			catch(Exception e){}
			
		}
	}	
	public static void main(String arg[])
	{
		String propertiesFileName = arg.length > 0 ? arg[0] : "./config/gateway.properties";
//??	m_qrServ.setAcceptedStandardSOPClasses(s)
//??	m_qrServ.setAcceptedTransferSyntax(s)
		try
		{
			Properties props=new Properties();
			try
			{
				FileInputStream in = new FileInputStream(propertiesFileName);
				props.load(in);
				in.close();
			}
			catch (IOException e)
			{
				Tools.LogException(Priority.ERROR, "Unable to read properties file", e);
			}
			new XNATGatewayServer(props);
			System.err.println("XNAT/DICOM gateway server v. 1.0, rev. "+m_ver);
			props.put(XnatServerProperties.XNATPass, "*****");
			System.err.println("properties="+props);
		}
		catch (Exception e)
		{
			Tools.LogException(Priority.ERROR, "Initialization exception", e);
		}
	}
	@Override
	protected void finalize() throws Throwable
	{
//		if(m_dcmServer!=null)
//			m_dcmServer.stop();
		super.finalize();
	}
	public class XNATFindRsp implements MultiDimseRsp
	{
		private XNATRestAdapter m_xre;
		private Dataset m_query;
		private LinkedList<Dataset> m_curLst=new LinkedList<Dataset>();

		public XNATFindRsp(Dataset query, String url, String usr, String pass)
		{
			m_xre=new XNATRestAdapter(url,usr,pass);
			m_query=query;
		}
		protected InformationEntity getInformationEntityForQueryRetieveLevel(String queryRetrieveLevel) 
		{
			if (queryRetrieveLevel == null)					return null;
			else if (queryRetrieveLevel.equals("PATIENT"))	return InformationEntity.PATIENT;
			else if (queryRetrieveLevel.equals("STUDY"))	return InformationEntity.STUDY;
			else if (queryRetrieveLevel.equals("SERIES"))	return InformationEntity.SERIES;
			else if (queryRetrieveLevel.equals("IMAGE"))	return InformationEntity.INSTANCE;
			else											return null;
		}

		public int executeQuery()
		{
			String qLevel=m_query.getString(Tags.QueryRetrieveLevel);
			
			//1. perform POST request via REST API - using XND's classes
			
			boolean bGenSearch=false;
			HttpMethodBase method=null;
			InformationEntity ieWanted=getInformationEntityForQueryRetieveLevel(qLevel);
			if(qLevel.toLowerCase().compareTo("patient")==0)
			if(ieWanted.compareTo(InformationEntity.STUDY)<=0)
			{
				//now, for Patient/Study level, use generic xml search.
				String xml=XNATQueryGenerator.getQueryXML(ieWanted, m_query);
				//verify the query xml
				Tools.LogMessage(Priority.INFO, "XNAT Search engine query xml body:\n"+xml);
				if(!m_xre.VerifyConnection()) return -1;
				bGenSearch=true;
				method=m_xre.PerformConnection(XNATRestAdapter.POST, "/search", xml);
			}
			else if(ieWanted.compareTo(InformationEntity.SERIES)<=0)
//			else if(ieWanted.compareTo(InformationEntity.SERIES)==0)
			{
				String path=XNATQueryGenerator.getRESTQuery(ieWanted, m_query);
				Tools.LogMessage(Priority.INFO, "REST query string:\n"+path);
				if(path==null)
				{
					Tools.LogMessage(Priority.ERROR, "Error generating REST query");
					return -1;
				}
				method=m_xre.PerformConnection(XNATRestAdapter.GET, path, "");					
			}
			else return 0;	
			if(method==null) return 0;
/*
			try
			{
//				String resp=method.getResponseBodyAsString();
//				System.err.println(resp);
			}				
			catch(Exception e){return;}
			method.releaseConnection();
*/				
			try
			{
				//2. parse the response				    			- using XND's classes
				LinkedList<TreeMap<String,String>> row_map=
					XNATTableParser.GetRows(new SAXReader().read(method.getResponseBodyAsStream()),!bGenSearch,"header");//bGenSearch?"header":null);
				for(TreeMap<String,String> row:row_map)
				{
					//3. translate the response to DICOM's AttributeList
					Dataset resp=DcmObjectFactory.getInstance().newDataset();
//					AttributeList al=new AttributeList();
					resp.putAll(m_query);
//					al.putAll(queryIdentifier);
					Dataset received=XNATQueryGenerator.GetVocabulary().GetDicomEntry(row,ieWanted);
					resp.putAll(received);
//					al.putAll(received);
					XNATQueryGenerator.GetVocabulary().modifySOPInstUID(resp, true);
					if(resp.size()>0)
						m_curLst.add(resp);
				}
			}
			catch(Exception e)
			{
				Tools.LogException(Priority.ERROR, "Error parsing the response to REST query", e);
			}
			finally
			{
				method.releaseConnection();
			}
			return m_curLst.size();
		}
		
		@Override
		public DimseListener getCancelListener()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Dataset next(ActiveAssociation assoc, Dimse rq, Command rspCmd)
				throws DcmServiceException
		{
			if(m_curLst.size()<1) return null;
			return m_curLst.remove();
		}

		@Override
		public void release()
		{
			// TODO Auto-generated method stub			
		}
		
	}
}