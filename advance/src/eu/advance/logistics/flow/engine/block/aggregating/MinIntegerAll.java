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

import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;

/**
 * Returns the smallest integer value from the collection along with the
 * collection of its occurrence indexes. Signature:
 * MinIntegerAll(collection<integer>) -> integer
 *
 * @author TTS
 */
@Block(id = "MinIntegerAll", category = "aggregation", scheduler = "IO", description = "Returns the smallest integer value from the collection along with the collection of its occurrence indexes")
public class MinIntegerAll extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(MinIntegerAll.class.getName());
    /**
     * In.
     */
    @Input("advance:collection<advance:integer>")
    protected static final String IN = "in";
    /**
     * Out.
     */
    @Output("advance:integer")
    protected static final String OUT1 = "out1";
    /**
     * Out.
     */
    @Output("advance:collection<advance:integer>")
    protected static final String OUT2 = "out2";

    @Override
    protected void invoke() {
    	List<Integer> positions = Lists.newArrayList();

    	int min = 0;
        int count = 0;

    	for (XNElement e : resolver().getItems(get(IN))) {
			int v = resolver().getInt(e);
    		
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
