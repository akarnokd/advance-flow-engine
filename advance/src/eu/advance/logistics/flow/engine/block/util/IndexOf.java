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
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * Returns the index of the first occurrence of the substring in the string.
 * Signature: IndexOf(string, string, integer) -> integer
 * @author szmarcell
 */
@Block(id = "IndexOf", category = "string", scheduler = "IO", description = "Returns the index of the first occurrence of the substring in the string.")
public class IndexOf extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(IndexOf .class.getName());
    /** In. */
    @Input("advance:string")
    protected static final String IN = "in";
    /** In. */
    @Input("advance:string")
    protected static final String SUBSTRING = "substring";
    /** In. */
    @Input(value = "advance:integer", defaultConstant = "<integer>0</integer>")
    protected static final String START = "start";
    /** Out. */
    @Output("advance:integer")
    protected static final String OUT = "out";
    @Override
    protected void invoke() {
        String in = getString(IN);
        String substring = getString(SUBSTRING);
        int start = getInt(START);
        
        dispatch(OUT, resolver().create(in.indexOf(substring, start)));
    }
    
}
