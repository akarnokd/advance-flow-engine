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

import eu.advance.logistics.flow.editor.model.AbstractBlock;

/**
 *
 * @author TTS
 */
public class BlockMoved extends UndoableEdit {
    /** */
	private static final long serialVersionUID = -3977837742592659424L;
	private AbstractBlock block;
    private Point oldLocation;
    private Point newLocation;

    public BlockMoved(AbstractBlock block, Point oldLocation, Point newLocation) {
        this.block = block;
        this.oldLocation = oldLocation;
        this.newLocation = newLocation;
    }

    @Override
    public void restore(boolean redo) {
        block.setLocation(redo ? newLocation : oldLocation);
    }

    @Override
    public String getPresentationName() {
        return "Move block";
    }

    @Override
    public void die() {
        super.die();
        block = null;
    }
}
