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

import java.util.HashSet;
import java.util.Set;

import eu.advance.logistics.flow.engine.inference.TypeRelation;


/**
 * The XML capability description, basically the class field description.
 * Attributes and inner elements share the same description
 * and probably won't overlap in terms of a name.
 *  @author akarnokd
 */
public class XCapability implements XComparable<XCapability> {
	/** The element name. */
	public XName name;
	/** The element cardinality. */
	public XCardinality cardinality;
	/** The element's simple type if non null. */
	public XValueType valueType;
	/** The element's complex type if non null. */
	public XType complexType;
	@Override
	public TypeRelation compareTo(XCapability o) {
		return compareTo(o, new XTypeRecursionTracker());
	}
	/**
	 * Compare two capabilities in respect to the given type memory to
	 * avoid recursion on complex capabilities.
	 * @param o the other capability 
	 * @param memory the type memory
	 * @return the relation
	 */
	public TypeRelation compareTo(XCapability o, XTypeRecursionTracker memory) {
		int equal = 0;
		int ext = 0;
		int sup = 0;
		switch (name.compareTo(o.name)) {
		case EQUAL:
			equal++;
			break;
		case EXTENDS:
			ext++;
			break;
		case SUPER:
			sup++;
			break;
		case NONE:
			return TypeRelation.NONE;
		default:
		}
		if (complexType != null && o.complexType != null) {
			switch (complexType.compareTo(o.complexType, memory)) {
			case EQUAL:
				equal++;
				break;
			case EXTENDS:
				ext++;
				break;
			case SUPER:
				sup++;
				break;
			case NONE:
				return TypeRelation.NONE;
			default:
			}
		} else
		if ((complexType == null) != (complexType == null)) {
			return TypeRelation.NONE;
		}
		if (valueType != null && valueType == o.valueType) {
			equal++;
		}
		switch (XCardinality.compare(cardinality, o.cardinality)) {
		case EQUAL:
			equal++;
			break;
		case EXTENDS:
			ext++;
			break;
		case SUPER:
			sup++;
			break;
		case NONE:
			return TypeRelation.NONE;
		default:
		}
		
		int all = equal + ext + sup;
		if (all == equal) {
			return TypeRelation.EQUAL;
		}
		if (all == equal + ext) {
			return TypeRelation.EXTENDS;
		}
		if (all == equal + sup) {
			return TypeRelation.SUPER;
		}
		// mixed content, inconclusive
		return TypeRelation.NONE;
	}
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		toStringPretty("", b, new HashSet<XType>());
		return b.toString();
	}
	/**
	 * Pretty print the contents of this XCapability.
	 * @param indent the current indentation
	 * @param out the output buffer
	 * @param memory the types already expressed won't be detailed again
	 */
	void toStringPretty(String indent, StringBuilder out, Set<XType> memory) {
		out.append(indent).append("XCapability {").append(String.format("%n"));
		out.append(indent).append("  name = ").append(name).append(String.format(",%n"));
		out.append(indent).append("  cardinality = ").append(cardinality).append(String.format(",%n"));
		if (valueType != null) {
			out.append(indent).append("  valueType = ").append(valueType).append(String.format("%n"));
		}
		if (complexType != null) {
			if (!memory.contains(complexType)) {
				memory.add(complexType);
				out.append(indent).append("  complexType = ").append(String.format("%n"));
				complexType.toStringPretty(indent + "    ", out, memory);
				memory.remove(complexType);
			} else {
				out.append(indent).append("  complexType = XType ...").append(String.format("%n"));
			}
		}
		if (complexType == null && valueType == null) {
			out.append(indent).append("  type could not be determined").append(String.format("%n"));
		}
		out.append(indent).append("}").append(String.format("%n"));
	}
}
