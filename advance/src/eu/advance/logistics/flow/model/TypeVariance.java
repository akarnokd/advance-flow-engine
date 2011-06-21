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
 * The expected type variance of a given input or output parameter.
 * @author karnokd, 2011.06.21.
 */
public enum TypeVariance {
	/** For types A and B you may wire in only if A = B. */
	NONVARIANT("nonvariant"),
	/** For types A and B where A extends B, you may wire in B where A is required. */
	COVARIANT("covariant"),
	/** For types A and B where B extends A, you may wire in A where B is required. */
	CONTRAVARIANT("contravariant")
	;
	/** The value in xml. */
	private final String asXML;
	/** 
	 * Constructor. Set the XML representation of the variance.
	 * @param asXML the variance name in {@code block-description.xsd} 
	 */
	TypeVariance(String asXML) {
		this.asXML = asXML;
	}
	/**
	 * Return a variance enum based on the xml value.
	 * Throws IllegalArgumentException if the xmlValue does not conform to an enum.
	 * @param xmlValue the value in the xml block description
	 * @return the type variance enum
	 */
	public static TypeVariance of(String xmlValue) {
		for (TypeVariance tv : values()) {
			if (tv.asXML.equals(xmlValue)) {
				return tv;
			}
		}
		throw new IllegalArgumentException(xmlValue);
	}
}
