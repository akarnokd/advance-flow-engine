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

import com.google.common.collect.Lists;
import eu.advance.logistics.flow.editor.diagram.FlowScene;
import eu.advance.logistics.flow.editor.model.AbstractBlock;
import eu.advance.logistics.flow.editor.model.BlockBind;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;

/**
 *
 * @author TTS
 */
public class DeleteBlockAction extends AbstractAction {

    private List<AbstractBlock> blocks;

    public DeleteBlockAction(AbstractBlock block) {
        this(Lists.newArrayList(block));
        putValue(NAME, NbBundle.getBundle(DeleteBlockAction.class).getString("DELETE_BLOCK"));
    }

    public DeleteBlockAction(List<AbstractBlock> blocks) {
        this.blocks = blocks;
        putValue(NAME, NbBundle.getBundle(DeleteBlockAction.class).getString("DELETE_SELECTION") + " (" + blocks.size() + ")");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (AbstractBlock block : blocks) {
            List<BlockBind> binds = block.getActiveBinds();
            // TODO if !binds.isEmpty() ask user...
            for (BlockBind bind : binds) {
                bind.destroy();
            }
            block.destroy();
        }
    }

    public static DeleteBlockAction build(FlowScene scene) {
        List<AbstractBlock> blocks = Lists.newArrayList();
        for (Object sel : scene.getSelectedObjects()) {
            if (sel instanceof AbstractBlock) {
                AbstractBlock block = (AbstractBlock) sel;
                if (block.getParent() != null) {
                    blocks.add(block);
                }
            }
        }
        return blocks.isEmpty() ? null : new DeleteBlockAction(blocks);
    }
}
