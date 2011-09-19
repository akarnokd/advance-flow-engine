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
 */package eu.advance.logistics.flow.editor;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import javax.swing.JTree;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 * @author TTS
 */
@ConvertAsProperties(dtd = "-//eu.advance.logistics.flow.editor//TreeBrowser//EN",
autostore = false)
@TopComponent.Description(preferredID = "TreeBrowserTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "eu.advance.logistics.flow.editor.TreeBrowserTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TreeBrowserAction",
preferredID = "TreeBrowserTopComponent")
public final class TreeBrowserTopComponent extends TopComponent
        implements ExplorerManager.Provider, PropertyChangeListener {

    private WeakReference<TopComponent> tcReference;
    private ExplorerManager explorerManager = new ExplorerManager();
    private BeanTreeView treeView = new BeanTreeView();

    public TreeBrowserTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(TreeBrowserTopComponent.class, "CTL_TreeBrowserTopComponent"));
        setToolTipText(NbBundle.getMessage(TreeBrowserTopComponent.class, "HINT_TreeBrowserTopComponent"));
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        associateLookup(ExplorerUtils.createLookup(explorerManager, getActionMap()));
        //explorerManager.setRootContext(new AbstractNode(new PaletteRootChildren()));
        //treeView.setRootVisible(false);
        add(treeView, BorderLayout.CENTER);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        WindowManager.getDefault().getRegistry().addPropertyChangeListener(this);
    }

    @Override
    public void componentClosed() {
        WindowManager.getDefault().getRegistry().removePropertyChangeListener(this);
        setDataObject(null);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private void setDataObject(FlowDescriptionDataObject dataObject) {
        if (dataObject != null) {
            setDisplayName(dataObject.getName());
            explorerManager.setRootContext(dataObject.getNodeDelegate());
            JTree t = (JTree) treeView.getViewport().getView();
            for (int i = 0; i < t.getRowCount(); i++) {
                t.expandRow(i);
            }
        } else {
            setDisplayName(NbBundle.getMessage(getClass(), "CTL_TreeBrowserTopComponent"));
            explorerManager.setRootContext(Node.EMPTY);
        }
    }

    private FlowDescriptionDataObject getDataObject() {
        if (tcReference != null) {
            TopComponent tc = tcReference.get();
            if (tc != null) {
                return tc.getLookup().lookup(FlowDescriptionDataObject.class);
            }
        }
        return null;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String pname = evt.getPropertyName();
        if (Registry.PROP_ACTIVATED.equals(pname)) {
            FlowDescriptionDataObject current = getDataObject();
            TopComponent tc = (TopComponent) evt.getNewValue();
            Mode mode = WindowManager.getDefault().findMode(tc);
            if (mode != null && mode.getName().equals("editor")) {
                FlowDescriptionDataObject dataObject = tc.getLookup().lookup(FlowDescriptionDataObject.class);
                if (dataObject != null) {
                    tcReference = new WeakReference<TopComponent>(tc);
                    if (dataObject != current) {
                        setDataObject(dataObject);
                    }
                } else {
                    tcReference = null;
                    setDataObject(null);
                }
            }
        } else if (Registry.PROP_TC_CLOSED.equals(pname)) {
            TopComponent tc = (TopComponent) evt.getNewValue();
            if (tc == tcReference.get()) {
                tcReference = null;
                setDataObject(null);
            }
        }
    }
}