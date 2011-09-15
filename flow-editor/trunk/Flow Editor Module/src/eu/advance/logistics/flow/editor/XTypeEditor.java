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
package eu.advance.logistics.flow.editor;

import com.google.common.collect.Lists;
import eu.advance.logistics.xml.typesystem.XType;
import java.beans.PropertyEditorSupport;
import java.util.List;
import java.util.Map;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author TTS
 */
public class XTypeEditor extends PropertyEditorSupport implements ExPropertyEditor {
    private Map<String, XType> xtypes;

    public XTypeEditor(Map<String, XType> xtypes) {
        this.xtypes = xtypes;
    }

    public XTypeEditor() {
        this(BlockRegistry.getInstance().getXTypes());
    }

    @Override
    public void attachEnv(PropertyEnv env) {
    }

    @Override
    public String getAsText() {
        XType value = (XType) getValue();
        if (value != null) {
            for (Map.Entry<String, XType> e : xtypes.entrySet()) {
                if (e.getValue().equals(value)) {
                    return e.getKey();
                }
            }
        }
        return null;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (text == null) {
            setValue(null);
        } else {
            XType t = xtypes.get(text);
            if (t != null) {
                setValue(t);
            } else {
                throw new java.lang.IllegalArgumentException(text);
            }
        }
    }

    @Override
    public String[] getTags() {
        List<String> keys = Lists.newArrayList(xtypes.keySet());
        keys.add(0, null);
        return keys.toArray(new String[keys.size()]);
    }
    
}
