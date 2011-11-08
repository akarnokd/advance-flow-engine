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

package eu.advance.logistics.flow.engine.block;

import java.util.Map;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockSettings;
import eu.advance.logistics.flow.engine.xml.typesystem.XData;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * A simple generic block that extracts an item from an advance:collection type construct.
 * @author akarnokd, 2011.07.01.
 */
@Block(scheduler = "NOW", 
description = "Block to retrieve a specific item from the input collection.", 
parameters = { "T" })
public class GetItem extends AdvanceBlock {
	/** In. */
    @Input("advance:collection<?T>")
    private static final String IN = "in";
    /** Index. */
    @Input("advance:integer")
    private static final String INDEX = "index";
    /** Out. */
    @Output("?T")
    private static final String OUT = "out";
	
	/**
	 * Constructor.
	 * @param settings the block settings
	 */
	public GetItem(AdvanceBlockSettings settings) {
		super(settings);
	}

	@Override
	protected void invoke(Map<String, XElement> params) {
		int index = XData.getInt(params.get(INDEX));
		XElement in = params.get(IN);
		if (in.children().size() > index) {
			dispatch(OUT, XData.getItem(in, index));
		}
	}

}
