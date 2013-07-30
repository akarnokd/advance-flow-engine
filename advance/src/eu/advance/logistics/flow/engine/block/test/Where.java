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

package eu.advance.logistics.flow.engine.block.test;

import hu.akarnokd.utils.xml.XNElement;

import java.util.Map;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;

/**
 * A simple block which takes an object type T and applies a filter function on it.
 * If the filter function returns true, the value is submitted to the output.
 * @author akarnokd, 2011.11.14.
 */
@Block(category = "data-filtering", description = "Filter values by Javascript", 
scheduler = "NOW", parameters = { "T" })
public class Where extends Lambda {
	/** The script input. */
	@Input("advance:string")
	protected static final String SCRIPT = "script";
	/** The input value. */
	@Input("?T")
	protected static final String INPUT = "input";
	/** The output value. */
	@Output("?T")
	protected static final String OUT = "out";

	@Override
	protected String scriptParamName() {
		return SCRIPT;
	}
	@Override
	protected void scriptValue(Map<String, XNElement> params, Object o) {
		if (o instanceof Boolean) {
			Boolean b = (Boolean) o;
			if (b) {
				dispatch(OUT, params.get(INPUT));
			}
		}
	}
}
