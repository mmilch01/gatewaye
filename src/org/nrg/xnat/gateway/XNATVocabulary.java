package org.nrg.xnat.gateway;
import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pixelmed.dicom.InformationEntity;

public class XNATVocabulary
{
	// index by DICOM tag
	private TreeMap<Integer, XNATVocabularyEntry> m_dcmtag_map = new TreeMap<Integer, XNATVocabularyEntry>();

	// index by dcmid
	private TreeMap<String, XNATVocabularyEntry> m_dcmid_map = new TreeMap<String, XNATVocabularyEntry>();
	
	// Aug 2012-index of dcmid by restvar (vocabulary.xml)
	private TreeMap<String,String> m_alias_dcmid_mapStudy=new TreeMap<String,String>();
	private TreeMap<String,String> m_alias_dcmid_mapSeries=new TreeMap<String,String>();
	public Collection<String> m_dcmFieldsStudy=new LinkedList<String>();
	public Collection<String> m_dcmFieldsSeries=new LinkedList<String>();
	
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
					m_dcmtag_map.put(new Integer(xve.m_DICOMTag), xve);
					m_dcmid_map.put(xve.m_dcmid, xve);
					
					if(xve.m_qLevel==XNATVocabularyEntry.PATIENT || xve.m_qLevel==XNATVocabularyEntry.STUDY)
					{
						for (String s : xve.m_aliases)						
							m_alias_dcmid_mapStudy.put(s.toLowerCase(), xve.m_dcmid);
						m_dcmFieldsStudy.add(xve.m_dcmid);
						m_dcmFieldsSeries.add(xve.m_dcmid);
					}
					else 
					{
						for (String s : xve.m_aliases)						
							m_alias_dcmid_mapSeries.put(s.toLowerCase(), xve.m_dcmid);
						m_dcmFieldsSeries.add(xve.m_dcmid);
					}
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
		String val;
		TreeMap<String, String> stMap = new TreeMap<String, String>();
		String nm;

// First, populate common attributes.
		String dcmid=null;
		XNATVocabularyEntry xve=null;
		for (String s : row.keySet())
		{
			if (ie.compareTo(InformationEntity.SERIES) == 0)			
				dcmid=m_alias_dcmid_mapSeries.get(s.toLowerCase());
			if(dcmid==null || ie.compareTo(InformationEntity.STUDY) == 0)
				dcmid=m_alias_dcmid_mapStudy.get(s.toLowerCase());			
			xve=(dcmid==null)?null:m_dcmid_map.get(dcmid);
			if (xve != null)
			{
				val = xnatTodcm(row.get(s),dcmid);
				try
				{
					xve.putDICOMAttribute(list, val);
				}
				catch(Exception e){};
			}
			dcmid=null;
		}
		return list;
	}
	public static String xnatTodcm(String val, String dcmid)
	{		
		if(dcmid.compareTo("stinstuid")==0 || dcmid.compareTo("serinstuid")==0)
		{
			if(val==null) return val;
			if(XNATGatewayServer.isDICOMUID()) return val;
			else return Utils.String2UID(val);
		}
		if(dcmid.compareTo("patdob")==0 || dcmid.compareTo("stdate")==0)
		{
			return val.replace("-", "");
		}
		if(dcmid.compareTo("patsex")==0)
		{
			if(val.toLowerCase().startsWith("m")) return "M";
			if(val.toLowerCase().startsWith("f")) return "F";
			return val;
		}
		if (dcmid.compareTo("stmodality") == 0)
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
		} else if (dcmid.compareTo("sermodality") == 0)
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
	private static String GetDateDcm(int AdjustYears)
	{
		Calendar c = Calendar.getInstance();
		c.add(Calendar.YEAR, AdjustYears);
		return String.format("%04d", c.get(Calendar.YEAR))+String.format("%02d", c.get(Calendar.MONTH))+
			String.format("%02d", c.get(Calendar.DAY_OF_MONTH));		
	}
	private static String DcmDateToXNAT(String from)
	{
		String tmp;
		if(from.startsWith("-")) return GetDateDcm(-100)+from;
		else if (from.endsWith("-")) return from+GetDateDcm(100);
		return from;
	}
	
	public static String dcmToXNATField(String field, String dcmid)
	{
		if(dcmid.compareTo("stdate")==0 || dcmid.compareTo("patdob")==0)
			return DcmDateToXNAT(field);
		if(dcmid.compareTo("patdob")==0 || dcmid.compareTo("stdate")==0)
		{
			if(field.length()!=8) return field;
			String s=field.substring(0,3)+"-"+field.substring(4,6)+"-"+field.substring(7,9);
			return s;
		}		
		if (dcmid.compareTo("stmodality") == 0)
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
		} else if (dcmid.compareTo("stdate") == 0)
		{
			return field;
		}
		return field;
	}

	public void modifySOPInstUID(Dataset al, boolean toDicom)
	{
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
		} catch (Exception de)
		{
		}
	}
	public XNATVocabularyEntry GetDcmEntry(int tag)
	{
		return m_dcmtag_map.get(new Integer(tag));
	}
	public XNATVocabularyEntry GetXNATEntry(String header)
	{
		return m_dcmid_map.get(header);
	}
}