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
package eu.advance.logistics.flow.editor.palette;

import com.google.common.collect.Maps;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.IOException;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.NodeTransfer;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

import eu.advance.logistics.flow.editor.BlockRegistry;
import eu.advance.logistics.flow.editor.diagram.SceneDropAction;
import eu.advance.logistics.flow.editor.model.BlockCategory;
import eu.advance.logistics.flow.editor.model.FlowDescription;
import eu.advance.logistics.flow.editor.model.SimpleBlock;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockReference;
import hu.akarnokd.reactive4java.base.Pair;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.openide.windows.WindowManager;

/**
 *
 * @author TTS
 */
public class BlockNode extends AbstractNode {
    /** The short block description text.*/
    protected String blockDesc;
    public BlockNode(AdvanceBlockDescription block) {
        super(Children.LEAF, Lookups.fixed(new Object[]{new DropAction(block)}));
        setIconBaseWithExtension("eu/advance/logistics/flow/editor/palette/images/block.png");
        setDisplayName(block.displayName);
        blockDesc = block.tooltip;
    }
    /**
     * @return the short block description
     */
    public String blockDesc() {
        return blockDesc;
    }
    @Override
    public Transferable drag() throws IOException {
        return NodeTransfer.transferable(this, NodeTransfer.DND_COPY_OR_MOVE);
    }

    private static class DropAction implements SceneDropAction {

        private AdvanceBlockDescription desc;
        private Image image;

        private DropAction(AdvanceBlockDescription desc) {
            this.desc = desc;
        }

        @Override
        public void accept(FlowDescription flowDescription, Point location) {
            AdvanceBlockDescription d = desc;
            Map<String, Integer> varargs = null;
            if (desc.hasVarargs) {
                final Pair<AdvanceBlockDescription, Map<String, Integer>> res = askUserForCounts(desc);
                if (res == null) {
                    return;
                }
                d = res.first;
                varargs = res.second;
            }
            
            SimpleBlock block = flowDescription.getActiveBlock().createBlock(d);
            block.varargs = varargs;
            block.setLocation(location);
        }

