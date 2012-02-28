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

package eu.advance.logistics.flow.engine;

import eu.advance.logistics.flow.engine.inference.TypeRelation;
import eu.advance.logistics.flow.engine.inference.TypeWrapper;
import eu.advance.logistics.flow.engine.inference.TypeWrapperFunctions;

/**
 * A type functions class to work with regular Java types.
 * @author akarnokd, 2011.11.18.
 */
public class AdvanceJavaTypeFunctions extends
		TypeWrapperFunctions<Class<?>> {

	@Override
	public TypeWrapper<Class<?>> intersection(TypeWrapper<Class<?>> first,
			TypeWrapper<Class<?>> second) {
		
		Class<?> t1 = first.base;
		Class<?> t2 = second.base;
		if (t1 == null) {
			return first;
		} else
		if (t2 == null) {
			return second;
		} else
		if (t1.isAssignableFrom(t2)) {
			return first;
		} else
		if (t2.isAssignableFrom(t1)) {
			return second;
		}
		TypeWrapper<Class<?>> tw = TypeWrapper.from(null);
		tw.base = Object.class;
		tw.baseName = Object.class.getName();
		
		return tw;
	}

	@Override
	public TypeWrapper<Class<?>> union(TypeWrapper<Class<?>> first,
			TypeWrapper<Class<?>> second) {
		Class<?> t1 = first.base;
		Class<?> t2 = second.base;
		if (t1 == null) {
			return second;
		} else
		if (t2 == null) {
			return first;
		} else
		if (t1.isAssignableFrom(t2)) {
			return second;
		} else
		if (t2.isAssignableFrom(t1)) {
			return first;
		}
		return null;
	}

	@Override
	public TypeRelation compare(TypeWrapper<Class<?>> first,
			TypeWrapper<Class<?>> second) {
		Class<?> t1 = first.base;
		Class<?> t2 = second.base;
		if (t1 == null && t2 == null) {
			return TypeRelation.EQUAL;
		} else
		if (t1 == null) {
			return TypeRelation.SUPER;
		} else
		if (t2 == null) {
			return TypeRelation.EXTENDS;
		} else
		if (t1.isAssignableFrom(t2)) {
			return TypeRelation.SUPER;
		} else
		if (t2.isAssignableFrom(t1)) {
			return TypeRelation.EXTENDS;
		}
		return TypeRelation.NONE;
	}

}
