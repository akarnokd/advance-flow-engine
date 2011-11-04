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

import eu.advance.logistics.flow.editor.diagram.FlowScene;
import eu.advance.logistics.flow.editor.model.BlockBind;
import eu.advance.logistics.flow.editor.model.FlowDescription;
import eu.advance.logistics.flow.editor.undo.UndoRedoSupport;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.model.rt.AdvanceCompilationResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.cookies.SaveCookie;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.ImageUtilities;

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
    private FlowDescriptionDataObject dataObject;
    private BreadcrumbView breadcrumbView = new BreadcrumbView();
    private UndoRedo undoRedo;
    private JLabel info;

    public EditorTopComponent(FlowDescriptionDataObject dataObject) {
        this.dataObject = dataObject;

        initComponents();
        //setName(NbBundle.getMessage(EditorTopComponent.class, "CTL_EditorTopComponent"));
        setName(dataObject.getName());
        setToolTipText(NbBundle.getMessage(EditorTopComponent.class, "HINT_EditorTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, false));

        FlowScene scene = dataObject.getLookup().lookup(FlowScene.class);
        scene.addObjectSceneListener(objectSceneListener,
                ObjectSceneEventType.OBJECT_SELECTION_CHANGED,
                ObjectSceneEventType.OBJECT_ADDED,
                ObjectSceneEventType.OBJECT_REMOVED);
        viewportView = scene.createView();

        scrollpane.setViewportView(viewportView);

        add(breadcrumbView.getControl(), BorderLayout.NORTH);
        FlowDescription fd = dataObject.getLookup().lookup(FlowDescription.class);
        breadcrumbView.populate(fd.getActiveBlock());
        fd.addListener(breadcrumbView);

        undoRedo = dataObject.getLookup().lookup(UndoRedoSupport.class).getUndoRedo();

        //associateLookup(Lookups.fixed(new Object[]{flowDiagramController}));
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
    public UndoRedo getUndoRedo() {
        return undoRedo;
    }

    private FlowScene getScene() {
        return dataObject.getLookup().lookup(FlowScene.class);
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
        //dataObject.getLookup().lookup(FlowDescription.class).close();
    }

    @Override
    protected void componentActivated() {
        getScene().getView().requestFocus();
    }

    @Override
    protected void componentShowing() {
        getScene().getView().requestFocus();
    }

    private boolean checkSave() {
        SaveCookie saveCookie = getActivatedNodes()[0].getLookup().lookup(SaveCookie.class);
        if (saveCookie != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getBundle(EditorTopComponent.class).getString("SAVE_CHANGES"),
                    dataObject.getName(),
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
                dataObject.setValid(false);
                return true;
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }

    private void addInfo(String text) {
        removeInfo();
        info = new JLabel(text);
        info.setOpaque(true);
        info.setBackground(new Color(0xACEB95));
        info.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(info, BorderLayout.SOUTH);
        revalidate();
        repaint();
    }

    private void removeInfo() {
        if (info != null) {
            remove(info);
            revalidate();
            repaint();
            info = null;
        }
    }
    private ObjectSceneListener objectSceneListener = new ObjectSceneListener() {

        @Override
        public void objectAdded(ObjectSceneEvent event, Object addedObject) {
            removeInfo();
        }

        @Override
        public void objectRemoved(ObjectSceneEvent event, Object removedObject) {
            removeInfo();
        }

        @Override
        public void objectStateChanged(ObjectSceneEvent event, Object changedObject, ObjectState previousState, ObjectState newState) {
        }

        @Override
        public void selectionChanged(ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection) {
            AdvanceCompilationResult cr = dataObject.getLookup().lookup(FlowDescription.class).getCompilationResult();
            BlockBind bind = null;
            if (newSelection.size() == 1) {
                Object sel = newSelection.iterator().next();
                if (sel instanceof BlockBind) {
                    bind = (BlockBind) sel;
                }
            }
            if (bind != null && cr != null) {
                AdvanceType at = cr.wireTypes.get(bind.id);
                addInfo(bind.id + ": " + (at != null ? at.toString() : "N/A"));
            } else {
                removeInfo();
            }
        }

        @Override
        public void highlightingChanged(ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting) {
        }

        @Override
        public void hoverChanged(ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject) {
        }

        @Override
        public void focusChanged(ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {
        }
    };
}
