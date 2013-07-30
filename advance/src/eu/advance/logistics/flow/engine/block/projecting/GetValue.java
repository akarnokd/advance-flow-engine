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

import hu.akarnokd.utils.xml.XNElement;

import java.util.Map;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * Get a value by the given key from the map or indicate if no such element
 * exists. Signature: GetValue(map<t, u>, t) -> (u, boolean)
 *
 * @author TTS
 */
@Block(id = "GetValue", 
	category = "projection", 
	scheduler = "IO", 
	description = "Get a value by the given key from the map or indicate if no such element exists", 
	parameters = { "K", "V" } 
)
public class GetValue extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(GetValue.class.getName());
    /**
     * In.
     */
    @Input("advance:map<?K, ?V>")
    protected static final String MAP = "map";
    /**
     * In.
     */
    @Input("?K")
    protected static final String KEY = "key";
    /**
     * Out.
     */
    @Output("?V")
    protected static final String OUT_VALUE = "out_value";
    /**
     * Out.
     */
    @Output("advance:boolean")
    protected static final String OUT_STATUS = "out_status";

    @Override
    protected void invoke() {
        final Map<XNElement, XNElement> map = resolver().getMap(get(MAP));
        final XNElement obj = map.get(get(KEY));

        dispatch(OUT_VALUE, resolver().create(obj));
        dispatch(OUT_STATUS, resolver().create((obj != null)));
    }
}
