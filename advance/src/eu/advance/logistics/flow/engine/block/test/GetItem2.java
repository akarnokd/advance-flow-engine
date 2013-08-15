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

import hu.akarnokd.utils.xml.XNElement;
import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * A simple generic block that extracts an item from an advance:collection type construct.
 * @author akarnokd, 2011.07.01.
 */
@Block(id = "GetItem2", scheduler = "NOW", 
description = "Block to retrieve a specific item from the input collection.", 
parameters = { "T" })
public class GetItem2 extends AdvanceBlock {
	/** In. */
    @Input("advance:collection<?T>")
    private static final String IN = "in";
    /** Index. */
    @Input("advance:integer")
    private static final String INDEX = "index";
    /** Out. */
    @Output("?T")
    private static final String OUT = "out";
	
	@Override
	protected void invoke() {
		int index = getInt(INDEX);
		XNElement in = params.get(IN);
		if (in.children().size() > index) {
			dispatch(OUT, resolver().getItem(in, index));
		}
	}

}
