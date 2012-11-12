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

package eu.advance.logistics.flow.engine.block.pattern;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * Retrieves a sequence of channel data based on the configured data source.
 * @author karnokd, 2012.05.27.
 */
@Block(id = "ChannelGet", 
category = "classification", scheduler = "IO", 
description = "Retrieves a sequence of channel data based on the configured data source.",
parameters = { "T" }
)
public class ChannelGet extends AdvanceBlock {
	/** The data source configuration. */
	@Input("?T")
	protected static final String CONFIG = "config";
	/** The trigger to start the data retrieval. */
    @Input("advance:object")
    protected static final String TRIGGER = "trigger";
    /** The stream of timed value group. */
    @Output("advance:option<advance:timedvaluegroup>")
    protected static final String DATA = "data";
	@Override
	protected void invoke() {
	}
}
