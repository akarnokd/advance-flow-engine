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

import com.google.common.collect.Lists;
import java.util.List;

/**
 *
 * @author TTS
 */
public class CompositeEdit extends UndoableEdit {

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
        for (UndoableEdit edit : edits) {
            edit.restore(redo);
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