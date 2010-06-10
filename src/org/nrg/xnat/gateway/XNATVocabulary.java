package org.nrg.xnat.gateway;
import java.io.File;
import java.util.Iterator;
import java.util.TreeMap;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/*
 import com.pixelmed.dicom.Attribute;
 import com.pixelmed.dicom.AttributeList;
 import com.pixelmed.dicom.AttributeTag;
 */
import com.pixelmed.dicom.InformationEntity;

public class XNATVocabulary
{
	// index by DICOM tag
	private TreeMap<Integer, XNATVocabularyEntry> m_dcm_entries = new TreeMap<Integer, XNATVocabularyEntry>();

	// index by XNAT field ID
	private TreeMap<String, XNATVocabularyEntry> m_xnat_entries = new TreeMap<String, XNATVocabularyEntry>();

	public XNATVocabulary(File f)
	{
		try
		{
			Document d = new SAXReader().read(f);
			Element el;
			XNATVocabularyEntry xve;
			for (Iterator<Element> it = d.getRootElement().elementIterator(); it
					.hasNext();)
			{
				el = it.next();
				if (el.getName().compareTo("entry") == 0)
				{
					xve = new XNATVocabularyEntry(el);
					m_dcm_entries.put(new Integer(xve.m_DICOMTag), xve);
					m_xnat_entries.put(xve.m_search_column_alias, xve);
				}
			}
		} catch (Exception e)
		{
			System.err.println("Exception initializing XNAT vocabulary.");
			e.printStackTrace();
		}
	}

