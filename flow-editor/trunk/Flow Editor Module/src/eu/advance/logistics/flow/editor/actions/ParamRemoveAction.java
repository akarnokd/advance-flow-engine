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

import eu.advance.logistics.flow.editor.model.AbstractBlock;
import eu.advance.logistics.flow.editor.model.BlockParameter;
import eu.advance.logistics.flow.editor.undo.ParameterRemoved;
import eu.advance.logistics.flow.editor.undo.UndoRedoSupport;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author TTS
 */
public class ParamRemoveAction extends AbstractAction {

    private UndoRedoSupport undoRedoSupport;
    private BlockParameter parameter;

    public ParamRemoveAction(UndoRedoSupport urs, BlockParameter param) {
        this.undoRedoSupport = urs;
        this.parameter = param;
        putValue(NAME, NbBundle.getBundle(ParamRemoveAction.class).getString("REMOVE"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getBundle(ParamRemoveAction.class).getString("REMOVE_CONFIRM") + parameter.getDisplayName() + "?",
                NotifyDescriptor.YES_NO_OPTION);
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
            undoRedoSupport.start();
            AbstractBlock owner = parameter.owner;
            parameter.destroy();
            undoRedoSupport.commit(new ParameterRemoved(owner, parameter));
        }
    }
}
