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

package eu.advance.logistics.flow.engine.block;

import java.util.Map;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.api.core.AdvanceData;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockSettings;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Creates a map with a single key-value pair.
 * @author akarnokd, 2011.11.10.
 */
@Block(scheduler = "NOW", parameters = { "K", "V" }, 
description = "Creates a map with a single key-value pair.",
category = "data-transformations")
public class SingletonMap extends AdvanceBlock {
	/** Key. */
    @Input("?K")
    private static final String KEY = "key";
    /** Value. */
    @Input("?V")
    private static final String VALUE = "value";
    /** Out. */
    @Output("advance:map<?K,?V>")
    private static final String OUT = "out";
	/**
	 * Constructor.
	 * @param settings the block settings
	 */
	public SingletonMap(AdvanceBlockSettings settings) {
		super(settings);
	}

	@Override
	protected void invoke(Map<String, XElement> params) {
		dispatch(OUT, AdvanceData.createMap(params.get(KEY), params.get(VALUE)));
	}

}
