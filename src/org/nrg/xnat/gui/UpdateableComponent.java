package org.nrg.xnat.gui;

/**
 * Classes implementing this interface typically take a GUI component change or
 * "refresh" it to accurately reflect the current configuration of the
 * Gateway.
 * @author Aditya Siram
 */
public interface UpdateableComponent {
    public void refresh ();
}
