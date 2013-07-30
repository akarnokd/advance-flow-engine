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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.AdvanceData;

/**
 * Converts all key-value pairs of the map into a collection of key value pairs.
 * Signature: MapEntries(map<t, u>) -> collection<pair<t, u>>
 *
 * @author TTS
 */
@Block(id = "MapEntries", 
	category = "projection", 
	scheduler = "IO", 
	description = "Converts all key-value pairs of the map into a collection of key value pairs", 
	parameters = { "K", "V" }
)
public class MapEntries extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(MapEntries.class.getName());
    /**
     * In.
     */
    @Input("advance:map<?K, ?V>")
    protected static final String IN = "in";
    /**
     * Out.
     */
    @Output("advance:collection<advance:pair<?K, ?V>>")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final Map<XNElement, XNElement> map = resolver().getMap(get(IN));
        final Set<XNElement> keys = map.keySet();

        final List<XNElement> result = new ArrayList<XNElement>();
        for (XNElement key : keys) {

            final XNElement xelem = ((AdvanceData) resolver()).createPair(key, map.get(key));
            result.add(xelem);
        }

        dispatch(OUT, resolver().create(result));
    }
}
