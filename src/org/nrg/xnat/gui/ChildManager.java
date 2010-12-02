package org.nrg.xnat.gui;

import java.util.ListIterator;
import java.util.Vector;

import javax.swing.JFrame;

/**
 *
 * @author Aditya Siram
 */
public class ChildManager implements ChildManagementInterface {
    private Vector<ChildManager> parents = new Vector<ChildManager>();
    private Vector<JFrame> children = new Vector<JFrame>();
    private int child_count;

    public ChildManager (){
        this.child_count = 0;
    };

    public void removed_last_child () {
        ListIterator p = parents.listIterator();
        while (p.hasNext()) {
            ChildManager _c = (ChildManager) p.next();
            _c.remove_from_child_count();
        }

    }

    public void dispose_children () {
        ListIterator c = children.listIterator();
        while (c.hasNext()) {
            JFrame j = (JFrame) c.next();
            if (j != null) {
                j.dispose();
            }
        }
    }

    public void add_parent(ChildManager c) {
        parents.add(c);
    }

    public void added_first_child() {
        ListIterator l = parents.listIterator();
        while (l.hasNext()) {
            ChildManager _c = (ChildManager) l.next();
            _c.add_to_child_count();
        }
    }

    public void add_to_child_count() {
        this.child_count ++;
        this.added_a_child();
    }

    public void remove_from_child_count() {
        this.child_count--;
        this.removed_a_child();
    }

    public void added_a_child() {
        if (child_count == 1) {
            this.added_first_child();
        }
    }

    public void removed_a_child() {
        if (child_count == 0) {
            this.removed_last_child();
        }
    }
}
