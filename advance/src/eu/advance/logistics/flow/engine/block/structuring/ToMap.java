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

import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;

/**
 * Uses the XPath expressions to create the key-value pairs from the source value and convert it into the standard map representation.
 * Signature: ToMap(t, xpath, xpath, schema<u>, schema<v>) -> map<u, v>
 * @author szmarcell
 */
@Block(id = "___ToMap", category = "data-transformations", scheduler = "IO", description = "Uses the XPath expressions to create the key-value pairs from the source value and convert it into the standard map representation.")
public class ToMap extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(ToMap .class.getName());
    /** In. */
    @Input("advance:real")
    protected static final String IN = "in";
    /** Out. */
    @Output("advance:real")
    protected static final String OUT = "out";
    /** The running count. */
    private int count;
    /** The running sum. */
    private double value;
    // TODO implement 
    @Override
    protected void invoke() {
        double val = getDouble(IN);
        value = (value * count++ + val) / count;
        dispatch(OUT, resolver().create(value));
    }
    
}
