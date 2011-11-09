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

import hu.akarnokd.reactive4java.base.Func2;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import eu.advance.logistics.flow.engine.error.CombinedTypeError;
import eu.advance.logistics.flow.engine.error.ConcreteVsParametricTypeError;
import eu.advance.logistics.flow.engine.error.IncompatibleBaseTypesError;
import eu.advance.logistics.flow.engine.error.IncompatibleTypesError;
import eu.advance.logistics.flow.engine.error.TypeArgumentCountError;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockBind;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.model.fd.AdvanceTypeKind;
import eu.advance.logistics.flow.engine.model.rt.AdvanceCompilationResult;
import eu.advance.logistics.flow.engine.xml.typesystem.XData;
import eu.advance.logistics.flow.engine.xml.typesystem.XRelation;
import eu.advance.logistics.flow.engine.xml.typesystem.XSchema;
import eu.advance.logistics.flow.engine.xml.typesystem.XType;

/**
 * Utility class for the type inference.
 * @author akarnokd, 2011.09.28.
 */
public final class AdvanceTypeInference {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceTypeInference.class);
	/** The upper bound of type variables, e.g., T >= Concrete, T2, T3. */
	final Multimap<AdvanceType, AdvanceType> upperBound = setMultimap();
	/** The lower bound of type variables, e.g., T3, T2, Concrete >= T. */
	final Multimap<AdvanceType, AdvanceType> lowerBound = setMultimap();
	/** The list of reflexive relations to propagate constraints to other type variables: T2 >= T1. */
	final List<TypeRelation> reflexives = Lists.newArrayList();
	/** Set of individual type variables. */
	final Set<AdvanceType> typeSet = Sets.newHashSet();
	/** The remaining type relations. */
	final Deque<TypeRelation> relations = Lists.newLinkedList();
	/** The initial wire relations. */
	final List<TypeRelation> wireRelations = Lists.newArrayList();
	/** The inference results and errors. */
	final AdvanceCompilationResult result = new AdvanceCompilationResult();
	/** The currently evaluated relation. */
	protected TypeRelation currentRelation;
	/**
	 * Constructor. Initializes the internal structures.
	 * @param relations the type relations to infer.
	 */
	public AdvanceTypeInference(Iterable<TypeRelation> relations) {
		Iterables.addAll(this.relations, relations);
		for (TypeRelation tr : relations) {
			wireRelations.add(new TypeRelation(tr));
			setTypeId(tr.left);
			setTypeId(tr.right);
		}
	}
	/**
	 * Assign the type ids to type variables recursively.
	 * @param t the start type
	 */
	private void setTypeId(AdvanceType t) {
		if (t.getKind() == AdvanceTypeKind.VARIABLE_TYPE) {
			if (typeSet.add(t)) {
				t.id = typeSet.size();
			}
		} else
		if (t.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE) {
			for (AdvanceType ta : t.typeArguments) {
				setTypeId(ta);
			}
		}
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
	 * @return the compilation result
	 */
	public AdvanceCompilationResult infer() {
		while (!relations.isEmpty()) {
			TypeRelation rel = relations.pop();
			currentRelation = rel;
			if (rel.left.getKind() == AdvanceTypeKind.VARIABLE_TYPE && rel.right.getKind() == AdvanceTypeKind.VARIABLE_TYPE) {
				if (!containsRelation(reflexives, rel.left, rel.right)) {
					// add new reflexive relations for x >= left and right >= y 
					int size = reflexives.size();
					for (int i = 0; i < size; i++) {
						TypeRelation ab = reflexives.get(i);
						// if ab.left >= left and left >= right then ab.left >= right
						if (ab.right == rel.left) {
							reflexives.add(new TypeRelation(ab.left, rel.right, rel.wire));
							if (!combineBounds(upperBound, ab.left, rel.right, unionFunc, rel.wire)) {
								return result;
							}
						}
						// if right >= ab.right and left >= right then left >= ab.right
						if (ab.left == rel.right) {
							reflexives.add(new TypeRelation(rel.left, ab.right, rel.wire));
							combineBounds(lowerBound, ab.right, rel.left, intersectFunc, rel.wire);
						}
					}
					if (!combineBounds(upperBound, rel.left, rel.right, unionFunc, rel.wire)) {
						return result;
					}
					combineBounds(lowerBound, rel.right, rel.left, intersectFunc, rel.wire);
					
					reflexives.add(new TypeRelation(rel));
					// call subc with lower(rel.left) >= upper(rel.right) ?! 
					for (AdvanceType lb : lowerBound.get(rel.left)) {
						for (AdvanceType ub : upperBound.get(rel.right)) {
							if (!subc(lb, ub, rel.wire)) {
								return result;
							}
						}
					}
				}
			} else
			if (rel.left.getKind() == AdvanceTypeKind.VARIABLE_TYPE && rel.right.getKind() != AdvanceTypeKind.VARIABLE_TYPE) {
				if (!upperBound.get(rel.left).contains(rel.right)) {
					if (rel.right.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE) {
						AdvanceType eb = findParametricBound(upperBound.get(rel.left));
						if (eb == null) {
							addBound(upperBound, rel.left, rel.right, unionFunc);
							for (AdvanceType lb : lowerBound.get(rel.left)) {
								if (!subc(lb, rel.right, rel.wire)) {
									return result;
								}
							}
						} else {
							AdvanceType bt = new AdvanceType();
							bt.type = eb.type;
							bt.typeURI = eb.typeURI;
							for (int i = 0; i < eb.typeArguments.size(); i++) {
								AdvanceType ta1 = eb.typeArguments.get(i);
								AdvanceType ta2 = rel.left.typeArguments.get(i);
								
								AdvanceType ta3 = AdvanceType.fresh();
								setTypeId(ta3);
								
								bt.typeArguments.add(ta3);
								
								if (!subc(ta3, ta1, rel.wire)) {
									return result;
								}
								if (!subc(ta3, ta2, rel.wire)) {
									return result;
								}
							}
							upperBound.remove(rel.left, eb);
							upperBound.put(rel.left, bt);
						}
					} else {
						// for each left >= ab.right
						for (TypeRelation ab : reflexives) {
							if (ab.right == rel.left) {
								// append the right to the upper bounds
								if (!addBound(upperBound, ab.left, rel.right, unionFunc)) {
									result.addError(new CombinedTypeError(rel.wire, ab.left, rel.right));
									return result;
								}
							}
						}
						addBound(upperBound, rel.left, rel.right, unionFunc);

						// for each lower bound of the type variable
						for (AdvanceType lb : lowerBound.get(rel.left)) {
							if (!subc(lb, rel.right, rel.wire)) {
								return result;
							}
						}

					}
				}
			} else
			if (rel.left.getKind() != AdvanceTypeKind.VARIABLE_TYPE && rel.right.getKind() == AdvanceTypeKind.VARIABLE_TYPE) {
				if (!lowerBound.get(rel.right).contains(rel.left)) {
					if (rel.left.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE) {
						AdvanceType eb = findParametricBound(lowerBound.get(rel.right));
						if (eb == null) {
							addBound(lowerBound, rel.right, rel.left, intersectFunc);
							// call subc with rel.left >= upper(rel.right)
							for (AdvanceType lb : upperBound.get(rel.right)) {
								if (!subc(rel.left, lb, rel.wire)) {
									return result;
								}
							}
						} else {
							AdvanceType bt = new AdvanceType();
							bt.type = eb.type;
							bt.typeURI = eb.typeURI;
							for (int i = 0; i < eb.typeArguments.size(); i++) {
								AdvanceType ta1 = eb.typeArguments.get(i);
								AdvanceType ta2 = rel.left.typeArguments.get(i);
								
								AdvanceType ta3 = AdvanceType.fresh();
								setTypeId(ta3);
								
								bt.typeArguments.add(ta3);
								
								if (!subc(ta1, ta3, rel.wire)) {
									return result;
								}
								if (!subc(ta2, ta3, rel.wire)) {
									return result;
								}
							}
							lowerBound.remove(rel.right, eb);
							lowerBound.put(rel.right, bt);
						}
					} else {
						// for each ab.left >= right
						for (TypeRelation ab : reflexives) {
							if (ab.left == rel.right) {
								addBound(lowerBound, ab.right, rel.left, intersectFunc);
							}
						}
						addBound(lowerBound, rel.right, rel.left, intersectFunc);
						// call subc with rel.left >= upper(rel.right)
						for (AdvanceType lb : upperBound.get(rel.right)) {
							if (!subc(rel.left, lb, rel.wire)) {
								return result;
							}
						}
					}
				}
			} else {
				// check if both constants or both parametric types
				if (rel.left.getKind() != rel.right.getKind()) {
					result.addError(new ConcreteVsParametricTypeError(rel.wire, rel.left, rel.right));
					return result;
				} else
				if (rel.left.getKind() == AdvanceTypeKind.CONCRETE_TYPE) {
					XRelation xr = XSchema.compare(rel.left.type, rel.right.type);
					if (xr != XRelation.EQUAL && xr != XRelation.EXTENDS) {
						result.addError(new IncompatibleTypesError(rel.wire, rel.left, rel.right));
						return result;
					} else {
						result.setType(rel.wire.id, rel.left);
					}
				} else {
					if (!subc(rel.left, rel.right, rel.wire)) {
						return result;
					}
				}
			}
		}

		for (TypeRelation rel : wireRelations) {
			String wireId = rel.wire.id;
			AdvanceType et = result.getType(wireId);
			AdvanceType t = findConcreteType(rel.left);
			AdvanceType t2 = findConcreteType(rel.right);
			if (et == null) {
				result.setType(wireId, mostSpecific(t, t2));
			} else {
				result.setType(wireId, mostSpecific(et, mostSpecific(t, t2)));
			}
		}
		return result;
	}
	/**
	 * Returns the most specific type from the sumbitted types.
	 * @param t1 the first type
	 * @param t2 the second type
	 * @return the most specific type
	 */
	protected AdvanceType mostSpecific(AdvanceType t1, AdvanceType t2) {
		if (t1.getKind() == AdvanceTypeKind.VARIABLE_TYPE) {
			return t2;
		} else
		if (t2.getKind() == AdvanceTypeKind.VARIABLE_TYPE) {
			return t1;
		}
		
		AdvanceType bt = null;
		XRelation xr = XSchema.compare(t1.type, t2.type);
		if (xr == XRelation.SUPER) {
			bt = t2;
		} else {
			bt = t1;
		}
		if (t1.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE && t2.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE) {
			AdvanceType result = new AdvanceType();
			result.type = bt.type;
			result.typeURI = bt.typeURI;
				
			for (int i = 0; i < t1.typeArguments.size(); i++) {
				AdvanceType ta1 = t1.typeArguments.get(i);
				AdvanceType ta2 = t2.typeArguments.get(i);
				result.typeArguments.add(mostSpecific(ta1, ta2));
			}
			return result;
		} else
		if (t1.getKind() == AdvanceTypeKind.CONCRETE_TYPE && t2.getKind() == AdvanceTypeKind.CONCRETE_TYPE) {
			return bt;
		} else
		if (t1.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE) {
			return t1;
		}
		return t2;
	}
	/**
	 * Find the parametric type in the supplied bound collection.
	 * @param bounds the collection of bounds
	 * @return the parametric bound or null if not present
	 */
	public static AdvanceType findParametricBound(Collection<AdvanceType> bounds) {
		for (AdvanceType bt : bounds) {
			if (bt.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE) {
				return bt;
			}
		}
		return null;
	}
	/**
	 * Find the appropriate type for the supplied type variable.
	 * @param type the type
	 * @return the more concrete type or the type variable itself
	 */
	public AdvanceType findConcreteType(AdvanceType type) {
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
		if (computedType.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE) {
			AdvanceType ct2 = new AdvanceType();
			ct2.type = computedType.type;
			ct2.typeURI = computedType.typeURI;
			ct2.typeArguments.addAll(computedType.typeArguments);
			for (int i = 0; i < computedType.typeArguments.size(); i++) {
				AdvanceType ta = computedType.typeArguments.get(i);
				ct2.typeArguments.set(i, findConcreteType(ta));
			}
			computedType = ct2;
		}
		return computedType;
	}
	/**
	 * Based on the structure of left >= right, creates new type relations and places it back to relations.
	 * @param left the left expression
	 * @param right the right expression
	 * @param wire the wire for the relation
	 * @return true if no conflict was detected
	 */
	boolean subc(AdvanceType left, AdvanceType right, 
			AdvanceBlockBind wire) {
		// if left >= right is elementary, e.g., neither of them is a parametric type, just return a relation with them
		if (left.getKind() != AdvanceTypeKind.PARAMETRIC_TYPE && right.getKind() != AdvanceTypeKind.PARAMETRIC_TYPE) {
			if (left.getKind() == AdvanceTypeKind.CONCRETE_TYPE && right.getKind() == AdvanceTypeKind.CONCRETE_TYPE) {
				// the two concrete types are not related
				XRelation xr = XSchema.compare(left.type, right.type);
				if (xr != XRelation.EQUAL && xr != XRelation.EXTENDS) {
					result.addError(new IncompatibleTypesError(wire, left, right));
					return false;
				}
			}
			relations.add(new TypeRelation(left, right, wire));
		} else
		// if C(t1,...,tn) >= right
		if (left.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE && right.getKind() != AdvanceTypeKind.PARAMETRIC_TYPE) {
			if (right.getKind() == AdvanceTypeKind.CONCRETE_TYPE) {
				result.addError(new ConcreteVsParametricTypeError(wire, left, right));
				return false;
			}
			for (AdvanceType t : left.typeArguments) {
				AdvanceType ta2 = AdvanceType.fresh(); 
				typeSet.add(ta2);
				ta2.id = typeSet.size();

				if (!subc(t, ta2, wire)) {
					return false;
				}
			}
		} else
		// if left >= C(t1,...,tn)
		if (left.getKind() != AdvanceTypeKind.PARAMETRIC_TYPE && right.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE) {
			if (left.getKind() == AdvanceTypeKind.CONCRETE_TYPE) {
				result.addError(new ConcreteVsParametricTypeError(wire, left, right));
				return false;
			}
			for (AdvanceType t : right.typeArguments) {
				if (!subc(AdvanceType.fresh(), t, wire)) {
					return false;
				}
			}
		} else {
			// if D(t1,...,tn) >= C(u1,...,un)
			if (left.typeArguments.size() != right.typeArguments.size()) {
				result.addError(new TypeArgumentCountError(wire, left, right));
				return false;
			}
			// the two concrete types are not related
			XRelation xr = XSchema.compare(left.type, right.type);
			if (xr != XRelation.EQUAL && xr != XRelation.EXTENDS) {
				result.addError(new IncompatibleBaseTypesError(wire, left, right));
				return false;
			}
			Iterator<AdvanceType> ts = left.typeArguments.iterator();
			Iterator<AdvanceType> us = right.typeArguments.iterator();
			while (ts.hasNext() && us.hasNext()) {
				if (!subc(ts.next(), us.next(), wire)) {
					return false;
				}
			}
		}
		
		return true;
	}
	/** The intersection function. */
	Func2<AdvanceType, AdvanceType, AdvanceType> intersectFunc = new Func2<AdvanceType, AdvanceType, AdvanceType>() {
		@Override
		public AdvanceType invoke(AdvanceType param1, AdvanceType param2) {
			return intersection(param1, param2);
		}
	};
	/** The intersection function. */
	Func2<AdvanceType, AdvanceType, AdvanceType> unionFunc = new Func2<AdvanceType, AdvanceType, AdvanceType>() {
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
	AdvanceType intersection(AdvanceType t1, AdvanceType t2) {
		AdvanceType t = new AdvanceType();
		// if parametric vs concrete, the common supertype is advance:object
		if (t1.getKind() != t2.getKind()) {
			t.type = new XType();
			t.typeURI = XData.OBJECT;
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
	/**
	 * Computes the new union type of two advance types if they are concrete or returns null if the types can't be unioned together.
	 * @param t1 the first type
	 * @param t2 the second type
	 * @return the union type or null
	 */
	AdvanceType union(AdvanceType t1, AdvanceType t2) {
		AdvanceType t = new AdvanceType();
		if (t1.getKind() != t2.getKind()) {
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
	/**
	 * Combines the bounds of {@code target} and {@code addBoundsOf} by intersecting the concrete types of both and
	 * joining with the rest of the bound types (variables or parametric types).
	 * @param bounds the multimap of the lower bounds
	 * @param target the target type to update the bounds
	 * @param addBoundsOf the new type to add the bounds
	 * @param func the function to calculate the combination of two concrete types and return a new concrete type or null if the combination failed
	 * @param wire the related wire
	 * @return true if the combination was successful
	 */
	boolean combineBounds(Multimap<AdvanceType, AdvanceType> bounds, 
			AdvanceType target, AdvanceType addBoundsOf,
			Func2<AdvanceType, AdvanceType, AdvanceType> func,
			AdvanceBlockBind wire
			) {
		Deque<AdvanceType> concreteTypes = Lists.newLinkedList();
		List<AdvanceType> newBounds = Lists.newArrayList();
		for (AdvanceType lbTarget : bounds.get(target)) {
			if (lbTarget.getKind() != AdvanceTypeKind.VARIABLE_TYPE) {
				concreteTypes.add(lbTarget);
			} else {
				newBounds.add(lbTarget);
			}
		}
		for (AdvanceType lbAdd : bounds.get(addBoundsOf)) {
			if (lbAdd.getKind() != AdvanceTypeKind.VARIABLE_TYPE) {
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
					result.addError(new CombinedTypeError(wire, t0, t1));
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
	boolean addBound(Multimap<AdvanceType, AdvanceType> bounds, 
			AdvanceType target, AdvanceType newBound,
			Func2<AdvanceType, AdvanceType, AdvanceType> func
			) {
		Deque<AdvanceType> concreteTypes = Lists.newLinkedList();
		List<AdvanceType> newBounds = Lists.newArrayList();
		for (AdvanceType lbTarget : bounds.get(target)) {
			if (lbTarget.getKind() != AdvanceTypeKind.VARIABLE_TYPE) {
				concreteTypes.add(lbTarget);
			} else {
				newBounds.add(lbTarget);
			}
		}
		AdvanceType concrete = null;
		if (newBound.getKind() != AdvanceTypeKind.VARIABLE_TYPE) {
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
	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		pw.println("Bounds:");
		
		for (AdvanceType t : Sets.union(upperBound.keySet(), lowerBound.keySet())) {
			pw.printf("%s >= %s >= %s%n", upperBound.get(t), t, lowerBound.get(t));
		}
		
		pw.println("Reflexives:");
		for (TypeRelation tr : reflexives) {
			pw.println(tr);
		}
		pw.println("Relations:");
		for (TypeRelation tr : relations) {
			pw.println(tr);
		}
		pw.println("Current:");
		pw.println(currentRelation);
		return sw.toString();
	}
}
