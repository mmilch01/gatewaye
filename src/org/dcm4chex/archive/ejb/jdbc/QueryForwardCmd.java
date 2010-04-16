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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Sep 10, 2009
 */
public final class QueryForwardCmd extends BaseReadCmd {

    private static final int SELECT_LEN = 7;
    public static int transactionIsolationLevel = 0;
    
    private QueryForwardCmd(String sql) throws SQLException {
        super(JdbcProperties.getInstance().getDataSource(),
                transactionIsolationLevel, sql);
        try {
            close();
        } catch (Throwable t) {
            log.warn("Initial close failed:"+t.getLocalizedMessage());
        }
    }
    
    public static QueryForwardCmd getInstance( String sql, int limit) throws SQLException {
        sql = prepareSql(sql, limit);
        return new QueryForwardCmd(sql);
    }
    
    public static String prepareSql(String sql, int limit) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sql = sql.trim();
        if ( sql.endsWith(";")) {
            sql = sql.substring(0, sql.length()-1);
        }
        sql = sql.replaceAll("\\s\\s+", " ");
        log.debug("Original SQL (formatted):"+sql);
        if (limit > 0 ) {
            String sql1 = sql.toUpperCase();
            int pos0 = sql1.indexOf("DISTINCT");
            if (pos0 != -1) {
                sqlBuilder.setDistinct(true);
                pos0 += 9;
            } else {
                pos0 = SELECT_LEN;
            }
            int pos1 = sql1.indexOf("FROM");
            StringBuffer sb = new StringBuffer(sql.length()+30);
            sb.append(sql.substring(0, SELECT_LEN));
            sqlBuilder.setLimit(limit);
            String[] fields = toFields(sql.substring(pos0, pos1)); 
            sqlBuilder.setFieldNamesForSelect(fields);
            sqlBuilder.addOrderBy(fields[0], SqlBuilder.ASC);
            sqlBuilder.appendLimitbeforeFrom(sb);
            sb.append(' ');
            int pos2 = sql1.indexOf("FOR READ ONLY", pos1); //DB2?
            if (pos2 > 0) {
                sb.append(sql.substring(pos1, pos2));
                sqlBuilder.appendLimitAtEnd(sb);
                sb.append(sql.substring(pos2));
            } else {
                sb.append(sql.substring(pos1));
                sqlBuilder.appendLimitAtEnd(sb);
            }
            log.debug("SQL with LIMIT:"+sb);
            return sb.toString();
        } else {
            return sql;
        }
    }
    
    private static String[] toFields(String s) {
        StringTokenizer st = new StringTokenizer(s, ",");
        String[] fields = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            fields[i++] = st.nextToken();
        }
        return fields;
    }

    public Map<String,List<String>> getSeriesIUIDs(Long updatedBefore) throws SQLException {
        if (stmt == null) 
            open();
        if ( updatedBefore != null ) {
            if ( log.isDebugEnabled() )
                log.debug("Set parameter (updatedBefore) to:"+updatedBefore+" Date:"+new Date(updatedBefore));
            ((PreparedStatement) stmt).setDate(1, new java.sql.Date(updatedBefore));
        } else if (log.isDebugEnabled()) {
            log.debug("Use of updatedBefore WHERE clause disabled! Dont set parameter of prepared statement");
        }
        execute();
        Map<String,List<String>> map = new HashMap<String,List<String>>();
        try {
            String uid, retrAet, lastAet=null;
            List<String> l = null;
            int pos;
            while (next()) {
                uid = rs.getString(1);
                retrAet = rs.getString(2);
                if ( retrAet == null ) {
                    log.warn("Series "+uid+" has no RetrieveAET! can't be forwarded!" );
                    continue;
                }
                if ( ( pos = retrAet.indexOf('\\') ) != -1 ) {
                    retrAet = retrAet.substring(0, pos);
                }
                if ( !retrAet.equals(lastAet) ) {
                    log.debug("retrAet changed!");
                    l = map.get(retrAet);
                    if ( l == null ) {
                        log.debug("create new list for new retrAet!");
                        l = new ArrayList<String>();
                        map.put(retrAet, l);
                        lastAet = retrAet;
                    }
                } 
                l.add(rs.getString(1));
            }
        } catch (Exception x) {
            log.error("QueryForwardCmd failed!",x);
        } finally {
            try {
                close();
            } catch (Exception ignore) {
                log.warn("Error closing connection!");
            }
        }
        return map;
    }
    
    public String getSQL() {
        return sql;
    }
}
