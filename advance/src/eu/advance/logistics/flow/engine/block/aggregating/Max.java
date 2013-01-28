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
package eu.advance.logistics.flow.engine.block.aggregating;

import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Returns the largest value in the collection along with its last occurrence.
 * Signature: Max(collection<t>) -> (real, integer)
 *
 * @author TTS
 */
@Block(id = "Max", 
	category = "aggregation", 
	scheduler = "IO", 
	description = "Returns the largest value in the collection along with its last occurrence", 
	parameters = { "T" }
)
public class Max extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(Max.class.getName());
    /**
     * In.
     */
    @Input("advance:collection<?T>")
    protected static final String IN = "in";
    /**
     * Out.
     */
    @Output("advance:real")
    protected static final String OUT1 = "out1";
    /**
     * Out.
     */
    @Output("advance:integer")
    protected static final String OUT2 = "out2";

    @Override
    protected void invoke() {
        double max = Double.MIN_VALUE;
        int lastPos = 0;

        int count = 0;
        for (XElement xelem : resolver().getItems(get(IN))) {

            if (xelem.name.equalsIgnoreCase("integer")) {
                final int curr = settings.resolver.getInt(xelem);
                if (curr >= max) {
                    max = curr;
                    lastPos = count;
                }
            } else if (xelem.name.equalsIgnoreCase("real")) {
                final double curr = settings.resolver.getDouble(xelem);
                if (curr >= max) {
                    max = curr;
                    lastPos = count;
                }
            }

            count++;
        }

        dispatch(OUT1, resolver().create(max));
        dispatch(OUT2, resolver().create(lastPos));
    }
}
