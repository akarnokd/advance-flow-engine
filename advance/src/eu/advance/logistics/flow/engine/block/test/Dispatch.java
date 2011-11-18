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

package eu.advance.logistics.flow.engine.block.test;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * The block dispatches its single input into two outputs.
 * @author akarnokd, 2011.11.04.
 */
@Block(scheduler = "NOW", parameters = { "T" }, 
description = "Dispatches its single input into two separate outputs.",
category = "data-transformations"
)
public class Dispatch extends AdvanceBlock {
	/** In. */
    @Input("?T")
    private static final String IN = "in";
    /** Out1. */
    @Output("?T")
    private static final String OUT1 = "out1";
    /** Out1. */
    @Output("?T")
    private static final String OUT2 = "out2";

	@Override
	protected void invoke() {
		dispatch(OUT1, params.get(IN), OUT2, params.get(IN));
	}

}
