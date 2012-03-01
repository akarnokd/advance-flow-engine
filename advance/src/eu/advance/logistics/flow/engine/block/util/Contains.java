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
 * Check if the substring is within the string. Signature: Contains(string,
 * string) -> boolean
 *
 * @author TTS
 */
@Block(id = "Contains", category = "string", scheduler = "NOW", description = "Check if the substring is within the string")
public class Contains extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(Contains.class.getName());
    /**
     * In.
     */
    @Input("advance:string")
    protected static final String IN1 = "in1";
    /**
     * Substring.
     */
    @Output("advance:string")
    protected static final String IN2 = "in2";
    /**
     * Out.
     */
    @Output("advance:boolean")
    protected static final String OUT = "boolean";

    @Override
    protected void invoke() {
        final String in1 = getString(IN1);
        final String in2 = getString(IN2);
        
        dispatch(OUT, resolver().create(in1.contains(in2)));
    }
}
