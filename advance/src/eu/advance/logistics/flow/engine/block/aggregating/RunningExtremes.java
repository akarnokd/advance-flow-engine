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

/**
 * Computes the smallest and largest values of the received inputs.
 * Signature: RunningExtremes(T +advance:real) -> (T, T)
 * @author szmarcell
 */
@Block(id = "RunningExtremes", category = "aggregation", scheduler = "NOW", description = "Computes the smallest and largest values of the received inputs.")
public class RunningExtremes extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(RunningExtremes .class.getName());
    /** In. */
    @Input("advance:real")
    protected static final String IN = "in";
    /** Out. */
    @Output("advance:real")
    protected static final String MIN = "min";
    /** Out. */
    @Output("advance:real")
    protected static final String MAX = "max";
    /** The running minimum. */
    private Double min = Double.MAX_VALUE;
    /** The running maximum. */
    private Double max = Double.MIN_VALUE;
    @Override
    protected void invoke() {
        double val = getDouble(IN);
        min = Math.min(val, min);
        max = Math.max(val, max);
        dispatch(MIN, resolver().create(min));
        dispatch(MAX, resolver().create(max));
    }
    
}
