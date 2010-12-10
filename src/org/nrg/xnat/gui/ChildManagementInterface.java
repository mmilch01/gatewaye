package org.nrg.xnat.gui;

/**
 * Helps maintain a collection of objects that represent children of some
 * parent objects.
 * @author Aditya Siram
 */
public interface ChildManagementInterface {
    public void add_to_child_count();
    public void remove_from_child_count();

    // These next two methods should not be called outside the class implementing
    // this interface
    public void removed_last_child();
    public void added_first_child();
}
