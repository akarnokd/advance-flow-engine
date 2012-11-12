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

package eu.advance.logistics.flow.engine.block.pattern;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * Takes two sequences of channel data and deetermines the cross relation parameters between them.
 * @author karnokd, 2012.05.27.
 */
@Block(id = "CrossChannelClassifier", 
category = "classification", scheduler = "CPU", 
description = "Takes two sequences of channel data and deetermines the cross relation parameters between them.",
parameters = { "T", "U" }
)
public class CrossChannelClassifier extends AdvanceBlock {
	/** The learner configuration. */
	@Input("?T")
	protected static final String CONFIG = "config";
	/** The trigger to start the process. */
    @Input("advance:object")
    protected static final String TRIGGER = "trigger";
    /** The sequence of timed values of the first channel. */
    @Input("advance:option<advance:timedvalue>")
    protected static final String CHANNEL1 = "channel1";
    /** The sequence of timed values of the second channel. */
    @Input("advance:option<advance:timedvalue>")
    protected static final String CHANNEL2 = "channel2";
    /** The detected categories per group. */
    @Output("advance:map<advance:pair<advance:string, advance:string>, ?U>")
    protected static final String RELATIONS = "relations";
    /** The sequence of timed values of the first channel. */
    @Output("advance:string")
    protected static final String CHANNEL1GET = "channel_1_trigger";
    /** The sequence of timed values of the second channel. */
    @Output("advance:string")
    protected static final String CHANNEL2GET = "channel_2_trigger";
	@Override
	protected void invoke() {
	}
}
