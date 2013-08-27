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

import hu.akarnokd.utils.xml.XNElement;

import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.AdvanceData;

/**
 * Returns the smallest value in the collection along with its last occurrence.
 * Signature: Min(collection<t>) -> (real, integer)
 *
 * @author TTS
 */
@Block(id = "Min", 
	category = "aggregation", 
	scheduler = "IO", 
	description = "Returns the smallest value in the collection along with its last occurrence", 
	parameters = { "T" }
)
public class Min extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(Min.class.getName());
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
        double min = Double.MAX_VALUE;
        int lastPos = 0;

        int count = 0;
        for (XNElement xelem : resolver().getItems(get(IN))) {
            String n = AdvanceData.realName(xelem).first;

            if (n.equalsIgnoreCase("integer")) {
                final int curr = resolver().getInt(xelem);
                if (curr <= min) {
                    min = curr;
                    lastPos = count;
                }
            } else if (n.equalsIgnoreCase("real")) {
                final double curr = resolver().getDouble(xelem);
                if (curr >= min) {
                    min = curr;
                    lastPos = count;
                }
            }

            count++;
        }

        dispatch(OUT1, resolver().create(min));
        dispatch(OUT2, resolver().create(lastPos));
    }
}
