package org.nrg.xnat.gateway;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.log4j.Priority;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dom4j.io.SAXReader;
import org.nrg.fileserver.XNATRestAdapter;
import org.nrg.xnd.ontology.XNATTableParser;

import com.pixelmed.dicom.InformationEntity;

public class XNATCMoveRsp
{
	private XNATRestAdapter m_xre;
	private String m_srv, m_usr, m_pass, m_StoreFolder;
	public XNATCMoveRsp(String srv, String usr, String pass,
			String storeFolder)
	{
		m_xre = new XNATRestAdapter(srv, usr, pass);
		m_StoreFolder=storeFolder;
		m_usr=usr; m_pass=pass; m_srv=srv;
	}

	private boolean HasUniqueID(Dataset ds, InformationEntity level)
	{
		if (level.compareTo(InformationEntity.SERIES) == 0)
		{
			return !(ds.getString(0x0020000E) == null);
		} else if (level.compareTo(InformationEntity.STUDY) == 0)
		{
			return !(ds.getString(0x0020000D) == null);
		}
		return false;
	}

	private FileInfo SaveDicomFile(InputStream is, Dataset xnatTags)
			throws IOException
	// , DicomException
	{
		String fname = (m_StoreFolder + "/" + Utils.PseudoUID()).replace('\\',
				'/').replace("//", "/");
		/*
		 * org.dcm4che2.io.DicomInputStream dis=null;
		 * org.dcm4che2.io.DicomOutputStream dos=null; try { dis= new
		 * org.dcm4che2.io.DicomInputStream(is); DicomObject
		 * dobj=dis.readDicomObject(); for(Object at:request.values()) {
		 * setTag(dobj,(Attribute)at); } fname= dos= new
		 * org.dcm4che2.io.DicomOutputStream(new File(fname));
		 * dos.serializeDicomObject(dobj); } finally {
		 * try{dis.close();dos.close();}finally{} }
		 */
		
		Dataset ds=DcmObjectFactory.getInstance().newDataset();
//		ds.readDataset(in, param, stopTag)		
		ds.readFile(is, null, 0xffffffff);
		ds.putAll(xnatTags);
		
		//remove query-specific tags
		ds.remove(Tags.QueryRetrieveLevel);
/*

		FileMetaInfo fmi = DcmObjectFactory.getInstance().newFileMetaInfo();
		fmi.read(is);
		fmi.putAll(request);
*/		
		File f = new File(fname);
		if (f.exists())
			f.delete();
		ds.writeFile(f, null);

/*		
		FileOutputStream fos = new FileOutputStream(f);
		try
		{
			fmi.write(fos);
		} finally
		{
			try
			{
				fos.close();
			} catch (Exception e)
			{
			}
		}
*/		
		/*
		 * AttributeList fal=new AttributeList(); fal.read(new
		 * DicomInputStream(is)); fal.putAll(request);
		 * fal.removeGroupLengthAttributes(); Attribute
		 * ts=fal.get(TagFromName.TransferSyntaxUID);
		 * fal.removeMetaInformationHeaderAttributes();
		 * fal.remove(TagFromName.DataSetTrailingPadding); //
		 * fal.correctDecompressedImagePixelModule(); String
		 * sts=ts.getSingleStringValueOrDefault
		 * (TransferSyntax.ExplicitVRLittleEndian);
		 * FileMetaInformation.addFileMetaInformation(fal,sts,m_AETitle);
		 * fal.write(fname,sts,true,true); new File(fname).deleteOnExit();
		 */
		return new FileInfo(ds, fname);
	}

	private FileInfo DownloadAndSaveDicomFile(XNATRestAdapter xre, String URI,
			Dataset request)
	{
		HttpMethodBase get;
		String path = (m_srv + "/" + URI).replace("//", "/");
		Tools.LogMessage(Priority.INFO_INT, "Retrieving file " + path);
		path = URI.substring(5);
		get = xre.PerformConnection(XNATRestAdapter.GET, path, "");
		if (get == null)
			return null;
		try
		{
			return SaveDicomFile(get.getResponseBodyAsStream(), request);
		} catch (Exception e)
		{
			return null;
		} finally
		{
			get.releaseConnection();
		}
	}
	byte[] expandBuf(byte[] buf) throws OutOfMemoryError
	{
		if (buf.length < 1)
			return buf;
		byte[] newBuf = new byte[buf.length * 2];
		for (int i = 0; i < buf.length; i++)
			newBuf[i] = buf[i];
		return newBuf;
	}

