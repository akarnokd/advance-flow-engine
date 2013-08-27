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

package eu.advance.logistics.flow.engine.block.projecting;

import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.utils.xml.XNElement;

import java.util.List;

import com.google.common.collect.Lists;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.AdvanceData;

/**
 * Aggregates the incoming SOME options until a NONE is detected and emits a regular collection of elements.
 * @author karnokd, 2012.02.24.
 */
@Block(id = "CollectOptions", category = "projection", 
scheduler = "NOW", 
description = "Aggregates the incoming SOME options until a NONE is detected and emits a regular collection of elements.",
parameters = { "T" }
)
public class CollectOptions extends AdvanceBlock {
    /** In. */
    @Input("advance:option<?T>")
    protected static final String IN = "in";
    /** Out. */
    @Output("advance:collection<?T>")
    protected static final String OUT = "out";
    /** The aggregator. */
    protected List<XNElement> collection = Lists.newArrayList();
    @Override
    protected void invoke() {
    	Option<XNElement> opt = AdvanceData.getOption(get(IN));
    	if (Option.isSome(opt)) {
    		collection.add(opt.value());
    	} else
    	if (Option.isNone(opt)) {
    		dispatch(OUT, resolver().create(collection));
    	}
    }
}
