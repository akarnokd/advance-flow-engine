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
package eu.advance.logistics.flow.engine.block.streaming;

import hu.akarnokd.utils.xml.XNElement;

import java.util.Map;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;

/**
 * Returns a new map with the given key-value pair added to the map. Signature:
 * AppendMap(map<t, u>, t, u) -> map<t, u>
 *
 * @author TTS
 */
@Block(id = "AppendMap", category = "streaming", scheduler = "IO",
        description = "Returns a new map with the given key-value pair added to the map", parameters = { "K", "V" })
public class AppendMap extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(AppendMap.class.getName());
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
     * In.
     */
    @Input("?V")
    protected static final String VALUE = "in3";
    /**
     * Out.
     */
    @Output("advance:map<?K, ?V>")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final Map<XNElement, XNElement> map = resolver().getMap(get(MAP));
        final XNElement key = get(KEY);
        final XNElement value = get(VALUE);

        map.put(key, value);

        dispatch(OUT, resolver().create(map));
    }
}
