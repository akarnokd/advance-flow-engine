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
import eu.advance.logistics.flow.engine.api.core.AdvanceData;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;

/**
 * Buffers the incoming values into a collection within the given time window.
 * Signature: BufferWithTime(t, integer) -> collection<t>
 * @author szmarcell
 */
@Block(id = "___BufferWithTime", category = "streaming", scheduler = "IO", parameters = "T", description = "Buffers the incoming values into a collection within the given time window.")
public class BufferWithTime extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(BufferWithTime .class.getName());
    /** In element. */
    @Input("?T")
    protected static final String ELEMENT = "element";
    /** In size. */
    @Input("advance:integer")
    protected static final String SIZE = "size";
    /** Out. */
    @Output("advance:collection<?T>")
    protected static final String OUT = "out";
    /** The running count. */
    private int count;
    /** The running sum. */
    private double value;
    // TODO implement 
    @Override
    protected void invoke() {
    }
    
}
