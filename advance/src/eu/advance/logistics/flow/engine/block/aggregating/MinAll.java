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
import hu.akarnokd.utils.xml.XNElement;

import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.AdvanceData;

/**
 * Returns the smallest value from the collection along with the collection of
 * its occurrence indexes. Signature: MinAll(collection<object>) -> (real,
 * collection<integer>)
 *
 * @author TTS
 */
@Block(id = "MinAll", 
	category = "aggregation", 
	scheduler = "IO", 
	description = "Returns the smallest value from the collection along with the collection of its occurrence indexes", 
	parameters = { "T" }
)
public class MinAll extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(MinAll.class.getName());
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
    @Output("advance:collection<advance:integer>")
    protected static final String OUT2 = "out2";

    @Override
    protected void invoke() {
    	List<Integer> positions = Lists.newArrayList();

    	double min = 0;
        int count = 0;

    	for (XNElement e : resolver().getItems(get(IN))) {
    		Pair<String, String> rn = AdvanceData.realName(e);
    		double v = 0.0;
    		if ("integer".equals(rn)) {
    			v = resolver().getInt(e);
            } else if ("real".equals(rn)) {
    			v = resolver().getDouble(e);
            } else {
    			continue;
            }

    		if (count == 0 || min > v) {
    			min = v;
    			positions.clear();
    		}
    		if (min == v) {
    			positions.add(count);
            }

            count++;
        }

    	if (count > 0) {
        dispatch(OUT1, resolver().create(min));
    	}
    	List<XNElement> xpos = Lists.newLinkedList();
    	for (Integer idx : positions) {
    		xpos.add(resolver().create(idx));
    	}
        dispatch(OUT2, resolver().create(xpos));
    }
}
