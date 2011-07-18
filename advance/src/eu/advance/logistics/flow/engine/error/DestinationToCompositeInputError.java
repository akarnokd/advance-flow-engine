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

package eu.advance.logistics.flow.engine.error;

import eu.advance.logistics.flow.model.AdvanceBlockBind;

/**
 * The wire's destination is bound to an input port of the parent composite block's.
 * @author karnokd, 2011.07.07.
 */
public class DestinationToCompositeInputError implements AdvanceCompilationError {
	/** The wire identifier. */
	public final AdvanceBlockBind binding;
	/**
	 * Constructor.
	 * <p>The wire's destination is bound to an input port of the parent composite block's.</p>
	 * @param binding the actual binding causing the problem
	 */
	public DestinationToCompositeInputError(AdvanceBlockBind binding) {
		this.binding = binding;
	}
}