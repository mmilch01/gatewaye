package org.nrg.xnat.gui;

import java.io.IOException;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import org.nrg.xnat.env.DefaultDeviceDeleteException;
import org.nrg.xnat.env.GatewayEnvironment;
import org.nrg.xnat.env.IncompleteEntryException;
import org.nrg.xnat.env.NetworkDevice;
import org.nrg.xnat.env.XNATServer;
import org.nrg.xnat.util.DuplicateEntryException;
import org.nrg.xnat.util.NonExistentEntryException;

/**
 *
 * @author Aditya Siram
 */
public class UpdateServerTable extends UpdateTable {
    public UpdateServerTable (JTable table, GatewayEnvironment env) {
        super(table,env);
    }
    public UpdateServerTable (JTable table, GatewayEnvironment env, Vector<RefreshableComponent> v) {
        super(table,env,v);
    }

    public void initialize () {
        this.table_model.addColumn("Name");
        this.table_model.addColumn("URL");
        GUIUtils.make_single_row_selection(this.table);
        fill_table();
    }

    public void fill_table () {
        int i = 0;
        for (XNATServer _s : this.env.get_all_servers()) {
            Vector v = new Vector();
            String name = _s.getName();
            if (_s.is_default()) {
                 name += "(default)";
            }
            v.add(name);
            v.add(_s.getHostname());
            this.table_model.addRow(new Vector(v));
            this.row_name.put(i, _s.getName());
            i++;
        }
    }

    public boolean add_server (String name, String hostname, String username, String password) {
        XNATServer s = new XNATServer(name);
        s.setHostname(hostname);
        s.setUsername(username);
        s.setPassword(password);
        try {
            env.add_server(s);
            refresh();
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

    public boolean update_server (String name,String hostname, String username, String password) {
        XNATServer s = env.get_server(name);
        s.setHostname(hostname);
        s.setUsername(username);
        s.setPassword(password);
        try {
            env.update_server(s);
            refresh();
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

    

    public boolean remove_server (String name) {
        XNATServer s = env.get_server(name);
        if (s != null) {
          try {
             env.remove_server(s);
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

    public void delete_current_selection () {
        String server_name = get_selected_name();
        if (server_name != null) {
            int delete_confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete " + server_name + "?", "Delete Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (delete_confirm == JOptionPane.YES_OPTION) {
                remove_server(server_name);
            }

        }
    }
    
    @Override
    public NetworkDevice get_selected_device () {
        if (get_selected_name() != null)  return env.get_server(get_selected_name());
        else return null;
    }

    public void set_default_server () {
        String server_name = get_selected_name();
        if (server_name != null) {
            try {
                env.set_default_server(server_name);
                refresh();
            }
            catch (NonExistentEntryException e){
                GUIUtils.warn(e.toString(), "Update error");
            }
            catch (IOException e) {
                GUIUtils.application_stop(e.toString(), "Update error");
            }
        }
    }
}
