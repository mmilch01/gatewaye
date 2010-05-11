package org.nrg.xnat.gateway;

import java.net.InetAddress;
import java.util.TreeMap;

import org.dcm4chex.archive.ejb.interfaces.AEDTO;

public class AEServer implements AEServerMBean
{
	private AEDTO m_localAE;
	public void setLocalAE(AEDTO ae)
	{
		m_localAE=ae;
	}
	private TreeMap<String, AEDTO> m_AEs = new TreeMap<String, AEDTO>();
	public AEDTO getAE(String aet, InetAddress addr)
	{
		return m_AEs.get(aet);
	}
	public void addAE(AEDTO ae)
	{
		m_AEs.put(ae.getTitle(), ae);
	}
	public String getAETList()
	{
		String res="";
		int i=0;
		for(AEDTO ae:m_AEs.values())
		{
			if(i>0)	
				res+=" ";
			res+=ae.getTitle();
			i++;
		}
		return res;
	}
}
