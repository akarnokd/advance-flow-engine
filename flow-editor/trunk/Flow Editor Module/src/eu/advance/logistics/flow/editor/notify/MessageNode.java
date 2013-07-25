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
package eu.advance.logistics.flow.editor.notify;

import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 *
 * @author TTS - Technology Transfer System S.r.l.
 */
public class MessageNode extends AbstractNode {

    private Message message;

    public MessageNode(Message msg) {
        super(Children.LEAF);
        this.message = msg;

        setName(message.getDescription());
        if (msg.getType() == MessageType.ERROR) {
            setIconBaseWithExtension("eu/advance/logistics/flow/editor/notify/error.png");
        } else if (msg.getType() == MessageType.WARNING) {
            setIconBaseWithExtension("eu/advance/logistics/flow/editor/notify/warning.png");
        }

    }

    @Override
    public Action[] getActions(boolean context) {
        return message.getActions();
    }

    @Override
    public Sheet createSheet() {
        Sheet s = Sheet.createDefault();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        ss.put(createProperties());
        return s;
    }

    Node.Property[] createProperties() {
        Node.Property[] props = new Node.Property[]{
            new PropertySupport.ReadOnly<String>("description", String.class, make("Description"), null) {
                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return message.getDescription();
                }
            },
            new PropertySupport.ReadOnly<String>("component", String.class, make("Component"), null) {
                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return message.getComponent();
                }
            },
            new PropertySupport.ReadOnly<String>("location", String.class, make("Location"), null) {
                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return message.getLocation();
                }
            }
        };
        props[0].setValue("node", this);
        return props;
    }

    private static String make(String text) {
        return "<html><b>" + text + "</b></html>";
    }
}
