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

package eu.advance.logistics.flow.engine.model.fd;

/**
 * The enum representing the kind of an Advance type.
 * @author akarnokd, 2011.07.07.
 */
public enum AdvanceTypeKind {
	/** A concrete and exact type, e.g., advance:integer and such. */
	CONCRETE_TYPE,
	/** A concrete basetype with one or more generic type parameter, such as advance:collection. */
	PARAMETRIC_TYPE,
	/** An arbitrary type variable with optional type constraints. */
	VARIABLE_TYPE
}
