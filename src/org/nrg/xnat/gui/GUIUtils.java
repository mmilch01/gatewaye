package org.nrg.xnat.gui;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/**
 *
 * @author Aditya Siram
 */
public class GUIUtils {
    public static void make_single_row_selection(JTable table) {
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(true);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public static void application_stop (String error, String title) {
        error = "The application will now shut down. The following fatal error has occured : \n" + error;
        JOptionPane.showMessageDialog(null, error, title, JOptionPane.WARNING_MESSAGE);
        System.exit(1);
    }

    public static void warn (String error, String title) {
        JOptionPane.showMessageDialog(null, error, title, JOptionPane.WARNING_MESSAGE);
    }

 }
