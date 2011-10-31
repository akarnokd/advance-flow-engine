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

package eu.advance.logistics.flow.engine.model.fd;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializable;
import eu.advance.logistics.flow.engine.xml.typesystem.XType;

/**
 * The type parameter bound definition for a generic type parameter.
 * @author akarnokd, 2011.07.01.
 */
public class AdvanceType implements XSerializable {
	/** Reference to another type parameter within the same set of declarations. */
	public String typeVariableName;
	/** The actual type variable object. */
	public AdvanceTypeVariable typeVariable;
	/** An existing concrete type definition schema. */
	public URI typeURI;
	/** The concrete XML type of the target schema. */
	public XType type;
	/** The type arguments used by the concrete type. */
	public final List<AdvanceType> typeArguments = Lists.newArrayList();
	@Override
	public String toString() {
		if (typeURI != null) {
			if (typeArguments.isEmpty()) {
				return typeURI.toString();
			}
			StringBuilder b = new StringBuilder(typeURI.toString());
			b.append("<");
			int i = 0;
			for (AdvanceType ta : typeArguments) {
				if (i > 0) {
					b.append(", ");
				}
				b.append(ta);
				i++;
			}
			b.append(">");
			return b.toString();
		}
		return typeVariableName;
		
	}
	/** @return create a new instance of this type declaration. */
	public AdvanceType copy() {
		AdvanceType result = new AdvanceType();
		result.typeVariableName = typeVariableName;
		result.typeURI = typeURI;
		result.type = type;
		if (typeVariable != null) {
			result.typeVariable = typeVariable.copy();
		}
		for (AdvanceType ta : typeArguments) {
			result.typeArguments.add(ta.copy());
		}
		return result;
	}
	/**
	 * @return Construct a fresh type variable with name T.
	 */
	public static AdvanceType fresh() {
		AdvanceType result = new AdvanceType();
		result.typeVariableName = "T";
		result.typeVariable = new AdvanceTypeVariable();
		result.typeVariable.name = "T";
		return result;
	}
	/**
	 * Load a type description from an XML element which conforms the {@code block-description.xsd}.
	 * @param root the root element of an input/output node.
	 */
	@Override
	public void load(XElement root) {
		typeVariableName = root.get("type-variable");
		String tu = root.get("type");
		if ((tu != null) == (typeVariableName != null)) {
			throw new IllegalArgumentException("Only one of the type-variable and type arguments should be defined! " + root);
		}
		if (tu != null) {
			try {
				typeURI = new URI(tu);
			} catch (URISyntaxException ex) {
				throw new RuntimeException(ex);
			}
		}
		for (XElement ta : root.childrenWithName("type-argument")) {
			AdvanceType at = new AdvanceType();
			at.load(ta);
			typeArguments.add(at);
		}
	}
	@Override
	public void save(XElement destination) {
		destination.set("type-variable", typeVariableName);
		destination.set("type", typeURI);
		for (AdvanceType at : typeArguments) {
			at.save(destination.add("type-argument"));
		}
	}
	/** @return the kind of this type. */
	public AdvanceTypeKind getKind() {
		if (typeURI != null) {
			if (typeArguments.isEmpty()) {
				return AdvanceTypeKind.CONCRETE_TYPE;
			}
			return AdvanceTypeKind.PARAMETRIC_TYPE;
		}
		return AdvanceTypeKind.VARIABLE_TYPE;
	}
}
