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

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import java.util.logging.Logger;

/**
 * Check if the string starts with another string. Signature: StartsWith(string,
 * string) -> boolean
 *
 * @author TTS
 */
@Block(id = "StartsWith", category = "string", scheduler = "IO", description = "Check if the string starts with another string")
public class StartsWith extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(StartsWith.class.getName());
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
     * Out.
     */
    @Output("advance:boolean")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final String value = getString(IN1);
        final String start = getString(IN2);
        
        dispatch(OUT, resolver().create(value.startsWith(start)));
    }
}
