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

package eu.advance.logistics.flow.engine.inference;

/**
 * Represents a type inequality, noted by left &lt;: right.
 * @author akarnokd, 2011.11.18.
 * @param <T> the type system type
 * @param <W> the reference to the source of the inequality
 */
public class Relation<T, W> {
	/** The left type. */
	public T left;
	/** The right type. */
	public T right;
	/** The binding wire. */
	public W wire;
	/** Construct an empty type relation. */
	public Relation() {

	}
	/**
	 * Construct a type relation with the initial values.
	 * @param left the left type
	 * @param right the right type
	 * @param wire the original wire
	 */
	public Relation(T left, T right, W wire) {
		this.left = left;
		this.right = right;
		this.wire = wire;
	}
	/**
	 * Copy-construct a type relation with the initial values.
	 * @param other the other type relation
	 */
	public Relation(Relation<? extends T, ? extends W> other) {
		this.left = other.left;
		this.right = other.right;
		this.wire = other.wire;
	}
	@Override
	public String toString() {
		//			return String.format("%s(%s):%s >= %s(%s):%s (%s)", wire.sourceBlock, wire.sourceParameter, left, wire.destinationBlock, wire.destinationParameter, right, wire.id);
		return String.format("%s >= %s (%s)", left, right, wire);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Relation) {
			Relation<?, ?> tr = (Relation<?, ?>) obj;
			return left == tr.left && right == tr.right;
		}
		return false;
	}
	@Override
	public int hashCode() {
		return left.hashCode() * 31 + right.hashCode();
	}
}