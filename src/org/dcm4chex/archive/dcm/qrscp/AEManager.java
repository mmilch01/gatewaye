package org.dcm4chex.archive.dcm.qrscp;

import java.util.List;

import org.dcm4chex.archive.ejb.interfaces.AEDTO;

public interface AEManager
{
	public AEDTO findByPrimaryKey(long l);
	public AEDTO findByAET(String S);
	public List<AEDTO> findAll();
}
