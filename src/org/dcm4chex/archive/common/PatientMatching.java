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
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2006-2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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
package org.dcm4chex.archive.common;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since May 8, 2009
 */
public class PatientMatching implements Serializable{

    private static final long serialVersionUID = -5066423063497788483L;

    private static final String PID = "pid";
    private static final String ISSUER = "issuer";
    private static final String FAMILYNAME = "familyname";
    private static final String GIVENNAME = "givenname";
    private static final String MIDDLENAME = "middlename";
    private static final String BIRTHDATE = "birthdate";

    public static final PatientMatching BY_ID = 
        new PatientMatching(
                true,   // trustPatientIDWithIssuer
                false,  // unknownPatientIDAlwaysMatch
                true,   // unknownIssuerAlwaysMatch
                false,  // familyNameMustMatch
                true,  // unknownFamilyNameAlwaysMatch
                false, // givenNameMustMatch
                true,  // unknownGivenNameAlwaysMatch
                false, // middleNameMustMatch,
                true,  // unknownMiddleNameAlwaysMatch,
                false, // birthDateMustMatch,
                true   // unknownBirthDateAlwaysMatch
                );

    public final boolean trustPatientIDWithIssuer;
    public final boolean unknownPatientIDAlwaysMatch;
    public final boolean unknownIssuerAlwaysMatch;
    public final boolean familyNameMustMatch;
    public final boolean unknownFamilyNameAlwaysMatch;
    public final boolean givenNameMustMatch;
    public final boolean unknownGivenNameAlwaysMatch;
    public final boolean middleNameMustMatch;
    public final boolean unknownMiddleNameAlwaysMatch;
    public final boolean birthDateMustMatch;
    public final boolean unknownBirthDateAlwaysMatch;
    
    public PatientMatching(String s) {
        int pid = indexOf(s, PID);
        int issuer = indexOf(s, ISSUER);
        int familyName = indexOf(s, FAMILYNAME);
        int givenName = indexOf(s, GIVENNAME);
        int middleName = indexOf(s, MIDDLENAME);
        int birthdate = indexOf(s, BIRTHDATE);
        int trust = s.indexOf("[");
        if (pid == -1 || issuer == -1) {
            throw new IllegalArgumentException(s);
        }
        familyNameMustMatch = familyName != -1;
        givenNameMustMatch = givenName != -1;
        middleNameMustMatch = middleName != -1;
        birthDateMustMatch = birthdate != -1;
        unknownPatientIDAlwaysMatch = unknownAlwaysMatch(s, pid, PID);
        unknownIssuerAlwaysMatch = unknownAlwaysMatch(s, issuer, ISSUER);
        unknownFamilyNameAlwaysMatch = 
                unknownAlwaysMatch(s, familyName, FAMILYNAME);
        unknownGivenNameAlwaysMatch =
                unknownAlwaysMatch(s, givenName, GIVENNAME);
        unknownMiddleNameAlwaysMatch =
                unknownAlwaysMatch(s, middleName, MIDDLENAME);
        unknownBirthDateAlwaysMatch =
                unknownAlwaysMatch(s, birthdate, BIRTHDATE);
        if (trust != -1) {
            if (trust < issuer || s.indexOf("]") != s.length()-1
                    || familyNameMustMatch && trust > familyName
                    || givenNameMustMatch && trust > givenName
                    || middleNameMustMatch && trust > middleName
                    || birthDateMustMatch && trust > birthdate) {
                throw new IllegalArgumentException(s);
            }
            trustPatientIDWithIssuer = true;
        } else {
            trustPatientIDWithIssuer = !familyNameMustMatch
                    && !givenNameMustMatch && !middleNameMustMatch
                    && !birthDateMustMatch;
        }
        if (unknownPatientIDAlwaysMatch && !familyNameMustMatch) {
            throw new IllegalArgumentException(s);
        }
    }

    private PatientMatching(boolean trustPatientIDWithIssuer, 
            boolean unknownPatientIDAlwaysMatch,
            boolean unknownIssuerAlwaysMatch,
            boolean familyNameMustMatch,
            boolean unknownFamilyNameAlwaysMatch,
            boolean givenNameMustMatch,
            boolean unknownGivenNameAlwaysMatch,
            boolean middleNameMustMatch,
            boolean unknownMiddleNameAlwaysMatch,
            boolean birthDateMustMatch,
            boolean unknownBirthDateAlwaysMatch) {
        this.trustPatientIDWithIssuer = trustPatientIDWithIssuer;
        this.unknownPatientIDAlwaysMatch = unknownPatientIDAlwaysMatch;
        this.unknownIssuerAlwaysMatch = unknownIssuerAlwaysMatch;
        this.familyNameMustMatch = familyNameMustMatch;
        this.unknownFamilyNameAlwaysMatch = unknownFamilyNameAlwaysMatch;
        this.givenNameMustMatch = givenNameMustMatch;
        this.unknownGivenNameAlwaysMatch = unknownGivenNameAlwaysMatch;
        this.middleNameMustMatch = middleNameMustMatch;
        this.unknownMiddleNameAlwaysMatch = unknownMiddleNameAlwaysMatch;
        this.birthDateMustMatch = birthDateMustMatch;
        this.unknownBirthDateAlwaysMatch = unknownBirthDateAlwaysMatch;

    }

