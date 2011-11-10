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
package eu.advance.logistics.flow.editor.actions;

import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.openide.util.NbBundle;

import eu.advance.logistics.flow.editor.model.BlockParameter;
import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.undo.ParameterCreated;
import eu.advance.logistics.flow.editor.undo.UndoRedoSupport;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;

/**
 *
 * @author TTS
 */
public class ParamAddAction extends AbstractAction {
    /** */
	private static final long serialVersionUID = 8159161494886177256L;
	private UndoRedoSupport undoRedoSupport;
    private CompositeBlock block;
    private BlockParameter.Type type;
    private Point location;

    public ParamAddAction(UndoRedoSupport urs, CompositeBlock block, BlockParameter.Type type) {
        this.undoRedoSupport = urs;
        this.block = block;
        this.type = type;
        putValue(NAME, NbBundle.getBundle(ParamAddAction.class).getString("ADD") + type + "...");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String tag = type.name().toLowerCase();
        ParameterDescriptionDialog2 dlg = ParameterDescriptionDialog2.create(tag);
        if (dlg == null) {
            return;
        }
        dlg.setTitle("Add new " + tag);
        dlg.setVisible(true);
        AdvanceBlockParameterDescription desc = dlg.getResult();
        if (desc == null) {
            return;
        }

        undoRedoSupport.start();
        BlockParameter param = null;
        if (type == BlockParameter.Type.INPUT) {
            param = block.createInput(desc);
        } else if (type == BlockParameter.Type.OUTPUT) {
            param = block.createOutput(desc);
        }

        // check param: createInput/createOutput can return null
        if (param != null) {
            undoRedoSupport.commit(new ParameterCreated(block, param));
        }
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
}
