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
import eu.advance.logistics.flow.engine.typesystem.XSchema;
import eu.advance.logistics.flow.engine.typesystem.XType;

/**
 * A type functions class to work with regular Java types.
 * @author akarnokd, 2011.11.18.
 */
public class AdvanceXTypeFunctions extends
		TypeWrapperFunctions<XType> {

	@Override
	public TypeWrapper<XType> intersection(TypeWrapper<XType> t1, TypeWrapper<XType> t2) {
		TypeWrapper<XType> t = TypeWrapper.from(null);
		// if parametric vs concrete, the common supertype is advance:object
		if (t1.kind() != t2.kind()) {
			t.base = new XType();
			t.baseName = AdvanceData.OBJECT.toString();
		} else {
			t.base = XSchema.intersection(t1.base, t2.base);
			if (t.base == t1.base) {
				t.baseName = t1.baseName;
			} else
			if (t.base == t2.base) {
				t.baseName = t2.baseName;
			} else {
				t.baseName =  "advance:intersect_" + System.identityHashCode(t);
			}
		}
		return t;
	}
	@Override
	public TypeWrapper<XType> union(TypeWrapper<XType> t1, TypeWrapper<XType> t2) {
		TypeWrapper<XType> t = TypeWrapper.from(null);
		if (t1.kind() != t2.kind()) {
			return null;
		}
		t.base = XSchema.union(t1.base, t2.base);
		if (t.base != null) {
			if (t.base == t1.base) {
				t.baseName = t1.baseName;
			} else
			if (t.base == t2.base) {
				t.baseName = t2.baseName;
			} else {
				t.baseName =  "advance:union_" + System.identityHashCode(t);
			}
			return t;
		}
		return null;
	}

	@Override
	public TypeRelation compare(TypeWrapper<XType> t1, TypeWrapper<XType> t2) {
		return XSchema.compare(t1.base, t2.base);
	}

}
