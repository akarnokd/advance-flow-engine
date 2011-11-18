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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

/**
 * Contains the base type definition and structure along with
 * any generic type information.
 * <p>Extracted from {@code simpleType} and {@code complexType} definitions. 
 * @author akarnokd, 2011.11.16.
 */
public class XGenericBaseType {
	/** The list of maybe generic capabilities. */
	private final List<XGenericCapability> capabilities = Lists.newArrayList();
	/** The generic type variables. */
	public final List<XGenericVariable> variables = Lists.newArrayList();
	/** The parent capability. */
	public XGenericCapability parent;
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		toStringPretty("", b, new HashSet<XGenericBaseType>());
		return b.toString();
	}
	/**
	 * Create a deep copy of this type.
	 * @return the copy
	 */
	public XGenericBaseType copy() {
		XGenericBaseType result = new XGenericBaseType();
		for (XGenericCapability cap : capabilities) {
			result.add(cap.copy());
		}
		for (XGenericVariable v : variables) {
			result.variables.add(v.copy());
		}
		return result;
	}
	/**
	 * Add an array of capabilities to this base type.
	 * @param capabilities the capabilitiy
	 */
	public void add(XGenericCapability... capabilities) {
		add(Arrays.asList(capabilities));
	}
	/**
	 * Add an array of capabilities to this base type.
	 * @param capabilities the capabilities
	 */
	public void add(Iterable<XGenericCapability> capabilities) {
		for (XGenericCapability cap : capabilities) {
			cap.parent = this;
			this.capabilities.add(cap);
		}
	}
	/**
	 * Returns an unmodifiable list of capabilities.
	 * @return the unmodifiable list of capabilitites
	 */
	public List<XGenericCapability> capabilities() {
		return Collections.unmodifiableList(capabilities);
	}
	/**
	 * Pretty print the contents of this XType.
	 * @param indent the current indentation
	 * @param out the output buffer
	 * @param memory the memory to avoid infinite type display
	 */
	void toStringPretty(String indent, StringBuilder out, 
			Set<XGenericBaseType> memory) {
		out.append(indent).append("XGenericBaseType");
		if (!variables.isEmpty()) {
			out.append("<");
			for (int i = 0; i < variables.size(); i++) {
				if (i > 0) {
					out.append(", ");
				}
				out.append(getMoreConcreteType(i));
			}
			out.append(">");
		}
		out.append(" [");
		if (capabilities.size() > 0) {
			out.append(String.format("%n"));
			for (XGenericCapability c : capabilities) {
				c.toStringPretty(indent + "  ", out, memory);
			}
			out.append(indent).append("]");
		} else {
			out.append("]");
		}
		out.append(String.format("%n"));
	}
	/**
	 * Try to find a more concrete type.
	 * @param index the variable index
	 * @return the more generic type
	 */
	public XGenericType getMoreConcreteType(int index) {
		if (parent != null) {
			if (index < parent.arguments.size()) {
				XGenericType t = parent.arguments.get(index);
				return t;
			}
		}
		XGenericType t = new XGenericType();
		t.typeVariable = variables.get(index);
		return t;
	}
}
