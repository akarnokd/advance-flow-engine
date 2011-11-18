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

import java.util.List;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * The interface to represent varios functions for the type inference.
 * @author karnokd, 2011.11.18.
 * @version $Revision 1.0$
 * @param <T> a type system type
 */
public interface TypeFunctions<T extends Type> {
	/**
	 * Returns the intersection type (e.g., the common supertype) of the given two types.
	 * <p>Intersection may be always possible, as two undelated types may still have a
	 * common top type (see createTop).</p>
	 * @param first the first type
	 * @param second the second type
	 * @return the intersection type
	 */
	@Nullable
	T intersection(@NonNull T first, @NonNull T second);
	/**
	 * Returns the union type (e.g., the common subtype) of the given two types if possible.
	 * <p>Union might not be possible.</p>
	 * @param first the first type
	 * @param second the second type
	 * @return the union type or null if no such type can be constructed.
	 */
	@Nullable
	T union(@NonNull T first, @NonNull T second);
	/**
	 * Compares two types.
	 * @param first the first type
	 * @param second the second type
	 * @return the relation
	 */
	@NonNull
	TypeRelation compare(@NonNull T first, @NonNull T second);
	/**
	 * Creates a top type, e.g., a type {@code k} which for every {@code t} {@code k <= t}.
	 * <p>A Java example would be a {@code java.lang.Object}.</p>
	 * @return the created top type
	 */
	@Nullable
	T createTop();
	/**
	 * Create a bottom type, e.g., a type {@code k} which for every {@code t} {@code t <= k}.
	 * @return the created bottom type
	 */
	@Nullable
	T createBottom();
	/**
	 * Adds the given type to the memory and sets an unique identifier.
	 * <p>Used for debugging purposes.</p>
	 * @param type the type
	 * @param memory the memory
	 */
	void setId(T type, Set<T> memory);
	/**
	 * Returns the list of the type arguments.
	 * @param type the type
	 * @return the type arguments
	 */
	List<T> arguments(T type);
	/**
	 * Create a fresh type variable.
	 * @return the new type variable
	 */
	T fresh();
	/**
	 * Creates a copy of the given type.
	 * @param type the source type
	 * @return the new type
	 */
	T copy(T type);
}