	public Dataset GetDicomEntry(TreeMap<String, String> row,
			InformationEntity ie)
	{
		Dataset list = DcmObjectFactory.getInstance().newDataset();
		// AttributeList list=new AttributeList();
		String val;
		TreeMap<String, String> stMap = new TreeMap<String, String>();
		String nm;

		// for now, special-case the STUDY level as new functionality
		// (05/19/2009)
		if (ie.compareTo(InformationEntity.STUDY) == 0)
		{

			//synonyms for study instance uid.
//			stMap.put("UID", "stinstuid");
			if(XNATGatewayServer.isDICOMUID())
			{
				stMap.put("UID","stinstuid");
			}
			else
				stMap.put("ID", "stinstuid");
//			stMap.put("session_ID","stinstuid");
			
			stMap.put("date", "stdate");
			stMap.put("xsiType", "stmodality");
			stMap.put("subject_ID", "patid");
			stMap.put("label", "staccessionnum");
			stMap.put("subject_label", "patname");
			
			//check if Study Instance UID is contained in response and if yes populate the resulting dataset.
//			boolean bStInstUIDDefined=false;
//			String temp=row.get("UID");
//			if(temp!=null && temp.length()>0)
//			{
//				bStInstUIDDefined=true;
//			}
			
			for (String s : row.keySet())
			{
				XNATVocabularyEntry xve;
				nm = stMap.get(s);
				if (nm == null)
					continue;
				//if Study instance UID was defined, skip its replacements.
//				if((s.compareTo("ID")==0 || s.compareTo("session_ID")==0) && bStInstUIDDefined) continue;
					
				xve = m_xnat_entries.get(nm);
				if (xve != null)
				{
					val = xnatTodcm(row.get(s), nm);
					xve.putDICOMAttribute(list, val);
					// list.putXX( xve.m_DICOMTag,val);
					// Attribute a=xve.GetDICOMAttribute(val);
					// if(a!=null) list.put(a);
				}
			}
		} else if (ie.compareTo(InformationEntity.SERIES) == 0)
		{
			if(!XNATGatewayServer.isDICOMUID())				
				stMap.put("ID", "id");
			else
			{
				stMap.put("xnat:imagesessiondata/scans/scan/uid","id");
				stMap.put("xnat:imagesessiondata/scans/scan/type","series_description");
			}
			
			stMap.put("xsiType", "sermodality");
			stMap.put("subject_ID", "patid");
			stMap.put("label", "staccessionnum");
			stMap.put("subject_label", "patname");
			for (String s : row.keySet())
			{
				XNATVocabularyEntry xve;
				nm = stMap.get(s);
				if (nm == null)
					continue;
				xve = m_xnat_entries.get(nm);
				if (xve != null)
				{
					val = xnatTodcm(row.get(s), nm);
					list.putXX(xve.m_DICOMTag, val);
					// Attribute a=xve.GetDICOMAttribute(val);
					// if(a!=null) list.put(a);
				}
			}
		}
		for (String s : row.keySet())
		{
			XNATVocabularyEntry xve = m_xnat_entries.get(s);
			if (xve != null)
			{
				val = row.get(s);
				list.putXX(xve.m_DICOMTag, val);
				// Attribute a=xve.GetDICOMAttribute(val);
				// if(a!=null) list.put(a);
			}
		}
		return list;
	}
	public static String xnatTodcm(String val, String alias)
	{
		if(alias.compareTo("stinstuid")==0 || alias.compareTo("id")==0)
		{
			if(val==null) return val;
			if(XNATGatewayServer.isDICOMUID()) return val;
			else return Utils.String2UID(val);
		}
		if (alias.compareTo("stmodality") == 0)
		{
			if (val == null || val.length() < 1)
				return val;
			if (val.compareTo("xnat:otherDicomSessionData") == 0)
				return "OT";
			int ind = val.indexOf("SessionData");
			if (ind > 5)
			{
				return val.substring(5, ind).toUpperCase();
			} else
				return val;
		} else if (alias.compareTo("sermodality") == 0)
		{
			if (val == null || val.length() < 1)
				return val;
			if (val.compareTo("xnat:otherDicomScanData") == 0)
				return "OT";			
			int ind = val.indexOf("ScanData");
			//fix for modality
			if(ind<0 && XNATGatewayServer.isDICOMUID())
				ind = val.indexOf("SessionData");
			if (ind > 5)
			{
				return val.substring(5, ind).toUpperCase();
			} else
				return val;
		}
		return val;
	}
	public static String dcmToXNATField(String field, String dcmAlias)
	{
		if (dcmAlias.compareTo("stinstuid")==0 || dcmAlias.compareTo("id")==0)
		{
			if(field==null) return null;
			return Utils.UID2String(field);
		}
		if (dcmAlias.compareTo("stmodality") == 0)
		{
			String allModalities = "xnat:imageSessionData";
			if (field == null || field.length() < 1)
				return allModalities;
			if (field.toLowerCase().startsWith("mr"))
				return "xnat:mrSessionData";
			else if (field.toLowerCase().startsWith("ct"))
				return "xnat:ctSessionData";
			else if (field.toLowerCase().startsWith("pet"))
				return "xnat:petSessionData";
			else
				return allModalities;
		} else if (dcmAlias.compareTo("stdate") == 0)
		{
			return field;
		}
		return field;
	}

	public void modifySOPInstUID(Dataset al, boolean toDicom)
	{
		// Attribute stInstUID=al.get(new AttributeTag(0x0020,0x000D));
		// Attribute serInstUID=al.get(new AttributeTag(0x0020,0x000E));
		// if(stInstUID==null || serInstUID==null) return;
		String s1 = al.getString(Tags.StudyInstanceUID), // stInstUID.getSingleStringValueOrNull(),
		s2 = al.getString(Tags.SeriesInstanceUID);// serInstUID.getSingleStringValueOrNull();
		if (s1 == null || s2 == null)
			return;
		try
		{
			s1=Utils.UID2String(s1);
			s2=Utils.UID2String(s2);
			String res=toDicom ? (s1 + "_" + s2) : (s2
					.substring(s1.length() + 1));
			al.putXX(Tags.SeriesInstanceUID, Utils.String2UID(res));
			// serInstUID.setValue(toDicom?(s1+"_"+s2):(s2.substring(s1.length()+1)));
			// al.put(serInstUID);
		} catch (Exception de)
		{
		}
	}
	public XNATVocabularyEntry GetDcmEntry(int tag)
	{
		return m_dcm_entries.get(new Integer(tag));
	}
	public XNATVocabularyEntry GetXNATEntry(String header)
	{
		return m_xnat_entries.get(header);
	}
}