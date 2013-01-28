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

import java.util.Map;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Remove the given key and value pair from the map and return the new map.
 * Signature: RemoveEntry(map<t, u>, t, u) -> (map<t, u>, boolean)
 *
 * @author TTS
 */
@Block(id = "RemoveEntry", 
	category = "streaming", 
	scheduler = "IO", 
	description = "Remove the given key and value pair from the map and return the new map.", 
	parameters = { "K", "V" }
)
public class RemoveEntry extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(RemoveEntry.class.getName());
    /**
     * In.
     */
    @Input("advance:map<?K, ?V>")
    protected static final String MAP = "map";
    /**
     * In.
     */
    @Input("advance:?K")
    protected static final String KEY = "key";
    /**
     * In.
     */
    @Input("advance:?V")
    protected static final String VALUE = "value";
    /**
     * Out.
     */
    @Output("advance:map<?K, ?V>")
    protected static final String OUT_MAP = "out_map";
    /**
     * Out.
     */
    @Output("advance:boolean")
    protected static final String OUT_STATUS = "out_status";

    @Override
    protected void invoke() {
        final Map<XElement, XElement> map = resolver().getMap(get(MAP));
        final XElement key = get(KEY);
        final XElement value = get(KEY);

        boolean status = false;
        final XElement assObj = map.get(key);
        if (assObj.equals(value)) {
            status = true;
            map.remove(key);
        }

        dispatch(OUT_MAP, resolver().create(map));
        dispatch(OUT_STATUS, resolver().create(status));
    }
}
