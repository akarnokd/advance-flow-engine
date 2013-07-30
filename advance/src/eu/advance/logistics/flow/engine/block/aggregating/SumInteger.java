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


import hu.akarnokd.utils.xml.XNElement;

import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * Compute the sum of the elements within the collection which have the type of
 * Integer. Signature: SumInteger(collection<integer>) -> integer
 *
 * @author TTS
 */
@Block(id = "SumInteger", category = "aggregation", scheduler = "IO", description = "Compute the sum of the collection of integers")
public class SumInteger extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(SumInteger.class.getName());
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
        int sum = 0;
        
        for (XNElement xelem : resolver().getItems(get(IN))) {
            sum += resolver().getInt(xelem);
        }
        
        dispatch(OUT, resolver().create(sum));
    }
}
