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
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;

/**
 *
 * @author TTS
 */
public class TypeSupport {

    public static TypeSupport[] create() {
        try {
            return new TypeSupport[]{
                        new TypeSupport("Integer", "advance:integer", Integer.class),
                        new TypeSupport("Boolean", "advance:boolean", Boolean.class),
                        new TypeSupport("Real", "advance:real", Double.class),
                        new TypeSupport("String", "advance:string", String.class),
                        new TypeSupport("Timestamp", "advance:timestamp", Date.class),
                        new TypeSupport("Custom", null, null)
                    };
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    static int find(TypeSupport[] types, AdvanceType target) {
        for (int i = 0; i < types.length; i++) {
            TypeSupport item = types[i];
            if (item.advanceType != null && item.advanceType.equals(target)) {
                return i;
            }
        }
        return (types.length - 1); // last is 'custom'
    }

    static int find(TypeSupport[] types, URI target) {
        for (int i = 0; i < types.length; i++) {
            TypeSupport item = types[i];
            if (item.advanceType != null && item.advanceType.typeURI.equals(target)) {
                return i;
            }
        }
        return (types.length - 1); // last is 'custom'
    }
    public AdvanceType advanceType;
    public String displayName;
    public Class clazz;

    private TypeSupport(String desc, String type, Class clazz) throws URISyntaxException {
        this.clazz = clazz;
        if (type != null) {
            advanceType = new AdvanceType();
            advanceType.typeURI = new URI(type);
            advanceType.type = BlockRegistry.getInstance().getXTypes().get(type);
            displayName = desc + " (" + advanceType.typeURI + ")";
        } else {
            displayName = desc;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }

    
}