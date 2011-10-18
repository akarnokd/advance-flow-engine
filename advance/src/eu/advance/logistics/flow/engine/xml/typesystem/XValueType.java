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
package eu.advance.logistics.flow.engine.xml.typesystem;

/**
 * The enumeration for simple XML value types.
 *  @author akarnokd
 */
public enum XValueType {
	/** Represents a binary value type. */
	BOOLEAN,
	/** Represents an integral value type (of any precision). */
	INTEGER,
	/** Represents a floating point value type (of any precision). */
	REAL,
	/** An exact timestamp value. */
	TIMESTAMP,
	/** Represents an arbitrary text type. */
	STRING
}
