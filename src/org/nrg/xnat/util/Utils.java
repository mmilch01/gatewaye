package org.nrg.xnat.util;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;

/**
 * 
 * @author Aditya Siram
 */

public class Utils {

    // Determine if the value of a properties file is comma or space
    // separated.
    public static boolean is_comma_separated (String s) {
        return (s.indexOf(",") != -1);
    }
    public static boolean is_space_separated (String s) {
        return (s.indexOf(" ") != -1);
    }

    /**
     * Determines if the given string is delimited by commas or spaces
     * and returns an array of individual trimmed values.
     *
     * It first searchs for commas and then spaces. So if it finds
     * a comma in a string that was meant to be space delimited the wrong
     * result will be returned.
     *
     * If the delimiter is known the Utils.split is the more robust
     * against this kind of error.
     *
     * @param v
     * @return
     */
    public static String [] break_up_value (String v) {
        String [] values = null;
        if (is_comma_separated(v.trim())) { values = v.split(","); }
        else if (is_space_separated(v.trim())) { values = v.split(" "); }
        else {values = new String[1]; values[0] = v;}
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].trim();
        }
        return values;
    }

    /**
     * Splits the values from a delimited string. Unlike String's split method this
     * ignores zero-length fields. eg.
     *    "hello,,,,,goodbye," => ["hello","goodbye"]
     *    ""                   => []
     *    ","                  => []
     *
     * A null input string throws a NullPointerException.
     * @param delimited_values
     * @param delimiter
     * @return
     */
    public static String [] split (String delimited_values, char delimiter) {
        Vector values = split_helper(delimited_values, Character.toString(delimiter), new Vector());
        return (String []) values.toArray(new String[values.size()]);
    }

    private static Vector split_helper (String delimited_values, String delimiter, Vector accum) {
        if (delimited_values.isEmpty()) {
            return accum;
        }
        else if (delimited_values.length() == 1 && delimited_values.startsWith(delimiter)) {
            return accum;
        }
        else {
            int next_delimiter = delimited_values.indexOf(delimiter);
            if (next_delimiter == -1) {
                accum.add(delimited_values);
                return accum;
            }
            else {
                if (next_delimiter != 0) {
                    accum.add(delimited_values.substring(0, next_delimiter));
                }
                split_helper(delimited_values.substring(next_delimiter + 1), delimiter,accum);
            }
        }
        // should never try_get here
        return accum;
    }

    /**
     * returns an array of duplicates, expects a glaumed array of
     * objects that can be compared to each other. By glaumed I mean
     * an array where similar objects are next to each other, eg.
     * ["C","C","b","A","A"] correctly outputs ["C", "A"] but
     * ["C","c","C","A,"A"] incorrectly outputs ["A"] because it
     * will fail to recognize that "C" is duplicated.
     *
     * The easiest way to ensure a glaumed array to it sort it.
     *
     * If any elements of the input array are null, a NullPointerException is thrown.
     *
     **/
    public static Comparable[] duplicates(Comparable[] sorted_os) {
        Arrays.sort(sorted_os);
        Vector dups = new Vector();
        int numObjects = sorted_os.length;
        if (numObjects == 0 || numObjects == 1) {return (Comparable[]) dups.toArray(new Comparable[dups.size()]);}
        else {
            Comparable start = sorted_os[0];
            int i = 0;
            boolean added = false;   
            do {
                if (sorted_os[i+1].compareTo(start) == 0) {
                    if (!added) {
                        dups.add(start);
                        added = true;
                    }
                    i++;
                }
                else {
                    added = false;
                    start = sorted_os[i+1];
                    i++;
                }
            }
            while(i < numObjects - 1);
        }
        return (Comparable []) dups.toArray(new Comparable[dups.size()]);
    }

    /**
     * Removes all the elements in the first array from the second array.
     *
     * Null values in either of the inputs result in a NullPointerException.
     * @param elements_to_remove
     * @param elements
     * @return
     */
    public static Comparable[] remove_elements(Comparable[] elements_to_remove, Comparable[] _elements) {
        List elements = Arrays.asList(_elements);
        Vector v = remove_elements_helper(elements_to_remove, elements.iterator(), new Vector());
        return (Comparable []) v.toArray(new Comparable[v.size()]);
    }

    private static Vector remove_elements_helper (Comparable [] elements_to_remove, Iterator elements, Vector accum) {
        if (! elements.hasNext()) {return accum;}
        else {
            boolean found_element_to_remove = false;
            Comparable element = (Comparable) elements.next();
            for (Comparable element_to_remove : elements_to_remove) {
                if (element_to_remove.equals(element)) {
                    found_element_to_remove = true;
                    break;
                }
            }
            if (! found_element_to_remove) {
                accum.add(element);
            }
            remove_elements_helper(elements_to_remove, elements, accum);
        }
        // should never try_get here
        return accum;
    }


    /**
     * Removes preceding and trailing whitespace around the string before checking
     * if its empty.
     *
     * If the input is null, a NullPointerException is thrown.
     * @param s
     * @return
     */
    public static boolean is_empty_string (String s) {return (s.trim().equals(""));}

    public static boolean is_a_number (String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Check if this string has any content by checking if it is null or empty.
     * @param s
     * @return
     */
    public static boolean has_content (String s) {return (s != null && !is_empty_string(s));}

    /**
     * Wraps Arrays class search function into a boolean.
     * @param os
     * @param o
     * @return
     */
    public static boolean contains_object (Comparable [] os, Comparable o) {
        return (Arrays.binarySearch(os,o) != -1);
    }

    
    /**
     * If a string starts with the given prefix, chop it off and return the resulting
     * string, if not just return the original
     */
    public static String removePrefix (String prefix, String s) {
        if (s.startsWith(prefix)) {s = s.substring(prefix.length());}
        return s;
    }

    /**
     * Check if the given string starts with any of the Strings in the given vector.
     * @param ss
     * @param s
     * @return
     */
    public static boolean starts_with_any_of (Vector ss, String s) {
        boolean starts_with = false;
        for (Object _s :  ss) {
            String temp = (String) _s;
            if (temp.startsWith(s)) {starts_with = true;}
        }
        return starts_with;
    }

    /**
     * Accepts the following url variations eg. "www.google.com...", "google.com...",
     * and the preceding examples prefixed with "http://" or "https://" and returns the url string
     * "google.com...". All other input is returned with whitespace trimmed.
     *
     * @param url
     * @return
     */
    public static String normalizeURL (String url) {
        url = url.trim();
        if (!is_empty_string(url)) {
            url = removePrefix("http://",url);
            url = removePrefix("https://", url);
            url = removePrefix("www",url);
        }
        return url;
    }

    /**
     * Create a value delimited string. eg
     * (["hello","world"], ',') => "hello,world"
     * This method can also be used to create a multi-line string by using
     * the systems EOL character as the delimiter.
     * @param ss
     * @param delimiter
     * @return
     */
    public static String create_delimited (String [] ss, String delimiter) {
        StringBuilder result = new StringBuilder();
        for (String string : ss) {
            result.append(string);
            result.append(delimiter);
        }
        return result.toString();
    }

    /**
     * Create a string of the given length containing only the given character
     * fill_string('a',5) => "aaaaa"
     * @param c
     * @param length
     * @return
     */
    public static String fill_string(char c, int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length ; i++){
            result.append(Character.toString(c));
        }
        return result.toString();
    }

    /**
     * Collect the keys of a hashtable into a vector
     * @param h
     * @return
     */
    public static Vector keys2Vector (Hashtable h) {
        Vector v = new Vector();
        for (Enumeration f = h.keys(); f.hasMoreElements();) {
            v.add(f.nextElement());
        }
        return v;
    }

    /**
     * Add the given string in front of each line in a multi-line string.
     * @param s
     * @param prefix
     * @return
     */
    public static String prefix_lines (String s, String prefix) {
        String[] lines = s.split(System.getProperty("line.separator"));
        for (int i = 0; i < lines.length; i++) {
            lines[i] = prefix + lines[i];
        }
        return create_delimited(lines, System.getProperty("line.separator"));
    }

    /**
     * Convert an array of objects to an array of strings. This is useful in
     * situations where the string array has been put through a polymorphic
     * function (a sorting method that acts on Comparable objects for instance)
     * that returns an array of objects. In these cases just casting the returned
     * array to (String []) is not sufficient.
     * @param os
     * @return
     */
    public static String[] toStringArray(Object[] os) {
        String[] s = new String[0];
        for (Object o : os) {
            s = (String[]) ArrayUtils.add(s, (String) o);
        }
        return s;
    }

    /**
     * Add the key value pair to the given hashtable. This wraps the Hashtable implementation
     * to throw an exception if the (key,value) pair exists.
     * @param key
     * @param o
     * @param h
     * @return
     * @throws DuplicateEntryException
     */
    public static Hashtable try_put(String key, Object o, Hashtable h) throws DuplicateEntryException {
        if (h.get(key) == null) {
            h.put(key, o);
        }
        else {
            throw new DuplicateEntryException("Key :" + key + ": exists.");
        }
        return h;
    }

    /**
     * Get the value at the given key in the given hashtable. This wraps the Hashtable
     * implementation to throw an exception if the (key,value) pair does not exist.
     * @param key
     * @param h
     * @return
     * @throws NonExistentEntryException
     */
    public static Object try_get(String key, Hashtable h) throws NonExistentEntryException {
        Object o = h.get(key);
        if (o == null) {
           throw new NonExistentEntryException("Key :" + key + " does not exist");
        }
        return o;
    }

    public static String print_elapsed_time (long milliseconds) {
        long seconds = Math.round(milliseconds/1000);
        long minutes = Math.round(seconds/60); seconds = seconds % 60;
        long hours = Math.round(minutes/60); minutes = minutes % 60;
        long days = Math.round(hours/24); hours = hours % 24;
        return (Long.toString(days) + " day(s) , " +
                Long.toString(hours) + " hour(s) , " +
                Long.toString(minutes) + " minute(s) ");
    }
}