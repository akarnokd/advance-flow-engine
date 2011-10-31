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

import eu.advance.logistics.flow.editor.model.FlowDescriptionChange;
import eu.advance.logistics.flow.editor.model.FlowDescriptionListener;
import javax.swing.event.UndoableEditEvent;
import org.openide.awt.UndoRedo;

/**
 *
 * @author TTS
 */
final public class UndoRedoSupport implements FlowDescriptionListener {

    private UndoRedo.Manager undoRedoManager = new UndoRedo.Manager();
    private boolean changes;

    public UndoRedoSupport() {
    }

    public UndoRedo getUndoRedo() {
        return undoRedoManager;
    }

    @Override
    public void flowDescriptionChanged(FlowDescriptionChange event, Object... params) {
        changes = true;
    }

    public void start() {
        changes = false;
    }

    public void commit(UndoableEdit edit) {
        if (changes) {
            undoRedoManager.undoableEditHappened(new UndoableEditEvent(this, edit));
        } else {
            System.out.println(edit.getPresentationName() + " has no effect!");
        }
    }
}
