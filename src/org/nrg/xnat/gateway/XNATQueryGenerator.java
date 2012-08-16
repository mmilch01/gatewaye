package org.nrg.xnat.gateway;

import java.io.File;
import java.net.URLEncoder;
import java.util.Iterator;

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
			String path = getRESTQuery(ie, ds);
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
	public static String getRESTQuery(InformationEntity ie, Dataset query)
	{
		if (ie.compareTo(InformationEntity.SERIES) == 0)
		{
			String path = "/experiments";
			String uid=query.getString(Tags.StudyInstanceUID);
			if(uid==null) return null;
			path+="?xsiType=xnat:imageSessionData&xnat:imageSessionData/UID="+uid;
			path+="&columns=";			
			int i=0;
			for (String s : m_vcbl.m_dcmFieldsSeries)
			{
				path += m_vcbl.GetXNATEntry(s).m_restvar;
				if(i<m_vcbl.m_dcmFieldsSeries.size()-1) path+=",";
				i++;
			} 
			return path;
			
		} else if (ie.compareTo(InformationEntity.STUDY) == 0)
		{
			String path = "/experiments";
			boolean bFirst = true;
			String param=null;
			
			for (String s : m_vcbl.m_dcmFieldsStudy)
			{
				param=getAttrClause(s,query);
				if(param!=null)
				{
					path += (bFirst ? "?" : "&") + param;
					bFirst = false;
				}				
			}
			path+="&columns=";
			int i=0;
			for (String s:m_vcbl.m_dcmFieldsStudy)
			{
				path+=m_vcbl.GetXNATEntry(s).m_restvar;
				if(i<m_vcbl.m_dcmFieldsStudy.size()-1) path+=",";
				i++;
			}
			return path;
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
	
	// Advanced XNAT search engine queries. Suspended as of 08/2012.
	// vocabulary.xml dropped "xnat_element_name" attribute as a result.
/*
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
				// DcmElement el=(DcmElement)it.next();

				// Attribute a = (Attribute)i.next();
				// AttributeTag at = a.getTag();
				// InformationEntity ieAttribute =
				// m_dd.getInformationEntityFromTag(at);
				// if(ieAttribute==null) continue;
				// add fields
				// if(ieAttribute.compareTo(ie)<=0)
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
						sf.setElementName(xve.m_elementName);
						sf.setFieldId(xve.m_field_id);
						sf.setHeader(xve.m_dcmid);
						sf.setType(xve.m_type);
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
*/	
}
