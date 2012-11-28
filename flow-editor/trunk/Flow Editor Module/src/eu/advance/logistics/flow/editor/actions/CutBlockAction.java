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
import eu.advance.logistics.flow.editor.diagram.FlowScene;
import eu.advance.logistics.flow.editor.model.AbstractBlock;
import eu.advance.logistics.flow.editor.model.BlockBind;
import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.editor.model.SimpleBlock;
import eu.advance.logistics.flow.editor.undo.BindRemoved;
import eu.advance.logistics.flow.editor.undo.CompositeBlockRemoved;
import eu.advance.logistics.flow.editor.undo.CompositeEdit;
import eu.advance.logistics.flow.editor.undo.ConstantBlockRemoved;
import eu.advance.logistics.flow.editor.undo.SimpleBlockRemoved;
import eu.advance.logistics.flow.editor.undo.UndoRedoSupport;
import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;

/**
 *
 * @author TTS
 */
public class CutBlockAction extends AbstractAction {

    private FlowScene scene;

    public void setScene(FlowScene scene) {
        this.scene = scene;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Set selection = scene.getSelectedObjects();

        if (selection != null) {
            FlowSceneClipboard.getInstance().setSelection(selection);
        }

        final UndoRedoSupport undoRedoSupport = scene.getUndoRedoSupport();
        undoRedoSupport.start();

        String name = NbBundle.getBundle(PasteBlockAction.class).getString("CUT_BLOCKS");
        CompositeEdit edit = new CompositeEdit(name);

        // first binds
        for (Object sel : selection) {
            if (sel instanceof BlockBind) {
                BlockBind bind_sel = (BlockBind) sel;
                CompositeBlock parent = bind_sel.getParent();

                parent.removeBind(bind_sel);
                edit.add(new BindRemoved(parent, bind_sel));
            }
        }

        // then blocks
        for (Object sel : selection) {

            if (sel instanceof AbstractBlock) {
                AbstractBlock abs_sel = (AbstractBlock) sel;
                CompositeBlock parent = abs_sel.getParent();

                if (abs_sel instanceof SimpleBlock) {
                    final SimpleBlock sb_src = (SimpleBlock) abs_sel;
                    parent.removeBlock(sb_src);

                    edit.add(new SimpleBlockRemoved(parent, sb_src));
                } else if (abs_sel instanceof ConstantBlock) {
                    final ConstantBlock cb_src = (ConstantBlock) abs_sel;
                    parent.removeConstant(cb_src);

                    edit.add(new ConstantBlockRemoved(parent, cb_src));
                } else if (abs_sel instanceof CompositeBlock) {
                    final CompositeBlock comp_src = (CompositeBlock) abs_sel;
                    parent.removeComposite(comp_src);

                    edit.add(new CompositeBlockRemoved(parent, comp_src));
                }
            }
        }

        scene.validate();
        scene.repaint();
        undoRedoSupport.commit(edit);
    }
}
