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
	public static final String[] m_dcmFieldsStudy =	{
		"stinstuid", "staccessionnum", "stmodality", "stdate", "patname", 
		"patid", "patsex", "patdob", "stdescr", "sttime"
		};
	public static final String[] m_dcmFieldsSeries = {
		"stinstuid", "staccessionnum", "stmodality", "stdate", "patname", 
		"patid", "patsex", "patdob", "stdescr", "sttime", "serinstuid", "sermodality", 
		"series_description", "sernumimages"
		};
	
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
				path+="&columns=xnat:imagesessiondata/scans/scan/uid,xnat:imagesessiondata/scans/scan/type,";
			}
//			String path1="columns=";
			for (int i=0; i<m_dcmFieldsStudy.length; i++)
			{
				path+=m_vcbl.GetXNATEntry(m_dcmFieldsStudy[i]).m_restvar;
				if(i<m_dcmFieldsStudy.length-1) path+=",";
			}				
			
//			path+="type,xsiType,series_description,subject_ID,label,subject_label";
			return path;
			
		} else if (ie.compareTo(InformationEntity.STUDY) == 0)
		{
			String path = "/experiments";
			String expID = GetValueFromAttributeList("stinstuid", query);
			String label = GetValueFromAttributeList("staccessionnum", query);
			String modality = GetValueFromAttributeList("stmodality", query);
			String date = GetValueFromAttributeList("stdate", query);
			String patname = GetValueFromAttributeList("patname", query);
			String patid = GetValueFromAttributeList("patid", query);
			boolean bFirst = true;

			String param=null;
//			String path1="";
			
			for (int i=0; i<m_dcmFieldsStudy.length; i++)
			{
				param=getAttrClause(m_dcmFieldsStudy[i],query);
				if(param!=null)
				{
					path += (bFirst ? "?" : "&") + param;
					bFirst = false;
				}
			}
/*			
			bFirst=true;
			if (expID != null)
			{
				path += (bFirst ? "?" : "&") + "ID=" + expID;
				bFirst = false;
			}
			if (label != null)
			{
				path += (bFirst ? "?" : "&") + "label=" + label;
				bFirst = false;
			}
			if (modality != null)
			{
				path += (bFirst ? "?" : "&") + "xsiType=" + modality;
				bFirst = false;
			}
			if (date != null)
			{
				path += (bFirst ? "?" : "&") + "date=" + date;
				bFirst = false;
			}
			if (patname != null)
			{
				path += (bFirst ? "?" : "&") + "subject_label=" + patname;
				bFirst = false;
			}
			if (patid != null)
			{
				path += (bFirst ? "?" : "&") + "subject_ID=" + patid;
				bFirst = false;
			}
			if(!bFirst)
			{
				path+="&";
				bFirst=false;
			}
*/			
			if(bDefineColumns)
			{
				path+="&columns=";
				for (int i=0; i<m_dcmFieldsStudy.length; i++)
				{
					path+=m_vcbl.GetXNATEntry(m_dcmFieldsStudy[i]).m_restvar;
					if(i<m_dcmFieldsStudy.length-1) path+=",";
				}
//				path+="columns=studyInstanceUID,subject_ID,date,xsiType,label,subject_label,ID";
			}
			try{return URLEncoder.encode(path, "UTF-8");}catch(Exception e){return path;}
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
		// Attribute attr=al.get(xve.m_DICOMTag);
		// if(attr==null) return null;
		// if(val==null) return null;
		if(XNATGatewayServer.isDICOMUID()) return val;
		return XNATVocabulary.dcmToXNATField(val, xnat_field_id);
		// attr.getSingleStringValueOrNull(),xnat_field_id);
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
}