	byte[] readZipBuf(ZipInputStream zin)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			int len = 0;
			byte[] buf = new byte[2048];
			while ((len = zin.read(buf)) > 0)
			{
				baos.write(buf, 0, len);
			}
			return baos.toByteArray();
		} catch (Exception e)
		{
			return null;
		} finally
		{
			if (baos != null)
				try
				{
					baos.close();
				} catch (Exception e)
				{
				}
		}
	}

	private Collection<FileInfo> retrieveSeries(Dataset al,
			boolean bUseCompression,TreeMap<String,String[]> scanMap)
	{
		// bUseCompression=false;//??
		// XNATRestAdapter xre = new
		// XNATRestAdapter(m_XNATServer,m_XNATUser,m_XNATPass);			

		HttpMethodBase get;
		LinkedList<FileInfo> files = new LinkedList<FileInfo>();
		String path="";
		
		//construct the path to request scan files
		if(!XNATGatewayServer.isDICOMUID())
		{
			// AttributeList mod_request=new AttributeList();
			// mod_request.putAll(request);
			XNATQueryGenerator.GetVocabulary().modifySOPInstUID(al, false);
	
			path = XNATQueryGenerator.getRetrieveRESTQuery(
					InformationEntity.SERIES, al);
			Tools.LogMessage(Priority.INFO_INT, "Rest query path: " + path);
			if (path == null)
			{
				Tools.LogMessage(Priority.ERROR_INT,
						"Error generating REST retrieve query");
				return files;
			}
		}
		else
		{
			String[] ids=scanMap.get(al.getString(Tags.SeriesInstanceUID));
			if(ids==null) return files;
			path="/experiments/"+ids[0]+"/scans/"+ids[1]+"/files";
		}
		
		if (bUseCompression)
			path += (path.contains("?") ? "&" : "?") + "format=zip";
		if (null == (get = m_xre.PerformConnection(XNATRestAdapter.GET, path,
				"")))
			return files;
		
		/*
		 * try { // String resp=get.getResponseBodyAsString(); //
		 * System.err.println(resp); } catch(Exception e){return;}
		 */
		if(!XNATGatewayServer.isDICOMUID())
			XNATQueryGenerator.GetVocabulary().modifySOPInstUID(al, true);
		FileInfo fi;

		try
		{
			// parse xml table and download each individual files
			if (!bUseCompression)
			{
				// 2. parse the response
				LinkedList<TreeMap<String, String>> row_map = XNATTableParser
						.GetRows(new SAXReader().read(get
								.getResponseBodyAsStream()), true, "header");// bGenSearch?"header":null);
				String URI;
				// get the actual DICOM files!
				// if(!bAppend || m_sdf==null) m_sdf=new SetOfDicomFiles();
				for (TreeMap<String, String> row : row_map)
				{
					URI = row.get("URI");
					if (URI == null)
						continue;
					fi = DownloadAndSaveDicomFile(m_xre, URI, al);
					if (fi != null)
						files.add(fi);
					// m_sdf.add(fname);
				}
				// if(m_sdf.size()<1) m_sdf=null;
			}
			// download entire file archive.
			else
			{
				// if(!bAppend || m_sdf==null) m_sdf=new SetOfDicomFiles();
				ZipInputStream zin = new ZipInputStream(get
						.getResponseBodyAsStream());
				String fname = null;
				ZipEntry entry;
				while ((entry = zin.getNextEntry()) != null)
				{
					try
					{
						byte[] buf = readZipBuf(zin);
						if (buf == null || buf.length < 1)
						{
							continue;
						}
						fi = SaveDicomFile(new ByteArrayInputStream(buf), al);
					} catch (IOException ioex)
					{
						continue;
					} finally
					{
						zin.closeEntry();
					}

					if (fi != null)
						files.add(fi);
				}
				zin.close();
			}
		} catch (Exception e)
		{
			Tools.LogException(Priority.ERROR,
					"Error parsing the response to REST query", e);
			// m_sdf=null;
		} finally
		{
			get.releaseConnection();
		}
		return files;
	}
	private FileInfo[][] fromCollection(Collection<FileInfo> cfi)
	{
		int sz = cfi.size();
		if (sz < 1)
			return new FileInfo[0][];
		FileInfo[][] res = new FileInfo[sz][];
		int i = 0;
		for (FileInfo fi : cfi)
		{
			res[i] = new FileInfo[1];
			res[i][0] = fi;
			i++;
		}
		return res;
	}
	public FileInfo[][] performRetrieve(Dataset query)
	{
		String qLevel = query.getString(Tags.QueryRetrieveLevel);
		InformationEntity ieWanted = Utils
				.getInformationEntityForQueryRetieveLevel(qLevel);
		Tools.LogMessage(Priority.INFO_INT, ieWanted.toString()
				+ " retrieve request received");
		boolean bStudyDefined = HasUniqueID(query, InformationEntity.STUDY), bSeriesDefined = HasUniqueID(
				query, InformationEntity.SERIES);

		// first, update the attribute list with subject info.
		// XNATRestAdapter xra = new
		// XNATRestAdapter(m_XNATServer,m_XNATUser,m_XNATPass);
		HttpMethodBase get;
		String p="experiments?"+(XNATGatewayServer.isDICOMUID()?
				"xnat:imageSessionData/UID=":"ID=");
		p+= XNATQueryGenerator.GetValueFromAttributeList("stinstuid", query)
					+ "&columns=ID,project,label,subject_ID,subject_label";
		if(XNATGatewayServer.isDICOMUID())
		{
//			p+=",xnat:imageSessionData/Scans/Scan/ID,xnat:imageSessionData/Scans/Scan/UID";
			p+=",xnat:imagescandata/id,xnat:imagescandata/uid";
		}
		
		p+="&format=xml";
		
		get = m_xre.PerformConnection(XNATRestAdapter.GET, p, "");
		TreeMap<String,String[]> scanMap=new TreeMap<String,String[]>();
		try
		{
			String rsp = get.getResponseBodyAsString();
			LinkedList<TreeMap<String, String>> rm = XNATTableParser.GetRows(
					new SAXReader().read(get.getResponseBodyAsStream()), true,
					"header");// bGenSearch?"header":null);
			int i = 0;
			
			for (TreeMap<String, String> row : rm)
			{
				if(XNATGatewayServer.isDICOMUID())
				{					
//					String scanUID=row.get("xnat:imagesessiondata/scans/scan/uid");
					String scanUID=row.get("xnat:imagescandata/uid");
//					String[] ids={row.get("ID"),row.get("xnat:imagesessiondata/scans/scan/id")};
					String[] ids={row.get("ID"),row.get("xnat:imagescandata/id")};
					scanMap.put(scanUID,ids);
				}
				if (i == 0)
				{
					
					Dataset received = XNATQueryGenerator.GetVocabulary()
							.GetDicomEntry(row, ieWanted);
					// patient name, id and staccessionnum
					query.putXX(0x00100010, received.getString(0x00100010));
					query.putXX(0x00100020, received.getString(0x00100020));
					query.putXX(0x00080050, received.getString(0x00080050));
				}
				i++;
			}
			get.releaseConnection();
		} catch (Exception e)
		{
		}
		Collection<FileInfo> files = null;
		// then, proceed with series retrieve.
		if (ieWanted.compareTo(InformationEntity.SERIES) == 0 && bSeriesDefined)
		{
			files = retrieveSeries(query, true,scanMap); // ??
		} else if ((ieWanted.compareTo(InformationEntity.STUDY) == 0 && bStudyDefined)
				|| (ieWanted.compareTo(InformationEntity.SERIES) == 0 && !bSeriesDefined))
		{
			// m_sdf=null;
			String path = XNATQueryGenerator.getRESTQuery(
					InformationEntity.SERIES, query,false);
			Tools.LogMessage(Priority.INFO_INT, "REST query: " + path);

			if (path == null)
			{
				Tools.LogMessage(Priority.ERROR_INT, "Error generating REST query");
				return null;
			}
			// query for all series.
			// XNATRestAdapter xre=new
			// XNATRestAdapter(m_XNATServer,m_XNATUser,m_XNATPass);
			HttpMethodBase method = m_xre.PerformConnection(
					XNATRestAdapter.GET, path, "");

			if (method == null)
				return null;
			try
			{
				String resp = method.getResponseBodyAsString();
				Tools.LogMessage(Priority.INFO_INT, "REST query response: " + resp);
			} catch (Exception e)
			{
				return null;
			}
			method.releaseConnection();
			files = new LinkedList<FileInfo>();
			try
			{
				// 2. parse the response - using XND's classes
				LinkedList<TreeMap<String, String>> row_map = XNATTableParser
						.GetRows(new SAXReader().read(method
								.getResponseBodyAsStream()), true, "header");// bGenSearch?"header":null);
				for (TreeMap<String, String> row : row_map)
				{
					// 3. translate the response to DICOM's AttributeList
					Dataset ds = DcmObjectFactory.getInstance().newDataset();
					// AttributeList al=new AttributeList();
					ds.putAll(query);
					// al.putAll(queryIdentifier);
					Dataset received = XNATQueryGenerator.GetVocabulary()
							.GetDicomEntry(row, InformationEntity.SERIES/*ieWanted*/);
					ds.putAll(received);
					// al.putAll(received);
					XNATQueryGenerator.GetVocabulary().modifySOPInstUID(ds,
							true);
					if (ds.size() > 0)
					{
						files.addAll(retrieveSeries(ds, true,scanMap));
					}
				}
			} catch (Exception e)
			{
				Tools.LogException(Priority.ERROR,
						"Error parsing the response to REST query", e);
			}
		} else
			files = null;
		return (files != null) ? fromCollection(files) : new FileInfo[0][];
		// if(files!=null) //update cache
		// {
		// return fromCollection(files);
		// } else
	}
}