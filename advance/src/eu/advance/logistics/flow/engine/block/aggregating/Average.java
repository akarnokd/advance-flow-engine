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

import hu.akarnokd.reactive4java.base.Pair;

import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.AdvanceData;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Compute the average of the integer or real values within the collection.
 * Signature: Average(collection<object>) -> real
 *
 * @author TTS
 */
@Block(id = "Average", category = "aggregation", 
scheduler = "IO", description = "Compute the average of the integer or real values within the collection")
public class Average extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(Average.class.getName());
    /**
     * In.
     */
    @Input("advance:collection<advance:object>")
    protected static final String IN = "in";
    /**
     * Out.
     */
    @Output("advance:real")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        int count = 0;
        double sum = 0.0;

        final XElement xc = get(IN);
        for (XElement e : resolver().getItems(xc)) {
        	Pair<String, String> rn = AdvanceData.realName(e);
        	if ("integer".equals(rn.first)) {
            	sum += resolver().getInt(e);
                count++;
        	} else
        	if ("real".equals(rn.first)) {
            	sum += resolver().getDouble(e);
                count++;
        	} else {
        		String s = resolver().getString(e);
        		if (s.matches("\\d+(\\.d+)?")) {
                	sum += resolver().getDouble(e);
                    count++;
        		}
        	}
        }
        dispatch(OUT, resolver().create(sum / count));
    }
}
