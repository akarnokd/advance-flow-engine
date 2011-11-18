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

import java.util.Deque;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


/**
 * An abstract base class for wrapped type functions.
 * @author akarnokd, 2011.11.18.
 * @param <T> the base type class that represents the structure
 */
public abstract class TypeWrapperFunctions<T> implements TypeFunctions<TypeWrapper<T>> {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(TypeWrapperFunctions.class);

	/* Implement: compare, intersection, union, fresh. */
	
	@Override
	public void setId(TypeWrapper<T> type, Set<TypeWrapper<T>> memory) {
		Deque<TypeWrapper<T>> deque = Lists.newLinkedList();
		deque.add(type);
		while (!deque.isEmpty()) {
			TypeWrapper<T> t = deque.removeFirst();
			if (type.kind() == TypeKind.VARIABLE_TYPE && memory.add(type)) {
				type.id = memory.size();
			}
			for (TypeWrapper<T> ta : t.typeArguments) {
				deque.addFirst(ta);
			}
		}
	}

	@Override
	public List<TypeWrapper<T>> arguments(TypeWrapper<T> type) {
		return type.typeArguments;
	}

	@Override
	public TypeWrapper<T> copy(TypeWrapper<T> type) {
		TypeWrapper<T> t = fresh();
		t.variable = type.variable;
		t.variableName = type.variableName;
		t.base = type.base;
		t.baseName = type.baseName;
		t.typeArguments.addAll(type.typeArguments);
		return t;
	}
	@Override
	public TypeWrapper<T> fresh() {
		return TypeWrapper.fresh();
	}
}
