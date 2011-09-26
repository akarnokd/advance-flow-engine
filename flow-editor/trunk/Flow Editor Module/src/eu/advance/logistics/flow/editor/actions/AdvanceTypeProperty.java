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

import eu.advance.logistics.flow.model.AdvanceConstantBlock;
import eu.advance.logistics.flow.model.AdvanceResolver;
import eu.advance.logistics.flow.model.AdvanceType;
import eu.advance.logistics.xml.typesystem.XElement;
import eu.advance.logistics.xml.typesystem.XType;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author TTS
 */
public class AdvanceTypeProperty {

    static Node.Property variableNameProperty(final AdvanceType target) {
        Node.Property p = new PropertySupport.ReadWrite<String>("variableName", String.class, "variableName", "variableName") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return target != null ? target.typeVariableName : null;
            }

            @Override
            public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                if (target != null) {
                    target.typeVariableName = val;
                }
            }
        };
        p.setValue("suppressCustomEditor", Boolean.TRUE);
        p.setValue("nullValue", "(null)");
        return p;
    }

    static Node.Property typeProperty(final AdvanceType target) {
        Node.Property p = new PropertySupport.ReadWrite<XType>("type", XType.class, "type", "type") {

            @Override
            public XType getValue() throws IllegalAccessException, InvocationTargetException {
                return target != null ? target.type : null;
            }

            @Override
            public void setValue(XType val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                if (target != null) {
                    target.type = val;
                }
            }
        };
        p.setValue("suppressCustomEditor", Boolean.TRUE);
        return p;
    }

    static Node.Property uriProperty(final AdvanceType target) {
        Node.Property p = new PropertySupport.ReadOnly<String>("uri", String.class, "uri", "uri") {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return target != null && target.typeURI != null ? target.typeURI.toString() : null;
            }
        };
        p.setValue("suppressCustomEditor", Boolean.TRUE);
        p.setValue("nullValue", "(null)");
        return p;
    }

    static void dump(AdvanceType t) {
        System.out.println("type " + t.type);
        System.out.println("typeVariableName " + t.typeVariableName);
        System.out.println("typeArguments " + t.typeArguments);
        System.out.println("typeURI " + t.typeURI);
        System.out.println("typeVariable " + t.typeVariable);
        for (AdvanceType x : t.typeArguments) {
            dump(x);
        }
        System.out.println();
    }

    static AdvanceConstantBlock createConstant() throws URISyntaxException {
        AdvanceConstantBlock c = new AdvanceConstantBlock();
        c.id = "constant1";
        c.typeURI = new URI("advance:integer");
        c.type = AdvanceResolver.resolveSchema(c.typeURI);
        c.value = new XElement("integer");
        c.value.content = Integer.toString(5);
        return c;
    }
}
