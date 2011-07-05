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
package eu.advance.logistics.xml.typesystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The definition of an XML type: basically a root element
 * or an element with complex type.
 * @author karnok, 2011.03.09.
 */
public class XType implements XComparable<XType> {
	/** The capability set of the type. */
	public final List<XCapability> capabilities = new ArrayList<XCapability>();
	@Override
	public XRelation compareTo(XType o) {
		return compareTo(o, new HashSet<XType>());
	}
	/**
	 * @return Create a copy of this XType object.
	 */
	public XType copy() {
		XType result = new XType();
		result.capabilities.addAll(capabilities);
		return result;
	}
	/**
	 * Perform the type comparison by using the given memory to avoid infinite recursion.
	 * @param o the type to check against
	 * @param memory the memory to keep track the traversed types
	 * @return the relation
	 */
	public XRelation compareTo(XType o, Set<XType> memory) {
		memory.add(this);
		int equal = 0;
		int ext = 0;
		int sup = 0;
		for (XCapability c0 : capabilities) {
			// FIXME recursive type check terrible
			if (c0.complexType == null || !memory.contains(c0.complexType)) {
				for (XCapability c1 : o.capabilities) {
					switch (c0.compareTo(c1, memory)) {
					case EQUAL:
						equal++;
						break;
					case EXTENDS:
						ext++;
						break;
					case SUPER:
						sup++;
						break;
					default:
					}
				}
			}
		}
		memory.remove(this);
		// common
		int all = equal + ext + sup;
		if (all < capabilities.size()
				&& all < o.capabilities.size()) {
			return XRelation.NONE;
		}
		int diff = capabilities.size() - o.capabilities.size();
		
		if (all == equal) {
			if (diff > 0) {
				return XRelation.EXTENDS;
			} else
			if (diff < 0) {
				return XRelation.SUPER;
			}
			return XRelation.EQUAL;
		}
		if (all == equal + ext && diff >= 0) {
			return XRelation.EXTENDS;
		}
		if (all == equal + sup && diff <= 0) {
			return XRelation.SUPER;
		}
		// mixed content, inclonclusive
		return XRelation.NONE;
	}
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		toStringPretty("", b, new HashSet<XType>());
		return b.toString();
	}
	/**
	 * Pretty print the contents of this XType.
	 * @param indent the current indentation
	 * @param out the output buffer
	 * @param memory the memory to avoid infinite type display
	 */
	void toStringPretty(String indent, StringBuilder out, Set<XType> memory) {
		out.append(indent).append("XType [").append(String.format("%n"));
		if (capabilities.size() > 0) {
			for (XCapability c : capabilities) {
				c.toStringPretty(indent + "  ", out, memory);
			}
			out.append(indent).append("]");
		} else {
			out.append(" ]");
		}
		out.append(String.format("%n"));
	}
}
