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
import eu.advance.logistics.flow.engine.xml.XElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Get the collection of keys where the given value is present. Signature:
 * GetKeys(map<t, u>, u) -> collection<t>
 *
 * @author TTS
 */
@Block(id = "GetKeys", 
	category = "projection", 
	scheduler = "IO", 
	description = "Get the collection of keys where the given value is present", 
	parameters = { "K", "V" } 
)
public class GetKeys extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(GetKeys.class.getName());
    /**
     * In.
     */
    @Input("advance:map<?K, ?V>")
    protected static final String MAP = "map";
    /**
     * In.
     */
    @Input("?V")
    protected static final String VALUE = "value";
    /**
     * Out.
     */
    @Output("advance:collection<?K>")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final List<XElement> result = new ArrayList<XElement>();

        final Map<XElement, XElement> map = resolver().getMap(get(MAP));
        final Set<XElement> keySet = map.keySet();
        final XElement objElem = get(VALUE);
        for (XElement key : keySet) {
            final XElement value = map.get(key);

            if (value.equals(objElem)) {
                result.add(key);
            }
        }

        dispatch(OUT, resolver().create(result));
    }
}
