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

package eu.advance.logistics.flow.engine.inference;

import java.util.List;

import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * A type wrapper for generic type-systems.
 * @author akarnokd, 2011.11.18.
 * @param <T> the type representing the concrete structure of the type
 */
public class TypeWrapper<T> implements Type {
	/** The counter used for type variables. */
	public int id;
	/** Reference to another type parameter within the same set of declarations. */
	public String variableName;
	/** The actual type variable object. */
	public TypeWrapperVariable<T> variable;
	/** An existing concrete type definition schema. */
	public String baseName;
	/** The concrete XML type of the target schema. */
	public T base;
	/** The type arguments used by the concrete type. */
	public final List<TypeWrapper<T>> typeArguments = Lists.newArrayList();
	@Override
	public TypeKind kind() {
		if (baseName != null) {
			if (typeArguments.isEmpty()) {
				return TypeKind.CONCRETE_TYPE;
			}
			return TypeKind.PARAMETRIC_TYPE;
		}
		return TypeKind.VARIABLE_TYPE;
	}
	/**
	 * A type variable with optional bounds.
	 * @author akarnokd, 2011.11.18.
	 * @param <T>
	 */
	public static class TypeWrapperVariable<T> {
		/** The type parameter name. */
		public String name;
		/** The upper bounds of this type parameter, e.g., T super SomeObject1 &amp; SomeObject2. */
		public final List<TypeWrapper<T>> bounds = Lists.newArrayList();
		/** Indicator if the bounds are representing the upper bound. */
		public boolean isUpperBound;
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			
			b.append(name);
			if (bounds.size() > 0) {
				if (isUpperBound) {
					b.append(" super ");
				} else {
					b.append(" extends ");
				}
				int i = 0;
				for (TypeWrapper<T> bound : bounds) {
					if (i > 0) {
						b.append(" & ");
					}
					b.append(bound);
				}
			}
			
			return b.toString();
		}
	}
	@Override
	public String toString() {
		if (baseName != null) {
			if (typeArguments.isEmpty()) {
				return baseName;
			}
			StringBuilder b = new StringBuilder(baseName);
			b.append("<");
			int i = 0;
			for (TypeWrapper<T> ta : typeArguments) {
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
			return String.format("%s[%d]", variableName, id);
		}
		return variableName;
		
	}
	/**
	 * Creates a fresh type variable with name T.
	 * @param <T> the base type that represents the structure
	 * @param name the type variable name
	 * @return Construct a fresh type variable with name T.
	 */
	public static <T> TypeWrapper<T> fresh(@Nullable String name) {
		TypeWrapper<T> result = new TypeWrapper<T>();
		result.variableName = name;
		result.variable = new TypeWrapperVariable<T>();
		result.variable.name = name;
		return result;
	}
	/**
	 * Creates a wrapped type from the supplied base type.
	 * <p>The base name is derived by invoking toString() on the base type.</p>
	 * @param <T> the base type system type
	 * @param baseType the base type
	 * @return the wrapped type
	 */
	@NonNull
	public static <T> TypeWrapper<T> from(@NonNull T baseType) {
		TypeWrapper<T> result = new TypeWrapper<T>();
		result.base = baseType;
		result.baseName = baseType.toString();
		return result;
	}
}
