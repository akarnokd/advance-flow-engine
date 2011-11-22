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

import hu.akarnokd.reactive4java.base.Func2;

import java.io.PrintWriter;
import java.io.StringWriter;
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

/**
 * Utility class for the type inference.
 * @author akarnokd, 2011.09.28.
 * @param <T> the type system type
 * @param <W> the back reference to the source of the type relations
 */
public final class TypeInference<T extends Type, W> {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(TypeInference.class);
	/** The upper bound of type variables, e.g., T >= Concrete, T2, T3. */
	final Multimap<T, T> upperBound = setMultimap();
	/** The lower bound of type variables, e.g., T3, T2, Concrete >= T. */
	final Multimap<T, T> lowerBound = setMultimap();
	/** The list of reflexive relations to propagate constraints to other type variables: T2 >= T1. */
	final List<Relation<T, W>> reflexives = Lists.newArrayList();
	/** Set of individual type variables. */
	final Set<T> typeSet = Sets.newHashSet();
	/** The remaining type relations. */
	final Deque<Relation<T, W>> relations = Lists.newLinkedList();
	/** The initial wire relations. */
	final List<Relation<T, W>> wireRelations = Lists.newArrayList();
	/** The inference results and errors. */
	protected InferenceResult<T, W> result;
	/** The currently evaluated relation. */
	protected Relation<T, W> currentRelation;
	/** The type functions. */
	final TypeFunctions<T> functions;
	/**
	 * Constructor. Initializes the internal structures.
	 * @param relations the type relations to infer.
	 * @param functions the type functions
	 */
	public TypeInference(final Iterable<Relation<T, W>> relations,
			final TypeFunctions<T> functions) {
		this.functions = functions;
		for (Relation<T, W> tr : relations) {
			this.relations.add(tr);
			wireRelations.add(new Relation<T, W>(tr));
			setTypeId(tr.left);
			setTypeId(tr.right);
		}
	}
	/**
	 * Assign the type ids to type variables recursively.
	 * @param t the start type
	 */
	private void setTypeId(T t) {
		functions.setId(t, typeSet);
	}
	/**
	 * Test if the given relations list contains a relation where the left and right are the given values.
	 * @param relations the iterable of the relations
	 * @param left the left type
	 * @param right the right type
	 * @return true if present
	 */
	boolean containsRelation(Iterable<? extends Relation<T, W>> relations, T left, T right) {
		for (Relation<T, W> t : relations) {
			if (t.left == left && t.right == right) {
				return true;
			}
		}
		return false;
	}
	/**
	 * <p>Type inference using the algorithm described by</p>
	 * <p>Francois Pottier: Type inference in presence of subtyping: from theory to practice</p>.
	 * @param <U> the inference result type
	 * @param result the output of the inference results
	 * @return the same as the input {@code result}
	 */
	public <U extends InferenceResult<T, W>> U infer(U result) {
		this.result = result;
		while (!relations.isEmpty()) {
			Relation<T, W> rel = relations.pop();
			currentRelation = rel;
			if (rel.left.kind() == TypeKind.VARIABLE_TYPE && rel.right.kind() == TypeKind.VARIABLE_TYPE) {
				if (!containsRelation(reflexives, rel.left, rel.right)) {
					// add new reflexive relations for x >= left and right >= y 
					int size = reflexives.size();
					for (int i = 0; i < size; i++) {
						Relation<T, W> ab = reflexives.get(i);
						// if ab.left >= left and left >= right then ab.left >= right
						if (ab.right == rel.left) {
							reflexives.add(new Relation<T, W>(ab.left, rel.right, rel.wire));
							if (!combineBounds(upperBound, ab.left, rel.right, unionFunc, rel.wire)) {
								return result;
							}
						}
						// if right >= ab.right and left >= right then left >= ab.right
						if (ab.left == rel.right) {
							reflexives.add(new Relation<T, W>(rel.left, ab.right, rel.wire));
							combineBounds(lowerBound, ab.right, rel.left, intersectFunc, rel.wire);
						}
					}
					if (!combineBounds(upperBound, rel.left, rel.right, unionFunc, rel.wire)) {
						return result;
					}
					combineBounds(lowerBound, rel.right, rel.left, intersectFunc, rel.wire);
					
					reflexives.add(new Relation<T, W>(rel));
					// call subc with lower(rel.left) >= upper(rel.right) ?! 
					for (T lb : lowerBound.get(rel.left)) {
						for (T ub : upperBound.get(rel.right)) {
							if (!subc(lb, ub, rel.wire)) {
								return result;
							}
						}
					}
				}
			} else
			if (rel.left.kind() == TypeKind.VARIABLE_TYPE && rel.right.kind() != TypeKind.VARIABLE_TYPE) {
				if (!upperBound.get(rel.left).contains(rel.right)) {
					if (rel.right.kind() == TypeKind.PARAMETRIC_TYPE) {
						T eb = findParametricBound(upperBound.get(rel.left));
						if (eb == null) {
							addBound(upperBound, rel.left, rel.right, unionFunc);
							for (T lb : lowerBound.get(rel.left)) {
								if (!subc(lb, rel.right, rel.wire)) {
									return result;
								}
							}
						} else {
							T bt = functions.copy(eb);
							List<T> btArgs = functions.arguments(bt);
							btArgs.clear();
							List<T> ebArgs = functions.arguments(eb);
							List<T> rightArgs = functions.arguments(rel.right);
							for (int i = 0; i < ebArgs.size(); i++) {
								T ta1 = ebArgs.get(i);
								T ta2 = rightArgs.get(i);
								
								T ta3 = functions.fresh("T");
								setTypeId(ta3);
								
								btArgs.add(ta3);
								
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
						for (Relation<T, W> ab : reflexives) {
							if (ab.right == rel.left) {
								// append the right to the upper bounds
								if (!addBound(upperBound, ab.left, rel.right, unionFunc)) {
									result.errorCombinedType(ab.left, rel.right, rel.wire);
									return result;
								}
							}
						}
						addBound(upperBound, rel.left, rel.right, unionFunc);

						// for each lower bound of the type variable
						for (T lb : lowerBound.get(rel.left)) {
							if (!subc(lb, rel.right, rel.wire)) {
								return result;
							}
						}

					}
				}
			} else
			if (rel.left.kind() != TypeKind.VARIABLE_TYPE && rel.right.kind() == TypeKind.VARIABLE_TYPE) {
				if (!lowerBound.get(rel.right).contains(rel.left)) {
					if (rel.left.kind() == TypeKind.PARAMETRIC_TYPE) {
						T eb = findParametricBound(lowerBound.get(rel.right));
						if (eb == null) {
							addBound(lowerBound, rel.right, rel.left, intersectFunc);
							// call subc with rel.left >= upper(rel.right)
							for (T lb : upperBound.get(rel.right)) {
								if (!subc(rel.left, lb, rel.wire)) {
									return result;
								}
							}
						} else {
							T bt = functions.copy(eb);
							List<T> btArgs = functions.arguments(bt);
							btArgs.clear();
							List<T> ebArgs = functions.arguments(eb);
							List<T> leftArgs = functions.arguments(rel.left);
							for (int i = 0; i < ebArgs.size(); i++) {
								T ta1 = ebArgs.get(i);
								T ta2 = leftArgs.get(i);
								
								T ta3 = functions.fresh("T");
								setTypeId(ta3);
								
								btArgs.add(ta3);
								
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
						for (Relation<T, W> ab : reflexives) {
							if (ab.left == rel.right) {
								addBound(lowerBound, ab.right, rel.left, intersectFunc);
							}
						}
						addBound(lowerBound, rel.right, rel.left, intersectFunc);
						// call subc with rel.left >= upper(rel.right)
						for (T lb : upperBound.get(rel.right)) {
							if (!subc(rel.left, lb, rel.wire)) {
								return result;
							}
						}
					}
				}
			} else {
				// check if both constants or both parametric types
				if (rel.left.kind() != rel.right.kind()) {
					result.errorConcreteVsParametricType(rel.left, rel.right, rel.wire);
					return result;
				} else
				if (rel.left.kind() == TypeKind.CONCRETE_TYPE) {
					TypeRelation r = functions.compare(rel.left, rel.right);
					if (r == TypeRelation.SUPER || r == TypeRelation.NONE) {
						result.errorIncompatibleTypes(rel.left, rel.right, rel.wire);
						return result;
					}
				} else {
					if (!subc(rel.left, rel.right, rel.wire)) {
						return result;
					}
				}
			}
		}

		for (Relation<T, W> rel : wireRelations) {
			T et = result.getType(rel.wire);
			T t = findConcreteType(rel.left);
			T t2 = findConcreteType(rel.right);
			if (et == null) {
				result.setType(rel.wire, mostSpecific(t, t2));
			} else {
				result.setType(rel.wire, mostSpecific(et, mostSpecific(t, t2)));
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
	protected T mostSpecific(T t1, T t2) {
		if (t1.kind() == TypeKind.VARIABLE_TYPE) {
			return t2;
		} else
		if (t2.kind() == TypeKind.VARIABLE_TYPE) {
			return t1;
		}
		
		T bt = null;
		TypeRelation xr = functions.compare(t1, t2);
		if (xr == TypeRelation.SUPER) {
			bt = t2;
		} else {
			bt = t1;
		}
		if (t1.kind() == TypeKind.PARAMETRIC_TYPE && t2.kind() == TypeKind.PARAMETRIC_TYPE) {
			T result = functions.copy(bt);
			
			List<T> resultArgs = functions.arguments(result);
			resultArgs.clear();
			List<T> t1Arg = functions.arguments(t1);
			List<T> t2Arg = functions.arguments(t2);
			for (int i = 0; i < t1Arg.size(); i++) {
				T ta1 = t1Arg.get(i);
				T ta2 = t2Arg.get(i);
				resultArgs.add(mostSpecific(ta1, ta2));
			}
			return result;
		} else
		if (t1.kind() == TypeKind.CONCRETE_TYPE && t2.kind() == TypeKind.CONCRETE_TYPE) {
			return bt;
		} else
		if (t1.kind() == TypeKind.PARAMETRIC_TYPE) {
			return t1;
		}
		return t2;
	}
	/**
	 * Find the parametric type in the supplied bound collection.
	 * @param bounds the collection of bounds
	 * @return the parametric bound or null if not present
	 */
	public T findParametricBound(Collection<T> bounds) {
		for (T bt : bounds) {
			if (bt.kind() == TypeKind.PARAMETRIC_TYPE) {
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
	public T findConcreteType(T type) {
		T computedType = null;
		// find minimum of upper bound
		for (T ub : upperBound.get(type)) {
			if (ub.kind() != TypeKind.VARIABLE_TYPE) {
				computedType = ub;
				break;
			}
		}
		if (computedType == null) {
			// find maximum of lower bound
			for (T lb : lowerBound.get(type)) {
				if (lb.kind() != TypeKind.VARIABLE_TYPE) {
					computedType = lb;
					break;
				}
			}
		}
		// if still no concrete type, have it abstract
		if (computedType == null) {
			computedType = type;
		}
		if (computedType.kind() == TypeKind.PARAMETRIC_TYPE) {
			T ct2 = functions.copy(computedType);
			
			List<T> arguments = functions.arguments(computedType);
			List<T> arguments2 = functions.arguments(ct2);
			for (int i = 0; i < arguments.size(); i++) {
				T ta = arguments.get(i);
				
				if (ta == type && type.kind() == TypeKind.VARIABLE_TYPE) {
					T rt = functions.fresh("...");
					setTypeId(rt);
					arguments2.set(i, rt);
				} else {
					arguments2.set(i, findConcreteType(ta));
				}
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
	boolean subc(T left, T right, 
			W wire) {
		if (left.kind() == TypeKind.PARAMETRIC_TYPE && right.kind() == TypeKind.PARAMETRIC_TYPE) {
			// if D(t1,...,tn) >= C(u1,...,un)
			List<T> leftArgs = functions.arguments(left);
			List<T> rigthArgs = functions.arguments(right);
			if (leftArgs.size() != rigthArgs.size()) {
				result.errorArgumentCount(left, right, wire);
				return false;
			}
			// the two concrete types are not related
			TypeRelation xr = functions.compare(left, right);
			if (xr != TypeRelation.EQUAL && xr != TypeRelation.EXTENDS) {
				result.errorIncompatibleBaseTypes(left, right, wire);
				return false;
			}
			Iterator<T> ts = leftArgs.iterator();
			Iterator<T> us = rigthArgs.iterator();
			while (ts.hasNext() && us.hasNext()) {
				if (!subc(ts.next(), us.next(), wire)) {
					return false;
				}
			}
		} else {
			relations.add(new Relation<T, W>(left, right, wire));
		}
		
		return true;
	}
	/** The intersection function. */
	Func2<T, T, T> intersectFunc = new Func2<T, T, T>() {
		@Override
		public T invoke(T param1, T param2) {
			return functions.intersection(param1, param2);
		}
	};
	/** The intersection function. */
	Func2<T, T, T> unionFunc = new Func2<T, T, T>() {
		@Override
		public T invoke(T param1, T param2) {
			return functions.union(param1, param2);
		}
	};
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
	boolean combineBounds(Multimap<T, T> bounds, 
			T target, T addBoundsOf,
			Func2<T, T, T> func,
			W wire
			) {
		Deque<T> concreteTypes = Lists.newLinkedList();
		List<T> newBounds = Lists.newArrayList();
		for (T lbTarget : bounds.get(target)) {
			if (lbTarget.kind() != TypeKind.VARIABLE_TYPE) {
				concreteTypes.add(lbTarget);
			} else {
				newBounds.add(lbTarget);
			}
		}
		for (T lbAdd : bounds.get(addBoundsOf)) {
			if (lbAdd.kind() != TypeKind.VARIABLE_TYPE) {
				concreteTypes.add(lbAdd);
			} else {
				newBounds.add(lbAdd);
			}
		}
		T concrete = null;
		if (!concreteTypes.isEmpty()) {
			concrete = concreteTypes.pop();
			while (!concreteTypes.isEmpty()) {
				T t0 = concrete;
				T t1 = concreteTypes.pop();
				concrete = func.invoke(t0, t1);
				if (concrete == null) {
					result.errorCombinedType(t0, t1, wire);
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
	boolean addBound(Multimap<T, T> bounds, 
			T target, T newBound,
			Func2<T, T, T> func
			) {
		Deque<T> concreteTypes = Lists.newLinkedList();
		List<T> newBounds = Lists.newArrayList();
		for (T lbTarget : bounds.get(target)) {
			if (lbTarget.kind() != TypeKind.VARIABLE_TYPE) {
				concreteTypes.add(lbTarget);
			} else {
				newBounds.add(lbTarget);
			}
		}
		T concrete = null;
		if (newBound.kind() != TypeKind.VARIABLE_TYPE) {
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
		
		for (T t : Sets.union(upperBound.keySet(), lowerBound.keySet())) {
			pw.printf("%s >= %s >= %s%n", upperBound.get(t), t, lowerBound.get(t));
		}
		
		pw.println("Reflexives:");
		for (Relation<?, ?> tr : reflexives) {
			pw.println(tr);
		}
		pw.println("Relations:");
		for (Relation<?, ?> tr : relations) {
			pw.println(tr);
		}
		pw.println("Current:");
		pw.println(currentRelation);
		return sw.toString();
	}
}
