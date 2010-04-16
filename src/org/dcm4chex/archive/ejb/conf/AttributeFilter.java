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

package org.dcm4chex.archive.ejb.conf;

import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;

import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.exceptions.ConfigurationException;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 12863 $ $Date: 2010-03-03 14:30:19 +0100 (Wed, 03 Mar 2010) $
 * @since 28.12.2003
 */
public final class AttributeFilter {
    private static final String CONFIG_URL = "resource:dcm4chee-attribute-filter.xml";
    static AttributeFilter patientFilter;
    static AttributeFilter studyFilter;
    static AttributeFilter seriesFilter;
    static HashMap<String, AttributeFilter> instanceFilters =
            new HashMap<String, AttributeFilter>();
    private int[] tags = {};
    private int[] noCoercion = {};
    private int[] iCase = {};
    private int[] vrs = {};
    private int[] fieldTags = {};
    private String[] fields = {};
    private final String tsuid;
    private final boolean exclude;
    private final boolean excludePrivate;
    private final boolean overwrite;
    private final boolean merge;
    private boolean noFilter = false;
    
    static {
        reload();
    }

    // Test Driver
    public static void main(String[] args) {
        AttributeFilterLoader.loadFrom(args[0]);
    }

    public static void reload() {
        AttributeFilter.patientFilter = null;
        AttributeFilter.studyFilter = null;
        AttributeFilter.seriesFilter = null;
        AttributeFilter.instanceFilters.clear();
        AttributeFilterLoader.loadFrom(CONFIG_URL);
    }

    public static long lastModified() {
        URLConnection conn;
        try {
            conn = new URL(CONFIG_URL).openConnection();
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
        return conn.getLastModified();
    }
    
    public static AttributeFilter getPatientAttributeFilter()  {
        return patientFilter;
    }

    public static AttributeFilter getStudyAttributeFilter() {
        return studyFilter;
    }

    public static AttributeFilter getSeriesAttributeFilter() {
        return seriesFilter;
    }
    
    public static AttributeFilter getInstanceAttributeFilter(String cuid) {
        AttributeFilter filter = instanceFilters.get(cuid);
        return filter == null ? instanceFilters.get(null) : filter;
    }

    AttributeFilter(String tsuid, boolean exclude, boolean excludePrivate,
            boolean overwrite, boolean merge) {
        this.tsuid = tsuid;
        this.exclude = exclude;
        this.excludePrivate = excludePrivate;
        this.overwrite = overwrite;
        this.merge = merge;
    }
    
    final void setNoCoercion(int[] noCoercion) {
        this.noCoercion = noCoercion;
    }

    final void setICase(int[] iCase) {
        this.iCase = iCase;
    }

    final void setTags(int[] tags) {
        this.tags = tags;
    }

    final int[] getTags() {
        return this.tags;
    }
    
    final void setFields(String[] fields) {
        if (!exclude) {
            this.fields = fields;
        }
    }

    final String[] getFields() {
        return this.fields;
    }
    
    public boolean hasTag(int tag) {
        int index = Arrays.binarySearch(tags,tag);
        return index>=0;
    }

    final void setFieldTags(int[] fieldTags) {
        this.fieldTags = fieldTags;        
    }
    
    public final int[] getFieldTags() {        
        return this.fieldTags;
    }
    
    public String getField(int tag) {
        for (int i = 0; i < fieldTags.length; i++) {
            if (fieldTags[i] == tag) {
                return fields[i];
            }
        }
        return null;
    }
    
    final void setVRs(int[] vrs) {
        this.vrs = vrs;
    }

    final int[] getVRs() {
        return this.vrs;
    }
    
    public final boolean isNoFilter() {
        return noFilter;
    }
         
    final void setNoFilter(boolean noFilter) {
        this.noFilter = noFilter;
    }
    
    final boolean isExclude() {
        return exclude;
    }
    
    public boolean isCoercionForbidden(int tag) {
    	return Arrays.binarySearch(noCoercion, tag) >= 0;
    }

    public boolean isICase(int tag) {
        return Arrays.binarySearch(iCase, tag) >= 0;
    }

    public final String getTransferSyntaxUID() {
        return tsuid;
    }

    public final boolean isOverwrite() {
        return overwrite;
    }

    public final boolean isMerge() {
        return merge;
    }

    public Dataset filter(Dataset ds) {
        return ds.subSet(tags, vrs, exclude, excludePrivate);
    }

    public String[] getStrings(Dataset ds, int tag) {
        return getStrings(ds, tag, tag);
    }

    public String[] getStrings(Dataset ds, int tag, int icasetag) {
        String[] ss = ds.getStrings(tag);
        if (ss != null && isICase(icasetag))
            for (int i = 0; i < ss.length; i++)
                ss[i] = toUpperCase(ss[i]);
        return ss;
    }

    public String getString(Dataset ds, int tag) {
        return toUpperCase(ds.getString(tag), tag);
    }

    public String toUpperCase(String s, int tag) {
        return s != null && isICase(tag) ? s.toUpperCase() : s;
    }

    public static String toUpperCase(String s) {
        return s != null ? s.toUpperCase() : s;
    }
}
