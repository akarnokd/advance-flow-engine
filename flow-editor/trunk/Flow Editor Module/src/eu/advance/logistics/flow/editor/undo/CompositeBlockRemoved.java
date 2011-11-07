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
package eu.advance.logistics.flow.editor.undo;

import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.model.FlowDescriptionChange;
import java.awt.Point;

/**
 *
 * @author TTS
 */
public class CompositeBlockRemoved extends UndoableEdit {

    private CompositeBlock parent;
    private CompositeBlock block;

    public CompositeBlockRemoved(CompositeBlock parent, CompositeBlock block) {
        this.parent = parent;
        this.block = block;
    }

    @Override
    protected void restore(boolean redo) {
        if (redo) {
            parent.removeComposite(block);
        } else {
            Point p = block.getLocation();
            parent.addComposite(block);
            block.setLocation(p);
            parent.getFlowDiagram().fire(FlowDescriptionChange.BLOCK_MOVED, block);
        }
    }

    @Override
    public String getPresentationName() {
        return "Remove composite block";
    }

    @Override
    public void die() {
        super.die();
        parent = null;
        block = null;
    }
}
