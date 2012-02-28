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
 * Computes the standard deviation of numerical elements of the supplied
 * collection. Signature: STDDeviation(collection<T>) -> real
 *
 * @author TTS
 */
@Block(id = "STDDeviation", category = "aggregation", scheduler = "IO", 
description = "Computes the standard deviation of numerical elements of the supplied collection")
public class STDDeviation extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(STDDeviation.class.getName());
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
    	int n = 0;
    	double mean = 0d;
    	double m2 = 0d;
    	
    	for (XElement e : resolver().getItems(get(IN))) {
    		Pair<String, String> rn = AdvanceData.realName(e);
    		double v = 0.0;
    		if ("integer".equals(rn)) {
    			v = resolver().getInt(e);
    		} else
    		if ("real".equals(rn)) {
    			v = resolver().getDouble(e);
    		} else {
    			continue;
    		}
    		n++;
    		
    		double delta = v - mean;
    		mean = mean + delta / n;
    		if (n > 1) {
    			m2 = m2 + delta * (v - mean);
    		}
    	}

        dispatch(OUT, resolver().create(Math.sqrt(m2 / n)));
    }
}
