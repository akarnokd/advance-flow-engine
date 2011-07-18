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

import eu.advance.logistics.flow.model.AdvanceBlockDescription;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.NodeTransfer;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author TTS
 */
public class BlockNode extends AbstractNode {

    private AdvanceBlockDescription blockDescription;

    public BlockNode(AdvanceBlockDescription block) {
        super(Children.LEAF, Lookups.fixed(new Object[]{block}));
        this.blockDescription = block;
        setIconBaseWithExtension("eu/advance/logistics/flow/editor/palette/images/block.png");
        setDisplayName(block.displayName);
    }

    @Override
    public Transferable drag() throws IOException {
        return NodeTransfer.transferable(this, NodeTransfer.DND_COPY_OR_MOVE);
    }
}
