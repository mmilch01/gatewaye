package org.nrg.xnat.gateway;

import java.util.LinkedList;
import java.util.TreeMap;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.log4j.Priority;
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
import org.dom4j.io.SAXReader;
import org.nrg.xnat.desktop.tools.XNATTableParser;
import org.nrg.xnat.repository.XNATRestAdapter;

import com.pixelmed.dicom.InformationEntity;

public class XNATCFindRsp implements MultiDimseRsp
{
	private XNATRestAdapter m_xre;
	private Dataset m_query;
	private LinkedList<Dataset> m_curLst = new LinkedList<Dataset>();

	public XNATCFindRsp(Dataset query, String url, String usr, String pass)
	{
		m_xre = new XNATRestAdapter(url, usr, pass);
		m_query = query;
	}
	public int executeQuery()
	{
		String qLevel = m_query.getString(Tags.QueryRetrieveLevel);

		// 1. perform POST request via REST API - using XND's classes

		boolean bGenSearch = false;
		HttpMethodBase method = null;
		InformationEntity ieWanted = Utils
				.getInformationEntityForQueryRetieveLevel(qLevel);
		// if(qLevel.toLowerCase().compareTo("patient")==0)
		if (ieWanted.compareTo(InformationEntity.PATIENT) == 0)
		{
			// now, for Patient/Study level, use generic xml search.
			try
			{
				String xml = XNATQueryGenerator.getQueryXML(ieWanted, m_query);
				// verify the query xml
				Tools.LogMessage(Priority.INFO_INT,
						"XNAT Search engine query xml body:\n" + xml);
				if (!m_xre.VerifyConnection())
					return -1;
				bGenSearch = true;
				method = m_xre.PerformConnection(XNATRestAdapter.POST,
						"/search", xml);
			} catch (Exception e)
			{
				System.err.println("Exception " + e.getClass().toString()
						+ ", message: " + e.getMessage());
			}
		} else if (ieWanted.compareTo(InformationEntity.SERIES) <= 0)
		// else if(ieWanted.compareTo(InformationEntity.SERIES)==0)
		{
			String path = XNATQueryGenerator.getRESTQuery(ieWanted, m_query);
			Tools.LogMessage(Priority.INFO_INT, "REST query string:\n" + path);
			if (path == null)
			{
				Tools.LogMessage(Priority.ERROR_INT, "Error generating REST query");
				return -1;
			}
			method = m_xre.PerformConnection(XNATRestAdapter.GET, path, "");
		} else
			return 0;
		if (method == null)
			return 0;
		/*
		 * try { // String resp=method.getResponseBodyAsString(); //
		 * System.err.println(resp); } catch(Exception e){return;}
		 * method.releaseConnection();
		 */
		try
		{
			// 2. parse the response - using XND's classes
			LinkedList<TreeMap<String, String>> row_map = XNATTableParser
					.GetRows(new SAXReader().read(method
							.getResponseBodyAsStream()), !bGenSearch, "header");// bGenSearch?"header":null);
			for (TreeMap<String, String> row : row_map)
			{
				// 3. translate the response to DICOM's AttributeList
				Dataset resp = DcmObjectFactory.getInstance().newDataset();
				// AttributeList al=new AttributeList();
				resp.putAll(m_query);
				// al.putAll(queryIdentifier);
				Dataset received = XNATQueryGenerator.GetVocabulary()
						.GetDicomEntry(row, ieWanted);
				resp.putAll(received);
				// al.putAll(received);
				XNATQueryGenerator.GetVocabulary().modifySOPInstUID(resp, true);
				if (resp.size() > 0)
					m_curLst.add(resp);
			}
		} catch (Exception e)
		{
			Tools.LogException(Priority.ERROR,
					"Error parsing the response to REST query", e);
		} finally
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
		if (m_curLst.size() < 1)
		{
			rspCmd.putUS(Tags.Status, Status.Success);
			return null;
		}
		return m_curLst.remove();
	}

	@Override
	public void release()
	{
		// TODO Auto-generated method stub
	}

}
