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

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.swing.etable.ETable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

/**
 * 
 * @author TTS
 */
public class LoginDialog extends javax.swing.JDialog implements ExplorerManager.Provider {

    private ExplorerManager explorerManager = new ExplorerManager();
    private LoginInfoTableModel model;
    private final ETable table = new ETable();

    public LoginDialog() {
        super(WindowManager.getDefault().getMainWindow(), true);
        initComponents();

        Dimension dim = new Dimension(600, 400);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setSize(dim);

        EngineController.getInstance().loadLoginInfo();

        model = new LoginInfoTableModel(EngineController.getInstance().getLoginInfo());
        model.addTableModelListener(new LabelCounterUpdater(recordsCounter, model));

        table.setFillsViewportHeight(true);
        table.setModel(model);
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    int index = Commons.syncTableSelection(table, e);
                    if (index != -1) {
                        doLogin(model.getLoginInfo(index));
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int index = Commons.syncTableSelection(table, e);
                if (index != -1 && e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                    JPopupMenu popup = new JPopupMenu();
                    popup.add(createEditAction(index));
                    popup.add(createClearAction(index));
                    popup.add(new JSeparator());
                    popup.add(createDeleteAction(index));
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                int index = table.getSelectedRow();
                boolean en = index != -1;
                loginButton.setEnabled(en);
                loginRadio.setEnabled(en);
                usernameLabel.setEnabled(en);
                usernameText.setEnabled(en);
                passwordLabel.setEnabled(en);
                passwordText.setEnabled(en);
                remeberCheck.setEnabled(en);
                syncText((index != -1) ? model.getLoginInfo(index) : null);
            }
        });
        if (table.getRowCount() != 0) {
            table.setRowSelectionInterval(0, 0);
        } else {
            table.clearSelection();
        }

