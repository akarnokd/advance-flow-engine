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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Returns the largest value from the collection along with the collection of
 * its occurrence indexes. Signature: MaxAll(collection<object>) -> (real,
 * collection<integer>)
 *
 * @author TTS
 */
@Block(id = "MaxAll", category = "aggregation", scheduler = "IO", description = "Returns the largest value from the collection along with the collection of its occurrence indexes")
public class MaxAll extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(MaxAll.class.getName());
    /**
     * In.
     */
    @Input("advance:collection<advance:object>")
    protected static final String IN = "in";
    /**
     * Out.
     */
    @Output("advance:real")
    protected static final String OUT1 = "out1";
    /**
     * Out.
     */
    @Output("advance:collection<advance:integer>")
    protected static final String OUT2 = "out2";

    @Override
    protected void invoke() {
        final List<XElement> position_array = new ArrayList<XElement>();
        double max = Double.MIN_VALUE;

        final XElement xcollection = get(IN);
        final Iterator<XElement> it = xcollection.children().iterator();
        int count = 0;
        while (it.hasNext()) {
            final XElement xelem = it.next();

            final double curVal;
            if (xelem.name.equalsIgnoreCase("integer")) {
                curVal = settings.resolver.getInt(xelem);
            } else if (xelem.name.equalsIgnoreCase("real")) {
                curVal = settings.resolver.getDouble(xelem);
            } else {
                curVal = 0.0;
            }

            if (curVal > max) {
                max = curVal;
                position_array.clear();
                position_array.add(resolver().create(count));
            } else if (curVal == max) {
                position_array.add(resolver().create(count));
            }

            count++;
        }

        dispatch(OUT1, resolver().create(max));
        dispatch(OUT2, resolver().create(position_array));
    }
}
