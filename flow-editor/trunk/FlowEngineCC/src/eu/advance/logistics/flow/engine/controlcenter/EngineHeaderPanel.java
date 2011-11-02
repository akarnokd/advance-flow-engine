/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * EngineHeaderPanel.java
 *
 * Created on 1-nov-2011, 17.20.39
 */
package eu.advance.logistics.flow.engine.controlcenter;

/**
 *
 * @author dalmaso
 */
public class EngineHeaderPanel extends javax.swing.JPanel {

    /** Creates new form EngineHeaderPanel */
    public EngineHeaderPanel() {
        initComponents();
    }

    void update() {
        update(EngineController.getInstance());
    }

    void update(EngineController ec) {
        engineAddress.setText(ec.getEngineAddress());
        engineVersion.setText(ec.getEngineVersion());
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

        jLabel1 = new javax.swing.JLabel();
        engineAddress = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        engineVersion = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(org.openide.util.NbBundle.getMessage(EngineHeaderPanel.class, "EngineHeaderPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(jLabel1, gridBagConstraints);

        engineAddress.setFont(engineAddress.getFont().deriveFont(engineAddress.getFont().getStyle() | java.awt.Font.BOLD));
        engineAddress.setText("https://localhost:8081/ADVANCE_DEV"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        add(engineAddress, gridBagConstraints);

        jLabel3.setText(org.openide.util.NbBundle.getMessage(EngineHeaderPanel.class, "EngineHeaderPanel.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 5);
        add(jLabel3, gridBagConstraints);

        engineVersion.setFont(engineVersion.getFont().deriveFont(engineVersion.getFont().getStyle() | java.awt.Font.BOLD));
        engineVersion.setText("0.00.001"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        add(engineVersion, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel engineAddress;
    private javax.swing.JLabel engineVersion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    // End of variables declaration//GEN-END:variables
}
