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
package eu.advance.logistics.flow.engine.block.structuring;

import hu.akarnokd.utils.xml.XNElement;

import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;

/**
 * Extracts elements from a collection and dispatches them one-by-one.
 * Signature: Unwrap(collection<t>) -> t
 * @author szmarcell
 */
@Block(id = "Unwrap", category = "data-transformations", scheduler = "IO", parameters = { "T" }, description = "Extracts elements from a collection and dispatches them one-by-one.")
public class Unwrap extends AdvanceBlock {

    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(Unwrap.class.getName());
    /** In. */
    @Input("advance:collection<?T>")
    protected static final String IN = "in";
    /** Out. */
    @Output("?T")
    protected static final String OUT = "out";
    @Override
    protected void invoke() {
        for (XNElement element : resolver().getList(get(IN))) {
            dispatch(OUT, element);
        }
    }
}