        @Override
        public Image getImage() {
            if (image == null) {
                BlockCategory category = BlockRegistry.getInstance().findByType(desc);
                if (category != null) {
                    String url = category.getImage();
                    url = url.substring(0, url.length() - 4) + "24.png";
                    image = ImageUtilities.loadImage("eu/advance/logistics/flow/editor/palette/images/" + url);
                }
            }
            return image;
        }
    }
    /**
     * Ask the user for the counts on the varargs parameters.
     * @param desc the initial description and the varargs count
     */
    static Pair<AdvanceBlockDescription, Map<String, Integer>> askUserForCounts(final AdvanceBlockDescription desc) {
        VarargsDialog d = new VarargsDialog(desc);
        d.setVisible(true);
        if (d.approved) {
            return d.derive();
        }
        return null;
    }
    /**
     * The variable arguments panel to set the counts.
     */
    static class VarargsPanel extends JPanel {
        /** The referenced parameter. */
        AdvanceBlockParameterDescription param;
        /** The value counter. */
        JSpinner counter;
        /** 
         * Constructor.
         * @param param the parameter reference 
         */
        public VarargsPanel(AdvanceBlockParameterDescription param) {
            this.param = param;
            JLabel paramNameLabel = new JLabel("Parameter:");
            JLabel paramTypeLabel = new JLabel("Type:");
            JLabel paramCountLabel = new JLabel("Count:");

            JLabel paramName = new JLabel(param.displayName != null ? param.displayName : param.id);
            JLabel paramType = new JLabel(param.type.toString());
            counter = new JSpinner(new SpinnerNumberModel(1, 1, 255, 1));
            
            GroupLayout gl = new GroupLayout(this);
            setLayout(gl);
            gl.setAutoCreateGaps(true);
            
            gl.setHorizontalGroup(
                gl.createSequentialGroup()
                .addGroup(
                    gl.createParallelGroup()
                    .addComponent(paramNameLabel)
                    .addComponent(paramTypeLabel)
                    .addComponent(paramCountLabel)
                )
                .addGroup(
                    gl.createParallelGroup()
                    .addComponent(paramName)
                    .addComponent(paramType)
                    .addComponent(counter)
                )
            );
            gl.setVerticalGroup(
                gl.createSequentialGroup()
                .addGroup(
                    gl.createParallelGroup(Alignment.BASELINE)
                    .addComponent(paramNameLabel)
                    .addComponent(paramName)
                )
                .addGroup(
                    gl.createParallelGroup(Alignment.BASELINE)
                    .addComponent(paramTypeLabel)
                    .addComponent(paramType)
                )
                .addGroup(
                    gl.createParallelGroup(Alignment.BASELINE)
                    .addComponent(paramCountLabel)
                    .addComponent(counter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                )
            );
        }
    }
    /**
     * The variable arguments setter dialog.
     */
    static class VarargsDialog extends JDialog {
        /** True if the user approved the changes. */
        public boolean approved;
        /** The original definition. */
        private final AdvanceBlockDescription desc;
        /** The varargs map. */
        private final Map<String, VarargsPanel> varargs = Maps.newLinkedHashMap();
        /** Constructor. Initializes the GUI. */
        public VarargsDialog(AdvanceBlockDescription desc) {
            super(WindowManager.getDefault().getMainWindow(), "Define argument counts", true);
            this.desc = desc;
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            
            Container c = getContentPane();
            GroupLayout gl = new GroupLayout(c);
            c.setLayout(gl);
            gl.setAutoCreateContainerGaps(true);
            gl.setAutoCreateGaps(true);

            JPanel paramsPanel = initPanel();
            
            JScrollPane sp = new JScrollPane(paramsPanel);
            
            JLabel typeLabel = new JLabel(desc.displayName != null ? desc.displayName : desc.id);
            
            JButton ok = new JButton("OK");
            ok.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    approved = true;
                    dispose();
                }
                
            });
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            
            gl.setHorizontalGroup(
                    gl.createParallelGroup(Alignment.CENTER)
                    .addComponent(typeLabel)
                    .addComponent(sp)
                    .addGroup(
                        gl.createSequentialGroup()
                        .addComponent(ok)
                        .addComponent(cancel)
                    )
            );
            gl.setVerticalGroup(
                   gl.createSequentialGroup()
                    .addComponent(typeLabel)
                    .addComponent(sp)
                    .addGroup(
                        gl.createParallelGroup(Alignment.BASELINE)
                        .addComponent(ok)
                        .addComponent(cancel)
                    )
            );
            pack();
            setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
        }
        private JPanel initPanel() {
            JPanel paramsPanel = new JPanel();
            GroupLayout gl2 = new GroupLayout(paramsPanel);
            paramsPanel.setLayout(gl2);
            gl2.setAutoCreateContainerGaps(true);
            gl2.setAutoCreateGaps(true);
            
            Group h = gl2.createParallelGroup();
            Group v = gl2.createSequentialGroup();
            
            for (AdvanceBlockParameterDescription d : desc.inputs.values()) {
                if (d.varargs) {
                    VarargsPanel p = new VarargsPanel(d);
                    varargs.put(d.id, p);
                    h.addComponent(p);
                    v.addComponent(p);
                }
            }
            
            gl2.setHorizontalGroup(h);
            gl2.setVerticalGroup(v);
            
            return paramsPanel;
        }
        /**
         * Derive the new description based on the form data.
         * @return the form data
         */
        public Pair<AdvanceBlockDescription, Map<String, Integer>> derive() {
            AdvanceBlockReference ref = new AdvanceBlockReference();
            
            ref.id = "<new>";
            ref.type = desc.id;
            
            for (VarargsPanel p : varargs.values()) {
                ref.varargs.put(p.param.id, (Integer)p.counter.getValue());
            }
            
            return Pair.of(desc.derive(ref), ref.varargs);
        }
    }
}
