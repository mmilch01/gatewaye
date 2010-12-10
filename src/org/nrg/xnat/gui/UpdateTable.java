package org.nrg.xnat.gui;

import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.nrg.xnat.env.GatewayEnvironment;
import org.nrg.xnat.env.NetworkDevice;

/**
 * Abstracts over a table view of the current network devices. Network devices
 * can be added, deleted and changes propagated to interested components.
 * @author Aditya Siram
 */
public abstract class UpdateTable implements UpdateableComponent {
    // Table that represents the information in this class
    protected JTable table;
    // the current Gateway configuration
    protected GatewayEnvironment env;
    // allows a row to be referenced using a unique column rather than a number
    // like the XNAT server name for instance
    protected Hashtable<Integer,String> row_name;

    // objects that need to be kept in sync with changes to this table. The
    // main GUI window, for instance, has a label showing the current default
    // XNAT server and user. If a new default XNAT server is set here that
    // change needs to be propogated to the label in the main window.
    protected Vector<UpdateableComponent> deps;

    //disallow editing of this table
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

    public UpdateTable (JTable table, GatewayEnvironment env, Vector<UpdateableComponent> deps) {
        this.table = table;
        this.table.setModel(table_model);
        this.env = env;
        row_name = new Hashtable<Integer, String>();
        this.deps = deps;
    }

    // the name of a row is it's unique column value. A name of row representing an XNAT server
    // for instance it the server's name
    public String get_selected_name() {
        String s = null;
        int t = this.table.getSelectedRow();
        if (t != -1) {
            s = row_name.get(t);
        }
        return s;
    }

    // clear the table so it can be refilled
    public void refresh() {
        this.table_model.getDataVector().removeAllElements();
        this.row_name.clear();
        fill_table();
        refresh_deps();
    }

    // propogate changes to dependent components
    public void refresh_deps () {
        for (UpdateableComponent c : deps) {
            c.refresh();
        }
    }

    public abstract void initialize ();
    // populate the table
    public abstract void fill_table();
    // delete a row
    public abstract void delete_current_selection();
    // return the network device represented by the current selection
    public abstract NetworkDevice get_selected_device();
}
