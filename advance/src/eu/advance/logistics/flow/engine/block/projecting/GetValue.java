/*
 * Copyright 2010-2012 The Advance EU 7th Framework project consortium
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
package eu.advance.logistics.flow.engine.block.projecting;

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
 * Get a value by the given key from the map or indicate if no such element exists.
 * Signature: GetValue(map<t, u>, t) -> (u, boolean)
 * @author szmarcell
 */
@Block(id = "___GetValue", category = "projection", scheduler = "IO", description = "Get a value by the given key from the map or indicate if no such element exists")
public class GetValue extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(GetValue .class.getName());
    /** In. */
    @Input("advance:real")
    protected static final String IN = "in";
    /** Out. */
    @Output("advance:real")
    protected static final String OUT = "out";
    /**
     * Constructor.
     * @param settings the block settings
     */
    public GetValue(AdvanceBlockSettings settings) {
        super(settings);
    }
    /** The running count. */
    private int count;
    /** The running sum. */
    private double value;
    // TODO implement 
    @Override
    protected void invoke(Map<String, XElement> map) {
        double val = XData.getDouble(map.get(IN));
        value = (value * count++ + val) / count;
        dispatch(OUT, XData.create(value));
    }
    
}