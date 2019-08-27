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
import org.nrg.fileserver.XNATRestAdapter;
import org.nrg.xnd.ontology.XNATTableParser;

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
		//first, find XNAT session ID.
		if (ieWanted.compareTo(InformationEntity.STUDY)>=0)
		{
			String path = XNATQueryGenerator.getRESTQuery(ieWanted, m_query,true);
			Tools.LogMessage(Priority.INFO_INT, "REST query string:\n" + path);
			if (path == null)
			{
				Tools.LogMessage(Priority.ERROR_INT, "Error generating REST query");
				return -1;
			}
			method = m_xre.PerformConnection(XNATRestAdapter.GET, path, "");
		} 
		else
		{
			String path = XNATQueryGenerator.getRESTQuery(ieWanted, m_query,true);
			Tools.LogMessage(Priority.INFO_INT, "REST query string:\n" + path);
			if (path == null)
			{
				Tools.LogMessage(Priority.ERROR_INT, "Error generating REST query");
				return -1;
			}
			method = m_xre.PerformConnection(XNATRestAdapter.GET, path, "");
		}
		if (method == null)
		{
			Tools.LogMessage(Priority.INFO_INT, "XML search request returned null");
			return 0;
		}
		try
		{
			// 2. parse the response - using XND's classes
			LinkedList<TreeMap<String, String>> row_map = XNATTableParser
					.GetRows(new SAXReader().read(method
							.getResponseBodyAsStream()), !bGenSearch, "header");// bGenSearch?"header":null);
			row_map=XNATQueryGenerator.FilterRowMap(row_map,ieWanted);
			for (TreeMap<String, String> row : row_map)
			{
				// 3. translate the response to DICOM's AttributeList
				Dataset resp = DcmObjectFactory.getInstance().newDataset();
				resp.putAll(m_query);
				Dataset received = XNATQueryGenerator.GetVocabulary()
						.GetDicomEntry(row, ieWanted);
				resp.putAll(received);
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
