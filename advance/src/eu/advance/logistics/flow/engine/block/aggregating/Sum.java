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

import java.util.Map;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockSettings;
import eu.advance.logistics.flow.engine.xml.typesystem.XData;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Compute the sum of the elements within the collection which have the type of Integer or Real.
 * Signature: Sum(collection<object>) -> real
 * @author szmarcell
 */
@Block(id="Sum", category="aggregation", scheduler="IO", description = "Compute the sum of the elements within the collection which have the type of Integer or Real.")
public class Sum extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(Sum .class.getName());
    /** In. */
    @Input("advance:real")
    private static final String IN = "in";
    /** Out. */
    @Output("advance:real")
    private static final String OUT = "out";
    /**
     * Constructor.
     * @param settings the block settings
     */
    public Sum(AdvanceBlockSettings settings) {
        super(settings);
    }
    /** The running sum. */
    private double value;
    @Override
    protected void invoke(Map<String, XElement> map) {
        double val = XData.getDouble(map.get(IN));
        value += val;
        dispatch(OUT, XData.create(value));
    }
    
}