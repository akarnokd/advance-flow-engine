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
import eu.advance.logistics.flow.editor.diagram.FlowScene;
import eu.advance.logistics.flow.editor.model.BlockParameter;
import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.model.FlowDescription;
import eu.advance.logistics.flow.editor.undo.BlockMoved;
import eu.advance.logistics.flow.editor.undo.CompositeBlockAdded;
import eu.advance.logistics.flow.editor.undo.CompositeEdit;
import eu.advance.logistics.flow.editor.undo.ParameterCreated;
import eu.advance.logistics.flow.editor.undo.UndoRedoSupport;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;
import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;

/**
 *
 * @author TTS
 */
public class GroupBlockAction extends AbstractAction {

    private FlowScene scene;
    private FlowDescription flowDescription;
    private Point location;

    public GroupBlockAction(FlowScene scene, FlowDescription fd) {
        this.scene = scene;
        this.flowDescription = fd;
        putValue(NAME, NbBundle.getBundle(GroupBlockAction.class).getString("CREATE_GROUP"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
//        Set sel = scene.getSelectedObjects();
//        if (sel.size() > 1) {
//            CompositeBlock cb = flowDescription.createComposite();
//            for (Object obj : sel) {
//                if (obj instanceof SimpleBlock) {
//                    SimpleBlock block = (SimpleBlock)obj;
//                    block.destroy();
//                }
//            }
//        }
        UndoRedoSupport urs = scene.getUndoRedoSupport();
        urs.start();
        String name = NbBundle.getBundle(GroupBlockAction.class).getString("CREATE_GROUP");
        CompositeEdit edit = new CompositeEdit(name);
        CompositeBlock parent = flowDescription.getActiveBlock();
        CompositeBlock cb = parent.createComposite();
        edit.add(new CompositeBlockAdded(parent, cb));
        if (location != null) {
            Point old = cb.getLocation();
            cb.setLocation(location);
            edit.add(new BlockMoved(cb, old, location));
        }
        BlockParameter param;
        AdvanceBlockParameterDescription inParam = new AdvanceBlockParameterDescription();
        inParam.type = BlockRegistry.getInstance().getDefaultAdvanceType();
        param = cb.createInput(inParam);
        edit.add(new ParameterCreated(cb, param));

        AdvanceBlockParameterDescription outParam = new AdvanceBlockParameterDescription();
        outParam.type = BlockRegistry.getInstance().getDefaultAdvanceType();
        param = cb.createOutput(outParam);
        edit.add(new ParameterCreated(cb, param));
        
        urs.commit(edit);
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
}
