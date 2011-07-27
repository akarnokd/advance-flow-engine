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
import eu.advance.logistics.flow.model.AdvanceType;

/**
 * The wire binds a concrete type and a parametric type which cannot be in subtype relation.
 * @author karnokd, 2011.07.21.
 */
public class ConcreteVsParametricTypeError implements AdvanceCompilationError {
	/** The wire identifier. */
	public final AdvanceBlockBind binding;
	/** The left side of the binding. */
	public final AdvanceType left;
	/** The right side of the binding. */
	public final AdvanceType right;
	/**
	 * Constructor.
	 * <p>The wire binds a concrete type and a parametric type which cannot be in subtype relation.</p>
	 * @param binding the actual binding causing the problem
	 * @param left the left side of the binding
	 * @param right the right side of the binding
	 */
	public ConcreteVsParametricTypeError(AdvanceBlockBind binding, AdvanceType left, AdvanceType right) {
		this.binding = binding;
		this.left = left;
		this.right = right;
	}
	@Override
	public String toString() {
		return "Concrete vs Parametric type conflict on wire " + binding.id + ": " + left + " vs. " + right;
	}
}
