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

import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.model.AdvanceConstantBlock;

/**
 *
 * @author TTS
 */
public class ConstantBlockChanged extends UndoableEdit {

    private ConstantBlock block;
    private AdvanceConstantBlock old;
    private AdvanceConstantBlock current;

    public ConstantBlockChanged(ConstantBlock block, AdvanceConstantBlock old, AdvanceConstantBlock current) {
        this.block = block;
        this.old = old;
        this.current = current;
    }

    @Override
    protected void restore(boolean redo) {
        if (redo) {
            block.setConstant(current);
        } else {
            block.setConstant(old);
        }
    }

    @Override
    public String getPresentationName() {
        return "Edit constant block";
    }

    @Override
    public void die() {
        super.die();
        block = null;
        old = null;
        current = null;
    }
}
