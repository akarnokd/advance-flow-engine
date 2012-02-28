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

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.AdvanceData;
import eu.advance.logistics.flow.engine.xml.XElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Converts all key-value pairs of the map into a collection of key value pairs.
 * Signature: MapEntries(map<t, u>) -> collection<pair<t, u>>
 *
 * @author TTS
 */
@Block(id = "MapEntries", category = "projection", scheduler = "IO", description = "Converts all key-value pairs of the map into a collection of key value pairs")
public class MapEntries extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(MapEntries.class.getName());
    /**
     * In.
     */
    @Input("advance:map")
    protected static final String IN = "in";
    /**
     * Out.
     */
    @Output("advance:collection")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final Map<XElement, XElement> map = resolver().getMap(get(IN));
        final Set<XElement> keys = map.keySet();

        final List<XElement> result = new ArrayList<XElement>();
        for (XElement key : keys) {

            final XElement xelem = ((AdvanceData) resolver()).createPair(key, map.get(key));
            result.add(xelem);
        }

        dispatch(OUT, resolver().create(result));
    }
}
