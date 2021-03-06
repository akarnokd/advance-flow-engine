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

import eu.advance.logistics.flow.editor.actions.CopyBlockAction;
import eu.advance.logistics.flow.editor.actions.CutBlockAction;
import eu.advance.logistics.flow.editor.actions.PasteBlockAction;
import eu.advance.logistics.flow.editor.diagram.FlowScene;
import eu.advance.logistics.flow.editor.model.BlockBind;
import eu.advance.logistics.flow.editor.model.FlowDescription;
import eu.advance.logistics.flow.editor.undo.UndoRedoSupport;
import eu.advance.logistics.flow.engine.compiler.AdvanceCompilationResult;
import eu.advance.logistics.flow.engine.model.AdvanceCompilationError;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockBind;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.ImageUtilities;

/**
 * Visual flow editor.
 * 
 * @author TTS
 */
@ConvertAsProperties(dtd = "-//eu.advance.logistics.flow.editor//EditorTopComponent//EN",
autostore = false)
@TopComponent.Description(preferredID = "EditorTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE",
persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
public final class EditorTopComponent extends TopComponent {

    private final static String ICON_PATH = "eu/advance/logistics/flow/editor/palette/images/block.png";
    private JComponent viewportView;
    private FlowDescriptionDataObject dataObject;
    private BreadcrumbView breadcrumbView = new BreadcrumbView();
    private UndoRedo undoRedo;
    private JPanel info;
    private PasteBlockAction pasteAction = new PasteBlockAction();
    private CopyBlockAction copyAction = new CopyBlockAction();
    private CutBlockAction cutAction = new CutBlockAction();

    public EditorTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(EditorTopComponent.class, "CTL_EditorTopComponent"));
        setToolTipText(NbBundle.getMessage(EditorTopComponent.class, "HINT_EditorTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, false));
    }

    public void setDataObject(FlowDescriptionDataObject dataObject) {
        if (this.dataObject != null || dataObject == null) {
            throw new IllegalArgumentException();
        }
        this.dataObject = dataObject;

        setName(dataObject.getName());

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

        pasteAction.setScene(scene);
        copyAction.setScene(scene);
        cutAction.setScene(scene);

        //associateLookup(Lookups.fixed(new Object[]{flowDiagramController}));
        TreeBrowserTopComponent.getDefault().setFlowDescription(fd);
        NavigatorTopComponent.getDefault().setFlowScene(scene);
        scene.getView().requestFocus();
    }

    public FlowDescriptionDataObject getDataObject() {
        return dataObject;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate = "collapsed" desc = "Generated Code">//GEN-BEGIN:initComponents
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

    private FlowScene getFlowScene() {
        return (dataObject != null) ? dataObject.getLookup().lookup(FlowScene.class) : null;
    }

    private FlowDescription getFlowDescription() {
        return (dataObject != null) ? dataObject.getLookup().lookup(FlowDescription.class) : null;
    }

    @Override
    public void componentOpened() {

        // maybe previously selected something
        pasteAction.setEnabled(FlowSceneClipboard.getInstance().isValidSelection());
        copyAction.setEnabled(false);
        cutAction.setEnabled(false);

        FlowSceneClipboard.getInstance().addSceneSelectionListener(pasteAction);

        getActionMap().put(DefaultEditorKit.pasteAction, pasteAction);
        getActionMap().put(DefaultEditorKit.copyAction, copyAction);
        getActionMap().put(DefaultEditorKit.cutAction, cutAction);

    }

    @Override
    public void componentClosed() {

        // do not erase the selection: can be useful for other documents
        FlowSceneClipboard.getInstance().removeSceneSelectionListener(pasteAction);

        NavigatorTopComponent.getDefault().setFlowScene(null);
        TreeBrowserTopComponent.getDefault().setFlowDescription(null);
    }

    @Override
    protected void componentActivated() {
        FlowScene scene = getFlowScene();
        NavigatorTopComponent.getDefault().setFlowScene(scene);
        TreeBrowserTopComponent.getDefault().setFlowDescription(getFlowDescription());
        if (scene != null) {
            scene.getView().requestFocus();
        }
    }

    @Override
    protected void componentShowing() {
        FlowScene scene = getFlowScene();
        if (scene != null) {
            scene.getView().requestFocus();
        }
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
        if (dataObject == null) {
            return true;
        }
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

    private void addInfo(final AdvanceBlockBind wire, final AdvanceCompilationResult cr) {
        removeInfo();
        info = new JPanel();
        final JLabel label = new JLabel();
        AdvanceType type = cr.getType(wire);
        List<AdvanceCompilationError> errors = cr.getErrors(wire.id);

        StringBuilder b = new StringBuilder();
        b.append(wire.id);
        if (type == null) {
            b.append(": N/A");
        } else {
            b.append(": ").append(type);
        }
        // TODO move this into the compilation result class
        for (AdvanceCompilationError e : errors) {
            b.append("   ").append(e.toString());
        }

        label.setText(b.toString());
        label.setFont(label.getFont().deriveFont(18.0f));

        info.setLayout(new BorderLayout());
        info.add(label, BorderLayout.NORTH);

        info.setOpaque(true);
        if (!errors.isEmpty()) {
            info.setBackground(new Color(0xFFCCCC));
        } else if (type == null) {
            info.setBackground(new Color(0xE6C040));
        } else {
            info.setBackground(new Color(0xACEB95));
        }
        info.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
                AdvanceBlockBind b = new AdvanceBlockBind();
                b.id = bind.id;
                addInfo(b, cr);
            } else {
                removeInfo();
            }
            copyAction.setEnabled(!newSelection.isEmpty());
            cutAction.setEnabled(!newSelection.isEmpty());
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

    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
        if (dataObject != null) {
            File file = FileUtil.toFile(dataObject.getPrimaryFile());
            if (file != null) {
                try {
                    p.setProperty("file", file.getCanonicalPath());
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        String path = null;
        if (version.equals("1.0")) {
            path = p.getProperty("file");
        }
        DataObject dobj = null;
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    dobj = DataObject.find(FileUtil.toFileObject(file));
                } catch (DataObjectNotFoundException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }
        EventQueue.invokeLater(new Restore(dobj));
    }

    private class Restore implements Runnable {

        private final DataObject dobj;

        private Restore(DataObject dobj) {
            this.dobj = dobj;
        }

        @Override
        public void run() {
            if (dobj instanceof FlowDescriptionDataObject) {
                ((FlowDescriptionDataObject) dobj).getInstanceContent().add(EditorTopComponent.this);
                dobj.getLookup().lookup(OpenCookie.class).open();
            } else {
                close(); // close the TopComponent
            }

        }
    }
}
