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

package eu.advance.logistics.flow.engine.api;

import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;

/**
 * Describes the ability to execute and terminate a list of blocks.
 * @author akarnokd, 2011.10.04.
 */
public interface AdvanceFlowExecutor {
	/**
	 * Execute the given sequence of blocks.
	 * @param blocks the sequence of blocks
	 */
	void run(Iterable<? extends AdvanceBlock> blocks);
	/**
	 * Terminate the given sequence of blocks.
	 * @param blocks the sequence of blocks
	 */
	void done(Iterable<? extends AdvanceBlock> blocks);
}
