/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chex.archive.ejb.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.MD5;

public final class QueryFilesOfSeriesCmd extends BaseReadCmd {
    private static final String QueryFilesBySeriesUIDCmd = "SELECT f.pk, f.filepath, f.file_md5, f.file_status, "
            + "fs.fs_group_id, fs.dirpath, fs.retrieve_aet, fs.availability, fs.user_info, i.sop_cuid, i.sop_iuid "
            + "FROM files f, filesystem fs, instance i, series s "
            + "WHERE f.filesystem_fk = fs.pk AND f.instance_fk = i.pk AND i.series_fk = s.pk AND s.series_iuid=?";

    public static int transactionIsolationLevel = 0;

    public QueryFilesOfSeriesCmd(String iuid) throws SQLException {
        super(JdbcProperties.getInstance().getDataSource(),
                transactionIsolationLevel, QueryFilesBySeriesUIDCmd);
        ((PreparedStatement) stmt).setString(1, iuid);
        execute();
    }

    private FileDTO getFileDTO() throws SQLException {
        FileDTO dto = new FileDTO();
        dto.setPk(rs.getLong(1));
        dto.setFilePath(rs.getString(2));
        dto.setFileMd5(MD5.toBytes(rs.getString(3)));
        dto.setFileStatus(rs.getInt(4));
        dto.setFileSystemGroupID(rs.getString(5));
        dto.setDirectoryPath(rs.getString(6));
        dto.setRetrieveAET(rs.getString(7));
        dto.setAvailability(rs.getInt(8));
        dto.setUserInfo(rs.getString(9));
        dto.setSopClassUID(rs.getString(10));
        dto.setSopInstanceUID(rs.getString(11));
        return dto;
    }

    public Map getBestFileDTOs() throws SQLException {
        List allFiles = new ArrayList();
        try {
            while (next()) {
            	allFiles.add(getFileDTO());
            }
        } finally {
            close();
        }
        Map bestFiles = new HashMap();
        Iterator i = allFiles.listIterator();
        while(i.hasNext()) {
            FileDTO dto = (FileDTO)i.next();
            String sopInstance = dto.getSopInstanceUID();
            if ( dto.getFileStatus()< 0 )
                continue;
            FileDTO compareDto = (FileDTO)bestFiles.get(sopInstance);
            if ( compareDto == null || isBetterLocation(compareDto, dto) ) {
                log.debug("Adding dto: " + dto + " for sop: " + sopInstance);
                bestFiles.put(sopInstance, dto);
            }
        }
        return bestFiles;
    }

    private boolean isBetterLocation(FileDTO compareDto, FileDTO dto) {
        log.debug("Checking to see if dto: " + dto + " is better than: " + compareDto);
		return( dto.getAvailability() < compareDto.getAvailability() )? true : false; 
	}

}
