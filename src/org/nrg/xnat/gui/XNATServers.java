/*
 * XNATServers.java
 *
 * Created on May 6, 2010, 12:56:02 PM
 */

package org.nrg.xnat.gui;

import java.util.ListIterator;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;

import org.nrg.xnat.env.GatewayEnvironment;


/**
 *
 * @author Aditya Siram
 */
public class XNATServers extends javax.swing.JFrame {
    private final GatewayEnvironment env;
    private final ChildManager c;
    private final ChildManager add_window;
    private final ChildManager edit_window;
    private final UpdateServerTable table_controller;
    private final Vector<RefreshableComponent> v;
    private final Vector<JFrame> children;
    private boolean changed = false;

    private DefaultTableModel table_model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
        };
    };
    /** Creates new form XNATServers */
    public XNATServers(GatewayEnvironment env, ChildManager c, Vector<RefreshableComponent> v) {
        this.env = env;
        this.v = v;
        children = new Vector<JFrame>();
        initComponents();
        this.c = c;
        this.table_controller = new UpdateServerTable(server_properties_table, env, v);
        add_window = new DefaultChildManager(this.add_server_button);
        edit_window = new DefaultChildManager(this.edit_server_button);
    }

    public XNATServers(GatewayEnvironment env, ChildManager c) {
        this(env, c, new Vector<RefreshableComponent>());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        server_properties_container = new javax.swing.JScrollPane();
        server_properties_table = new javax.swing.JTable();
        edit_server_button = new javax.swing.JButton();
        delete_server_button = new javax.swing.JButton();
        add_server_button = new javax.swing.JButton();
        close_button = new javax.swing.JButton();
        make_default_button = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Edit XNAT Servers");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        server_properties_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null}
            },
            new String [] {
                "Name", "URL"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        server_properties_table.setColumnSelectionAllowed(true);
        server_properties_table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        server_properties_table.getTableHeader().setReorderingAllowed(false);
        server_properties_container.setViewportView(server_properties_table);
        server_properties_table.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        edit_server_button.setText("Edit");
        edit_server_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                edit_server_buttonMousePressed(evt);
            }
        });

        delete_server_button.setText("Delete");
        delete_server_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                delete_server_buttonMousePressed(evt);
            }
        });

        add_server_button.setText("Add");
        add_server_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                add_server_buttonMousePressed(evt);
            }
        });

        close_button.setText("Close");
        close_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                close_buttonMousePressed(evt);
            }
        });

        make_default_button.setText("Make Default");
        make_default_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                make_default_buttonMousePressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(server_properties_container, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(edit_server_button, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                    .addComponent(delete_server_button, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                    .addComponent(add_server_button, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                    .addComponent(make_default_button, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                    .addComponent(close_button, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(server_properties_container, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(add_server_button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(delete_server_button, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(edit_server_button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(make_default_button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                        .addComponent(close_button)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        this.table_controller.initialize();
    }//GEN-LAST:event_formWindowOpened

    private void close_buttonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_close_buttonMousePressed
        this.dispose();
    }//GEN-LAST:event_close_buttonMousePressed

    private void add_server_buttonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_add_server_buttonMousePressed
        if (this.add_server_button.isEnabled()) {
            this.add_window.add_to_child_count();
            final AddXNATServer add = new AddXNATServer(add_window, table_controller);
            this.children.add(add);
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    add.setVisible(true);
                }
            });
        }
    }//GEN-LAST:event_add_server_buttonMousePressed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        c.remove_from_child_count();
        ListIterator l = this.children.listIterator();
        while(l.hasNext()) {
            JFrame j = (JFrame) l.next();
            if (j != null) {
                j.dispose();
            }
        }
    }//GEN-LAST:event_formWindowClosed

    private void delete_server_buttonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delete_server_buttonMousePressed
        this.table_controller.delete_current_selection();
    }//GEN-LAST:event_delete_server_buttonMousePressed

    private void edit_server_buttonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_edit_server_buttonMousePressed
        if (this.edit_server_button.isEnabled()) {
            if (table_controller.get_selected_name() != null) {
                final EditXNATServer e = new EditXNATServer(edit_window, table_controller);
                this.children.add(e);
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        e.setVisible(true);
                    }
                });
            }
        }
    }//GEN-LAST:event_edit_server_buttonMousePressed

    private void make_default_buttonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_make_default_buttonMousePressed
        this.table_controller.set_default_server();
    }//GEN-LAST:event_make_default_buttonMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton add_server_button;
    private javax.swing.JButton close_button;
    private javax.swing.JButton delete_server_button;
    private javax.swing.JButton edit_server_button;
    private javax.swing.JButton make_default_button;
    private javax.swing.JScrollPane server_properties_container;
    private javax.swing.JTable server_properties_table;
    // End of variables declaration//GEN-END:variables

}
