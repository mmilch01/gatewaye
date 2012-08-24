package org.nrg.xnat.gateway;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

import org.dcm4che.data.Dataset;

public interface XNATGatewayServerMBean
{
	public FileInfo[][] retrieveSeries(Dataset query, TreeMap tm);
	public LinkedList<Object> getSeriesRequests(Dataset query);
	public void instancesSent(ArrayList fileInfos);
	public Boolean isAnonymousAEAllowed();
}
