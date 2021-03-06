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
package eu.advance.logistics.flow.engine.typesystem;

import eu.advance.logistics.flow.engine.inference.TypeRelation;

/**
 * The cardinality enum for an element.
 * @author akarnokd
 */
public enum XCardinality {
	/** Not occurring (e.g., explicitly forbidden). */
	ZERO,
	/** Zero or one occurrence. */
	ZERO_OR_ONE,
	/** Zero or any number of occurrence. */
	ZERO_OR_MANY,
	/** Exactly one occurrence. */
	ONE,
	/** One or more. */
	ONE_OR_MANY
	;
	/*
	private static final XMLRelation[][] relation = {
		        /*  0      ?      *      1      +   * /
		/* 0 * / { EQUAL, SUPER, SUPER, SUPER, SUPER },
		/* ? * / { null,  EQUAL, SUPER, SUPER, SUPER },
		/* * * / { null,  null,  EQUAL, SUPER, SUPER },
		/* 1 * / { null,  null,  null,  EQUAL, SUPER },
		/* + * / { null,  null,  null,  null,  EQUAL },
	};*/

	/**
	 * Computes the relation between two cardinality values. 
	 * @param n1 the first cardinality value
	 * @param n2 the second cardinality value
	 * @return the relation
	 */
	public static TypeRelation compare(XCardinality n1, XCardinality n2) {
		if (n1 == n2) {
			return TypeRelation.EQUAL;
		} else
		if (n1.ordinal() < n2.ordinal()) {
			return TypeRelation.SUPER;
		}
		return TypeRelation.EXTENDS;
	}
}
