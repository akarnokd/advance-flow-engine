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

import hu.akarnokd.utils.xml.XNElement;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;

/**
 * Run a regular expression on the string and return a collection of the matched
 * groups. Signature: RegexpMatch(string, string) -> collection<string>
 *
 * @author TTS
 */
@Block(id = "RegexpMatch", category = "string", scheduler = "NOW", description = "Run a regular expression on the  string and return a collection of the matched groups")
public class RegexpMatch extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(RegexpMatch.class.getName());
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
     * Out groups.
     */
    @Output("advance:collection<advance:string>")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final Pattern pattern = Pattern.compile(getString(IN2));
        final Matcher matcher = pattern.matcher(getString(IN1));
        
        final ArrayList<XNElement> result = new ArrayList<XNElement>();
        while (matcher.find()) {
            result.add(resolver().create(matcher.group()));
        }
        
        dispatch(OUT, resolver().create(result));
    }
}
