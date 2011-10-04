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

import eu.advance.logistics.flow.editor.model.BlockParameter;

/**
 *
 * @author TTS
 */
public class ParameterRenamed extends UndoableEdit {

    private BlockParameter parameter;
    private String old;
    private String id;
    private String name;

    public ParameterRenamed(BlockParameter parameter, String old, String id) {
        this.parameter = parameter;
        this.old = old;
        this.id = id;
        name = "Rename " + parameter.type.toString().toLowerCase();
    }

    @Override
    protected void restore(boolean redo) {
        parameter.setId(redo ? id : old);
    }

    @Override
    public String getPresentationName() {
        return name;
    }

    @Override
    public void die() {
        super.die();
        parameter = null;
    }
}