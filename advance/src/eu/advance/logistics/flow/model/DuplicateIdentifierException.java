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

package eu.advance.logistics.flow.model;

/**
 * A duplicate identifier was found in the descriptions. 
 * @author karnokd, 2011.07.07.
 */
public class DuplicateIdentifierException extends RuntimeException {
	/** */
	private static final long serialVersionUID = 4323213617722459290L;
	/** The path to the identifier. */
	public final String path;
	/** The identifier value. */
	public final String identifier;
	/**
	 * Constructor.
	 * @param path the path to the identifier
	 * @param identifier the identifier
	 */
	public DuplicateIdentifierException(String path, String identifier) {
		this.path = path;
		this.identifier = identifier;
	}
	@Override
	public String toString() {
		return path + " " + identifier;
	}
	@Override
	public String getMessage() {
		return toString();
	}
}
