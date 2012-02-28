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
package eu.advance.logistics.flow.engine.block.structuring;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Creates a map with a single key-value pair. Signature: SingletonMap(t, u) ->
 * map<t, u>
 *
 * @author TTS
 */
@Block(id = "SingletonMap", category = "data-transformations", scheduler = "IO", description = "Creates a map with a single key-value pair.")
public class SingletonMap extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(Singleton.class.getName());
    /**
     * In.
     */
    @Input("advance:object")
    protected static final String KEY = "key";
    /**
     * In.
     */
    @Input("advance:object")
    protected static final String value = "value";
    /**
     * Out.
     */
    @Output("advance:map")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final XElement keyElem = get(KEY);
        final XElement valElem = get(KEY);
        final Map<XElement, XElement> singleton = new HashMap<XElement, XElement>();
        singleton.put(keyElem, valElem);

        dispatch(OUT, resolver().create(singleton));
    }
}
