/*
 * Copyright 2010-2012 The Advance EU 7th Framework project consortium
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

package eu.advance.logistics.flow.engine;

import hu.akarnokd.reactive4java.base.Option;


/**
 * The diagnostic port for the ADVANCE blocks.
 * @author karnokd, 2011.06.22.
 */
public final class AdvanceBlockDiagnostic {
	/** The affected block. */
	public final AdvanceBlock block;
	/** The possible copy of the value within the port. */
	public final Option<AdvanceBlockState> state;
	/** The timestamp when the port received this value. */
	public final long timestamp = System.currentTimeMillis();
	/**
	 * Constructor.
	 * @param block the affected block
	 * @param state the block state
	 */
	public AdvanceBlockDiagnostic(AdvanceBlock block, Option<AdvanceBlockState> state) {
		this.block = block;
		this.state = state;
	}
}