    public boolean isUnknownPersonNameAlwaysMatch() {
        return unknownFamilyNameAlwaysMatch && unknownGivenNameAlwaysMatch
                && unknownMiddleNameAlwaysMatch;
    }

    private boolean unknownAlwaysMatch(String s, int index, String substr) {
        int after;
        return index == -1 
                || (after = index + substr.length()) < s.length()
                && s.charAt(after) == '?';
    }

    private int indexOf(String str, String substr) {
        int index = str.indexOf(substr);
        if (index != -1) {
            int after;
            if (index > 0 
                    && " ,[".indexOf(str.charAt(index-1)) == -1
                    || (after = index + substr.length()) < str.length() 
                    && " ,]?".indexOf(str.charAt(after)) == -1) {
                throw new IllegalArgumentException(str);
            }
        }
        return index;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(PID);
        if (unknownPatientIDAlwaysMatch) {
            sb.append('?');
        }
        sb.append(',').append(ISSUER);
        if (unknownIssuerAlwaysMatch) {
            sb.append('?');
        }
        if (familyNameMustMatch || givenNameMustMatch || middleNameMustMatch
                || birthDateMustMatch) {
            sb.append(',');
            if (trustPatientIDWithIssuer) {
                sb.append('[');
            }
            int count = 0;
            if (familyNameMustMatch) {
                count++;
                sb.append(FAMILYNAME);
                if (unknownFamilyNameAlwaysMatch) {
                    sb.append('?');
                }
            }
            if (givenNameMustMatch) {
                if (count++ > 0) {
                    sb.append(',');
                }
                sb.append(GIVENNAME);
                if (unknownGivenNameAlwaysMatch) {
                    sb.append('?');
                }
            }
            if (middleNameMustMatch) {
                if (count++ > 0) {
                    sb.append(',');
                }
                sb.append(MIDDLENAME);
                if (unknownMiddleNameAlwaysMatch) {
                    sb.append('?');
                }
            }
            if (birthDateMustMatch) {
                if (count > 0) {
                    sb.append(',');
                }
                sb.append(BIRTHDATE);
                if (unknownBirthDateAlwaysMatch) {
                    sb.append('?');
                }
            }
            if (trustPatientIDWithIssuer) {
                sb.append(']');
            }
        }
        return sb.toString();
    }

    public Pattern compilePNPattern(String familyName, String givenName,
            String middleName) {
        if (allMatchesFor(familyName, givenName, middleName)) {
            return null;
        }
        StringBuilder regex = new StringBuilder();
        if (familyNameMustMatch && familyName != null
                || givenNameMustMatch && givenName != null
                || middleNameMustMatch && middleName != null) {
            appendRegex(regex, familyName, familyNameMustMatch,
                    unknownFamilyNameAlwaysMatch);
            if (givenNameMustMatch && givenName != null
                    || middleNameMustMatch && middleName != null) {
                appendRegex(regex, givenName, givenNameMustMatch,
                        unknownGivenNameAlwaysMatch);
                if (middleNameMustMatch && middleName != null) {
                    appendRegex(regex, middleName, middleNameMustMatch,
                            unknownMiddleNameAlwaysMatch);
                }
            }
        }
        regex.append(".*");
        return Pattern.compile(regex.toString());
    }

    private static void appendRegex(StringBuilder regex, String value,
            boolean mustMatch, boolean unknownAlwaysMatch) {
        if (!mustMatch || value == null) {
            regex.append("[^\\^]*");
        } else {
            regex.append(unknownAlwaysMatch ? "(\\Q" : "\\Q")
                 .append(value)
                 .append(unknownAlwaysMatch ? "\\E)?" : "\\E");
        }
        regex.append("\\^");
    }

    public boolean noMatchesFor(String pid, String issuer, String familyName,
            String givenName, String middleName, String birthdate) {
        return !unknownPatientIDAlwaysMatch && pid == null
                || !unknownIssuerAlwaysMatch && issuer == null
                || !(trustPatientIDWithIssuer && pid != null && issuer != null)
                && noMatchesFor(familyName, givenName, middleName, birthdate);
    }

    public boolean noMatchesFor(String familyName, String givenName,
            String middleName, String birthdate) {
        return !unknownFamilyNameAlwaysMatch && familyName == null
                || !unknownGivenNameAlwaysMatch && givenName == null
                || !unknownMiddleNameAlwaysMatch && middleName == null
                || !unknownBirthDateAlwaysMatch && birthdate == null;
    }

    public boolean allMatchesFor(String familyName, String givenName,
            String middleName) {
        return (!familyNameMustMatch 
                        || unknownFamilyNameAlwaysMatch && familyName == null)
            && (!givenNameMustMatch
                        || unknownGivenNameAlwaysMatch && givenName == null)
            && (!middleNameMustMatch
                        || unknownMiddleNameAlwaysMatch && middleName == null);
    }

    public boolean allMatchesFor(String familyName, String givenName,
            String middleName, String birthdate) {
        return allMatchesFor(familyName, givenName, middleName)
            && (!birthDateMustMatch
                        || unknownBirthDateAlwaysMatch && birthdate == null);
    }
}
