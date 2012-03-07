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

import com.google.common.collect.Lists;
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
import eu.advance.logistics.flow.editor.tree.CompositeBlockNode.CompositeBlockChildren;
import eu.advance.logistics.flow.editor.tree.FlowDescriptionNode;
import eu.advance.logistics.flow.editor.tree.SimpleBlockNode;
import eu.advance.logistics.flow.editor.util.Util;
import eu.advance.logistics.flow.engine.xml.XElement;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Children;
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
    /** The block tip displayed on node selection. */
    protected JLabel blockTip;
    /** The search/filter box. */
    protected JTextField search;
    /** Initializes the component. */
    public TreeBrowserTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(TreeBrowserTopComponent.class, "CTL_TreeBrowserTopComponent"));
        setToolTipText(NbBundle.getMessage(TreeBrowserTopComponent.class, "HINT_TreeBrowserTopComponent"));
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        associateLookup(ExplorerUtils.createLookup(explorerManager, getActionMap()));
        //explorerManager.setRootContext(new AbstractNode(new PaletteRootChildren()));
        //treeView.setRootVisible(false);
        add(treeView, BorderLayout.CENTER);
        blockTip = new JLabel();
        
        blockTip.setVisible(false);
        
        add(blockTip, BorderLayout.SOUTH);
        search = new JTextField();
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doFilter();
            }
        });
        search.setToolTipText("Type your search pattern here and press ENTER. Use * and ? for wildcards unless you want exact match.");
        
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
                        if (n instanceof SimpleBlockNode) {
                            SimpleBlockNode bn = (SimpleBlockNode)n;
                            blockTip.setText("<html><p>" + XElement.sanitize(bn.getBlock().getTooltip()) + "</p></html>");
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
    /** Filter the tree. */
    void doFilter() {
        Node rn = explorerManager.getRootContext();
        
        String text = search.getText();
        Pattern p;
        if (text.isEmpty()) {
            p = null;
        } else {
            p = Pattern.compile(Util.wildcardToRegex(text));
        }

        Children c = rn.getChildren();
        if (c instanceof CompositeBlockChildren) {
            CompositeBlockChildren bc = (CompositeBlockChildren)c;
            bc.setPattern(p);
        }
        
        if (p != null) {
            LinkedList<Node> nodes = Lists.newLinkedList();
            nodes.add(rn);
            while (!nodes.isEmpty()) {
                Node n0 = nodes.removeFirst();
                treeView.expandNode(n0);
                nodes.addAll(Arrays.asList(n0.getChildren().getNodes()));
            }
        }
    }
}
