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

package eu.advance.logistics.flow.engine.block.projecting;

import java.util.List;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Computes the logical AND of all inputs.
 * @author karnokd, 2012.02.24.
 */
@Block(id = "And", category = "projection", 
scheduler = "NOW", 
description = "Computes the logical AND of all inputs."
)
public class And extends AdvanceBlock {
	/** 1. */
    @Input(value = "advance:boolean", variable = true, required = true)
    protected static final String IN = "in";
    /** Out. */
    @Output("advance:boolean")
    protected static final String OUT = "out";

	@Override
	protected void invoke() {
		List<XElement> varargs = varargs(IN);
		if (varargs.size() > 0) {
			for (XElement e : varargs) {
				if (!resolver().getBoolean(e)) {
					dispatch(OUT, resolver().create(false));
					return;
				}
			}
			dispatch(OUT, resolver().create(true));
		}
	}

}
