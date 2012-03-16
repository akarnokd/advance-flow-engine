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
import java.util.logging.Logger;

/**
 * Compute the sum of the elements within the collection which have the type of
 * Integer or Real. Signature: SumReal(collection<real>) -> real
 *
 * @author TTS
 */
@Block(id = "SumReal", category = "aggregation", scheduler = "IO", description = "Compute the sum of the collection of integers")
public class SumReal extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(SumReal.class.getName());
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
        double sum = 0.0;
        
        for (XElement xelem : resolver().getItems(get(IN))) {
            sum += resolver().getDouble(xelem);
        }
        
        dispatch(OUT, resolver().create(sum));
    }
}
