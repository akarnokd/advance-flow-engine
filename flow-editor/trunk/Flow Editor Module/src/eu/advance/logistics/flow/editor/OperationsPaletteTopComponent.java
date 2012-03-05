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

import eu.advance.logistics.flow.editor.palette.BlockNode;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.modules.InstalledFileLocator;
import org.openide.nodes.AbstractNode;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import eu.advance.logistics.flow.editor.palette.PaletteRootChildren;
import eu.advance.logistics.flow.engine.runtime.BlockRegistryEntry;
import eu.advance.logistics.flow.engine.xml.XElement;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.openide.nodes.Node;

/**
 * Operations.
 * 
 * @author TTS
 */
@ConvertAsProperties(dtd = "-//eu.advance.logistics.flow.editor//OperationsPalette//EN",
autostore = false)
@TopComponent.Description(preferredID = "OperationsPaletteTopComponent",
//iconBase = "SET/PATH/TO/ICON/HERE",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "topright", openAtStartup = true)
@ActionID(category = "Window", id = "eu.advance.logistics.flow.editor.OperationsPaletteTopComponent")
@ActionReference(path = "Menu/Window", position = 100)
@TopComponent.OpenActionRegistration(displayName = "#CTL_OperationsPaletteAction", preferredID = "OperationsPaletteTopComponent")
public final class OperationsPaletteTopComponent extends TopComponent implements ExplorerManager.Provider {

    private ExplorerManager explorerManager = new ExplorerManager();
    private BeanTreeView treeView = new BeanTreeView();
    /** The block tip displayed on node selection. */
    protected JLabel blockTip;
    /** The search/filter box. */
    protected JTextField search;
    public OperationsPaletteTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(OperationsPaletteTopComponent.class, "CTL_OperationsPaletteTopComponent"));
        setToolTipText(NbBundle.getMessage(OperationsPaletteTopComponent.class, "HINT_OperationsPaletteTopComponent"));

        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        associateLookup(ExplorerUtils.createLookup(explorerManager, getActionMap()));
        explorerManager.setRootContext(new AbstractNode(new PaletteRootChildren()));
        treeView.setRootVisible(false);
        add(treeView);
        
        blockTip = new JLabel();
        
        blockTip.setVisible(false);
        
        add(blockTip, BorderLayout.SOUTH);
        search = new JTextField();
        
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(new JLabel("Filter:"), BorderLayout.WEST);
        searchPanel.add(search, BorderLayout.CENTER);
        
        searchPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        
        add(searchPanel, BorderLayout.NORTH);

        explorerManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                    Node[] nodes = (Node[])evt.getNewValue();
                    if (nodes.length == 1) {
                        Node n = nodes[0];
                        if (n instanceof BlockNode) {
                            BlockNode bn = (BlockNode)n;
                            blockTip.setText("<html><p>" + XElement.sanitize(bn.blockDesc()) + "</p></html>");
                            blockTip.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
                            blockTip.setVisible(true);
                            return;
                        }
                    } 
                    blockTip.setText(null);
                    blockTip.setBorder(null);
                    blockTip.setVisible(false);
                }
            }
        });

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
    // <editor-fold defaultstate = "collapsed" desc = "Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        File file;
        file = InstalledFileLocator.getDefault().locate("categories.xml", "eu.advance.logistics.core", false);  // NOI18N
        BlockRegistry.getInstance().readCategories(file);

        file = InstalledFileLocator.getDefault().locate("LocalEngine/schemas/block-registry.xml", "eu.advance.logistics.core", false);  // NOI18N
        try {
            if (file == null) {
                InputStream in = BlockRegistryEntry.class.getResourceAsStream("/block-registry.xml");
                if (in == null) {
                    in = BlockRegistryEntry.class.getResourceAsStream("/schemas/block-registry.xml");
                }
                if (in != null) {
                    BlockRegistryDataObject.read(in);
                } else {
                    Exceptions.printStackTrace(new FileNotFoundException("block-registry.xml"));
                }
            } else {
                BlockRegistryDataObject.read(new FileInputStream(file));
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
//        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
