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
import eu.advance.logistics.flow.engine.AdvanceBlock;

/**
 * Computes the running count, mean, variance and standard deviation of the input values.
 * Signature: SimpleRunningStatistics(collection<object>) -> real
 * See: http://www.johndcook.com/standard_deviation.html
 * @author szmarcell
 */
@Block(id = "SimpleRunningStatistics", category = "aggregation", scheduler = "IO", description = "Computes the running count, mean, variance and standard deviation of the input values.")
public class SimpleRunningStatistics extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(SimpleRunningStatistics.class.getName());
    /** In. */
    @Input("advance:real")
    protected static final String IN = "in";
    /** Count out. */
    @Output("advance:integer")
    protected static final String OUT_COUNT = "count";
    /** Mean out. */
    @Output("advance:real")
    protected static final String OUT_MEAN = "mean";
    /** Variance out. */
    @Output("advance:real")
    protected static final String OUT_VAR = "variance";
    /** Deviation out. */
    @Output("advance:real")
    protected static final String OUT_DEV = "deviation";
    /** The running count. */
    private int count;
    /** The running mean. */
    private double mean;
    /** The running variance. */
    private double s;
    @Override
    protected void invoke() {
        double val = getDouble(IN);
        count++;
        double newMean = mean + (val - mean) / count;
        s += (val - mean) * (val - newMean);
        mean = newMean;
        dispatch(OUT_COUNT, resolver().create(count));
        dispatch(OUT_MEAN, resolver().create(mean));
        double variance = count > 1 ? s / (count - 1) : 0.0;
        dispatch(OUT_VAR, resolver().create(variance));
        dispatch(OUT_DEV, resolver().create(Math.sqrt(variance)));
    }
    
}
