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

package eu.advance.logistics.flow.engine;

import hu.akarnokd.reactive4java.base.Func2;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import eu.advance.logistics.flow.engine.AdvanceCompiler.TypeRelation;
import eu.advance.logistics.flow.engine.error.CombinedTypeError;
import eu.advance.logistics.flow.engine.error.ConcreteVsParametricTypeError;
import eu.advance.logistics.flow.engine.error.IncompatibleBaseTypesError;
import eu.advance.logistics.flow.engine.error.IncompatibleTypesError;
import eu.advance.logistics.flow.engine.model.AdvanceCompilationError;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockBind;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.model.fd.AdvanceTypeKind;
import eu.advance.logistics.flow.engine.model.rt.AdvanceCompilationResult;
import eu.advance.logistics.flow.engine.xml.typesystem.SchemaParser;
import eu.advance.logistics.flow.engine.xml.typesystem.XRelation;

/**
 * Utility class for the type inference.
 * @author akarnokd, 2011.09.28.
 */
public final class AdvanceTypeInference {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceTypeInference.class);
	/**
	 * Utility class.
	 */
	private AdvanceTypeInference() {
	}
	/**
	 * Test if the given relations list contains a relation where the left and right are the given values.
	 * @param relations the iterable of the relations
	 * @param left the left type
	 * @param right the right type
	 * @return true if present
	 */
	static boolean containsRelation(Iterable<TypeRelation> relations, AdvanceType left, AdvanceType right) {
		for (TypeRelation t : relations) {
			if (t.left == left && t.right == right) {
				return true;
			}
		}
		return false;
	}
	/**
	 * <p>Type inference using the algorithm described by</p>
	 * <p>Francois Pottier: Type inference in presence of subtyping: from theory to practice</p>.
	 * @param relations the available type relations as extracted from the program.
	 * @param result the compilation result
	 */
	public static void infer(Deque<TypeRelation> relations, AdvanceCompilationResult result) {
		Multimap<AdvanceType, AdvanceType> upperBound = setMultimap();
		Multimap<AdvanceType, AdvanceType> lowerBound = setMultimap();
		List<TypeRelation> reflexives = Lists.newArrayList();
		Multimap<AdvanceType, String> wireRelations = setMultimap();
		for (TypeRelation tr : relations) {
			wireRelations.put(tr.left, tr.wire.id);
			wireRelations.put(tr.right, tr.wire.id);
		}
		
		while (!relations.isEmpty()) {
			TypeRelation rel = relations.pop();
			if (rel.left.getKind() == AdvanceTypeKind.VARIABLE_TYPE && rel.right.getKind() == AdvanceTypeKind.VARIABLE_TYPE) {
				if (!containsRelation(reflexives, rel.left, rel.right)) {
					// add new reflexive relations for x >= left and right >= y 
					int size = reflexives.size();
					boolean found1 = false;
					boolean found2 = false;
					for (int i = 0; i < size; i++) {
						TypeRelation ab = reflexives.get(i);
						// if ab.left >= left and left >= right then ab.left >= right
						if (ab.right == rel.left) {
							found1 = true;
							reflexives.add(new TypeRelation(ab.left, rel.right, rel.wire));
							if (!combineBounds(upperBound, ab.left, rel.right, unionFunc, result.errors, rel.wire)) {
								return;
							}
						}
						// if right >= ab.right and left >= right then left >= ab.right
						if (ab.left == rel.right) {
							found2 = true;
							reflexives.add(new TypeRelation(rel.left, ab.right, rel.wire));
							combineBounds(lowerBound, ab.right, rel.left, intersectFunc, result.errors, rel.wire);
						}
					}
					if (!found1) {
						if (!combineBounds(upperBound, rel.left, rel.right, unionFunc, result.errors, rel.wire)) {
							return;
						}
					}
					if (!found2) {
						combineBounds(lowerBound, rel.right, rel.left, intersectFunc, result.errors, rel.wire);
					}
					
					reflexives.add(new TypeRelation(rel));
					// call subc with lower(rel.left) >= upper(rel.right) ?! 
					for (AdvanceType lb : lowerBound.get(rel.left)) {
						for (AdvanceType ub : upperBound.get(rel.right)) {
							if (!subc(lb, ub, rel.wire, relations, result.errors)) {
								return;
							}
						}
					}
				}
			} else
			if (rel.left.getKind() == AdvanceTypeKind.VARIABLE_TYPE && rel.right.getKind() != AdvanceTypeKind.VARIABLE_TYPE) {
				if (!upperBound.get(rel.left).contains(rel.right)) {
					// for each left >= ab.right
					boolean found = false;
					for (TypeRelation ab : reflexives) {
						if (ab.right == rel.left) {
							found = true;
							// append the right to the upper bounds
							if (!addBound(upperBound, ab.left, rel.right, unionFunc)) {
								return;
							}
						}
					}
					if (!found) {
						addBound(upperBound, rel.left, rel.right, unionFunc);
					}
					for (AdvanceType lb : lowerBound.get(rel.left)) {
						if (!subc(lb, rel.right, rel.wire, relations, result.errors)) {
							return;
						}
					}
				}
			} else
			if (rel.left.getKind() != AdvanceTypeKind.VARIABLE_TYPE && rel.right.getKind() == AdvanceTypeKind.VARIABLE_TYPE) {
				if (!lowerBound.get(rel.right).contains(rel.left)) {
					// for each ab.left >= right
					boolean found = false;
					for (TypeRelation ab : reflexives) {
						if (ab.left == rel.right) {
							found = true;
							addBound(lowerBound, ab.right, rel.left, intersectFunc);
						}
					}
					if (!found) {
						addBound(lowerBound, rel.right, rel.left, intersectFunc);
					}
					// call subc with rel.left >= upper(rel.right)
					for (AdvanceType lb : upperBound.get(rel.right)) {
						if (!subc(rel.left, lb, rel.wire, relations, result.errors)) {
							return;
						}
					}
				}
			} else {
				// check if both constants or both parametric types
				if (rel.left.getKind() != rel.right.getKind()) {
					result.errors.add(new ConcreteVsParametricTypeError(rel.wire, rel.left, rel.right));
					return;
				} else
				if (rel.left.getKind() == AdvanceTypeKind.CONCRETE_TYPE) {
					XRelation xr = SchemaParser.compare(rel.left.type, rel.right.type);
					if (xr != XRelation.EQUAL && xr != XRelation.EXTENDS) {
						result.errors.add(new IncompatibleTypesError(rel.wire, rel.left, rel.right));
						return;
					} else {
						result.wireTypes.put(rel.wire.id, rel.right);
					}
				} else {
					if (!subc(rel.left, rel.right, rel.wire, relations, result.errors)) {
						return;
					}
				}
			}
		}
		// FIXME not sure this is correct
		// if we get here, all wires should have some type
		for (AdvanceType type : Sets.union(upperBound.keySet(), lowerBound.keySet())) {
			AdvanceType computedType = null;
			// find minimum of upper bound
			for (AdvanceType ub : upperBound.get(type)) {
				if (ub.getKind() != AdvanceTypeKind.VARIABLE_TYPE) {
					computedType = ub;
					break;
				}
			}
			if (computedType == null) {
				// find maximum of lower bound
				for (AdvanceType lb : lowerBound.get(type)) {
					if (lb.getKind() != AdvanceTypeKind.VARIABLE_TYPE) {
						computedType = lb;
						break;
					}
				}
			}
			// if still no concrete type, have it abstract
			if (computedType == null) {
				computedType = type;
			}
			// locate wire with mentioning the type
			for (String wire : wireRelations.get(type)) {
				result.wireTypes.put(wire, computedType);
			}
		}
	}
	/**
	 * Based on the structure of left >= right, creates new type relations and places it back to relations.
	 * @param left the left expression
	 * @param right the right expression
	 * @param wire the wire for the relation
	 * @param relations the relations output
	 * @param error the list where the errors should be reported
	 * @return true if no conflict was detected
	 */
	static boolean subc(AdvanceType left, AdvanceType right, AdvanceBlockBind wire, 
			Deque<TypeRelation> relations, List<AdvanceCompilationError> error) {
		// if left >= right is elementary, e.g., neither of them is a parametric type, just return a relation with them
		if (left.getKind() != AdvanceTypeKind.PARAMETRIC_TYPE && right.getKind() != AdvanceTypeKind.PARAMETRIC_TYPE) {
			if (left.getKind() == AdvanceTypeKind.CONCRETE_TYPE && right.getKind() == AdvanceTypeKind.CONCRETE_TYPE) {
				// the two concrete types are not related
				XRelation xr = SchemaParser.compare(left.type, right.type);
				if (xr != XRelation.EQUAL && xr != XRelation.EXTENDS) {
					error.add(new IncompatibleTypesError(wire, left, right));
					return false;
				}
			}
			relations.add(new TypeRelation(left, right, wire));
		} else
		// if C(t1,...,tn) >= right
		if (left.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE && right.getKind() != AdvanceTypeKind.PARAMETRIC_TYPE) {
			if (right.getKind() == AdvanceTypeKind.CONCRETE_TYPE) {
				error.add(new ConcreteVsParametricTypeError(wire, left, right));
				return false;
			}
			for (AdvanceType t : left.typeArguments) {
				if (!subc(t, AdvanceType.fresh(), wire, relations, error)) {
					return false;
				}
			}
		} else
		// if left >= C(t1,...,tn)
		if (left.getKind() != AdvanceTypeKind.PARAMETRIC_TYPE && right.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE) {
			if (left.getKind() == AdvanceTypeKind.CONCRETE_TYPE) {
				error.add(new ConcreteVsParametricTypeError(wire, left, right));
				return false;
			}
			for (AdvanceType t : right.typeArguments) {
				if (!subc(AdvanceType.fresh(), t, wire, relations, error)) {
					return false;
				}
			}
		} else {
			// if D(t1,...,tn) >= C(u1,...,un)
			if (left.typeArguments.size() != right.typeArguments.size()) {
				return false;
			}
			// the two concrete types are not related
			XRelation xr = SchemaParser.compare(left.type, right.type);
			if (xr != XRelation.EQUAL && xr != XRelation.EXTENDS) {
				error.add(new IncompatibleBaseTypesError(wire, left, right));
				return false;
			}
			Iterator<AdvanceType> ts = left.typeArguments.iterator();
			Iterator<AdvanceType> us = right.typeArguments.iterator();
			while (ts.hasNext() && us.hasNext()) {
				if (!subc(ts.next(), us.next(), wire, relations, error)) {
					return false;
				}
			}
		}
		
		return true;
	}
	/** The intersection function. */
	static Func2<AdvanceType, AdvanceType, AdvanceType> intersectFunc = new Func2<AdvanceType, AdvanceType, AdvanceType>() {
		@Override
		public AdvanceType invoke(AdvanceType param1, AdvanceType param2) {
			return intersection(param1, param2);
		}
	};
	/** The intersection function. */
	static Func2<AdvanceType, AdvanceType, AdvanceType> unionFunc = new Func2<AdvanceType, AdvanceType, AdvanceType>() {
		@Override
		public AdvanceType invoke(AdvanceType param1, AdvanceType param2) {
			return union(param1, param2);
		}
	};
	/**
	 * Computes the intersection of two Advance types if they are concrete.
	 * @param t1 the first type
	 * @param t2 the second type
	 * @return the intersection type
	 */
	static AdvanceType intersection(AdvanceType t1, AdvanceType t2) {
		AdvanceType t = new AdvanceType();
		try {
			t.typeURI =  new URI("advance:custom_" + System.identityHashCode(t));
		} catch (URISyntaxException ex) {
			LOG.debug(ex.toString(), ex);
		}
		t.type = SchemaParser.intersection(t1.type, t2.type);
		return t;
	}
	/**
	 * Computes the new union type of two advance types if they are concrete or returns null if the types can't be unioned together.
	 * @param t1 the first type
	 * @param t2 the second type
	 * @return the union type or null
	 */
	static AdvanceType union(AdvanceType t1, AdvanceType t2) {
		AdvanceType t = new AdvanceType();
		try {
			t.typeURI =  new URI("advance:custom_" + System.identityHashCode(t));
		} catch (URISyntaxException ex) {
			LOG.debug(ex.toString(), ex);
		}
		t.type = SchemaParser.union(t1.type, t2.type);
		if (t.type != null) {
			return t;
		}
		return null;
	}
	/**
	 * Combines the bounds of {@code target} and {@code addBoundsOf} by intersecting the concrete types of both and
	 * joining with the rest of the bound types (variables or parametric types).
	 * @param bounds the multimap of the lower bounds
	 * @param target the target type to update the bounds
	 * @param addBoundsOf the new type to add the bounds
	 * @param func the function to calculate the combination of two concrete types and return a new concrete type or null if the combination failed
	 * @param error the error output 
	 * @param wire the related wire
	 * @return true if the combination was successful
	 */
	static boolean combineBounds(Multimap<AdvanceType, AdvanceType> bounds, 
			AdvanceType target, AdvanceType addBoundsOf,
			Func2<AdvanceType, AdvanceType, AdvanceType> func,
			List<AdvanceCompilationError> error,
			AdvanceBlockBind wire
			) {
		Deque<AdvanceType> concreteTypes = Lists.newLinkedList();
		List<AdvanceType> newBounds = Lists.newArrayList();
		for (AdvanceType lbTarget : bounds.get(target)) {
			if (lbTarget.getKind() == AdvanceTypeKind.CONCRETE_TYPE) {
				concreteTypes.add(lbTarget);
			} else {
				newBounds.add(lbTarget);
			}
		}
		for (AdvanceType lbAdd : bounds.get(addBoundsOf)) {
			if (lbAdd.getKind() == AdvanceTypeKind.CONCRETE_TYPE) {
				concreteTypes.add(lbAdd);
			} else {
				newBounds.add(lbAdd);
			}
		}
		AdvanceType concrete = null;
		if (!concreteTypes.isEmpty()) {
			concrete = concreteTypes.pop();
			while (!concreteTypes.isEmpty()) {
				AdvanceType t0 = concrete;
				AdvanceType t1 = concreteTypes.pop();
				concrete = func.invoke(t0, t1);
				if (concrete == null) {
					error.add(new CombinedTypeError(wire, t0, t1));
					return false;
				}
			}
		}
		if (concrete != null) {
			newBounds.add(concrete);
		}
		bounds.replaceValues(target, newBounds);
		return true;
	}
	/**
	 * Adds the {@code newBound} to the exiting bounds of {@code target} and joins any concrete types with the given function if
	 * newBound is concrete.
	 * @param bounds the multimap of the lower bounds
	 * @param target the target type to update the bounds
	 * @param newBound the new type to add the bounds
	 * @param func the function to calculate the combination of two concrete types and return a new concrete type or null if the combination failed 
	 * @return true if the combination was successful
	 */
	static boolean addBound(Multimap<AdvanceType, AdvanceType> bounds, 
			AdvanceType target, AdvanceType newBound,
			Func2<AdvanceType, AdvanceType, AdvanceType> func
			) {
		Deque<AdvanceType> concreteTypes = Lists.newLinkedList();
		List<AdvanceType> newBounds = Lists.newArrayList();
		for (AdvanceType lbTarget : bounds.get(target)) {
			if (lbTarget.getKind() == AdvanceTypeKind.CONCRETE_TYPE) {
				concreteTypes.add(lbTarget);
			} else {
				newBounds.add(lbTarget);
			}
		}
		AdvanceType concrete = null;
		if (newBound.getKind() == AdvanceTypeKind.CONCRETE_TYPE) {
			concrete = newBound;
			while (!concreteTypes.isEmpty()) {
				concrete = func.invoke(concrete, concreteTypes.pop());
				if (concrete == null) {
					return false;
				}
			}
		}
		if (concrete != null) {
			newBounds.add(concrete);
		}
		bounds.replaceValues(target, newBounds);
		return true;
	}
	/**
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return Create a hashmap of key to hashset of values.
	 */
	static <K, V> Multimap<K, V> setMultimap() {
		return Multimaps.newSetMultimap(new HashMap<K, Collection<V>>(), new Supplier<Set<V>>() {
			@Override
			public Set<V> get() {
				return Sets.newHashSet();
			}
		});
	}

}
