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

import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * Removes all the elements equal to the supplied value and returns a new map without them, plus a collection of keys which had the specified value.
 * Signature: RemoveValue(map<t, u>, u) -> (map<t, u>, collection<t>)
 * @author szmarcell
 */
@Block(id = "___RemoveValue", category = "streaming", scheduler = "IO", description = "Removes all the elements equal to the supplied value and returns a new map without them, plus a collection of keys which had the specified value.")
public class RemoveValue extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(RemoveValue .class.getName());
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
