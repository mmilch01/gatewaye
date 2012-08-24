package org.nrg.xnat.gateway;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.dcm4che.data.Dataset;
import org.dom4j.Element;
import org.nrg.xdat.bean.XdatCriteriaBean;
import org.nrg.xdat.bean.XdatCriteriaSetBean;

/*
 import com.pixelmed.dicom.Attribute;
 import com.pixelmed.dicom.AttributeFactory;
 import com.pixelmed.dicom.AttributeTag;
 import com.pixelmed.dicom.DateAttribute;
 import com.pixelmed.dicom.DicomException;
 */

public class XNATVocabularyEntry
{
	public int m_DICOMTag;
	public String m_type = "";
	public String m_restvar="";
	public String m_dcmid = "";
	public String m_xnatElementName="";
	public static final int PATIENT=1,STUDY=2,SERIES=3;
	public int m_qLevel=0;
	//used by the xml search.
	public String m_schema_path = "";
	//column header aliase.
	public String m_alias = null;

	public boolean isDateTag()
	{
		if (m_DICOMTag == 524320)
			return true;
		return false;
	}
	public void putDICOMAttribute(Dataset ds, String val)
	{
		try
		{
			if (isDateTag())
			{
				val = val.replace("-", "");
			}
			ds.putXX(m_DICOMTag, val);
		} catch (Exception de)
		{
		}
	}
	public static int tagFromGrElem(int group, int elem)
	{
		return ((int) ((group << 16) | elem)) & 0xffffffff;
	}
	public static int getGroup(int tag)
	{
		return ((tag & 0xffff0000) >> 16) & 0xffff;
	}
	public static int getElem(int tag)
	{
		return tag & 0xffff;
	}
	public XNATVocabularyEntry(Element el) throws Exception
	{
		String tag=el.attributeValue("dcm_tag");
		int group = Integer.parseInt(tag.substring(0,4), 16), elem = Integer
		.parseInt(tag.substring(4,8), 16);		
		m_DICOMTag = tagFromGrElem(group, elem);
		m_type = "string";
		m_schema_path=el.attributeValue("schema_path");
		int ind=m_schema_path.indexOf('/', 0);
		
		if(ind>0)
			m_xnatElementName=m_schema_path.substring(0,ind);
		else
			m_xnatElementName=m_schema_path;
		
		m_restvar=m_schema_path;
		m_dcmid = el.attributeValue("dcmid");
		String qLevel=el.attributeValue("qLevel");
		if(qLevel==null) m_qLevel=PATIENT;
		if(qLevel.compareTo("PATIENT")==0) m_qLevel=PATIENT;
		else if(qLevel.compareTo("STUDY")==0) m_qLevel=STUDY;
		else if(qLevel.compareTo("SERIES")==0) m_qLevel=SERIES;
		else throw new Exception("Unsupported query level value for entry: tag="+m_DICOMTag+", dcmid="+m_dcmid);				
		m_alias=el.attributeValue("alias");
		if(m_alias==null) m_alias=m_xnatElementName;
	}
	public void toElement(Element el)
	{
		el.addAttribute("dcm_tag",getDcmTag());
		el.addAttribute("xnat_type", m_type);
		el.addAttribute("rest_var", m_restvar);
		el.addAttribute("dcmid", m_dcmid);
		el.addAttribute("alias", m_alias);
	}
	public String getDcmTag()
	{
		return Integer
		.toHexString(getGroup(m_DICOMTag)).concat(
				Integer.toHexString(getElem(m_DICOMTag)));
	}
	public XdatCriteriaSetBean getCriteriaSet(String val)
	{
//		if (m_criteria == null)
//			return null;
		XdatCriteriaSetBean xcsb = new XdatCriteriaSetBean();
		xcsb.setMethod("LIKE");
		XdatCriteriaBean xcb = new XdatCriteriaBean();
		xcb.setSchemaField(m_schema_path);
		xcb.setValue(val);
		xcb.setComparisonType("OR");
		xcsb.addCriteria(xcb);
		return xcsb;
	}
	public class criterion
	{
		public String m_search_schema_field;
		public String m_method;
		public String m_comparison_type;

		public criterion(Element el)
		{
			m_search_schema_field = el.attributeValue("search_schema_field");
			m_method = el.attributeValue("method");
			m_comparison_type = el.attributeValue("comparison_type");
		}
		public void fillElement(Element el)
		{
			el.addAttribute("search_schema_field", m_search_schema_field);
			el.addAttribute("method", m_method);
			el.addAttribute("comparison_type", m_comparison_type);
		}
	}
}