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
 * Converts the elements of the collection into a new structure.
 * Signature: FromCollection(collection<t>, xpath, schema<u>) -> u
 * @author szmarcell
 */
@Block(id = "___FromCollection", category = "data-transformations", 
scheduler = "IO", 
description = "Converts the elements of the collection into a new structure.",
parameters = { "T", "U" })
public class FromCollection extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(FromCollection.class.getName());
    /** The collection of values. */
    @Input("advance:collection<?T>")
    protected static final String IN = "in";
    /** The XPath expression.*/
    @Input("advance:string")
    protected static final String XPATH = "xpath";
    /** The type token for the output. */
    @Input("advance:type<?U>")
    protected static final String TYPE = "type";
    /** Out. */
    @Output("?U")
    protected static final String OUT = "out";
    // TODO implement 
    @Override
    protected void invoke() {
    }
    
}
