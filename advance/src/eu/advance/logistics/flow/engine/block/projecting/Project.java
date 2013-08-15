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

import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * Applies the XPath expression to extract a value from the incoming value.
 * Signature: Project(t, xpath, type&lt;u>) -> u
 * @author szmarcell
 */
@Block(id = "___Project", category = "projection", scheduler = "IO", 
description = "Applies the XPath expression to extract a value from the incoming value.",
parameters = { "T", "U" }
)
public class Project extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(Project .class.getName());
    /** In. */
    @Input("?T")
    protected static final String IN = "in";
    /** The XPath expression. */
    @Input("advance:string")
    protected static final String XPATH = "xpath";
    /** Type token. */
    @Input("advance:type<?U>")
    protected static final String TYPE = "in";
    /** Out. */
    @Output("advance:real")
    protected static final String OUT = "out";
    // TODO implement 
    @Override
    protected void invoke() {
    }
    
}
