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
package eu.advance.logistics.flow.engine.block.util;

import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;

/**
 * Extract a substring from a string.
 * Signature: Substring(string, integer, integer) -> string
 * @author szmarcell
 */
@Block(id = "___Substring", category = "string", scheduler = "NOW", description = "Extract a substring from a string")
public class Substring extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(Substring .class.getName());
    /** In. */
    @Input("advance:string")
    protected static final String IN = "in";
    /** In. */
    @Input("advance:string")
    protected static final String START = "start";
    /** In. */
    @Input(value = "advance:string", defaultConstant = "<integer>-1</integer>")
    protected static final String END = "end";
    /** Out. */
    @Output("advance:string")
    protected static final String OUT = "out";
    @Override
    protected void invoke() {
    	String in = getString(IN);
    	int start = getInt(START);
    	int end = getInt(END);
    	if (end < 0) {
    		end = in.length();
    	}
    	set(OUT, in.substring(start, end));
    }
    
}
