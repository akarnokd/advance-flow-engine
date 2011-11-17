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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

/**
 * A maybe generic capability defining a simple, complex or generic typed {@code element}.
 * @author akarnokd, 2011.11.16.
 */
public class XGenericCapability {
	/** The element name. */
	public final XName name = new XName();
	/** The element cardinality. */
	public XCardinality cardinality;
	/** The element's simple type if non null. */
	public XValueType valueType;
	/** The element's complex type if non null. */
	private XGenericBaseType complexType;
	/** The generic type arguments. */
	public final List<XGenericType> arguments = Lists.newArrayList();
	/** The parent type. */
	public XGenericBaseType parent;
	/**
	 * Create a deep copy of this capability.
	 * @return the copy
	 */
	public XGenericCapability copy() {
		XGenericCapability result = new XGenericCapability();
		
		result.name.assign(name);
		result.cardinality = cardinality;
		result.valueType = valueType;
		result.complexType = complexType.copy();
		
		for (XGenericType gt : arguments) {
			result.arguments.add(gt.copy());
		}
		
		return result;
	}
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		toStringPretty("", b, new HashSet<XGenericBaseType>());
		return b.toString();
	}
	/**
	 * Pretty print the contents of this XCapability.
	 * @param indent the current indentation
	 * @param out the output buffer
	 * @param memory the types already expressed won't be detailed again
	 */
	void toStringPretty(String indent, StringBuilder out, 
			Set<XGenericBaseType> memory) {
		out.append(indent).append("XCapability");
		if (!arguments.isEmpty()) {
			out.append("<");
			for (int i = 0; i < arguments.size(); i++) {
				if (i > 0) {
					out.append(", ");
				}
				out.append(arguments.get(i));
			}
			out.append(">");
		}
		out.append(" {").append(String.format("%n"));
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
				out.append(indent).append("  complexType = XGenericBaseType ...").append(String.format("%n"));
			}
		}
		if (complexType == null && valueType == null) {
			out.append(indent).append("  type could not be determined").append(String.format("%n"));
		}
		out.append(indent).append("}").append(String.format("%n"));
	}
	/**
	 * Returns the complex type if any.
	 * @return the complex type or null
	 */
	public XGenericBaseType complexType() {
		return complexType;
	}
	/**
	 * Sets the complex type.
	 * @param newComplexType the complex type
	 */
	public void complexType(XGenericBaseType newComplexType) {
		this.complexType = newComplexType;
		if (complexType != null) {
			this.complexType.parent = this;
		}
	}
}
