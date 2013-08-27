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

import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;

/**
 * Join two objects structurally and return the new structure.
 * Signature: Join(t, u, schema<v>) -> v
 * @author szmarcell
 */
@Block(id = "___Join", category = "data-transformations", scheduler = "IO", parameters = { "T", "U", "V +T,+U" }, description = "Join two objects structurally and return the new structure.")
public class Join extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(Join .class.getName());
    /** In T. */
    @Input("?T")
    protected static final String IN_T = "t";
    /** In U. */
    @Input("?U")
    protected static final String IN_U = "u";
    /** Out. */
    @Output("?V")
    protected static final String OUT = "v";
    @Override
    protected void invoke() {
    }
    
}
