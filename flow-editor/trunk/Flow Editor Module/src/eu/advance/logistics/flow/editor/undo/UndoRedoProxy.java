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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.openide.awt.UndoRedo;
import org.openide.util.ChangeSupport;

/**
 *
 * @author TTS
 */
public class UndoRedoProxy implements UndoRedo {

    private final ChangeSupport cs = new ChangeSupport(this);
    private UndoRedo active;
    private ChangeListener changeListener = new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
            cs.fireChange();
        }
    };

    public UndoRedo getActive() {
        return active;
    }

    public void setActive(UndoRedo undoRedo) {
        if (active != null) {
            active.removeChangeListener(changeListener);
        }
        this.active = undoRedo;
        if (active != null) {
            active.addChangeListener(changeListener);
        }
        cs.fireChange();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    @Override
    public String getUndoPresentationName() {
        return active != null ? active.getUndoPresentationName() : ""; // NOI18N
    }

    @Override
    public String getRedoPresentationName() {
        return active != null ? active.getRedoPresentationName() : ""; // NOI18N
    }

    @Override
    public boolean canUndo() {
        return active != null ? active.canUndo() : false;
    }

    @Override
    public boolean canRedo() {
        return active != null ? active.canRedo() : false;
    }

    @Override
    public void undo() throws CannotUndoException {
        if (active != null) {
            active.undo();
        }
    }

    @Override
    public void redo() throws CannotRedoException {
        if (active != null) {
            active.redo();
        }
    }
}