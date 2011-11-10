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

import java.awt.Point;

import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.editor.model.FlowDescriptionChange;

/**
 *
 * @author TTS
 */
public class ConstantBlockRemoved extends UndoableEdit {

    /** */
	private static final long serialVersionUID = -3550248189293867769L;
	private CompositeBlock parent;
    private ConstantBlock block;

    public ConstantBlockRemoved(CompositeBlock parent, ConstantBlock block) {
        this.parent = parent;
        this.block = block;
    }

    @Override
    protected void restore(boolean redo) {
        if (redo) {
            parent.removeConstant(block);
        } else {
            Point p = block.getLocation();
            parent.addConstant(block);
            block.setLocation(p);
            parent.getFlowDiagram().fire(FlowDescriptionChange.BLOCK_MOVED, block);
        }
    }

    @Override
    public String getPresentationName() {
        return "Remove constant block";
    }

    @Override
    public void die() {
        super.die();
        parent = null;
        block = null;
    }
}
