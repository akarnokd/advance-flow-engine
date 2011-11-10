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

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 *
 * @author TTS
 */
public abstract class UndoableEdit extends AbstractUndoableEdit {

    /** */
	private static final long serialVersionUID = 6525416198659035817L;

	protected abstract void restore(boolean redo);

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        restore(true);
    }
    
    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        restore(false);
    }

    @Override
    public void die() {
        super.die();
    }
}
