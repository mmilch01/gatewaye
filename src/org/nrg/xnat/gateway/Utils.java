package org.nrg.xnat.gateway;

import java.util.Calendar;

import com.pixelmed.dicom.InformationEntity;

public final class Utils
{
	private static long m_RandSeed = 0;
	public static InformationEntity getInformationEntityForQueryRetieveLevel(
			String queryRetrieveLevel)
	{
		if (queryRetrieveLevel == null)
			return null;
		else if (queryRetrieveLevel.equals("PATIENT"))
			return InformationEntity.PATIENT;
		else if (queryRetrieveLevel.equals("STUDY"))
			return InformationEntity.STUDY;
		else if (queryRetrieveLevel.equals("SERIES"))
			return InformationEntity.SERIES;
		else if (queryRetrieveLevel.equals("IMAGE"))
			return InformationEntity.INSTANCE;
		else
			return null;
	}
	public static String PseudoUID()
	{
		return new Long(Calendar.getInstance().getTimeInMillis()).toString()
				+ new Integer(Calendar.getInstance().get(Calendar.DATE))
						.toString()
				+ "."
				+ new Integer(Calendar.getInstance().get(Calendar.YEAR))
						.toString() + "." + new Long(m_RandSeed++).toString();
	}
}
