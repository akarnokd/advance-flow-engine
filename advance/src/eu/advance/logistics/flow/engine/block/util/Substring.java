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
import eu.advance.logistics.flow.engine.api.core.AdvanceData;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Extract a substring from a string.
 * Default values of the from and to ports are the beginning and the end of the string respectively.
 * Signature: Substring(string, integer, integer) -> string
 * @author szmarcell
 */
@Block(id = "Substring", category = "string", scheduler = "IO", description = "Extract a substring from a string. Default values of the from and to ports are the beginning and the end of the string respectively.")
public class Substring extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(Substring .class.getName());
    /** In string. */
    @Input("advance:string")
    protected static final String STRING = "string";
    /** In from. */
    @Input(value = "advance:integer", required = false)
    protected static final String FROM = "from";
    /** In to. */
    @Input(value = "advance:integer", required = false)
    protected static final String TO = "to";
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
        String string = getString(STRING);
        XElement fromEl = get(FROM);
        XElement toEl = get(TO);
        if (toEl == null) {
            if (fromEl == null) {
                dispatch(OUT, AdvanceData.create(string));
            } else {
                dispatch(OUT, AdvanceData.create(string.substring(Integer.parseInt(fromEl.content))));
            }
        } else {
            int from = fromEl == null ? 0 : Integer.parseInt(fromEl.content);
            dispatch(OUT, AdvanceData.create(string.substring(from, Integer.parseInt(toEl.content))));
        }
    }
    
}
