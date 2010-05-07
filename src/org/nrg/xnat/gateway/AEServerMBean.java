package org.nrg.xnat.gateway;

import java.net.InetAddress;

import org.dcm4chex.archive.ejb.interfaces.AEDTO;

public interface AEServerMBean
{
	public AEDTO getAE(String aet, InetAddress addr);
	public void addAE(AEDTO ae);
}
