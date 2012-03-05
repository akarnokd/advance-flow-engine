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

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Selects the first child element with the supplied name.
 * @author karnokd, 2012.02.24.
 */
@Block(id = "SelectChild", category = "projection", 
scheduler = "NOW", 
description = "Selects the first child element with the supplied name.",
parameters = { "T", "U" }
)
public class SelectChild extends AdvanceBlock {
    /** In. */
    @Input("?T")
    protected static final String IN = "in";
    /** The child name. */
    @Input("advance:string")
    protected static final String NAME = "name";
    /** The child name. */
    @Input("advance:type<?U>")
    protected static final String TYPE = "type";
    /** Out. */
    @Output("?U")
    protected static final String OUT = "out";
	@Override
	protected void invoke() {
		XElement in = get(IN);
		
		XElement ce = in.childElement(getString(NAME));
		
		if (ce != null) {
			dispatch(OUT, ce.copy());
		}
	}

}