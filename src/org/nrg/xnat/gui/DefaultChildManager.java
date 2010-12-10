package org.nrg.xnat.gui;

import javax.swing.JComponent;

/**
 * A simple wrapper around ChildManager that enables/disables a GUI component
 * when the first child has been added or the last child removed.
 * @author Aditya Siram
 */
public class DefaultChildManager extends ChildManager {
    private JComponent c;

    public DefaultChildManager(JComponent c) {
        super();
        this.c = c;
    }

    @Override
    public void removed_last_child() {
        super.removed_last_child();
        c.setEnabled(true);
    }

    @Override
    public void added_first_child() {
        super.added_first_child();
        c.setEnabled(false);
    }
}
