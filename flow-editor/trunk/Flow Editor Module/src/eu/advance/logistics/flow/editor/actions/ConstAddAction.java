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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;

import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import eu.advance.logistics.flow.editor.BlockRegistry;
import eu.advance.logistics.flow.editor.diagram.FlowScene;
import eu.advance.logistics.flow.editor.model.BlockBind;
import eu.advance.logistics.flow.editor.model.BlockParameter;
import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.editor.undo.BindCreated;
import eu.advance.logistics.flow.editor.undo.BlockMoved;
import eu.advance.logistics.flow.editor.undo.CompositeEdit;
import eu.advance.logistics.flow.editor.undo.ConstantBlockAdded;
import eu.advance.logistics.flow.editor.undo.UndoRedoSupport;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceConstantBlock;
import eu.advance.logistics.flow.engine.model.fd.AdvanceTypeKind;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 *
 * @author TTS
 */
public class ConstAddAction extends AbstractAction {
    /** */
	private static final long serialVersionUID = -7737062280779360996L;
	private UndoRedoSupport undoRedoSupport;
    private FlowScene scene;
    private CompositeBlock parent;
    private BlockParameter target;
    private Point location;
    /** To override the target's URI if non-null. */
    private String constantURI;

    public ConstAddAction(UndoRedoSupport urs, FlowScene scene, CompositeBlock parent, BlockParameter target, 
            String constantName, String constantURI) {
        this.undoRedoSupport = urs;
        this.scene = scene;
        this.parent = parent;
        this.target = target;
        this.constantURI = constantURI;
        
        putValue(NAME, String.format(NbBundle.getBundle(ConstAddAction.class).getString("ADD_CONSTANT"), constantName));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        AdvanceConstantBlock c = getAdvanceConstantBlock();
        if (c == null) {
            return;
        }
        placeConstantBlock(c);
    }
    void placeConstantBlock(AdvanceConstantBlock c) {
                undoRedoSupport.start();
        String name = NbBundle.getBundle(ConstAddAction.class).getString("ADD_CONSTANT");
        CompositeEdit edit = new CompositeEdit(name);

        ConstantBlock block = parent.createConstant(c.id);
        block.setConstant(c);
        edit.add(new ConstantBlockAdded(parent, block));

        BlockBind bind = parent.createBind(block.getParameter(), target);
        edit.add(new BindCreated(parent, bind));

        Widget w1 = scene.findWidget(target);
        Widget w2 = scene.findWidget(block);
        if (w1 != null && w2 != null) {
            Point loc = w1.convertLocalToScene(w1.getLocation());
            Rectangle r = w2.getBounds();
            loc.x -= r.width + 80;
            loc = w2.convertSceneToLocal(loc);
            //w2.setPreferredLocation(loc);
            Point old = block.getLocation();
            block.setLocation(loc);
            edit.add(new BlockMoved(block, old, loc));
            scene.validate();
            scene.repaint();
        }

        undoRedoSupport.commit(edit);
    }
    private AdvanceConstantBlock getAdvanceConstantBlock() {
        AdvanceConstantBlock c = new AdvanceConstantBlock();
        c.id = parent.generateConstantId();
        AdvanceBlockParameterDescription d = target.getDescription();
        if (d.type.getKind() == AdvanceTypeKind.CONCRETE_TYPE) {
            c.typeURI = d.type.typeURI;
            c.type = d.type.type;
        } else {
            try {
                c.typeURI = new URI(constantURI);
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
            c.type = BlockRegistry.getInstance().resolveSchema(c.typeURI);
        }
        c.value = new XElement(c.typeURI.getSchemeSpecificPart());
        c.value.content = "";
        c.value.content = EditSupport.edit(c.value.content, c.typeURI);
        return c.value.content != null ? c : null;
    }

    public static String convertToXml(AdvanceConstantBlock c) {
        XElement temp = new XElement("constant");
        c.save(temp);
        return temp.toString();
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
}
