package org.nrg.xnat.gateway;

import java.io.File;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.nrg.xdat.bean.XdatCriteriaSetBean;
import org.nrg.xdat.bean.XdatSearchFieldBean;
import org.nrg.xdat.bean.XdatStoredSearchBean;

import com.pixelmed.dicom.InformationEntity;

public class XNATQueryGenerator
{
	private static XNATVocabulary m_vcbl = null;
	// private static DicomDictionary m_dd=new DicomDictionary();

	public static void LoadVocabulary(String fname)
	{
		m_vcbl = new XNATVocabulary(new File(fname));
	}
	public static XNATVocabulary GetVocabulary()
	{
		return m_vcbl;
	}
	private static String GetRootStoredSearchElementName(InformationEntity ie)
	{
		String res = "";
		if (ie == InformationEntity.PATIENT)
			res = "xnat:subjectData";
		else if (ie == InformationEntity.STUDY)
			res = "xnat:imagesessionData";
		else if (ie == InformationEntity.SERIES)
			res = "xnat:scanData";
		else
			return null;
		return res;
	}
	public static String getRetrieveRESTQuery(InformationEntity ie, Dataset ds)
	{
		if (ie.compareTo(InformationEntity.SERIES) == 0)
		{
			// get series path
			String path = getRESTQuery(ie, ds,false);
			String scanID = GetValueFromAttributeList("serinstuid", ds);
			if (scanID == null)
				return null;
			path += "/" + scanID + "/files";
			return path;
		}
		return null;
	}
	private static String getAttrClause(String att, Dataset ds)
	{
		String s=GetValueFromAttributeList(att,ds);
		if (s==null) return s;
		String s1=null;
		try
		{
			s1=m_vcbl.GetXNATEntry(att).m_restvar;
		}
		catch(Exception e) 
		{
			return null;
		}
		
		return s1+"="+s;
	}

	public static LinkedList<TreeMap<String, String>> FilterRowMap(LinkedList<TreeMap<String, String>> row_map, InformationEntity ie)
	{
		String key="ID";
		if(ie.compareTo(InformationEntity.STUDY)==0) key="UID";
		else key="xnat:imagescandata/uid";
		
		TreeMap<String, String> keys=new TreeMap<String, String>();
		String cur_key;
		TreeMap<String, String> row;
		LinkedList<TreeMap<String, String>> new_rows=new LinkedList<TreeMap<String, String>>(); 
		for (int i=0; i<row_map.size(); i++)			
		{
			row=row_map.get(i);
			cur_key=row.get(key);
			if(cur_key==null) { new_rows.add(row); continue; }
			if(keys.get(cur_key) == null)
			{
				keys.put(cur_key,cur_key);
				new_rows.add(row);
			}						
		}
		return new_rows;
	}
	
	
	public static String getRESTQuery(InformationEntity ie, Dataset query, boolean bDefineColumns)
	{
		if (ie.compareTo(InformationEntity.SERIES) == 0)
		{
			String path = "/experiments";
			if(!XNATGatewayServer.isDICOMUID())
			{
				// get experiment ID
				String expID = GetValueFromAttributeList("stinstuid", query);
				if (expID == null)
					return null;
				path += "/"+expID + "/scans";
				if(bDefineColumns) 
					path+="&columns=xnat:imagesessiondata/scans/scan/type,";
				else return path;
			}
			else
			{
				String uid=query.getString(Tags.StudyInstanceUID);
				if(uid==null) return null;
				path+="?xsiType=xnat:imageSessionData&xnat:imageSessionData/UID="+uid;
//				path+="&columns=xnat:imagesessiondata/scans/scan/uid,xnat:imagesessiondata/scans/scan/type,";
				path+="&columns=xnat:imagescandata/uid,xnat:imagescandata/type,";
			}
//			path+="type,xsiType,series_description,subject_ID,label,subject_label";
			
			if (bDefineColumns)
			{
				//Adding more schema-like fields leads to returning multiple rows per series (Aug 2012).
//				path+="type,xsiType,series_description,subject_ID,label,subject_label,ID";

				int i=0;
				for (String s : m_vcbl.m_dcmFieldsSeries)
				{
					path += m_vcbl.GetXNATEntry(s).m_restvar;
					if(i<m_vcbl.m_dcmid_map.keySet().size()-1) path+=",";
					i++;
				}
			
			}			
			return path;
			
			
		} else if (ie.compareTo(InformationEntity.STUDY) == 0)
		{
			String path = "/experiments?";
			String param=null;
			boolean bFirst=true;
			
			for (String s : m_vcbl.m_dcmid_map.keySet())
			{
				param=getAttrClause(s,query);
				if(param!=null)
				{
					path += ((bFirst)? "" : "&") + param;
					bFirst=false;
				}
			}
			if (bDefineColumns)
			{
				path+="&columns=";
				int i=0;
				for (String s:m_vcbl.m_dcmFieldsStudy)
				{
					path+=m_vcbl.GetXNATEntry(s).m_restvar;
					if(i<m_vcbl.m_dcmFieldsStudy.size()-1) path+=",";
					i++;
				}
			}
			return path;
						
//			if(bDefineColumns)
//				path+="columns=studyInstanceUID,subject_ID,date,xsiType,label,subject_label,ID";
		}
		return null;
	}

	public static String GetValueFromAttributeList(String xnat_field_id,
			Dataset ds)
	{
		XNATVocabularyEntry xve;
		// get experiment ID
		xve = m_vcbl.GetXNATEntry(xnat_field_id);
		if (xve == null)
			return null;
		String val = ds.getString(xve.m_DICOMTag);
		if(val==null) return null;
		return XNATVocabulary.dcmToXNATField(val, xnat_field_id);
	}

	public static String getQueryXML(InformationEntity ie, Dataset query)
	{
		XdatStoredSearchBean search = new XdatStoredSearchBean();
		String name = GetRootStoredSearchElementName(ie);
		if (name == null)
			return null;
		search.setRootElementName(name);
		XdatSearchFieldBean sf;
		for (Iterator i = query.iterator(); i.hasNext();)
		{
			DcmElement el = (DcmElement) i.next();
			// for(Iterator it=dob.iterator(); it.hasNext();)
			{
				{
					XNATVocabularyEntry xve = null;
					try
					{
						xve = m_vcbl.GetDcmEntry(el.tag());
					} catch (Exception e)
					{
						System.err.println("Exception "
								+ e.getClass().toString());
					}
					if (xve != null)
					{
						sf = new XdatSearchFieldBean();
						sf.setElementName(xve.m_xnatElementName);
						sf.setFieldId(xve.m_schema_path);
						sf.setHeader(xve.m_dcmid);
						sf.setType("string");
						
						sf.setSequence(0);
						search.addSearchField(sf);

						// try to add a where clause
						try
						{
							String val = el.getString(null);
							// String val=el. getString(cs)
							// a.getSingleStringValueOrDefault(null);
							if (val != null)
							{
								XdatCriteriaSetBean xcsb = xve
										.getCriteriaSet("%" + val + "%");
								if (xcsb != null)
									search.addSearchWhere(xcsb);
							}
						} catch (Exception e)
						{
							System.err.println("Exception "
									+ e.getClass().toString());
						}
					}
				}
			}
		}
		return search.toString();
	}
}
