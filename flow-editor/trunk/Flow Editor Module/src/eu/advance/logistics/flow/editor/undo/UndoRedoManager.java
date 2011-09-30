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

import eu.advance.logistics.flow.editor.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import org.openide.awt.UndoRedo;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * @author TTS
 */
public class UndoRedoManager extends UndoManager implements UndoRedo {

    private final ChangeSupport cs = new ChangeSupport(this);
    private Mutex.ExceptionAction runUndo = new Mutex.ExceptionAction() {

        @Override
        public Object run() throws Exception {
            superUndo();
            return null;
        }
    };
    private Mutex.ExceptionAction runRedo = new Mutex.ExceptionAction() {

        @Override
        public Object run() throws Exception {
            superRedo();
            return null;
        }
    };

    private void superUndo() throws CannotUndoException {
        super.undo();
    }

    private void superRedo() throws CannotRedoException {
        super.redo();
    }

    @Override
    public void undo() throws CannotUndoException {
        if (java.awt.EventQueue.isDispatchThread()) {
            superUndo();
        } else {
            try {
                Mutex.EVENT.readAccess(runUndo);
            } catch (MutexException ex) {
                Exception e = ex.getException();
                if (e instanceof CannotUndoException) {
                    throw (CannotUndoException) e;
                } else // should not happen, ignore
                {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    @Override
    public void redo() throws CannotRedoException {
        if (java.awt.EventQueue.isDispatchThread()) {
            superRedo();
        } else {
            try {
                Mutex.EVENT.readAccess(runRedo);
            } catch (MutexException ex) {
                Exception e = ex.getException();
                if (e instanceof CannotRedoException) {
                    throw (CannotRedoException) e;
                } else // should not happen, ignore
                {
                    Exceptions.printStackTrace(e);
                }
            }
        }
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
        return this.canUndo() ? super.getUndoPresentationName() : ""; // NOI18N
    }

    @Override
    public String getRedoPresentationName() {
        return this.canRedo() ? super.getRedoPresentationName() : ""; // NOI18N
    }

    public void addEditAndNotify(UndoableEdit edit) {
        undoableEditHappened(new UndoableEditEvent(this, edit));
        cs.fireChange();
    }
}