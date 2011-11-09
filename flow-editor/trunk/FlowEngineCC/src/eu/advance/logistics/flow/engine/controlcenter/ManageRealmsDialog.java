/*
 * Copyright 2010-2012 The Advance EU 7th Framework project consortium
 *
 * This file is part of Advance.
 *
 * Advance is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Advance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Advance.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
package eu.advance.logistics.flow.engine.controlcenter;

import eu.advance.logistics.flow.engine.api.core.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.ds.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.ds.AdvanceRealmStatus;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUser;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRealmRights;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author TTS
 */
public class ManageRealmsDialog extends javax.swing.JDialog {

    private AdvanceEngineControl engine;
    private RealmTableModel tableModel = new RealmTableModel(true);
    private Icon[] icons = new Icon[3];

    public ManageRealmsDialog(AdvanceEngineControl engine) {
        super(WindowManager.getDefault().getMainWindow(), true);
        this.engine = engine;
        initComponents();

        Dimension dim = new Dimension(600, 400);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setSize(dim);

        engineHeaderPanel1.update();

        icons[0] = ImageUtilities.loadImageIcon("eu/advance/logistics/flow/engine/controlcenter/runProject.png", false);
        icons[1] = ImageUtilities.loadImageIcon("eu/advance/logistics/flow/engine/controlcenter/stop.png", false);
        icons[2] = ImageUtilities.loadImageIcon("eu/advance/logistics/flow/engine/controlcenter/delete.gif", false);

        TableColumn tc;
        tc = realmsTable.getColumnModel().getColumn(4);
//        tc.setCellEditor(new PlayButton());
        tc.setCellRenderer(new PlayButton());
        tc.setMaxWidth(50);
        tc.setPreferredWidth(50);
        tc.setWidth(50);
        tc = realmsTable.getColumnModel().getColumn(5);
//        tc.setCellEditor(new RemoveButton());
        tc.setCellRenderer(new RemoveButton());
        tc.setMaxWidth(50);
        tc.setPreferredWidth(50);
        tc.setWidth(50);

        realmsTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                    int c = realmsTable.columnAtPoint(e.getPoint());
                    if (c == 4 || c == 5) {
                        int r = realmsTable.rowAtPoint(e.getPoint());
                        Object value = realmsTable.getValueAt(r, c);
                        if (c == 4) {
                            play(value, r, c);
                        } else /*if (c == 5)*/ {
                            delete(value, r, c);
                        }
                    }
                }
            }
        });

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {
                tableModel.refresh();
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        refreshButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        realmText = new javax.swing.JTextField();
        addButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        realmsTable = new javax.swing.JTable();
        jSeparator1 = new javax.swing.JSeparator();
        engineHeaderPanel1 = new eu.advance.logistics.flow.engine.controlcenter.EngineHeaderPanel();
        jSeparator2 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(ManageRealmsDialog.class, "ManageRealmsDialog.title")); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        refreshButton.setText(org.openide.util.NbBundle.getMessage(ManageRealmsDialog.class, "ManageRealmsDialog.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        jPanel1.add(refreshButton);

        cancelButton.setText(org.openide.util.NbBundle.getMessage(ManageRealmsDialog.class, "ManageRealmsDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        jPanel1.add(cancelButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        getContentPane().add(jPanel1, gridBagConstraints);

        jLabel5.setText(org.openide.util.NbBundle.getMessage(ManageRealmsDialog.class, "ManageRealmsDialog.jLabel5.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 5);
        getContentPane().add(jLabel5, gridBagConstraints);

        realmText.setText(org.openide.util.NbBundle.getMessage(ManageRealmsDialog.class, "ManageRealmsDialog.realmText.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(realmText, gridBagConstraints);

        addButton.setText(org.openide.util.NbBundle.getMessage(ManageRealmsDialog.class, "ManageRealmsDialog.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 10);
        getContentPane().add(addButton, gridBagConstraints);

        realmsTable.setModel(tableModel);
        jScrollPane1.setViewportView(realmsTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        getContentPane().add(jScrollPane1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jSeparator1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        getContentPane().add(engineHeaderPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jSeparator2, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        tableModel.refresh();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        String name = realmText.getText().trim();
        if (name.length() == 0) {
            return;
        }
        try {
            AdvanceUser user = engine.getUser();
            engine.datastore().createRealm(name, user.name);
            tableModel.refresh();
        } catch (AdvanceControlException ex) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } catch (IOException ex) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }//GEN-LAST:event_addButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton cancelButton;
    private eu.advance.logistics.flow.engine.controlcenter.EngineHeaderPanel engineHeaderPanel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField realmText;
    private javax.swing.JTable realmsTable;
    private javax.swing.JButton refreshButton;
    // End of variables declaration//GEN-END:variables

    private void play(Object value, int row, int column) {
        try {
            AdvanceUser user = engine.getUser();
            AdvanceRealm realm = (AdvanceRealm) value;
            if (realm.status == AdvanceRealmStatus.RUNNING) {
                Commons.fixRights(engine, realm, AdvanceUserRealmRights.STOP);
                engine.stopRealm(realm.name, user.name);
                realm.status = AdvanceRealmStatus.STOPPING;
            } else {
                Commons.fixRights(engine, realm, AdvanceUserRealmRights.START);
                engine.startRealm(realm.name, user.name);
                realm.status = AdvanceRealmStatus.STARTING;
            }

            // immediate feedback (may not be accurate)
            tableModel.update(row, column);

            // request a full update from the engine
            tableModel.refresh();
        } catch (AdvanceControlException ex) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } catch (IOException ex) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    private void delete(Object value, int row, int column) {
        AdvanceRealm r = (AdvanceRealm) value;
        try {
            engine.datastore().deleteRealm(r.name);

            // immediate feedback (may not be accurate)
            tableModel.remove(row);

            // request a full update from the engine
            tableModel.refresh();
        } catch (AdvanceControlException ex) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } catch (IOException ex) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }

    }

    private class PlayButton extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER);
            setText("");
            // ToDo: better status detection and report
            AdvanceRealm r = (AdvanceRealm) value;
            setIcon(r.status == AdvanceRealmStatus.RUNNING ? icons[1] : icons[0]);
            return this;
        }
    }

    private class RemoveButton extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER);
            setText("");
            setIcon(icons[2]);
            return this;
        }
    }
}
