package org.nrg.xnat.gateway;

import java.util.ArrayList;

import org.dcm4che.data.Dataset;

public interface XNATGatewayServerMBean
{
	public FileInfo[][] retrieveFiles(Dataset query);
	public void instancesSent(ArrayList fileInfos);
}
