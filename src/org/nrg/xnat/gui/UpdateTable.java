package org.nrg.xnat.gui;

import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.nrg.xnat.env.GatewayEnvironment;
import org.nrg.xnat.env.NetworkDevice;

/**
 *
 * @author Aditya Siram
 */
public abstract class UpdateTable extends RefreshableComponent {
    protected JTable table;
    protected GatewayEnvironment env;
    protected Hashtable<Integer,String> row_name;
    protected Vector<RefreshableComponent> deps;
    protected DefaultTableModel table_model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
        };
    };

    public UpdateTable (JTable table, GatewayEnvironment env) {
        this.table = table;
        this.table.setModel(table_model);
        this.env = env;
        row_name = new Hashtable<Integer, String>();
        this.deps = new Vector();
    }

    public UpdateTable (JTable table, GatewayEnvironment env, Vector<RefreshableComponent> deps) {
        this.table = table;
        this.table.setModel(table_model);
        this.env = env;
        row_name = new Hashtable<Integer, String>();
        this.deps = deps;
    }

    public String get_selected_name() {
        String s = null;
        int t = this.table.getSelectedRow();
        if (t != -1) {
            s = row_name.get(t);
        }
        return s;
    }

    public void refresh() {
        this.table_model.getDataVector().removeAllElements();
        this.row_name.clear();
        fill_table();
        refresh_deps();
    }

    public void refresh_deps () {
        for (RefreshableComponent c : deps) {
            c.refresh();
        }
    }

    public abstract void initialize ();
    public abstract void fill_table();
    public abstract void delete_current_selection();
    public abstract NetworkDevice get_selected_device();
}
