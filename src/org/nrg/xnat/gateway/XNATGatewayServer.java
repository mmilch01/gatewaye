package org.nrg.xnat.gateway;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.management.ObjectName;
import org.apache.log4j.Priority;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;
import org.dcm4chex.archive.dcm.qrscp.QueryRetrieveScpService;

public class XNATGatewayServer
{
	private static String m_ver="April 19, 2010";
//	private static QueryRetrieveScpService m_qrServ;
	private Server m_dcmServer;
	public XNATGatewayServer(Properties props)
	throws Exception
	{
		QueryRetrieveScpService srv=new QueryRetrieveScpService();
		srv.setCalledAETs(props.getProperty("Dicom.CallingAETitle"));
//		srv.setCallingAETs(props.getProperty("Dicom.CalledAETitile"));
		srv.setCoerceRequestPatientIds(true);
		String aets=props.getProperty("Dicom.RemoteAEs");
		aets=aets.replace(' ', '\\');
		srv.setCallingAETs(aets);
//		srv.setDcmServerName(new ObjectName("GatewayDcmQRSCP"));
		srv.startService();
		m_dcmServer=ServerFactory.getInstance().newServer(srv.getDcmHandler());
		m_dcmServer.setPort(4008);
		m_dcmServer.start();
		
//		new Thread(new SCPServerRunnable(srv)).start();
//??	srv.setPerfMonServiceName(perfMonServiceName)
//		XNATQueryGenerator.LoadVocabulary("./config/vocabulary.xml");
//		new Thread(new SCPServerRunnable(srv)).start();
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
			System.err.println("properties="+props);
		}
		catch (Exception e)
		{
			Tools.LogException(Priority.ERROR, "Initialization exception", e);
		}
	}
	private class SCPServerRunnable implements Runnable
	{
		private QueryRetrieveScpService m_srv;
		public SCPServerRunnable(QueryRetrieveScpService srv)
		{
			m_srv=srv;
		}
		@Override
		public void run()
		{
			try
			{
				m_srv.startService();
				while(true)
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
					m_srv.stopService();
				}
				catch(Exception e){}
			}
		}
	}
	@Override
	protected void finalize() throws Throwable
	{
		if(m_dcmServer!=null)
			m_dcmServer.stop();
		super.finalize();
	}
}