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
package eu.advance.logistics.flow.engine.xml.typesystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * The definition of an XML type: basically a root element
 * or an element with complex type.
 * @author akarnokd, 2011.03.09.
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
				inner:
				for (XCapability c1 : o.capabilities) {
					// the same member?
					if (c0.name.compareTo(c1.name) != XRelation.NONE) {
						switch (c0.compareTo(c1, memory)) {
						case EQUAL:
							equal++;
							break inner;
						case EXTENDS:
							ext++;
							break inner;
						case SUPER:
							sup++;
							break inner;
						default:
						}
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
		// mixed content, inconclusive
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
		out.append(indent).append("XType");
		out.append(" [");
		if (capabilities.size() > 0) {
			out.append(String.format("%n"));
			for (XCapability c : capabilities) {
				c.toStringPretty(indent + "  ", out, memory);
			}
			out.append(indent).append("]");
		} else {
			out.append("]");
		}
		out.append(String.format("%n"));
	}
	/**
	 * Computes the intersection type of {@code this} type and the {@code other} type where
	 * this extends intersect and other extends intersect. 
	 * <p>If this extends other then other is returned;</p> 
	 * <p>if other extends this, this is returned;</p>
	 * <p>otherwise, a common subset is computed and a new XType is returned.</p> 
	 * @param other the other type
	 * @return the intersection type
	 */
	public XType intersection(XType other) {
		return intersection(other, new HashSet<XType>());
	}
	/**
	 * Computes the intersection type of {@code this} type and the {@code other} type where
	 * this extends intersect and other extends intersect. 
	 * <p>If this extends other then other is returned;</p> 
	 * <p>if other extends this, this is returned;</p>
	 * <p>otherwise, a common subset is computed and a new XType is returned.</p> 
	 * @param other the other type
	 * @param memory the recursion memory
	 * @return the intersection type
	 */
	public XType intersection(XType other, Set<XType> memory) {
		XRelation rel = compareTo(other);
		if (rel == XRelation.EXTENDS) {
			return other;
		} else
		if (rel == XRelation.SUPER || rel == XRelation.EQUAL) {
			return this;
		}
		XType is = new XType();
		memory.add(this);
		for (XCapability c0 : capabilities) {
			if (c0.complexType == null || !memory.contains(c0.complexType)) {
				inner:
				for (XCapability c1 : other.capabilities) {
					// the same member?
					if (c0.name.compareTo(c1.name) != XRelation.NONE) {
						XCapability c2 = new XCapability();
						c2.name = c0.name;
						// both complex types?
						if ((c0.complexType != null) && (c1.complexType != null)) {
							c2.complexType = c0.complexType.intersection(c1.complexType, memory);
						} else {
							if (c0.valueType == c1.valueType) {
								c2.valueType = c0.valueType;
							} else {
								c2.complexType = new XType(); // object
							}
						}
						switch (XCardinality.compare(c0.cardinality, c1.cardinality)) {
						case SUPER:
						case EQUAL:
							c2.cardinality = c0.cardinality;
							break;
						case EXTENDS:
							c2.cardinality = c1.cardinality;
							break;
						default:
							// can't happen
						}
						is.capabilities.add(c2);
						break inner;
					}
				}
			}
		}
		memory.remove(this);
		return is;
	}
	/**
	 * Computes the union type of {@code this} and {@code other}, meaning {@code union extends this} and
	 * {@code union extends other}.
	 * The union function may return null if no union type could be created due conflicting
	 * primitive types.
	 * @param other the other type
	 * @return the union type 
	 */
	public XType union(XType other) {
		return union(other, new HashSet<XType>());
	}

	/**
	 * Computes the union type of {@code this} and {@code other}, meaning {@code union extends this} and
	 * {@code union extends other}.
	 * The union function may return null if no union type could be created due conflicting
	 * primitive types.
	 * @param other the other type
	 * @param memory the type memory to avoid recursion
	 * @return the union type 
	 */
	public XType union(XType other, Set<XType> memory) {
		XRelation rel = compareTo(other);
		if (rel == XRelation.EXTENDS || rel == XRelation.EQUAL) {
			return this;
		} else
		if (rel == XRelation.SUPER) {
			return other;
		}
		XType is = new XType();
		memory.add(this);
		outer:
		for (XCapability c0 : capabilities) {
			if (c0.complexType == null || !memory.contains(c0.complexType)) {
				for (XCapability c1 : other.capabilities) {
					// the same member?
					if (c0.name.compareTo(c1.name) != XRelation.NONE) {
						XCapability c2 = new XCapability();
						c2.name = c0.name;
						// both complex types?
						if ((c0.complexType != null) && (c1.complexType != null)) {
							c2.complexType = c0.complexType.union(c1.complexType, memory);
							if (c2.complexType == null) {
								return null; // can't union the types
							}
						} else {
							if (c0.valueType == c1.valueType) {
								c2.valueType = c0.valueType;
							} else {
								return null; // can't union the types
							}
						}
						switch (XCardinality.compare(c0.cardinality, c1.cardinality)) {
						case EXTENDS:
						case EQUAL:
							c2.cardinality = c0.cardinality;
							break;
						case SUPER:
							c2.cardinality = c1.cardinality;
							break;
						default:
							// can't happen
						}
						is.capabilities.add(c2);
						continue outer;
					}
				}
				// not found in c1
				is.capabilities.add(c0);
			}
		}
		outer2:
		for (XCapability c1 : other.capabilities) {
			for (XCapability c0 : capabilities) {
				if (c0.complexType == null || !memory.contains(c0.complexType)) {
					if (c0.name.compareTo(c1.name) != XRelation.NONE) {
						continue outer2;
					}					
				}
			}
			is.capabilities.add(c1);
		}
		memory.remove(this);
		return is;
	}
}
