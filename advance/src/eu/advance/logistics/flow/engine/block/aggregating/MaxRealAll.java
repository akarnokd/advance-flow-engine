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

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Returns the largest real value from the collection along with the collection
 * of its occurrence indexes. Signature: MaxRealAll(collection<integer>) ->
 * integer
 *
 * @author TTS
 */
@Block(id = "MaxRealAll", category = "aggregation", scheduler = "IO", description = "Returns the largest real value from the collection along with the collection of its occurrence indexes")
public class MaxRealAll extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(MaxRealAll.class.getName());
    /**
     * In.
     */
    @Input("advance:collection<advance:real>")
    protected static final String IN = "in";
    /**
     * Out.
     */
    @Output("advance:real")
    protected static final String MAX = "max";
    /**
     * Out.
     */
    @Output("advance:collection<advance:integer>")
    protected static final String COLLECTION = "collection";

    @Override
    protected void invoke() {
        final List<XElement> position_array = new ArrayList<XElement>();
        double max = Double.MIN_VALUE;

        final XElement xcollection = get(IN);
        final Iterator<XElement> it = xcollection.children().iterator();
        int count = 0;
        while (it.hasNext()) {
            final double curVal = settings.resolver.getDouble(it.next());

            if (curVal > max) {
                max = curVal;
                position_array.clear();
                position_array.add(resolver().create(count));
            } else if (curVal == max) {
                position_array.add(resolver().create(count));
            }

            count++;
        }

        dispatch(MAX, resolver().create(max));
        dispatch(COLLECTION, resolver().create(position_array));
    }
}
