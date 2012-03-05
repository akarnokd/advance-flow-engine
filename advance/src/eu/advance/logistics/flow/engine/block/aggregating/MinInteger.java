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
package eu.advance.logistics.flow.engine.block.aggregating;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Returns the smallest integer in the collection along with its last occurrence
 * index. Signature: MinInteger(collection<integer>) -> integer
 *
 * @author TTS
 */
@Block(id = "MinInteger", category = "aggregation", scheduler = "IO", description = "Returns the smallest integer in the collection along with its last occurrence index")
public class MinInteger extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(MinInteger.class.getName());
    /**
     * In.
     */
    @Input("advance:collection<advance:integer>")
    protected static final String IN = "in";
    /**
     * Out.
     */
    @Output("advance:integer")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        int min = Integer.MAX_VALUE;

        final XElement xcollection = get(IN);
        final Iterator<XElement> it = xcollection.children().iterator();
        while (it.hasNext()) {
            min = Math.min(min, settings.resolver.getInt(it.next()));
        }

        dispatch(OUT, resolver().create(min));
    }
}