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
import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.engine.model.fd.AdvanceConstantBlock;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XType;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.AbstractAction;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author TTS
 */
public class ConstAddAction extends AbstractAction {

    private FlowScene scene;
    private CompositeBlock parent;
    private BlockParameter target;
    private Point location;

    public ConstAddAction(FlowScene scene, CompositeBlock parent, BlockParameter target) {
        this.scene = scene;
        this.parent = parent;
        this.target = target;
        putValue(NAME, NbBundle.getBundle(ConstAddAction.class).getString("ADD_CONSTANT"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        AdvanceConstantBlock c = getAdvanceConstantBlock();
        if (c == null) {
            return;
        }

        ConstantBlock block = parent.createConstant(c.id);
        block.setConstant(c);

        parent.createBind(block.getParameter(), target);

        Widget w1 = scene.findWidget(target);
        Widget w2 = scene.findWidget(block);
        if (w1 != null && w2 != null) {
            Point loc = w1.convertLocalToScene(w1.getLocation());
            Rectangle r = w2.getBounds();
            loc.x -= r.width + 80;
            w2.setPreferredLocation(w2.convertSceneToLocal(loc));
            scene.validate();
            scene.repaint();
        }

    }

    private AdvanceConstantBlock getAdvanceConstantBlock() {
        String value = getDefault();
        while (true) {
            EditDialog dlg = new EditDialog();
            dlg.setDefaultValue(value);
            dlg.setVisible(true);
            value = dlg.getValue();
            if (value == null) {
                return null;
            }
            try {
                XElement xe = XElement.parseXML(new StringReader(value));
                AdvanceConstantBlock c = new AdvanceConstantBlock();
                c.load(xe);
                return c;
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private String getDefault() {
        AdvanceConstantBlock c = new AdvanceConstantBlock();
        c.id = parent.generateConstantId();
        try {
            c.typeURI = new URI("advance:integer");
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }

        c.type = BlockRegistry.getInstance().resolveSchema(c.typeURI);
        c.value = new XElement("integer");
        c.value.content = Integer.toString(5);
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
