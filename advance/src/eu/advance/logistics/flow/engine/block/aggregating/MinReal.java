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

import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Returns the smallest integer in the collection along with its last occurrence
 * index. Signature: MinReal(collection<integer>) -> integer
 *
 * @author TTS
 */
@Block(id = "MinReal", category = "aggregation", scheduler = "IO", description = "Returns the smallest integer in the collection along with its last occurrence index")
public class MinReal extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(MinReal.class.getName());
    /**
     * In.
     */
    @Input("advance:collection<advance:real>")
    protected static final String IN = "in";
    /**
     * Out.
     */
    @Output("advance:real")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        double min = Double.MAX_VALUE;

        for (XElement xelem : resolver().getItems(get(IN))) {
            min = Math.min(min, settings.resolver.getDouble(xelem));
        }

        dispatch(OUT, resolver().create(min));
    }
}
