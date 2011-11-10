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

import eu.advance.logistics.flow.editor.model.BlockBind;
import eu.advance.logistics.flow.editor.model.CompositeBlock;

/**
 *
 * @author TTS
 */
public class BindCreated extends UndoableEdit {
    /** */
	private static final long serialVersionUID = -1804006990397520240L;
	private CompositeBlock parent;
    private BlockBind bind;

    public BindCreated(CompositeBlock parent, BlockBind bind) {
        this.parent = parent;
        this.bind = bind;
    }

    @Override
    protected void restore(boolean redo) {
        if (redo) {
            parent.addBind(bind);
        } else {
            parent.removeBind(bind);
        }
    }

    @Override
    public String getPresentationName() {
        return "Add connection";
    }

    @Override
    public void die() {
        super.die();
        parent = null;
        bind = null;
    }
}
