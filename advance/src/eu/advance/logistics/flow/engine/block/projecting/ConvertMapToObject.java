/*
 * Copyright 2010-2013 The Advance EU 7th Framework project consortium
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

package eu.advance.logistics.flow.engine.block.projecting;

import java.util.Map;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.AdvanceData;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Converts a map of key-value pairs into a single-element XML with attributes or child elements as the given map keys and values.
 * @author karnokd, 2012.02.22.
 */
@Block(id = "ConvertMapToObject", category = "projection", 
scheduler = "NOW", 
description = "Converts a map of key-value pairs into a single-element XML with attributes or child elements as the given map keys and values.",
parameters = { "T" }
)
public class ConvertMapToObject extends AdvanceBlock {
    /** The input map. */
    @Input("advance:map<advance:string,advance:object>")
    protected static final String VALUE = "value";
    /** The type constructor. */
    @Input("advance:type<?T>")
    protected static final String TYPE = "type";
    /** Make attributes or child elements? */
    @Input("advance:boolean")
    protected static final String ATTRIBUTES = "attributes";
    /** The output value. */
    @Output("?T")
    protected static final String OUT = "out";
	@Override
	protected void invoke() {
		boolean attr = getBoolean(ATTRIBUTES);
		Map<XElement, XElement> map = resolver().getMap(get(VALUE));
		
		XElement type = get(TYPE);
		String ta = type.get("type");
		if (ta.contains(":")) {
			ta = ta.substring(ta.indexOf(':') + 1);
		}
		
		XElement result = new XElement(ta);
		
		for (Map.Entry<XElement, XElement> e : map.entrySet()) {
			XElement other = e.getValue();
			if (attr) {
				if (other.hasChildren() || other.hasAttributes()) {
					log(new IllegalArgumentException("Value is not simple value type for attribute representation: " + e.getKey() + " " + other));
				} else {
					result.set(resolver().getString(e.getKey()), resolver().getString(other));
				}
			} else {
				result.add(AdvanceData.rename(e.getValue(), resolver().getString(e.getKey())));
			}
		}
		dispatch(OUT, result);
	}
}
