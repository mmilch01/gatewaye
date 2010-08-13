package org.nrg.xnat.env.util;

/**
 *
 * @author Aditya Siram
 */
public class NonExistentEntryException extends Exception {
    public NonExistentEntryException (String message) {
        super(message);
    }

}
