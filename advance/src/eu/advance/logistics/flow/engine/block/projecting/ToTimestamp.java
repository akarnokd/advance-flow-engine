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
package eu.advance.logistics.flow.engine.block.projecting;

import hu.akarnokd.utils.xml.XNElement;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;

/**
 * Convert a string into a timestamp or indicate an error. Signature:
 * ToTimestamp(string) -> (timestamp, boolean)
 *
 * @author TTS
 */
@Block(id = "ToTimestamp", category = "projection", scheduler = "IO", description = "Convert a string into a timestamp or indicate an error")
public class ToTimestamp extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(ToTimestamp.class.getName());
    /**
     * In.
     */
    @Input("advance:string")
    protected static final String IN = "in";
    /**
     * Out.
     */
    @Output("advance:timestamp")
    protected static final String OUT_TIMESTAMP = "out_timestamp";
    /**
     * Out.
     */
    @Output("advance:boolean")
    protected static final String OUT_STATUS = "out_status";

    @Override
    protected void invoke() {
        final XNElement xelem = get(IN);

        try {
            final Date res = XNElement.parseDateTime(resolver().getString(xelem));

            dispatch(OUT_TIMESTAMP, resolver().create(res));
            dispatch(OUT_STATUS, resolver().create(true));

        } catch (ParseException ex) {
            log(ex);
            
            dispatch(OUT_TIMESTAMP, resolver().create(new Date()));
            dispatch(OUT_STATUS, resolver().create(false));
        }
    }
}
