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

package eu.advance.logistics.flow.engine.model.fd;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.inference.Type;
import eu.advance.logistics.flow.engine.inference.TypeKind;
import eu.advance.logistics.flow.engine.typesystem.XType;
import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.flow.engine.xml.XSerializable;

/**
 * The type parameter bound definition for a generic type parameter.
 * @author akarnokd, 2011.07.01.
 */
public class AdvanceType implements Type, XSerializable {
	/** The counter used for type variables. */
	public int id;
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
		if (id > 0) {
			return String.format("%s[%d]", typeVariableName, id);
		}
		return typeVariableName;
		
	}
	/** @return create a new instance of this type declaration. */
	public AdvanceType copy() {
		AdvanceType result = new AdvanceType();
		result.id = id;
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
	 * Create a fresh type variable with the given name.
	 * @param name the type variable name
	 * @return Construct a fresh type variable with name.
	 */
	public static AdvanceType fresh(String name) {
		AdvanceType result = new AdvanceType();
		result.typeVariableName = name;
		result.typeVariable = new AdvanceTypeVariable();
		result.typeVariable.name = name;
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
		id = root.getInt("type-id", 0);
		for (XElement ta : root.childrenWithName("type-argument")) {
			AdvanceType at = new AdvanceType();
			at.load(ta);
			typeArguments.add(at);
		}
	}
	@Override
	public void save(XElement destination) {
		if (id > 0) {
			destination.set("type-id", id);
		}
		destination.set("type-variable", typeVariableName);
		destination.set("type", typeURI);
		for (AdvanceType at : typeArguments) {
			at.save(destination.add("type-argument"));
		}
	}
	@Override
	public TypeKind kind() {
		if (typeURI != null) {
			if (typeArguments.isEmpty()) {
				return TypeKind.CONCRETE_TYPE;
			}
			return TypeKind.PARAMETRIC_TYPE;
		}
		return TypeKind.VARIABLE_TYPE;
	}
	/**
	 * Creates a concrete (e.g., no type arguments or parametric type with the supplied base type and type arguments.
	 * <p>Note: the {@code AdvanceType.type} is not resolved.</p>
	 * <p>Example:</p>
	 * <pre>createType(COLLECTION, createType(STRING))</pre>
	 * @param baseType the base type
	 * @param types the type arguments.
	 * @return the created new type
	 */
	@NonNull
	public static AdvanceType createType(@NonNull URI baseType, AdvanceType... types) {
		return createType(baseType, Arrays.asList(types));
	}
	/**
	 * Creates an unbounded variable type.
	 * @param name the variable name
	 * @return the created type
	 */
	@NonNull
	public static AdvanceType createType(@NonNull String name) {
		AdvanceType t = new AdvanceType();
		t.typeVariableName = name;
		t.typeVariable = new AdvanceTypeVariable();
		t.typeVariable.name = name;
		return t;
	}
	/**
	 * Creates a bounded variable type.
	 * @param name the variable name
	 * @param upperBound defines if the type bounds are the upper bounds, e.g., {@code T super B1, B2, B3}.
	 * @param bound1 the first bound
	 * @param boundsRest the rest of the bounds
	 * @return the created type
	 */
	@NonNull
	public static AdvanceType createType(@NonNull String name, boolean upperBound, AdvanceType bound1, AdvanceType... boundsRest) {
		AdvanceType t = new AdvanceType();
		t.typeVariableName = name;
		t.typeVariable = new AdvanceTypeVariable();
		t.typeVariable.name = name;
		t.typeVariable.isUpperBound = upperBound;
		t.typeVariable.bounds.add(bound1);
		t.typeVariable.bounds.addAll(Arrays.asList(boundsRest));
		return t;
	}
	/**
	 * Creates a bounded variable type.
	 * @param name the variable name
	 * @param upperBound defines if the type bounds are the upper bounds, e.g., {@code T super B1, B2, B3}.
	 * @param bounds the type bounds
	 * @return the created type
	 */
	@NonNull
	public static AdvanceType createType(@NonNull String name, boolean upperBound, Iterable<? extends AdvanceType> bounds) {
		AdvanceType t = new AdvanceType();
		t.typeVariableName = name;
		t.typeVariable = new AdvanceTypeVariable();
		t.typeVariable.name = name;
		t.typeVariable.isUpperBound = upperBound;
		Iterables.addAll(t.typeVariable.bounds, bounds);
		return t;
	}
	
	/**
	 * Creates a concrete (e.g., no type arguments or parametric type with the supplied base type and type arguments.
	 * <p>Note: the {@code AdvanceType.type} is not resolved.</p>
	 * <p>Example:</p>
	 * <pre>createType(COLLECTION, createType(STRING))</pre>
	 * @param baseType the base type
	 * @param types the type arguments.
	 * @return the created new type
	 */
	@NonNull
	public static AdvanceType createType(@NonNull URI baseType, Iterable<? extends AdvanceType> types) {
		AdvanceType t = new AdvanceType();
		t.typeURI = baseType;
		Iterables.addAll(t.typeArguments, types);
		return t;
		
	}
	/**
	 * Creates an {@code advance:type} type constructor XML.
	 * @param type the type to convert to XML
	 * @return the XML representing the type constructor
	 */
	public static XElement createType(AdvanceType type) {
		XElement result = new XElement("type");
		type.save(result);
		return result;
	}
	/**
	 * Converts the XML type declaration into an actual AdvanceType object.
	 * @param type the type XML
	 * @return the  the type object
	 */
	public static AdvanceType getType(XElement type) {
		AdvanceType t = new AdvanceType();
		t.load(type);
		return t;
	}
}
