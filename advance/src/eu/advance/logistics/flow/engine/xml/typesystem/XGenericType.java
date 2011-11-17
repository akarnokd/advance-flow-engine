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

import java.util.List;

import com.google.common.collect.Lists;

/**
 * The XML type argument.
 * @author akarnokd, 2011.11.15.
 */
public class XGenericType implements XSerializable {
	/** The type variable.*/
	public XGenericVariable typeVariable;
	/** The base type. */
	public String typeURI;
	/** The type structure. */
	public XGenericBaseType type;
	/** The arguments of a parametric type. */
	public final List<XGenericType> arguments = Lists.newArrayList();
	/** @return is this a type variable? */
	public boolean isVariable() {
		return typeVariable != null;
	}
	/**
	 * Create a deep copy of this generic type.
	 * @return the copy
	 */
	public XGenericType copy() {
		XGenericType result = new XGenericType();
		if (typeVariable != null) {
			result.typeVariable = typeVariable.copy();
		}
		result.typeURI = typeURI;
		result.type = type.copy(); // FIXME
		for (XGenericType arg : arguments) {
			result.arguments.add(arg.copy());
		}
		return result;
	}
	/** @return is this a parametric type? */
	public boolean isParametric() {
		return typeURI != null && !arguments.isEmpty();
	}
	/** @return is this a concrete type? */
	public boolean isConcrete() {
		return typeURI != null && arguments.isEmpty();
	}
	@Override
	public String toString() {
		if (isVariable()) {
			return typeVariable.toString();
		}
		StringBuilder b = new StringBuilder();
		b.append(typeURI);
		if (!arguments.isEmpty()) {
			b.append("<");
			for (int i = 0; i < arguments.size(); i++) {
				if (i > 0) {
					b.append(", ");
				}
				b.append(arguments.get(i));
			}
			b.append(">");
		}
		return b.toString();
	}
	@Override
	public void load(XElement source) {
		typeURI = source.get("type");
		String tv = source.get("type-variable");
		if (tv != null) {
			typeVariable = new XGenericVariable();
			typeVariable.name = tv;
		} else {
			for (XElement ta : source.childrenWithName("type-argument")) {
				XGenericType targ = new XGenericType();
				targ.load(ta);
				arguments.add(targ);
			}
		}
	}
	@Override
	public void save(XElement destination) {
		throw new UnsupportedOperationException();
	}
}