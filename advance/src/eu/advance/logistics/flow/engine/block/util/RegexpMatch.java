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

import com.google.common.collect.Lists;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.api.core.AdvanceData;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Run a regular expression on the string and return a collection of the matched groups.
 * Signature: RegexpMatch(string, string) -> collection<regexpgroup>
 * @author szmarcell
 */
@Block(id = "RegexpMatch", category = "string", scheduler = "NOW", description = "Run a regular expression on the string and return a collection of the matched groups.")
public class RegexpMatch extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(RegexpMatch .class.getName());
    /** In string. */
    @Input("advance:string")
    protected static final String STRING = "string";
    /** In pattern. */
    @Input("advance:string")
    protected static final String PATTERN = "pattern";
    /** Out groups. */
    @Output("advance:collection<advance:string>")
    protected static final String GROUPS = "groups";
    @Override
    protected void invoke() {
        String string = get(STRING).content;
        String patternStr = get(PATTERN).content;
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(string);
        ArrayList<XElement> matches = Lists.newArrayList();
        while (matcher.find()) {
            final String group = matcher.group();
            matches.add(AdvanceData.create(group));
        }
        dispatch(GROUPS, AdvanceData.create(matches));
    }
    
}
