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

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.AdvanceData;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Converts a collection map of key-value pairs into a collection of single-element XML with attributes or child elements as the given map keys and values.
 * @author karnokd, 2012.02.22.
 */
@Block(id = "ConvertMapToObject", category = "projection", 
scheduler = "NOW", 
description = "Converts a map of key-value pairs into a single-element XML with attributes or child elements as the given map keys and values.",
parameters = { "T" }
)
public class ConvertMapsToObjects extends AdvanceBlock {
    /** The input map. */
    @Input("advance:collection<advance:map<advance:string,advance:object>>")
    protected static final String VALUE = "value";
    /** The type constructor. */
    @Input("advance:type<?T>")
    protected static final String TYPE = "type";
    /** Make attributes or child elements? */
    @Input("advance:boolean")
    protected static final String ATTRIBUTES = "attributes";
    /** The output value. */
    @Output("advance:collection<?T>")
    protected static final String OUT = "out";
	@Override
	protected void invoke() {
		boolean attr = getBoolean(ATTRIBUTES);

		XElement type = get(TYPE);
		String elementNames = type.get("type");
		int idx = elementNames.indexOf(':');
		if (idx > 0) {
			elementNames = elementNames.substring(idx + 1);
		}
		
		XElement coll = get(VALUE);
		
		List<XElement> result = Lists.newLinkedList();
		
		for (XElement ce : resolver().getItems(coll)) {
			Map<XElement, XElement> map = resolver().getMap(ce);
			
			
			XElement re = new XElement(elementNames);
			
			for (Map.Entry<XElement, XElement> e : map.entrySet()) {
				XElement other = e.getValue();
				if (attr) {
					if (other.hasChildren() || other.hasAttributes()) {
						log(new IllegalArgumentException("Value is not simple value type for attribute representation: " + e.getKey() + " " + other));
					} else {
						re.set(resolver().getString(e.getKey()), resolver().getString(other));
					}
				} else {
					re.add(AdvanceData.rename(e.getValue(), resolver().getString(e.getKey())));
				}
			}
			
			result.add(re);
		}
		
		dispatch(OUT, resolver().create(result));
	}
}
