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
package eu.advance.logistics.flow.editor.tree;

import java.io.IOException;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

import eu.advance.logistics.flow.editor.BlockRegistry;
import eu.advance.logistics.flow.editor.model.BlockCategory;
import eu.advance.logistics.flow.editor.model.FlowDescriptionChange;
import eu.advance.logistics.flow.editor.model.FlowDescriptionListener;
import eu.advance.logistics.flow.editor.model.SimpleBlock;

/**
 *
 * @author TTS
 */
public class SimpleBlockNode extends AbstractNode implements FlowDescriptionListener {

    private SimpleBlock block;

    SimpleBlockNode(SimpleBlock block) {
        super(Children.LEAF);
        this.block = block;

        BlockCategory cat = BlockRegistry.getInstance().findByType(block.description);
        if (cat != null) {
            setIconBaseWithExtension(cat.getImagePath());
        }
        updateName();

        block.getFlowDiagram().addListener(this);
    }

    private void updateName() {
        setName(block.getId());
        setDisplayName(block.getId());
    }

    @Override
    public void flowDescriptionChanged(FlowDescriptionChange event, Object... params) {
        switch (event) {
            case BLOCK_RENAMED:
                if (params[0] == block) {
                    updateName();
                }
                break;
        }
    }

    @Override
    public void destroy() throws IOException {
        super.destroy();
        block = null;
    }
    /**
     * @return the referenced block
     */
    public SimpleBlock getBlock() {
        return block;
    }
}
