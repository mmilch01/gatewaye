package org.nrg.xnat.gateway;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
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
	public String m_elementName = "";
	public String m_field_id = "";
	public String m_type = "";
	// public String m_header="";
	public String m_search_column_alias = "";
	public String m_rest_column_alias = "";

	public Collection<criterion> m_criteria = null;

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
			// Attribute attr=AttributeFactory.newAttribute(m_DICOMTag);

			// if(attr instanceof DateAttribute)

			// workaround for overly-sensitive parser that doesn't accept dashes
			// for dates.
			if (isDateTag())
			{
				val = val.replace("-", "");
			}

			// for now, we only support string values.
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
	public XNATVocabularyEntry(Element el)
	{
		int group = Integer.parseInt(el.attributeValue("dicom_group"), 16), elem = Integer
				.parseInt(el.attributeValue("dicom_element"), 16);
		m_DICOMTag = tagFromGrElem(group, elem);
		// m_DICOMTag=new AttributeTag(group,elem);
		m_elementName = el.attributeValue("xnat_element_name");
		m_field_id = el.attributeValue("xnat_field_id");
		m_type = "string";
		// m_header=el.attributeValue("xnat_header");
		m_search_column_alias = el.attributeValue("search_column_alias");

		LinkedList<criterion> llsw = new LinkedList<criterion>();
		for (Iterator it = el.elementIterator(); it.hasNext();)
		{
			Element se = (Element) it.next();
			if (se.getName().compareTo("criterion") == 0)
				llsw.add(new criterion(se));
		}
		if (llsw.size() > 0)
			m_criteria = llsw;
	}
	public void toElement(Element el)
	{
		el.addAttribute("dicom_group", Integer
				.toHexString(getGroup(m_DICOMTag)));
		el.addAttribute("dicom_element", Integer
				.toHexString(getElem(m_DICOMTag)));
		el.addAttribute("xnat_element_name", m_elementName);
		el.addAttribute("xnat_field_id", m_field_id);
		el.addAttribute("xnat_type", m_type);
		// el.addAttribute("xnat_header", m_header);
		el.addAttribute("search_column_alias", m_search_column_alias);
		if (m_criteria != null)
		{
			for (criterion c : m_criteria)
			{
				Element sub = el.addElement("criterion");
				c.fillElement(sub);
			}
		}
	}
	public XdatCriteriaSetBean getCriteriaSet(String val)
	{
		if (m_criteria == null)
			return null;
		XdatCriteriaSetBean xcsb = new XdatCriteriaSetBean();
		xcsb.setMethod("AND");

		for (criterion c : m_criteria)
		{
			XdatCriteriaBean xcb = new XdatCriteriaBean();
			xcb.setSchemaField(c.m_search_schema_field);
			xcb.setValue(val);
			xcb.setComparisonType(c.m_comparison_type);
			xcsb.addCriteria(xcb);
		}
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