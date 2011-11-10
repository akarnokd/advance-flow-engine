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

import java.util.List;

import com.google.common.collect.Lists;

/**
 *
 * @author TTS
 */
public class CompositeEdit extends UndoableEdit {

    /** */
	private static final long serialVersionUID = 6102450412561633481L;
	private String name;
    private List<UndoableEdit> edits = Lists.newArrayList();

    public CompositeEdit(String name) {
        this.name = name;
    }

    public void add(UndoableEdit edit) {
        edits.add(edit);
    }

    @Override
    protected void restore(boolean redo) {
        if (redo) {
            for (UndoableEdit edit : edits) {
                edit.restore(true);
            }
        } else {
            for (int i = edits.size() - 1; i >= 0; i--) {
                edits.get(i).restore(false);
            }
        }
    }

    @Override
    public String getPresentationName() {
        return name;
    }

    @Override
    public void die() {
        super.die();
        for (UndoableEdit edit : edits) {
            edit.die();
        }
    }
}