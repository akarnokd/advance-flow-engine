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

import eu.advance.logistics.flow.editor.FlowSceneClipboard;
import eu.advance.logistics.flow.editor.FlowSceneSelectionListener;
import eu.advance.logistics.flow.editor.diagram.FlowScene;
import eu.advance.logistics.flow.editor.model.AbstractBlock;
import eu.advance.logistics.flow.editor.model.BlockBind;
import eu.advance.logistics.flow.editor.model.BlockParameter;
import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.editor.model.SimpleBlock;
import eu.advance.logistics.flow.editor.undo.BindCreated;
import eu.advance.logistics.flow.editor.undo.CompositeBlockAdded;
import eu.advance.logistics.flow.editor.undo.CompositeEdit;
import eu.advance.logistics.flow.editor.undo.ConstantBlockAdded;
import eu.advance.logistics.flow.editor.undo.SimpleBlockAdded;
import eu.advance.logistics.flow.editor.undo.UndoRedoSupport;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import javax.swing.AbstractAction;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.NbBundle;

/**
 *
 * @author TTS
 */
public class PasteBlockAction extends AbstractAction implements FlowSceneSelectionListener {

    private FlowScene scene;

    public void setScene(FlowScene scene) {
        this.scene = scene;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (FlowSceneClipboard.getInstance().isValidSelection()) {
            final List selection = FlowSceneClipboard.getInstance().getSelection();

            final UndoRedoSupport undoRedoSupport = scene.getUndoRedoSupport();
            undoRedoSupport.start();

            String name = NbBundle.getMessage(PasteBlockAction.class, "PASTE_BLOCKS");
            CompositeEdit edit = new CompositeEdit(name);

            final HashMap<String, AbstractBlock> clones = new HashMap<String, AbstractBlock>();

            CompositeBlock targetContainer = scene.getFlowDescription().getActiveBlock();

            // first blocks
            for (Object sel : selection) {

                if (sel instanceof AbstractBlock) {
                    AbstractBlock abs_sel = (AbstractBlock) sel;
                    AbstractBlock cloned = null;

                    if (abs_sel instanceof SimpleBlock) {
                        final SimpleBlock sb_src = (SimpleBlock) abs_sel;
                        final SimpleBlock sb_clone = sb_src.createClone(targetContainer);

                        targetContainer.addBlock(sb_clone);

                        clones.put(sb_clone.getId(), sb_clone);
                        edit.add(new SimpleBlockAdded(targetContainer, sb_clone));
                        cloned = sb_clone;
                    } else if (abs_sel instanceof ConstantBlock) {
                        final ConstantBlock cb_src = (ConstantBlock) abs_sel;
                        final ConstantBlock cb_clone = cb_src.createClone(targetContainer);

                        targetContainer.addConstant(cb_clone);

                        clones.put(cb_clone.getId(), cb_clone);
                        edit.add(new ConstantBlockAdded(targetContainer, cb_clone));
                        cloned = cb_clone;

                        final Widget w_src = scene.findWidget(cb_src);
                        final Widget w_dest = scene.findWidget(cb_clone);
                        if ((w_src != null) && (w_dest != null)) {
                            Point loc = w_src.getLocation();
                            loc.x += 50;
                            loc.y += 50;

                            cb_clone.setLocation(loc);
                        }
                    } else if (abs_sel instanceof CompositeBlock) {
                        final CompositeBlock comp_src = (CompositeBlock) abs_sel;
                        final CompositeBlock comp_clone = comp_src.createClone(targetContainer);

                        targetContainer.addComposite(comp_clone);
                        clones.put(comp_clone.getId(), comp_clone);
                        edit.add(new CompositeBlockAdded(targetContainer, comp_clone));
                        cloned = comp_clone;
                    }

                    if (cloned != null) {
                        final Widget w_src = scene.findWidget(abs_sel);
                        final Widget w_dest = scene.findWidget(cloned);
                        if ((w_src != null) && (w_dest != null)) {
                            Point loc = w_src.getLocation();
                            loc.x += 50;
                            loc.y += 50;
                            cloned.setLocation(loc);
                        }
                    }
                }
            }
//
//            scene.validate();
//            scene.repaint();

            // then binds
            for (Object sel : selection) {
                if (sel instanceof BlockBind) {
                    BlockBind bind_sel = (BlockBind) sel;


                        final AbstractBlock src_parent = clones.get(bind_sel.source.getOwner().getId() + " (copy)");
                        BlockParameter new_src = null;
                        final String id_src = bind_sel.source.getId();
                        for (BlockParameter output : src_parent.getOutputs()) {
                            if (id_src.equals(output.getId())) {
                                new_src = output;
                                break;
                            }
                        }


                        final AbstractBlock dest_parent = clones.get(bind_sel.destination.getOwner().getId() + " (copy)");
                        BlockParameter new_dest = null;
                        final String id_dest = bind_sel.destination.getId();
                        for (BlockParameter input : dest_parent.getInputs()) {
                            if (id_dest.equals(input.getId())) {
                                new_dest = input;
                                break;
                            }
                        }

                        if ((new_src != null) && (new_dest != null)) {
                            final BlockBind bind = targetContainer.createBind(new_src, new_dest);
                            edit.add(new BindCreated(targetContainer, bind));
                        }
                } /*else if (sel instanceof AbstractBlock) {
                    AbstractBlock abs_sel = (AbstractBlock) sel;
                    CompositeBlock parent = abs_sel.getParent();

                    // i binds dei CompositeBlock
                    if (abs_sel instanceof CompositeBlock) {
                        final CompositeBlock comp_src = (CompositeBlock) abs_sel;

                        // add as children only Binds that are in selection
                        final Collection<BlockBind> binds = comp_src.getBinds();
                        for (BlockBind bind : binds) {

                            if (selection.contains(bind)) {

                                final AbstractBlock src_parent = clones.get(bind.destination.getOwner().getId() + " (copy)");
                                BlockParameter new_src = null;
                                for (BlockParameter output : src_parent.getOutputs()) {
                                    if (bind.source.getId().equals(output.getId())) {
                                        new_src = output;
                                        break;
                                    }
                                }


                                final AbstractBlock dest_parent = clones.get(bind.destination.getOwner().getId() + " (copy)");
                                BlockParameter new_dest = null;
                                for (BlockParameter input : dest_parent.getInputs()) {
                                    if (bind.destination.getId().equals(input.getId())) {
                                        new_dest = input;
                                        break;
                                    }
                                }

                                if ((new_src != null) && (new_dest != null)) {
                                    final BlockBind bind_new = parent.createBind(new_src, new_dest);

                                    edit.add(new BindCreated(parent, bind_new));
                                }
                            }
                        }
                    }
                }*/
            }


            scene.validate();
            scene.repaint();
            undoRedoSupport.commit(edit);
        }
    }

    @Override
    public void SelectionChanged() {
        setEnabled(FlowSceneClipboard.getInstance().isValidSelection());
    }
}
