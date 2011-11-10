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

import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.editor.model.FlowDescriptionChange;
import eu.advance.logistics.flow.editor.model.FlowDescriptionListener;

/**
 *
 * @author TTS
 */
class ConstantBlockNode extends AbstractNode implements FlowDescriptionListener {

    private ConstantBlock block;

    ConstantBlockNode(ConstantBlock block) {
        super(Children.LEAF);
        this.block = block;

        setIconBaseWithExtension("eu/advance/logistics/flow/editor/palette/images/const_block.png");
        updateName();

        block.getFlowDiagram().addListener(this);
    }

    private void updateName() {
        String name = block.getTypeAsString() + "=" + block.getValueAsString();
        setName(block.getId());
        setDisplayName(name);
    }

    @Override
    public void flowDescriptionChanged(FlowDescriptionChange event, Object... params) {
        switch (event) {
            case BLOCK_RENAMED:
            case CONSTANT_BLOCK_CHANGED:
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
}
