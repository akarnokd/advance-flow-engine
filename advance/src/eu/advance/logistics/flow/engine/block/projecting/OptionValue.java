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
import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.AdvanceData;

/**
 * Extracts the contained value from the option if it has any.
 * @author karnokd, 2012.02.24.
 */
@Block(id = "OptionValue", category = "projection", 
scheduler = "NOW", 
description = "Extracts the contained value from the option if it has any.",
parameters = { "T" }
)
public class OptionValue extends AdvanceBlock {
    /** In. */
    @Input("advance:option<?T>")
    protected static final String IN = "in";
    /** Out. */
    @Output("?T")
    protected static final String OUT = "out";
    @Override
    protected void invoke() {
    	Option<XNElement> opt = AdvanceData.getOption(get(IN));
    	if (Option.isSome(opt)) {
    		dispatch(OUT, opt.value());
    	}
    }
}
