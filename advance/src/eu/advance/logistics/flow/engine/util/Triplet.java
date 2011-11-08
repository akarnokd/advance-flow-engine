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

package eu.advance.logistics.flow.engine.util;

import com.google.common.base.Objects;

/**
 * A three element tuple.
 * @author akarnokd, 2011.07.07.
 * @param <A> the first type
 * @param <B> the second type
 * @param <C> the third type
 */
public final class Triplet<A, B, C> {
	/** The first element. */
	public final A first;
	/** The second element. */
	public final B second;
	/** The third element. */
	public final C third;
	/**
	 * Constructor.
	 * @param first the first element
	 * @param second the second element
	 * @param third the third element
	 */
	public Triplet(A first, B second, C third) {
		super();
		this.first = first;
		this.second = second;
		this.third = third;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Triplet<?, ?, ?>) {
			Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) obj;
			return 
					Objects.equal(first, triplet.first)
					&& Objects.equal(second, triplet.second)
					&& Objects.equal(third, triplet.third);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return Objects.hashCode(first, second, third);
	}
	@Override
	public String toString() {
		return "{" + first + ", " + second + ", " + third + "}";
	}
	/**
	 * Construct a triplet.
	 * @param <A> the first type
	 * @param <B> the second type
	 * @param <C> the third type
	 * @param first the first value
	 * @param second the second value
	 * @param third the third value
	 * @return the triplet
	 */
	public static <A, B, C> Triplet<A, B, C> of(A first, B second, C third) {
		return new Triplet<A, B, C>(first, second, third);
	}
}
