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
 * Concatenate two strings.
 * Signature: ConcatString(string, string) -> string
 * @author szmarcell
 */
@Block(id = "ConcatString", category = "string", scheduler = "NOW", description = "Concatenate two strings.")
public class ConcatString extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(ConcatString .class.getName());
    /** In. */
    @Input("advance:string")
    protected static final String IN1 = "in1";
    /** In. */
    @Input("advance:string")
    protected static final String IN2 = "in2";
    /** Out. */
    @Output("advance:string")
    protected static final String OUT = "out";
    @Override
    protected void invoke() {
        String in1 = getString(IN1);
        String in2 = getString(IN2);
        dispatch(OUT, resolver().create(in1 + in2));
    }
    
}