        scrollpane.setViewportView(table);
        getRootPane().setDefaultButton(loginButton);
    }

    private void syncText(LoginInfo info) {
        if (info != null) {
            usernameText.setText(info.username);
            passwordText.setText(info.password);
            serverCertPath.setText(info.serverCert);
        } else {
            usernameText.setText("");
            passwordText.setText("");
            serverCertPath.setText("");
        }
    }

    private Action createEditAction(int index) {
        final LoginInfo target = model.getLoginInfo(index);
        return new AbstractAction("Edit...") {

            @Override
            public void actionPerformed(ActionEvent e) {
                String message = "URL:";
                String title = "Edit engine address";
                String a = JOptionPane.showInputDialog(LoginDialog.this, title, target.address);
                if (a != null) {
                    a = checkAdress(a);
                    if (a != null) {
                        target.address = a;
                    }
                }

//                NotifyDescriptor nd = new NotifyDescriptor.InputLine(message, title);
//                nd.setValue(target.address);
//                Object r = DialogDisplayer.getDefault().notify(nd);
//                if (r == NotifyDescriptor.OK_OPTION && nd.getValue() != null) {
//                    String address = checkAdress(nd.getValue().toString());
//                    if (address != null) {
//                        target.address = address;
//                    }
//                }
            }
        };
    }

    private Action createClearAction(int index) {
        final LoginInfo target = model.getLoginInfo(index);
        return new AbstractAction("Clear saved credentials...") {

            @Override
            public void actionPerformed(ActionEvent e) {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation("<html>Are you sure you want to remove the saved credentials<br>from the account information?</html>");
                Object r = DialogDisplayer.getDefault().notify(nd);
                if (r == NotifyDescriptor.YES_OPTION) {
                    target.username = null;
                    target.password = null;
                    syncText(target);
                }
            }
        };
    }

    private Action createDeleteAction(final int index) {
        return new AbstractAction("Delete...") {

            @Override
            public void actionPerformed(ActionEvent e) {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation("Delete?");
                Object r = DialogDisplayer.getDefault().notify(nd);
                if (r == NotifyDescriptor.YES_OPTION) {
                    model.remove(index);
                }
            }
        };
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
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

        recordsPanel = new javax.swing.JPanel();
        recordLabel = new javax.swing.JLabel();
        recordsCounter = new javax.swing.JLabel();
        upPanel = new javax.swing.JPanel();
        usernameLabel = new javax.swing.JLabel();
        usernameText = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordText = new javax.swing.JTextField();
        buttonsPanel = new javax.swing.JPanel();
        loginButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        addressText = new javax.swing.JTextField();
        addressLabel = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        scrollpane = new javax.swing.JScrollPane();
        loginRadio = new javax.swing.JRadioButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        remeberCheck = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        serverCertPath = new javax.swing.JTextField();
        browseCert = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(LoginDialog.class, "LoginDialog.title")); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        recordLabel.setText(org.openide.util.NbBundle.getMessage(LoginDialog.class, "LoginDialog.recordLabel.text")); // NOI18N
        recordsPanel.add(recordLabel);

        recordsCounter.setFont(recordsCounter.getFont().deriveFont(recordsCounter.getFont().getStyle() | java.awt.Font.BOLD));
        recordsCounter.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        recordsCounter.setText("3"); // NOI18N
        recordsPanel.add(recordsCounter);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        getContentPane().add(recordsPanel, gridBagConstraints);

        upPanel.setLayout(new java.awt.GridBagLayout());

        usernameLabel.setText(org.openide.util.NbBundle.getMessage(LoginDialog.class, "LoginDialog.usernameLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        upPanel.add(usernameLabel, gridBagConstraints);

        usernameText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernameTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 0.5;
        upPanel.add(usernameText, gridBagConstraints);

        passwordLabel.setText(org.openide.util.NbBundle.getMessage(LoginDialog.class, "LoginDialog.passwordLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 5);
        upPanel.add(passwordLabel, gridBagConstraints);

        passwordText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 0.5;
        upPanel.add(passwordText, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 35, 0, 10);
        getContentPane().add(upPanel, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        loginButton.setText(org.openide.util.NbBundle.getBundle(LoginDialog.class).getString("LoginDialog.loginButton.text")); // NOI18N
        loginButton.setEnabled(false);
        loginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(loginButton);

        cancelButton.setText(org.openide.util.NbBundle.getBundle(LoginDialog.class).getString("LoginDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(cancelButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 10);
        getContentPane().add(buttonsPanel, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        addressText.setText(org.openide.util.NbBundle.getMessage(LoginDialog.class, "LoginDialog.addressText.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel1.add(addressText, gridBagConstraints);

        addressLabel.setText(org.openide.util.NbBundle.getMessage(LoginDialog.class, "LoginDialog.addressLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanel1.add(addressLabel, gridBagConstraints);

        addButton.setText(org.openide.util.NbBundle.getMessage(LoginDialog.class, "LoginDialog.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        jPanel1.add(addButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        getContentPane().add(scrollpane, gridBagConstraints);

        loginRadio.setSelected(true);
        loginRadio.setText(org.openide.util.NbBundle.getMessage(LoginDialog.class, "LoginDialog.loginRadio.text")); // NOI18N
        loginRadio.setActionCommand(org.openide.util.NbBundle.getBundle(LoginDialog.class).getString("LoginDialog.loginRadio.actionCommand")); // NOI18N
        loginRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginRadioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        getContentPane().add(loginRadio, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jSeparator1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jSeparator3, gridBagConstraints);

        remeberCheck.setText(org.openide.util.NbBundle.getMessage(LoginDialog.class, "LoginDialog.remeberCheck.text")); // NOI18N
        remeberCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remeberCheckActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 20);
        getContentPane().add(remeberCheck, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(org.openide.util.NbBundle.getMessage(LoginDialog.class, "LoginDialog.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        jPanel2.add(jLabel1, gridBagConstraints);

        serverCertPath.setText(org.openide.util.NbBundle.getMessage(LoginDialog.class, "LoginDialog.serverCertPath.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(serverCertPath, gridBagConstraints);

        browseCert.setText(org.openide.util.NbBundle.getMessage(LoginDialog.class, "LoginDialog.browseCert.text")); // NOI18N
        browseCert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseCertActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        jPanel2.add(browseCert, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel2, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        LoginInfo info = new LoginInfo();
        info.address = checkAdress(addressText.getText());
        if (info.address != null) {
            model.add(info);
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void usernameTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameTextActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_usernameTextActionPerformed

    private void passwordTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordTextActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_passwordTextActionPerformed

    private void loginRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginRadioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_loginRadioActionPerformed

    private void remeberCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remeberCheckActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_remeberCheckActionPerformed

    private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
        int index = table.getSelectedRow();
        if (index != -1) {
            doLogin(model.getLoginInfo(index));
        }

    }//GEN-LAST:event_loginButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void browseCertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseCertActionPerformed
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Certificates (*.CER)", "cer", "cert", "crt"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            serverCertPath.setText(fc.getSelectedFile().toString());
        }
    }//GEN-LAST:event_browseCertActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JTextField addressText;
    private javax.swing.JButton browseCert;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton loginButton;
    private javax.swing.JRadioButton loginRadio;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField passwordText;
    private javax.swing.JLabel recordLabel;
    private javax.swing.JLabel recordsCounter;
    private javax.swing.JPanel recordsPanel;
    private javax.swing.JCheckBox remeberCheck;
    private javax.swing.JScrollPane scrollpane;
    private javax.swing.JTextField serverCertPath;
    private javax.swing.JPanel upPanel;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JTextField usernameText;
    // End of variables declaration//GEN-END:variables

    private void doLogin(final LoginInfo info) {
        final String address = info.address;
        final String username = usernameText.getText();
        final char[] password = passwordText.getText().toCharArray();
        final boolean saveCredentials = remeberCheck.isSelected();
        final LoginProgressDialog dlg = new LoginProgressDialog();
        final String serverCert = serverCertPath.getText();

        new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                EngineController ec = EngineController.getInstance();
                if (ec.login(address, username, password, serverCert)) {
                    // successfully logged in -> save last login info and changes
                    if (saveCredentials) {
                        info.username = username;
                        info.password = new String(password);
                    }
                    info.lastLogin = new Date();
                    ec.setLoginInfo(model.getData());
                    ec.saveLoginInfo();
                }
                return ec.getEngine();
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    EngineController ec = EngineController.getInstance();
                    dlg.dispose();
                    if (ec.getEngine() != null) {
                        StatusDisplayer.getDefault().setStatusText("Logged " + ec.getEngineAddress() + " version " + ec.getEngineVersion());
                        dispose();
                    }
                    ec.getEventBus().post(ec);
                }
            }
        }.execute();
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                dlg.setVisible(true);

            }
        });

    }

    private static String checkAdress(String text) {
        try {
            URL url = new URL(text);
            return url.toString();
        } catch (MalformedURLException ex) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("Please enter a valid URL.", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
