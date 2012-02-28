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
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Computes the standard deviation of the collection of reals. Signature:
 * STDDeviationReal(collection<real>) -> real
 *
 * @author TTS
 */
@Block(id = "STDDeviationReal", category = "aggregation", scheduler = "IO", description = "Computes the standard deviation of the collection of reals")
public class STDDeviationReal extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(STDDeviationReal.class.getName());
    /**
     * In.
     */
    @Input("advance:collection<advance:integer>")
    protected static final String IN = "in";
    /**
     * Out.
     */
    @Output("advance:real")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final XElement xcollection = get(IN);
        final Iterator<XElement> it = xcollection.children().iterator();

        /**
         * Knuth method
         */
        double avg = 0.0;
        double sum = 0.0;
        double i = 0;
        boolean first = true;
        while (it.hasNext()) {
            final double num = resolver().getDouble(it.next());
            
            if (first) {
                avg = num;
                first = false;
            }

            double newavg = avg + (num - avg) / (i + 1);
            sum += (num - avg) * (num - newavg);
            avg = newavg;
            i++;
        }

        dispatch(OUT, resolver().create(Math.sqrt(sum / i)));
    }
}
