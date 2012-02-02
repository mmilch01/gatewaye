/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nrg.xnat.gui;

import javax.swing.JComponent;

/**
 *
 * @author aditya
 */
public class ChildManagerWithUpdate extends ChildManager {
    private JComponent c;
    private AEs aes;

    public ChildManagerWithUpdate(JComponent c, AEs aes) {
        super();
        this.c = c;
        this.aes=aes;
    }

    @Override
    public void removed_last_child() {
        super.removed_last_child();
        c.setEnabled(true);
        aes.refresh();
    }

    @Override
    public void added_first_child() {
        super.added_first_child();
        c.setEnabled(false);
    }

}
