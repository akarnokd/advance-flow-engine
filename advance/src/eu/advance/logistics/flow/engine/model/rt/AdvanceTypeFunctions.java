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

package eu.advance.logistics.flow.engine.model.rt;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.inference.TypeFunctions;
import eu.advance.logistics.flow.engine.inference.TypeKind;
import eu.advance.logistics.flow.engine.inference.TypeRelation;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.xml.typesystem.XSchema;
import eu.advance.logistics.flow.engine.xml.typesystem.XType;

/**
 * The type functions for the AdvanceType type system.
 * @author karnokd, 2011.11.18.
 * @version $Revision 1.0$
 */
public class AdvanceTypeFunctions implements TypeFunctions<AdvanceType> {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceTypeFunctions.class);
	@Override
	public AdvanceType intersection(AdvanceType t1, AdvanceType t2) {
		AdvanceType t = new AdvanceType();
		// if parametric vs concrete, the common supertype is advance:object
		if (t1.kind() != t2.kind()) {
			t.type = new XType();
			t.typeURI = AdvanceData.OBJECT;
		} else {
			t.type = XSchema.intersection(t1.type, t2.type);
			if (t.type == t1.type) {
				t.typeURI = t1.typeURI;
			} else
			if (t.type == t2.type) {
				t.typeURI = t2.typeURI;
			} else {
				try {
					t.typeURI =  new URI("advance:intersect_" + System.identityHashCode(t));
				} catch (URISyntaxException ex) {
					LOG.debug(ex.toString(), ex);
				}
			}
		}
		return t;
	}
	@Override
	public AdvanceType union(AdvanceType t1, AdvanceType t2) {
		AdvanceType t = new AdvanceType();
		if (t1.kind() != t2.kind()) {
			return null;
		}
		t.type = XSchema.union(t1.type, t2.type);
		if (t.type != null) {
			if (t.type == t1.type) {
				t.typeURI = t1.typeURI;
			} else
			if (t.type == t2.type) {
				t.typeURI = t2.typeURI;
			} else {
				try {
					t.typeURI =  new URI("advance:union_" + System.identityHashCode(t));
				} catch (URISyntaxException ex) {
					LOG.debug(ex.toString(), ex);
				}
			}
			return t;
		}
		return null;
	}

	@Override
	public TypeRelation compare(AdvanceType t1, AdvanceType t2) {
		return XSchema.compare(t1.type, t2.type);
	}

	@Override
	public AdvanceType createTop() {
		AdvanceType t = fresh();
		t.typeURI = AdvanceData.OBJECT;
		t.type = new XType();
		return t;
	}

	@Override
	public AdvanceType createBottom() {
		return null;
	}

	@Override
	public void setId(AdvanceType type, Set<AdvanceType> memory) {
		Deque<AdvanceType> deque = Lists.newLinkedList();
		deque.add(type);
		while (!deque.isEmpty()) {
			AdvanceType t = deque.removeFirst();
			if (type.kind() == TypeKind.VARIABLE_TYPE && memory.add(type)) {
				type.id = memory.size();
			}
			for (AdvanceType ta : t.typeArguments) {
				deque.addFirst(ta);
			}
		}
	}

	@Override
	public List<AdvanceType> arguments(AdvanceType type) {
		return type.typeArguments;
	}

	@Override
	public AdvanceType fresh() {
		return new AdvanceType();
	}

	@Override
	public AdvanceType copy(AdvanceType type) {
		AdvanceType t = fresh();
		t.type = type.type;
		t.typeURI = type.typeURI;
		t.typeArguments.addAll(type.typeArguments);
		return null;
	}
	
}
