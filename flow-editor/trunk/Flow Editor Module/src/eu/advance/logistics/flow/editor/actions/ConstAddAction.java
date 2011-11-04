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
import eu.advance.logistics.flow.engine.model.fd.AdvanceTypeKind;
import eu.advance.logistics.flow.engine.model.fd.AdvanceConstantBlock;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author TTS
 */
public class ConstAddAction extends AbstractAction {

    private UndoRedoSupport undoRedoSupport;
    private FlowScene scene;
    private CompositeBlock parent;
    private BlockParameter target;
    private Point location;

    public ConstAddAction(UndoRedoSupport urs, FlowScene scene, CompositeBlock parent, BlockParameter target) {
        this.undoRedoSupport = urs;
        this.scene = scene;
        this.parent = parent;
        this.target = target;
        putValue(NAME, NbBundle.getBundle(ConstAddAction.class).getString("ADD_CONSTANT"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        AdvanceBlockParameterDescription d = target.getDescription();
        if (d.type.getKind() != AdvanceTypeKind.CONCRETE_TYPE) {
            JPopupMenu popup = new JPopupMenu();
               
            addConstantItem(popup, "Boolean", "advance:boolean");
            addConstantItem(popup, "Integer", "advance:integer");
            addConstantItem(popup, "Real", "advance:real");
            addConstantItem(popup, "String", "advance:string");
            addConstantItem(popup, "Timestamp", "advance:timestamp");
            
            Point pm = MouseInfo.getPointerInfo().getLocation();
            
            popup.show(null, pm.x, pm.y);
            return;
        }
        AdvanceConstantBlock c = getAdvanceConstantBlock();
        if (c == null) {
            return;
        }
        placeConstantBlock(c);
    }
    /**
     * Add an item to the popup menu with the appropriate action.
     * @param popup the popup menu
     * @param title the entry title
     * @param typeURI the type URI of the constant
     */
    void addConstantItem(final JPopupMenu popup, String title, final String typeURI) {
        JMenuItem mi = new JMenuItem(title);
        popup.add(mi);
        mi.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                popup.setVisible(false);
                placeConstantBlock(getAdvanceConstantBlock(typeURI));
            }
            
        });
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
    /**
     * Create a constant block with the given base type (e.g., advance:string).
     * @param baseTypeURI the base type name
     * @return the constant block or null if the user cancelled the editor box
     */
    private AdvanceConstantBlock getAdvanceConstantBlock(String baseTypeURI) {
        AdvanceConstantBlock c = new AdvanceConstantBlock();
        c.id = parent.generateConstantId();
        try {
            c.typeURI = new URI(baseTypeURI);
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
        c.type = BlockRegistry.getInstance().resolveSchema(c.typeURI);
        c.value = new XElement(c.typeURI.getSchemeSpecificPart());
        c.value.content = "";
        c.value.content = EditSupport.edit(c.value.content, c.typeURI);
        return c.value.content != null ? c : null;
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
                c.typeURI = new URI("advance:string");
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
