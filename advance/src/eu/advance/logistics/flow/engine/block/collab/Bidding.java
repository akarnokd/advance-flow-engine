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

package eu.advance.logistics.flow.engine.block.collab;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * Scatters the bidding request to partners and collects their bid values for consignments.
 * @author karnokd, 2012.02.27.
 */
@Block(id = "Bidding", 
category = "collaboration", scheduler = "IO", 
description = "Scatters the bidding request to partners and collects their bid values for consignments.",
parameters = { "T", "U" }
)
public class Bidding extends AdvanceBlock {
	/** The learner configuration. */
	@Input("?T")
	protected static final String CONFIG = "config";
	/** The trigger to start the process. */
    @Input("advance:object")
    protected static final String TRIGGER = "trigger";
	/** The current time index. */
    @Input("advance:integer")
    protected static final String TIME = "time";
    /** The current virtual situation. */
    @Input("?U")
    protected static final String VS = "virtualSituation";
    /** The sequence of consignments to bid for. */
    @Input("advance:option<advance:consignment>")
    protected static final String CONSIGNMENTS = "consignments";
    /** The bid values per group for the given consignment identifiers. */
    @Output("advance:map<advance:string,advance:collection<advance:pair<advance:integer,advance:real>>>")
    protected static final String BIDS = "bids";
	@Override
	protected void invoke() {
	}
}
