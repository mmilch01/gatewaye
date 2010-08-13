package org.nrg.xnat.gui;

import javax.swing.JComponent;

/**
 *
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
