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
 * Returns the index of the last occurrence of the substring in the string.
 * Signature: LastIndexOf(string, string, integer) -> integer
 *
 * @author TTS
 */
@Block(id = "LastIndexOf", category = "string", scheduler = "IO", description = "Returns the index of the last occurrence of the substring in the string")
public class LastIndexOf extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(LastIndexOf.class.getName());
    /**
     * In.
     */
    @Input("advance:string")
    protected static final String IN1 = "in1";
    /**
     * In.
     */
    @Input("advance:string")
    protected static final String IN2 = "in2";
    /**
     * In.
     */
    @Input(value = "advance:integer", defaultConstant = "<integer>-1</integer>")
    protected static final String IN3 = "in3";
    /**
     * Out.
     */
    @Output("advance:integer")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final String in = getString(IN1);
        final String substring = getString(IN2);
        
        int start = getInt(IN3);
        if (start < 0) {
            start = in.length();
        }

        dispatch(OUT, resolver().create(in.lastIndexOf(substring, start)));
    }
}
