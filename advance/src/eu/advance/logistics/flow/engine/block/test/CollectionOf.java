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

import java.util.List;

import com.google.common.collect.Lists;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.runtime.Port;

/**
 * Creates a collection from the variable parameters.
 * @author akarnokd, 2011.07.01.
 */
@Block(scheduler = "NOW", description = "Creates a collection from the variable parameters.", parameters = { "T" })
public class CollectionOf extends AdvanceBlock {
	/** 1. */
    @Input(value = "?T", variable = true, required = false)
    protected static final String IN = "in";
    /** Out. */
    @Output("advance:collection<?T>")
    private static final String OUT = "out";
	
	@Override
	protected void invoke() {
		List<XNElement> result = Lists.newArrayList();
		
		for (Port<XNElement, AdvanceType> p : inputs()) {
			if (p.name().startsWith(IN)) {
				result.add(get(p.name()));
			}
		}
		
		dispatch(OUT, resolver().create(result));
	}
}
