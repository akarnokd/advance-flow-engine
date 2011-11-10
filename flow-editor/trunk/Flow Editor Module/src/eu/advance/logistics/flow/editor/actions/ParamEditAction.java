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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import eu.advance.logistics.flow.editor.model.BlockParameter;
import eu.advance.logistics.flow.editor.undo.CompositeEdit;
import eu.advance.logistics.flow.editor.undo.ParameterChanged;
import eu.advance.logistics.flow.editor.undo.ParameterRenamed;
import eu.advance.logistics.flow.editor.undo.UndoRedoSupport;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;

/**
 *
 * @author TTS
 */
public class ParamEditAction extends AbstractAction {
    /** */
	private static final long serialVersionUID = -4681084550784153831L;
	private UndoRedoSupport undoRedoSupport;
    private BlockParameter parameter;

    public ParamEditAction(UndoRedoSupport urs, BlockParameter param) {
        this.undoRedoSupport = urs;
        this.parameter = param;
        putValue(NAME, NbBundle.getBundle(ParamEditAction.class).getString("EDIT"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String tag = parameter.type.toString().toLowerCase();
        ParameterDescriptionDialog2 dlg = ParameterDescriptionDialog2.create(tag);
        if (dlg == null) {
            return;
        }
        dlg.setTitle("Edit " + tag);
        AdvanceBlockParameterDescription old = parameter.getDescription();
        dlg.setParameterDescription(old);
        dlg.setVisible(true);
        AdvanceBlockParameterDescription d = dlg.getResult();
        if (d != null) {
            if (!parameter.canChangeId(d.id)) {
                NotifyDescriptor nd = new NotifyDescriptor.Message("ID aleady used!");
                DialogDisplayer.getDefault().notify(nd);
                return;
            }
            undoRedoSupport.start();
            CompositeEdit edit = new CompositeEdit((String) getValue(NAME));
            parameter.setDescription(d);
            edit.add(new ParameterChanged(parameter, old, d));
            if (!old.id.equals(d.id)) {
                parameter.setId(d.id);
                edit.add(new ParameterRenamed(parameter, old.id, d.id));
            }
            undoRedoSupport.commit(edit);
        }
    }
}
