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

import java.awt.BorderLayout;


import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import eu.advance.logistics.flow.editor.model.FlowDescription;
import eu.advance.logistics.flow.editor.tree.FlowDescriptionNode;
import org.openide.explorer.view.BeanTreeView;
import org.openide.windows.WindowManager;

/**
 * @author TTS
 */
@ConvertAsProperties(dtd = "-//eu.advance.logistics.flow.editor//TreeBrowser//EN",
autostore = false)
@TopComponent.Description(preferredID = "TreeBrowserTopComponent",
//iconBase = "SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "eu.advance.logistics.flow.editor.TreeBrowserTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TreeBrowserAction",
preferredID = "TreeBrowserTopComponent")
public final class TreeBrowserTopComponent extends TopComponent implements ExplorerManager.Provider {

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
    // <editor-fold defaultstate = "collapsed" desc = "Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    void setFlowDescription(FlowDescription fd) {
        if (fd != null) {
            FlowDescriptionNode n = new FlowDescriptionNode(fd);
            explorerManager.setRootContext(n);
        } else {
            explorerManager.setRootContext(Node.EMPTY);
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    static TreeBrowserTopComponent getDefault() {
        return (TreeBrowserTopComponent) WindowManager.getDefault().findTopComponent("TreeBrowserTopComponent");
    }
}
