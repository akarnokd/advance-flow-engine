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

import com.google.common.base.Objects;

import eu.advance.logistics.flow.engine.inference.TypeRelation;

/**
 * A basic URI semantics with only equivalence relation.
 * @author akarnokd, 2011.07.05.
 */
public class UriSemantics implements XSemantics {
	/** The URI for the semantics. */
	public final String uri;
	/**
	 * Constructor.
	 * @param uri the semantics used
	 */
	public UriSemantics(String uri) {
		this.uri = uri;
	}
	@Override
	public TypeRelation compareTo(XSemantics other) {
		if (other instanceof UriSemantics) {
			if (Objects.equal(((UriSemantics) other).uri, uri)) {
				return TypeRelation.EQUAL;
			}
		}
		return TypeRelation.NONE;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UriSemantics) {
			UriSemantics that = (UriSemantics) obj;
			return Objects.equal(uri, that.uri);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return uri != null ? uri.hashCode() : 0;
	}
	@Override
	public String toString() {
		return uri;
	}
}
