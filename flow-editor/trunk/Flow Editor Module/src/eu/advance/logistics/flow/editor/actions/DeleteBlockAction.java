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

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import org.openide.util.NbBundle;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.editor.diagram.FlowScene;
import eu.advance.logistics.flow.editor.model.AbstractBlock;
import eu.advance.logistics.flow.editor.model.BlockBind;
import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.editor.model.FlowDescription;
import eu.advance.logistics.flow.editor.model.SimpleBlock;
import eu.advance.logistics.flow.editor.undo.BindRemoved;
import eu.advance.logistics.flow.editor.undo.CompositeBlockRemoved;
import eu.advance.logistics.flow.editor.undo.CompositeEdit;
import eu.advance.logistics.flow.editor.undo.ConstantBlockRemoved;
import eu.advance.logistics.flow.editor.undo.SimpleBlockRemoved;
import eu.advance.logistics.flow.editor.undo.UndoRedoSupport;
import eu.advance.logistics.flow.editor.undo.UndoableEdit;

/**
 *
 * @author TTS
 */
public class DeleteBlockAction extends AbstractAction {
    /** */
	private static final long serialVersionUID = -2773928407114841355L;
	private UndoRedoSupport undoRedoSupport;
    protected FlowDescription flowDescription;
    private List<AbstractBlock> blocks;

    public DeleteBlockAction(UndoRedoSupport urs, FlowDescription fd, AbstractBlock block) {
        this(urs, fd, Lists.newArrayList(block));
        putValue(NAME, NbBundle.getBundle(DeleteBlockAction.class).getString("DELETE_BLOCK"));
    }

    public DeleteBlockAction(UndoRedoSupport urs, FlowDescription fd, List<AbstractBlock> blocks) {
        this.undoRedoSupport = urs;
        this.flowDescription = fd;
        this.blocks = blocks;
        putValue(NAME, NbBundle.getBundle(DeleteBlockAction.class).getString("DELETE_SELECTION") + " (" + blocks.size() + ")");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        undoRedoSupport.start();
        CompositeEdit edit = new CompositeEdit((String) getValue(NAME));
        for (AbstractBlock block : blocks) {
            delete(block, edit);
        }
        undoRedoSupport.commit(edit);
    }

    public static void delete(AbstractBlock block, CompositeEdit edit) {
        UndoableEdit blockEdit = createDeleteEdit(block);
        if (block != null) {
            List<BlockBind> binds = block.getActiveBinds();
            for (BlockBind bind : binds) {
                CompositeBlock parent = bind.getParent();
                bind.destroy();
                edit.add(new BindRemoved(parent, bind));
            }
            block.destroy();
            edit.add(blockEdit);
        }
    }

    public static DeleteBlockAction build(FlowScene scene) {
        UndoRedoSupport urs = scene.getUndoRedoSupport();
        List<AbstractBlock> blocks = Lists.newArrayList();
        for (Object sel : scene.getSelectedObjects()) {
            if (sel instanceof AbstractBlock) {
                AbstractBlock block = (AbstractBlock) sel;
                if (block.getParent() != null) {
                    blocks.add(block);
                }
            }
        }
        return blocks.isEmpty() ? null : new DeleteBlockAction(urs, scene.getFlowDescription(), blocks);
    }

    private static UndoableEdit createDeleteEdit(AbstractBlock block) {
        CompositeBlock parent = block.getParent();
        if (block instanceof SimpleBlock) {
            return new SimpleBlockRemoved(parent, (SimpleBlock) block);
        } else if (block instanceof ConstantBlock) {
            return new ConstantBlockRemoved(parent, (ConstantBlock) block);
        } else if (block instanceof CompositeBlock) {
            return new CompositeBlockRemoved(parent, (CompositeBlock) block);
        } else {
            return null;
        }
    }
}
