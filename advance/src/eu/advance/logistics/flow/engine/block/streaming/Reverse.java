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

import hu.akarnokd.utils.xml.XNElement;

import java.util.LinkedList;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;

/**
 * Reverses the element order within the collection. Signature:
 * Reverse(collection<t>) -> collection<t>
 *
 * @author TTS
 */
@Block(id = "Reverse", 
	category = "streaming", 
	scheduler = "NOW", 
	description = "Reverses the element order within the collection", 
	parameters = { "T" }
)
public class Reverse extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(Reverse.class.getName());
    /**
     * In.
     */
    @Input("advance:collection<?T>")
    protected static final String IN = "in";
    /**
     * Out.
     */
    @Output("advance:collection<?T>")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
		XNElement in = params.get(IN);
		
		LinkedList<XNElement> out = new LinkedList<XNElement>();
		for (XNElement e : resolver().getItems(in)) {
			out.addFirst(e.copy());
		}
		XNElement e = resolver().create(out);
		dispatch(OUT, e);
    }
}
