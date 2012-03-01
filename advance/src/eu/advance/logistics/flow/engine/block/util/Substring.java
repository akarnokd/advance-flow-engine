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
import eu.advance.logistics.flow.engine.xml.XElement;
import java.util.logging.Logger;

/**
 * Extract a substring from a string. Signature: Substring(string, integer,
 * integer) -> string
 *
 * @author TTS
 */
@Block(id = "Substring", category = "string", scheduler = "IO", description = "Extract a substring from a string")
public class Substring extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(Substring.class.getName());
    /**
     * In string.
     */
    @Input("advance:string")
    protected static final String STRING = "string";
    /**
     * In from.
     */
    @Input(value = "advance:integer", required = false)
    protected static final String FROM = "from";
    /**
     * In to.
     */
    @Input(value = "advance:integer", required = false)
    protected static final String TO = "to";
    /**
     * Out.
     */
    @Output("advance:string")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final String string = getString(STRING);
        final XElement fromEl = get(FROM);
        final XElement toEl = get(TO);
        
        try {
            
            if (toEl == null) {
                if (fromEl == null) {
                    dispatch(OUT, resolver().create(string));
                } else {
                    final String fromStr = resolver().getString(fromEl);
                    final int fromI = Integer.getInteger(fromStr);
                    dispatch(OUT, resolver().create(string.substring(fromI)));

                }
            } else {
                final String toStr = resolver().getString(toEl);
                final int toI = Integer.getInteger(toStr);

                if (fromEl == null) {
                    dispatch(OUT, resolver().create(string.substring(0, toI)));
                } else {
                    final String fromStr = resolver().getString(fromEl);
                    final int fromI = Integer.getInteger(fromStr);

                    dispatch(OUT, resolver().create(string.substring(fromI, toI)));
                }
            }
            
        } catch (NumberFormatException ex) {
            log(ex);
        }
    }
}
