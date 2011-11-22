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
package eu.advance.logistics.flow.editor.actions;

import java.awt.Dimension;
import java.io.StringReader;

import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.WindowManager;

import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 *
 * @author TTS
 */
public class ParameterDescriptionDialog2 extends javax.swing.JDialog {
    /** */
	private static final long serialVersionUID = -6181210698532583165L;
	private String tag;
    private AdvanceBlockParameterDescription result;
    private TypeSupport[] types;

    static ParameterDescriptionDialog2 create(String tag) {
        TypeSupport[] items = TypeSupport.create();
        return items != null ? new ParameterDescriptionDialog2(tag, items) : null;
    }

    private ParameterDescriptionDialog2(String tag, TypeSupport[] types) {
        super(WindowManager.getDefault().getMainWindow(), true);
        this.tag = tag;
        this.types = types;

        initComponents();

        getRootPane().setDefaultButton(okButton);

        typeCombo.setModel(new DefaultComboBoxModel(types));
        jTabbedPane1.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                // disabled for now because it doesn't work
//                if (jTabbedPane1.getSelectedIndex() == 1) {
//                    switchToCustom();
//                } else {
//                    switchToGeneral();
//                }
            }
        });

        setSize(new Dimension(500, 300));
        setMinimumSize(getSize());
        setLocationRelativeTo(getOwner());
    }

    void setParameterDescription(AdvanceBlockParameterDescription d) {
        types[types.length - 1].advanceType = d.type;
        populateGeneral(d);
        customText.setText(convertToText(d));
    }

    void switchToCustom() {
        AdvanceBlockParameterDescription d = createFromGeneral();
        if (d != null) {
            customText.setText(convertToText(d));
        } else {
            customText.setText(String.format("<%s id='' displayname='' documentation='' type=''>\n</%s>", tag, tag));
        }
    }

    void switchToGeneral() {
        populateGeneral(createFromCustom());
    }

    private String convertToText(AdvanceBlockParameterDescription d) {
        XElement temp = new XElement(tag);
        d.save(temp);
        return temp.toString();
    }

    private AdvanceBlockParameterDescription createFromGeneral() {
        AdvanceBlockParameterDescription d;
        Object sel = typeCombo.getSelectedItem();
        TypeSupport ts = (sel instanceof TypeSupport) ? (TypeSupport) sel : null;
        if (ts != null && ts.advanceType != null) {
            d = new AdvanceBlockParameterDescription();
            d.id = getId();
            d.displayName = jTextField1.getText();
            if (d.displayName == null || d.displayName.length() == 0) {
                d.displayName = d.id;
            }
            d.documentation = docText.getText();
            d.type = ts.advanceType;
        } else {
            d = null;
        }
        return d;
    }

    private void populateGeneral(AdvanceBlockParameterDescription d) {
        if (d != null) {
            idText.setText(d.id);
            jTextField1.setText(d.displayName);
            docText.setText(d.documentation);
            typeCombo.setSelectedIndex(TypeSupport.find(types, d.type));
        }
    }

    private AdvanceBlockParameterDescription createFromCustom() {
        return createFromText(customText.getText());
    }

    private AdvanceBlockParameterDescription createFromText(String xml) {
        if (xml != null && xml.length() > 0) {
            try {
                XElement temp = XElement.parseXML(new StringReader(xml));
                AdvanceBlockParameterDescription d = new AdvanceBlockParameterDescription();
                d.load(temp);
                return d;
            } catch (Exception ignore) {
                //Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    public AdvanceBlockParameterDescription getResult() {
        return result;
    }

    private String getId() {
        String id = idText.getText();
        return (id != null) ? id.trim() : "";
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        idLabel = new javax.swing.JLabel();
        idText = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        docText = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        typeCombo = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        customText = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        okButton.setText(org.openide.util.NbBundle.getMessage(ParameterDescriptionDialog2.class, "ParameterDescriptionDialog2.okButton.text")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        jPanel1.add(okButton);

        cancelButton.setText(org.openide.util.NbBundle.getMessage(ParameterDescriptionDialog2.class, "ParameterDescriptionDialog2.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        jPanel1.add(cancelButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jPanel1, gridBagConstraints);

        jPanel2.setOpaque(false);
        jPanel2.setLayout(new java.awt.GridBagLayout());

        idLabel.setText(org.openide.util.NbBundle.getMessage(ParameterDescriptionDialog2.class, "ParameterDescriptionDialog2.idLabel.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 0, 5);
        jPanel2.add(idLabel, gridBagConstraints);

        idText.setText(org.openide.util.NbBundle.getMessage(ParameterDescriptionDialog2.class, "ParameterDescriptionDialog2.idText.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 10);
        jPanel2.add(idText, gridBagConstraints);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(ParameterDescriptionDialog2.class, "ParameterDescriptionDialog2.jLabel1.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 5);
        jPanel2.add(jLabel1, gridBagConstraints);

        jTextField1.setText(org.openide.util.NbBundle.getMessage(ParameterDescriptionDialog2.class, "ParameterDescriptionDialog2.jTextField1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        jPanel2.add(jTextField1, gridBagConstraints);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(ParameterDescriptionDialog2.class, "ParameterDescriptionDialog2.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        jPanel2.add(jLabel2, gridBagConstraints);

        docText.setText(org.openide.util.NbBundle.getMessage(ParameterDescriptionDialog2.class, "ParameterDescriptionDialog2.docText.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        jPanel2.add(docText, gridBagConstraints);

        jLabel3.setText(org.openide.util.NbBundle.getMessage(ParameterDescriptionDialog2.class, "ParameterDescriptionDialog2.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 5);
        jPanel2.add(jLabel3, gridBagConstraints);

        typeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 10, 10);
        jPanel2.add(typeCombo, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ParameterDescriptionDialog2.class, "ParameterDescriptionDialog2.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setOpaque(false);
        jPanel3.setLayout(new java.awt.BorderLayout());

        customText.setColumns(40);
        customText.setRows(5);
        jScrollPane1.setViewportView(customText);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ParameterDescriptionDialog2.class, "ParameterDescriptionDialog2.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        getContentPane().add(jTabbedPane1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        AdvanceBlockParameterDescription d;
        if (jTabbedPane1.getSelectedIndex() == 0) {
            d = createFromGeneral();
        } else {
            d = createFromCustom();
        }
        if (d != null && d.id.length() > 0) {
            result = d;
            dispose();
        } else {
            NotifyDescriptor nd = new NotifyDescriptor.Message("Invalid parameters");
            DialogDisplayer.getDefault().notify(nd);
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextArea customText;
    private javax.swing.JTextField docText;
    private javax.swing.JLabel idLabel;
    private javax.swing.JTextField idText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox typeCombo;
    // End of variables declaration//GEN-END:variables

}
