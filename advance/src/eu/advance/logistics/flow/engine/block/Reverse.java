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

package eu.advance.logistics.flow.engine.block;

import java.util.LinkedList;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceData;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * A simple generic block that reserves the children of the supplied advance:collection type object.
 * @author akarnokd, 2011.07.01.
 */
@Block(scheduler = "NOW", 
description = "Block to reverse the elements of the input collection.", 
parameters = { "T" }, category = "data-transformations")
public class Reverse extends AdvanceBlock {
	/** In. */
    @Input("advance:collection<?T>")
    private static final String IN = "in";
    /** Out. */
    @Output("advance:collection<?T>")
    private static final String OUT = "out";
	
	@Override
	protected void invoke() {
		XElement in = params.get(IN);
		
		LinkedList<XElement> out = new LinkedList<XElement>();
		for (XElement e : AdvanceData.getItems(in)) {
			out.addFirst(e.copy());
		}
		XElement e = AdvanceData.create();
		e.children().addAll(out);
		dispatch(OUT, e);
	}

}
