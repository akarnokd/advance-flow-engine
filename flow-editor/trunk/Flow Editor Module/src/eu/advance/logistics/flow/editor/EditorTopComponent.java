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
package eu.advance.logistics.flow.editor;

import java.beans.PropertyVetoException;
import java.io.IOException;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 * Visual flow editor.
 * 
 * @author TTS
 */
@TopComponent.Description(preferredID = "EditorTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
public final class EditorTopComponent extends TopComponent {

    private final static String ICON_PATH = "eu/advance/logistics/flow/editor/palette/images/block.png";
    private JComponent viewportView;
    private FlowDiagramController flowDiagramController;

    public EditorTopComponent(FlowDiagramController controller) {
        this.flowDiagramController = controller;

        initComponents();
        //setName(NbBundle.getMessage(EditorTopComponent.class, "CTL_EditorTopComponent"));
        setName(controller.getDisplayName());
        setToolTipText(NbBundle.getMessage(EditorTopComponent.class, "HINT_EditorTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, false));

        viewportView = controller.getScene().createView();

        scrollpane.setViewportView(viewportView);
        //add(scene.createSatelliteView(), BorderLayout.WEST);

        associateLookup(Lookups.fixed(new Object[]{flowDiagramController}));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollpane = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());
        add(scrollpane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollpane;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        flowDiagramController.getScene().layoutScene();
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    private boolean checkSave() {
        SaveCookie saveCookie = getActivatedNodes()[0].getLookup().lookup(SaveCookie.class);
        if (saveCookie != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation("Save changes?",
                    flowDiagramController.getDisplayName(),
                    NotifyDescriptor.YES_NO_CANCEL_OPTION);
            Object res = DialogDisplayer.getDefault().notify(nd);
            if (NotifyDescriptor.OK_OPTION.equals(res)) {
                // User requested to save changes
                try {
                    saveCookie.save();
                    return true;
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    return false;
                }
            } else if (NotifyDescriptor.NO_OPTION.equals(res)) {
                // User requested to discharge changes
                return true;
            } else {
                // User requested to abort closing
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canClose() {
        if (checkSave()) {
            try {
                flowDiagramController.close();
                return true;
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }
}