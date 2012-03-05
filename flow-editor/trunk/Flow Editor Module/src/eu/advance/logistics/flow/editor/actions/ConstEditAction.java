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

import eu.advance.logistics.flow.editor.BlockRegistry;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.openide.util.NbBundle;

import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.editor.undo.ConstantBlockChanged;
import eu.advance.logistics.flow.editor.undo.UndoRedoSupport;
import eu.advance.logistics.flow.engine.block.AdvanceData;
import eu.advance.logistics.flow.engine.model.fd.AdvanceConstantBlock;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.xml.XElement;
import java.net.URISyntaxException;
import org.openide.util.Exceptions;

/**
 *
 * @author TTS
 */
public class ConstEditAction extends AbstractAction {
    /** */
	private static final long serialVersionUID = -1618036093974995937L;
	private UndoRedoSupport undoRedoSupport;
    private ConstantBlock target;

    public ConstEditAction(UndoRedoSupport urs, ConstantBlock target) {
        this.undoRedoSupport = urs;
        this.target = target;
        putValue(NAME, NbBundle.getBundle(ConstEditAction.class).getString("EDIT"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        AdvanceConstantBlock newConstant;
        AdvanceConstantBlock currentConstant = target.getConstant();
        try {
            AdvanceType at = currentConstant.getAdvanecType();
            if (AdvanceData.TYPE.equals(at.typeURI)) {
                XElement value = EditType.edit(currentConstant.value);
                if (value == null) {
                    return;
                }
                newConstant = clone(target.getConstant());
                newConstant.typeString = EditType.createTypeConstructor(value);
                newConstant.value = value;
            } else {
                String content = EditSupport.edit(currentConstant.value.content, at.typeURI);
                if (content == null) {
                    return;
                }
                newConstant = clone(target.getConstant());
                newConstant.value.content = content;
            }
            undoRedoSupport.start();
            target.setConstant(newConstant);
            undoRedoSupport.commit(new ConstantBlockChanged(target, currentConstant, newConstant));

            target.getFlowDiagram().setCompilationResult(BlockRegistry.getInstance()
            .verify(target.getFlowDiagram().build()));
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static AdvanceConstantBlock clone(AdvanceConstantBlock source) {
        AdvanceConstantBlock cloned = new AdvanceConstantBlock();
        XElement temp = new XElement("constant");
        source.save(temp);
        cloned.load(temp);
        return cloned;
    }
}
