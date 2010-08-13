package org.nrg.xnat.gui;

import java.io.IOException;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import org.nrg.xnat.env.DefaultDeviceDeleteException;
import org.nrg.xnat.env.GatewayEnvironment;
import org.nrg.xnat.env.IncomingAE;
import org.nrg.xnat.env.IncompleteEntryException;
import org.nrg.xnat.env.NetworkDevice;
import org.nrg.xnat.util.DuplicateEntryException;
import org.nrg.xnat.util.NonExistentEntryException;

/**
 *
 * @author Aditya Siram
 */
public class UpdateAETable extends UpdateTable {
    public UpdateAETable (JTable table, GatewayEnvironment env) {
        super(table,env);
    }

    public void initialize () {
        this.table_model.addColumn("Name");
        this.table_model.addColumn("AE Title");
        this.table.setModel(table_model);
        GUIUtils.make_single_row_selection(this.table);
        fill_table();
    }

    public void fill_table () {
        int i = 0;
        for (IncomingAE _i : this.env.get_all_incomingaes()) {
            Vector v = new Vector();
            String n = _i.getName();
            v.add(n);
            v.add(_i.getCalledAETitle());
            this.row_name.put(i, n);
            this.table_model.addRow(new Vector(v));
            i++;
        }
    }

    public boolean add_ae(String name, String hostname, String callingAETitle, String _port) {
        IncomingAE s = new IncomingAE(name);
        s.setHostname(hostname);
        s.setCalledAETitle(callingAETitle);
        try {
            int port = Integer.parseInt(_port);
            s.setPort(port);
            env.add_incomingae(s);
            refresh();
        }
        catch (NumberFormatException e ) {
            GUIUtils.warn(e.toString() + " Could not read port.", "Add error");
        }
        catch (DuplicateEntryException e) {
            GUIUtils.warn(e.toString(), "Add error");
            return false;
        }
        catch (IncompleteEntryException e) {
            GUIUtils.warn(e.toString(), "Add error");
            return false;
        }
        catch (IOException e) {
            GUIUtils.application_stop(e.toString(), "Add error");
        }
        return true;
    }

    public boolean update_ae (String name,String hostname, String callingAETitle, String port) {
        IncomingAE s = new IncomingAE(name);
        s.setHostname(hostname);
        s.setCalledAETitle(callingAETitle);
        try {
            s.setPort(Integer.parseInt(port));
            env.update_incoming_ae(s);
            refresh();
        }
        catch (NumberFormatException e) {
            GUIUtils.warn(e.toString() + "Could not read the port number", "Update error");
            return false;
        }
        catch (DuplicateEntryException e) {
            GUIUtils.warn(e.toString(), "Update error");
            return false;
        }
        catch (IncompleteEntryException e) {
            GUIUtils.warn(e.toString(), "Update error");
            return false;
        }
        catch (NonExistentEntryException e) {
            GUIUtils.warn(e.toString(), "Update error");
            return false;
        }
        catch (IOException e) {
            GUIUtils.warn(e.toString(), "Update error");
        }
        return true;
    }

    public boolean remove_ae (String name) {
        IncomingAE s = env.get_incomingae(name);
        if (s != null) {
          try {
             env.remove_incomingae(s);
             refresh();
          }
          catch (NonExistentEntryException e) {
              GUIUtils.warn(e.toString(), "Remove error");
              return false;
          }
          catch (DefaultDeviceDeleteException e) {
              GUIUtils.warn(e.toString(), "Remove error");
              return false;
          }
          catch (IncompleteEntryException e) {
              GUIUtils.warn(e.toString(), "Remove error");
              return false;
          }
          catch (IOException e) {
              GUIUtils.application_stop(e.toString(), "Remove error");
          }
        }
        return true;
    }

    @Override
    public void delete_current_selection () {
        String ae_name = get_selected_name();
        if (ae_name != null) {
            int delete_confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete " + ae_name + "?", "Delete Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (delete_confirm == JOptionPane.YES_OPTION) {
                remove_ae(ae_name);
            }
        }
    }

    @Override
    public NetworkDevice get_selected_device () {
        if (get_selected_name() != null) return env.get_incomingae(get_selected_name());
        else return null;
    }
}
