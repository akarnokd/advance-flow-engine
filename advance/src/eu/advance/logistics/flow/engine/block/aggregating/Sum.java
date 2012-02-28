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
import java.util.Iterator;

/**
 * Compute the sum of the elements within the collection which have the type of
 * Integer or Real. Signature: Sum(collection<object>) -> real
 *
 * @author TTS
 */
@Block(id = "Sum", category = "aggregation", scheduler = "IO", description = "Compute the sum of the elements within the collection which have the type of Integer or Real.")
public class Sum extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(Sum.class.getName());
    /**
     * In.
     */
    @Input("advance:collection<advance:real>")
    protected static final String IN = "in";
    /**
     * Out.
     */
    @Output("advance:real")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        double sum = 0;

        final XElement xcollection = get(IN);
        final Iterator<XElement> it = xcollection.children().iterator();
        while (it.hasNext()) {
            final XElement xelem = it.next();

            if (xelem.name.equalsIgnoreCase("integer")) {
                sum += settings.resolver.getInt(xelem);
            } else if (xelem.name.equalsIgnoreCase("real")) {
                sum += settings.resolver.getDouble(xelem);
            }
        }

        dispatch(OUT, resolver().create(sum));
    }
}
