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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test if the given regular expression matches the string. Signature:
 * RegexpMatches(string, string) -> boolean
 *
 * @author TTS
 */
@Block(id = "RegexpMatches", category = "string", scheduler = "NOW", description = "Test if the given regular expression matches the string")
public class RegexpMatches extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(RegexpMatches.class.getName());
    /**
     * In string.
     */
    @Input("advance:string")
    protected static final String IN1 = "in1";
    /**
     * In pattern.
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
        final Pattern pattern = Pattern.compile(getString(IN2));
        final Matcher matcher = pattern.matcher(getString(IN1));

        dispatch(OUT, resolver().create(matcher.matches()));
    }
}
