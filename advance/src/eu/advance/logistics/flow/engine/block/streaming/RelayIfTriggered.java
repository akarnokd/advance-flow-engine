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
package eu.advance.logistics.flow.engine.block.streaming;

import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * Relay a single given value only if the trigger value has arrived.
 * Signature: RelayIfTriggered(trigger, t) -> t
 * @author szmarcell
 */
@Block(id = "RelayIfTriggered", category = "streaming", scheduler = "NOW", description = "Relay a single given value only if the trigger value has arrived.",
parameters = { "T", "U" })
public class RelayIfTriggered extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(RelayIfTriggered .class.getName());
    /** In. */
    @Input("?T")
    protected static final String IN = "in";
    /** The trigger value. */
    @Input("?U")
    protected static final String TRIGGER = "trigger";
    /** Out. */
    @Output("advance:real")
    protected static final String OUT = "out";
    @Override
    protected void invoke() {
    	// the trigger is ignored as it is used by the collector to synchronize.
    	dispatch(OUT, get(IN));
    }
    
}
