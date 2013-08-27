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

import hu.akarnokd.utils.xml.XNElement;
import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;

/**
 * Creates a type token from the supplied type instance.
 * @author karnokd, 2012.02.24.
 */
@Block(id = "CreateType", category = "projection", 
scheduler = "NOW", 
description = "Creates a type token from the supplied type instance.",
parameters = { "T" }
)
public class CreateType extends AdvanceBlock {
    /** In. */
    @Input("?T")
    protected static final String IN = "in";
    /** Out. */
    @Output("advance:type<?T>")
    protected static final String OUT = "out";
	@Override
	protected void invoke() {
		XNElement in = get(IN);
		XNElement t = new XNElement("type");
		t.set("type", "advance:" + in.name);
		dispatch(OUT, t);
		// FIXME use runtime type information of generics!!!
	}

}
