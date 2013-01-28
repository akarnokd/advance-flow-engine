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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Removes all the elements equal to the supplied value and returns a new map
 * without them, plus a collection of keys which had the specified value.
 * Signature: RemoveValue(map<t, u>, u) -> (map<t, u>, collection<t>)
 *
 * @author TTS
 */
@Block(id = "RemoveValue", 
	category = "streaming", 
	scheduler = "IO", 
	description = "Removes all the elements equal to the supplied value and returns a new map without them, plus a collection of keys which had the specified value", 
	parameters = { "K", "V" }
)
public class RemoveValue extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(RemoveValue.class.getName());
    /**
     * In.
     */
    @Input("advance:map<?K, ?V>")
    protected static final String MAP = "map";
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
    @Output("advance:collection<?K>")
    protected static final String OUT_KEYS = "out_keys";

    @Override
    protected void invoke() {
        final Map<XElement, XElement> map = resolver().getMap(get(MAP));
        final XElement value = get(VALUE);

        final List<XElement> keysList = new ArrayList<XElement>();
        if (map.containsValue(value)) {

            final Set<XElement> keySet = map.keySet();
            XElement key = null;
            for (XElement keyVal : keySet) {
                final XElement obj = map.get(keyVal);

                if (obj.equals(value)) {
                    map.remove(key);
                    keysList.add(key);
                }
            }
        }

        dispatch(OUT_MAP, resolver().create(map));
        dispatch(OUT_KEYS, resolver().create(keysList));
    }
}
