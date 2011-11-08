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
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockSettings;
import eu.advance.logistics.flow.engine.xml.typesystem.XData;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Creates a collection with a single element.
 * @author akarnokd, 2011.11.04.
 */
@Block(scheduler = "NOW", parameters = { "T" }, 
description = "Creates a collection with the supplied single element.",
category = "data-transformations")
public class Singleton extends AdvanceBlock {
	/** In. */
    @Input("?T")
    private static final String IN = "in";
    /** Out. */
    @Output("advance:collection<?T>")
    private static final String OUT = "out";
	/**
	 * Constructor.
	 * @param settings the block settings
	 */
	public Singleton(AdvanceBlockSettings settings) {
		super(settings);
	}

	@Override
	protected void invoke(Map<String, XElement> params) {
		dispatch(OUT, XData.create(params.get(IN)));
	}

}
