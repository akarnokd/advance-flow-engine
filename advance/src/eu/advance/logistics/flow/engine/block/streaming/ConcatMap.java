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

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Concatenate two maps. Signature: ConcatMap(map<t, u>, map<t, u>) -> map<t, u>
 *
 * @author TTS
 */
@Block(id = "ConcatMap", 
	category = "streaming", 
	scheduler = "IO", 
	description = "Concatenate two maps", 
	parameters = { "K", "V" }
)
public class ConcatMap extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(ConcatMap.class.getName());
    /**
     * In.
     */
    @Input("advance:map<?K, ?V>")
    protected static final String IN1 = "in1";
    /**
     * In.
     */
    @Input("advance:map<?K, ?V>")
    protected static final String IN2 = "in2";
    /**
     * Out.
     */
    @Output("advance:map<?K, ?V>")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final Map<XElement, XElement> map1 = resolver().getMap(get(IN1));
        final Map<XElement, XElement> map2 = resolver().getMap(get(IN2));

        map1.putAll(map2);

        dispatch(OUT, resolver().create(map1));
    }
}
