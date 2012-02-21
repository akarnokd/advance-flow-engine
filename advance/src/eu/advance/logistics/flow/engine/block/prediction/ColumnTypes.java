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

package eu.advance.logistics.flow.engine.block.prediction;

/**
 * The set of binary column types.
 * @author karnokd, 2012.02.21.
 */
public enum ColumnTypes {
	/** 1 byte. */
	BYTE(1),
	/** 1 short. */
	SHORT(2),
	/** 1 integer. */
	INT(4),
	/** 1 long. */
	LONG(8),
	/** Number of minutes since epoch. */
	MINUTES(4),
	/** Number of days since epoch. */
	DAYS(4),
	/** GPS coordinates: lattitute and longitude integers (1E10 * lat, 1E10 * long). */
	GPS(8)
	;
	/** The record size in bytes. */
	public final int size;
	/**
	 * Create the enumeration value.
	 * @param size the record size in bytes
	 */
	ColumnTypes(int size) {
		this.size = size;
	}
}
