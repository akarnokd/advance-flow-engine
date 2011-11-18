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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * A base interface to report the inference results and errors.
 * @author karnokd, 2011.11.18.
 * @version $Revision 1.0$
 * @param <T> the type system type
 * @param <W> the back-reference to the original relation
 */
public interface InferenceResult<T extends Type, W> {
	/**
	 * Returns the current associated type with the given wire.
	 * @param wire the wire
	 * @return the current type or null if not yet set
	 */
	@Nullable
	T getType(@NonNull W wire);
	/**
	 * Set the wire type.
	 * @param wire the target wire
	 * @param type the target type
	 */
	void setType(@NonNull W wire, T type);
	/**
	 * Signal an inference error.
	 * @param type1 the relation's first type
	 * @param type2 the relation's second type
	 * @param wire the source of the relation
	 */
	void errorIncompatibleTypes(T type1, T type2, W wire);
	/**
	 * Signal an inference error.
	 * @param type1 the relation's first type
	 * @param type2 the relation's second type
	 * @param wire the source of the relation
	 */
	void errorIncompatibleBaseTypes(T type1, T type2, W wire);
	/**
	 * Signal an inference error.
	 * @param type1 the relation's first type
	 * @param type2 the relation's second type
	 * @param wire the source of the relation
	 */
	void errorArgumentCount(T type1, T type2, W wire);
	/**
	 * Signal an inference error.
	 * @param type1 the relation's first type
	 * @param type2 the relation's second type
	 * @param wire the source of the relation
	 */
	void errorCombinedType(T type1, T type2, W wire);
	/**
	 * Signal an inference error.
	 * @param type1 the relation's first type
	 * @param type2 the relation's second type
	 * @param wire the source of the relation
	 */
	void errorConcreteVsParametricType(T type1, T type2, W wire);
}
