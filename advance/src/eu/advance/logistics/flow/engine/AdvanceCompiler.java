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

import hu.akarnokd.reactive4java.reactive.Observer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.advance.logistics.flow.engine.error.AdvanceCompilationError;
import eu.advance.logistics.flow.engine.error.ConstantOutputError;
import eu.advance.logistics.flow.engine.error.DestinationToCompositeInputError;
import eu.advance.logistics.flow.engine.error.DestinationToCompositeOutputError;
import eu.advance.logistics.flow.engine.error.DestinationToOutputError;
import eu.advance.logistics.flow.engine.error.MissingDestinationError;
import eu.advance.logistics.flow.engine.error.MissingDestinationPortError;
import eu.advance.logistics.flow.engine.error.MissingSourceError;
import eu.advance.logistics.flow.engine.error.MissingSourcePortError;
import eu.advance.logistics.flow.engine.error.MultiInputBindingError;
import eu.advance.logistics.flow.engine.error.SourceToCompositeInputError;
import eu.advance.logistics.flow.engine.error.SourceToCompositeOutputError;
import eu.advance.logistics.flow.engine.error.SourceToInputBindingError;
import eu.advance.logistics.flow.engine.error.TypeMismatchError;
import eu.advance.logistics.flow.model.AdvanceBlockBind;
import eu.advance.logistics.flow.model.AdvanceBlockDescription;
import eu.advance.logistics.flow.model.AdvanceBlockParameterDescription;
import eu.advance.logistics.flow.model.AdvanceBlockReference;
import eu.advance.logistics.flow.model.AdvanceBlockRegistryEntry;
import eu.advance.logistics.flow.model.AdvanceCompositeBlock;
import eu.advance.logistics.flow.model.AdvanceConstantBlock;
import eu.advance.logistics.flow.model.AdvanceResolver;
import eu.advance.logistics.flow.model.AdvanceType;
import eu.advance.logistics.flow.model.AdvanceTypeKind;
import eu.advance.logistics.flow.model.AdvanceTypeVariable;
import eu.advance.logistics.util.Triplet;
import eu.advance.logistics.xml.typesystem.SchemaParser;
import eu.advance.logistics.xml.typesystem.XElement;
import eu.advance.logistics.xml.typesystem.XRelation;

/**
 * The ADVANCE block compiler which turns the the flow description into runnable advance blocks.
 * @author karnokd, 2011.06.27.
 */
public final class AdvanceCompiler {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceFlowEngine.class);
	/** Utility class. */
	private AdvanceCompiler() {
		
	}
	/**
	 * Compile the composite block.
	 * @param root the flow description
	 * @param flow the entire compiled flow model
	 */
	public static void compile(
			AdvanceCompositeBlock root, 
			List<AdvanceBlock> flow
			) {
		for (AdvanceCompositeBlock cb : root.composites.values()) {
			compile(cb, flow);
		}
		// current level constants
		for (AdvanceBlockReference br : root.blocks.values()) {
			Map<String, AdvanceConstantBlock> consts = Maps.newHashMap();
			AdvanceBlockDescription bd = AdvanceBlockLookup.lookup(br.type);
			for (AdvanceBlockParameterDescription bdp : bd.inputs.values()) {
				ConstantOrBlock cb = walkBinding(root, br.id, bdp.id);
				if (cb != null && cb.constant != null) {
					consts.put(bdp.id, cb.constant);
				}
			}
			AdvanceBlock ab = AdvanceBlockLookup.create(flow.size(), root, br.id);
			ab.init(bd, consts);
			flow.add(ab);
		}
		// bind
		if (root.parent == null) {
			for (AdvanceBlock ab : flow) {
				for (AdvancePort p : ab.inputs) {
					if (p instanceof AdvanceBlockPort) {
						ConstantOrBlock cb = walkBinding(ab.parent, ab.getDescription().id, p.name());
						for  (AdvanceBlock ab2 : flow) {
							if (ab2.parent == cb.composite && ab2.name.equals(cb.block)) {
								((AdvanceBlockPort) p).connect(ab2.getOutput(cb.param));
								break;
							}
						}
					}
				}
			}
		}
		
	}
	/**
	 * Run the flow graph.
	 * @param flow the list of blocks
	 * @param schedulers the available set of schedulers
	 */
	public static void run(List<AdvanceBlock> flow, Schedulers schedulers) {
		// arm
		List<Observer<Void>> notifycations = Lists.newLinkedList();
		for (AdvanceBlock ab : flow) {
			notifycations.add(ab.run(schedulers.get(ab.schedulerPreference)));
		}
		// notify
		for (Observer<Void> n : notifycations) {
			n.next(null);
		}
	}
	/**
	 * Terminate the blocks' connections and observations.
	 * @param flow the flow
	 */
	public static void done(List<AdvanceBlock> flow) {
		for (AdvanceBlock ab : flow) {
			ab.done();
		}
	}
	/** Composite for the walk binding. */
	static class ConstantOrBlock {
		/** The composite block where the constant or block has been found. */
		AdvanceCompositeBlock composite;
		/** Found a constant block. */
		AdvanceConstantBlock constant;
		/** Found a regular block. */
		String block;
		/** The regular block parameter id. */
		String param;
	}
	/**
	 * Walk the binding graph to locate a root constant block or return null if no such block was found.
	 * @param start the starting composite block
	 * @param block the starting block
	 * @param param the starting parameter
	 * @return the constant block or null
	 */
	static ConstantOrBlock walkBinding(AdvanceCompositeBlock start, String block, String param) {
		while (!Thread.currentThread().isInterrupted()) {
			for (AdvanceBlockBind bb : start.bindings) {
				if (bb.destinationBlock.equals(block) && bb.destinationParameter.equals(param)) {
					if (start.constants.containsKey(bb.sourceBlock)) {
						// constant found in the current level
						ConstantOrBlock result = new ConstantOrBlock();
						result.constant = start.constants.get(bb.sourceBlock);
						result.composite = start;
						return result;
					} else
					if (bb.sourceBlock.isEmpty() && start.inputs.containsKey(bb.sourceParameter)) {
						// if binding is to an input parameter, trace that
						AdvanceCompositeBlock q = start;
						start = q.parent;
						block = q.id;
						param = bb.sourceParameter;
						break;
					} else
					if (start.composites.containsKey(bb.sourceBlock)) {
						// if it binds to an output of a composite block, trace that
						AdvanceCompositeBlock q = start;
						start = q.composites.get(bb.sourceBlock);
						block = "";
						param = bb.sourceParameter;
					} else
					if (start.blocks.containsKey(bb.sourceBlock)) {
						// otherwise, its a regular block
						ConstantOrBlock result = new ConstantOrBlock();
						result.composite = start;
						result.block = bb.sourceBlock;
						result.param = bb.sourceParameter;
						return result;
					}
					return null;
				}
			}
		}
		return null;
	}
	/** The base class for parameters. */
	static class Parameter {
		/** The parameter's type. */
		AdvanceType type;
		/** The type variables available. */
		Map<String, AdvanceTypeVariable> typeVariables;
	}
	/** An input parameter declaration. */
	static class InputParameter extends Parameter {
		/** The connected output. */
		OutputParameter output;
	}
	/** An output parameter declaration. */
	static class OutputParameter extends Parameter {
		/** The connected inputs. */
		final List<InputParameter> inputs = Lists.newArrayList();
	}
	/** Definition of a type relation. */
	static class TypeRelation {
		/** The left type. */
		public AdvanceType left;
		/** The right type. */
		public AdvanceType right;
		/** The binding wire. */
		public AdvanceBlockBind wire;
		/** Construct an empty type relation. */
		public TypeRelation() {
			
		}
		/**
		 * Construct a type relation with the initial values.
		 * @param left the left type
		 * @param right the right type
		 * @param wire the original wire
		 */
		public TypeRelation(AdvanceType left, AdvanceType right, AdvanceBlockBind wire) {
			this.left = left;
			this.right = right;
			this.wire = wire;
		}
		/**
		 * Copy-construct a type relation with the initial values.
		 * @param other the other type relation
		 */
		public TypeRelation(TypeRelation other) {
			this.left = other.left;
			this.right = other.right;
			this.wire = other.wire;
		}
		@Override
		public String toString() {
//			return String.format("%s(%s):%s >= %s(%s):%s (%s)", wire.sourceBlock, wire.sourceParameter, left, wire.destinationBlock, wire.destinationParameter, right, wire.id);
			return String.format("%s[%08X] >= %s[%08X] (%s)", left, System.identityHashCode(left), right, System.identityHashCode(right), wire.id);
		}
	}
	/**
	 * Verify the types along the bindings of this composite block.
	 * @param enclosingBlock the most outer block
	 * @return the list compilation errors
	 */
	public static List<AdvanceCompilationError> verify(AdvanceCompositeBlock enclosingBlock) {
		List<AdvanceCompilationError> result = Lists.newArrayList();
		
		LinkedList<TypeRelation> relations = Lists.newLinkedList();
		
		LinkedList<AdvanceCompositeBlock> blockRecursion = Lists.newLinkedList();
		blockRecursion.add(enclosingBlock);
		
		Map<Triplet<AdvanceCompositeBlock, String, String>, AdvanceType> compositePortTypes = Maps.newHashMap();
		
		while (!blockRecursion.isEmpty()) {
			AdvanceCompositeBlock cb = blockRecursion.removeFirst();
			blockRecursion.addAll(cb.composites.values());
			
			// verify bindings
			List<AdvanceBlockBind> validBindings = Lists.newArrayList();
			verifyBindings(result, cb, validBindings);
			
			// for each individual block, have an individual type variable mapping per ports
			Map<String, Map<String, AdvanceType>> typeMemory = Maps.newHashMap();

			// build type relations
			for (AdvanceBlockBind bb : validBindings) {
				TypeRelation tr = new TypeRelation();
				tr.wire = bb;
				// evaluate source
				if (cb.constants.containsKey(bb.sourceBlock)) {
					Map<String, AdvanceType> at = typeMemory.get(bb.sourceBlock);
					if (at == null) {
						AdvanceType at2 = new AdvanceType();
						AdvanceConstantBlock constblock = cb.constants.get(bb.sourceBlock);
						at2.typeURI = constblock.typeURI;
						at2.type = constblock.type;
						typeMemory.put(bb.sourceBlock, Collections.singletonMap("", at2));
						tr.left = at2;
					} else {
						tr.left = at.get("");
					}
				} else
				if (cb.blocks.containsKey(bb.sourceBlock)) {
					Map<String, AdvanceType> at = typeMemory.get(bb.sourceBlock);
					if (at == null) {
						AdvanceBlockDescription lookup = AdvanceBlockLookup.lookup(cb.blocks.get(bb.sourceBlock).type).copy();
						at = Maps.newHashMap();
						for (AdvanceBlockParameterDescription bpd : lookup.inputs.values()) {
							at.put(bpd.id, bpd.type);
						}
						for (AdvanceBlockParameterDescription bpd : lookup.outputs.values()) {
							at.put(bpd.id, bpd.type);
						}
						typeMemory.put(bb.sourceBlock, at);
					}
					tr.left = at.get(bb.sourceParameter);
				} else
				if (cb.composites.containsKey(bb.sourceBlock)) {
					AdvanceCompositeBlock cb1 = cb.composites.get(bb.sourceBlock);
					Triplet<AdvanceCompositeBlock, String, String> typePort = 
							Triplet.of(cb1, "", bb.sourceParameter);
					AdvanceType at = compositePortTypes.get(typePort);
					if (at == null) {
						// construct a simple unbounded type variable
						at = new AdvanceType();
						at.typeVariableName = "T";
						at.typeVariable = new AdvanceTypeVariable();
						at.typeVariable.name = "T";
						compositePortTypes.put(typePort, at);
					}					
					tr.left = at;
				} else
				if (!bb.hasSourceBlock() && cb.inputs.containsKey(bb.sourceParameter)) {
					Triplet<AdvanceCompositeBlock, String, String> typePort = Triplet.of(cb, "", bb.sourceParameter);
					AdvanceType at = compositePortTypes.get(typePort);
					if (at == null) {
						at = new AdvanceType();
						at.typeVariableName = "T";
						at.typeVariable = new AdvanceTypeVariable();
						at.typeVariable.name = "T";
						compositePortTypes.put(typePort, at);
					}					
					tr.left = at;
				}
				// evaluate destination
				if (cb.blocks.containsKey(bb.destinationBlock)) {
					Map<String, AdvanceType> at = typeMemory.get(bb.destinationBlock);
					if (at == null) {
						AdvanceBlockDescription lookup = AdvanceBlockLookup.lookup(cb.blocks.get(bb.destinationBlock).type).copy();
						at = Maps.newHashMap();
						for (AdvanceBlockParameterDescription bpd : lookup.inputs.values()) {
							at.put(bpd.id, bpd.type);
						}
						for (AdvanceBlockParameterDescription bpd : lookup.outputs.values()) {
							at.put(bpd.id, bpd.type);
						}
						typeMemory.put(bb.destinationBlock, at);
					}
					tr.right = at.get(bb.destinationParameter);
				} else
				if (cb.composites.containsKey(bb.destinationBlock)) {
					AdvanceCompositeBlock cb1 = cb.composites.get(bb.destinationBlock);
					Triplet<AdvanceCompositeBlock, String, String> typePort = 
							Triplet.of(cb1, "", bb.destinationParameter);
					AdvanceType at = compositePortTypes.get(typePort);
					if (at == null) {
						// construct a simple unbounded type variable
						at = new AdvanceType();
						at.typeVariableName = "T";
						at.typeVariable = new AdvanceTypeVariable();
						at.typeVariable.name = "T";
						compositePortTypes.put(typePort, at);
					}					
					tr.right = at;
					
				} else
				if (!bb.hasDestinationBlock() && cb.outputs.containsKey(bb.destinationParameter)) {
					Triplet<AdvanceCompositeBlock, String, String> typePort = Triplet.of(cb, "", bb.destinationParameter);
					AdvanceType at = compositePortTypes.get(typePort);
					if (at == null) {
						at = new AdvanceType();
						at.typeVariableName = "T";
						at.typeVariable = new AdvanceTypeVariable();
						at.typeVariable.name = "T";
						compositePortTypes.put(typePort, at);
					}					
					tr.right = at;
				}
				
				relations.add(tr);
			}
		}
		
		// ---------------------------------------------------------------------------------
		// TODO perform the type resolution
		
		infer(relations, result);
		
		return result;
	}
	/**
	 * Verify the validity of the bindings in the current composite block and report errors.
	 * <ul>
	 * <li>The destination block + port must only occur once.</li>
	 * <li>Do not wire input-input or output-output.</li>
	 * <li>Constants may not appear as an output.</li>
	 * <li>Duplicate wires (duplicates are simply ignored).</li>
	 * </ul>
	 * @param result the output for errors
	 * @param cb the current composite block
	 * @param validBindings the valid bindings
	 */
	public static void verifyBindings(final List<? super AdvanceCompilationError> result,
			final AdvanceCompositeBlock cb, final List<AdvanceBlockBind> validBindings) {
		Set<List<Object>> bindingMemory = Sets.newHashSet();
		Set<List<Object>> sameOutputMemory = Sets.newHashSet();
		for (AdvanceBlockBind bb : cb.bindings) {
			// locate input port object
			Object input = null;
			Object inputPort = null;
			
			if (!bb.hasSourceBlock() && cb.outputs.containsKey(bb.sourceParameter)) {
				result.add(new SourceToCompositeOutputError(bb));
				continue;
			}
			if (!bb.hasSourceBlock() && cb.inputs.containsKey(bb.sourceParameter)) {
				input = cb;
				inputPort = cb.inputs.get(bb.sourceParameter);
			} else
			if (cb.constants.containsKey(bb.sourceBlock)) {
				input = cb.constants.get(bb.sourceBlock);
				inputPort = ""; // default as constants have only a single output
			} else
			if (cb.blocks.containsKey(bb.sourceBlock)) {
				AdvanceBlockReference b = cb.blocks.get(bb.sourceBlock);
				input = b;
				AdvanceBlockRegistryEntry lookup = AdvanceBlockLookup.lookup(b.type);
				if (lookup.inputs.containsKey(bb.sourceParameter)) {
					result.add(new SourceToInputBindingError(bb));
					continue;
				}
				inputPort = lookup.outputs.get(bb.sourceParameter);
			} else
			if (cb.composites.containsKey(bb.sourceBlock)) {
				AdvanceCompositeBlock cb1 = cb.composites.get(bb.sourceBlock);
				input = cb1;
				if (cb1.inputs.containsKey(bb.sourceParameter)) {
					result.add(new SourceToCompositeInputError(bb));
					continue;
				}
				inputPort = cb1.outputs.get(bb.sourceParameter);
			}
			
			
			if (input == null) {
				result.add(new MissingSourceError(bb));
				continue;
			}
			if (inputPort == null) {
				result.add(new MissingSourcePortError(bb));
				continue;
			}
			
			Object output = null;
			Object outputPort = null;
			
			// check if destination is a constant
			if (cb.constants.containsKey(bb.destinationBlock)) {
				result.add(new ConstantOutputError(bb));
				continue;
			}
			if (!bb.hasDestinationBlock() && cb.inputs.containsKey(bb.destinationParameter)) {
				result.add(new DestinationToCompositeInputError(bb));
				continue;
			}
			if (!bb.hasDestinationBlock() && cb.outputs.containsKey(bb.destinationParameter)) {
				output = cb;
				outputPort = cb.outputs.get(bb.destinationParameter);
			} else
			if (cb.blocks.containsKey(bb.destinationBlock)) {
				AdvanceBlockReference b = cb.blocks.get(bb.destinationBlock);
				output = b;
				AdvanceBlockRegistryEntry lookup = AdvanceBlockLookup.lookup(b.type);
				if (lookup.outputs.containsKey(bb.destinationParameter)) {
					result.add(new DestinationToOutputError(bb));
					continue;
				}
				outputPort = lookup.inputs.get(bb.destinationParameter);
			} else
			if (cb.composites.containsKey(bb.destinationBlock)) {
				AdvanceCompositeBlock cb1 = cb.composites.get(bb.destinationBlock);
				output = cb1;
				if (cb1.outputs.containsKey(bb.destinationParameter)) {
					result.add(new DestinationToCompositeOutputError(bb));
					continue;
				}
				outputPort = cb1.inputs.get(bb.destinationParameter);
			}
			
			if (output == null) {
				result.add(new MissingDestinationError(bb));
				continue;
			}
			if (outputPort == null) {
				result.add(new MissingDestinationPortError(bb));
				continue;
			}

			if (!sameOutputMemory.add(Arrays.asList(output, outputPort))) {
				result.add(new MultiInputBindingError(bb));
				continue;
			}

			// ignore duplicate bindings between the same things
			if (bindingMemory.add(Arrays.asList(input, inputPort, output, outputPort))) {
				validBindings.add(bb);
			}

		}
	}
	/** 
	 * Test the compiler.
	 * @param args ignored
	 * @throws Exception ignored 
	 */
	public static void main(String[] args) throws Exception {
		XElement flow = XElement.parseXML("test/flow1.xml");
		AdvanceCompositeBlock cb = AdvanceCompositeBlock.parseFlow(flow);
		System.out.println(verify(cb));
	}
	/** The type substitution record. */
	public static class TypeSubstitution {
		/** The variable to substitute. */
		public AdvanceType left;
		/** The substitution value. */
		public AdvanceType right;
		/** Is this substitution from left to right, e.g left >= right. */
		public boolean isLeftToRight;
		/** The original wire. */
		public AdvanceBlockBind wire;
		/**
		 * Constructor.
		 * @param left the variable
		 * @param right the substitution
		 * @param isLeftToRight is left >= right
		 * @param wire the original wire
		 */
		public TypeSubstitution(AdvanceType left, AdvanceType right,
				boolean isLeftToRight, AdvanceBlockBind wire) {
			this.left = left;
			this.right = right;
			this.isLeftToRight = isLeftToRight;
			this.wire = wire;
		}
		@Override
		public String toString() {
			return String.format("%s(%08X) |-> %s(%08X) (%s, %s) ",
					left, System.identityHashCode(left),
					right, System.identityHashCode(right),
					isLeftToRight, wire.id
			);
		}
	}
	/**
	 * Run the type inference algorithm.
	 * @param relations the set of relations.
	 * @param error the output for errors
	 * @return the substitution relations
	 */
	static List<TypeSubstitution> infer(Deque<TypeRelation> relations, List<AdvanceCompilationError> error) {
		// the most common supertype
		final AdvanceType object = new AdvanceType();
		try {
			object.typeURI = new URI("advance:object");
			object.type = AdvanceResolver.resolveSchema(object.typeURI);
		} catch (URISyntaxException ex) {
			throw new AssertionError(ex);
		}

		return inferHindleyMilner(relations, error);
	}
	/**
	 * Checks if the in type contains any sign of the what type by
	 * traversing the type definition recursively.
	 * @param what the type to look for
	 * @param in the type to test
	 * @return true if present
	 */
	static boolean containsType(AdvanceType what, AdvanceType in) {
		Deque<AdvanceType> stack = Lists.newLinkedList();
		stack.add(in);
		while (!stack.isEmpty()) {
			AdvanceType t = stack.pop();
			if (t == what) {
				return true;
			}
			stack.addAll(t.typeArguments);
		}
		return false;
	}
	/** The type index map. */
	static final Map<AdvanceType, Integer> TMI = Maps.newHashMap();
	/**
	 * Assign an index to the given type.
	 * @param at the type
	 * @return the index
	 */
	static int getTypeIndex(AdvanceType at) {
		Integer i = TMI.get(at);
		if (i == null) {
			i = TMI.size() + 1;
			TMI.put(at, i);
		}
		return i;
	}
	/**
	 * A type inference algorithm using the simple Hindley-Milner algorithm with
	 * allowed T >= U on concrete type pairs.
	 * @param relations the type relations to process
	 * @param error the encountered errors
	 * @return the substitution table
	 */
	static List<TypeSubstitution> inferHindleyMilner(
			Deque<TypeRelation> relations, List<AdvanceCompilationError> error) {
		List<TypeSubstitution> substitution = Lists.newArrayList();
		while (!relations.isEmpty()) {
			LOG.debug("RELS: " + relations.toString());
			TypeRelation rel = relations.pop();
			LOG.debug("CURR: " + rel.toString());
			// Type relation is X >= Y
			if (rel.left.getKind() == AdvanceTypeKind.VARIABLE_TYPE 
					&& rel.right.getKind() == AdvanceTypeKind.VARIABLE_TYPE) {
				// do nothing
				LOG.debug("Step 1: Left & Right are identifiers");
			} else
			if (rel.left.getKind() == AdvanceTypeKind.VARIABLE_TYPE && !containsType(rel.left, rel.right)) {
				// alpha <: C<t1, ..., tn>
				if (rel.right.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE) {
					// create fresh types for each type argument
					LOG.debug("Expand-L");
					int i = 1;
					AdvanceType cp = new AdvanceType();
					cp.type = rel.right.type;
					cp.typeURI = rel.right.typeURI;
					
					for (AdvanceType at : rel.right.typeArguments) {
						// create fresh type
						AdvanceType lt = new AdvanceType();
						lt.typeVariableName = "T" + i;
						lt.typeVariable = new AdvanceTypeVariable();
						lt.typeVariable.name = lt.typeVariableName;
						
						cp.typeArguments.add(lt);
						
						relations.push(new TypeRelation(lt, at, rel.wire));
						
						i++;
					}
					// replace all alpha with the new composite type
					replaceTypes(relations, substitution, rel.left, cp);
					// add alpha -> C(t1,...,tn) as substitution
					substitution.add(new TypeSubstitution(rel.left, cp, true, rel.wire));
				} else {
					// replace X by Y on the stack and in existing substitution
					AdvanceType left = rel.left;
					LOG.debug("Step 2: Left identifier, Right something else");
					replaceTypes(relations, substitution, left, rel.right);
					substitution.add(new TypeSubstitution(left, rel.right, true, rel.wire));
				}
			} else
			if (rel.right.getKind() == AdvanceTypeKind.VARIABLE_TYPE && !containsType(rel.right, rel.left)) {
				if (rel.left.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE) {
					LOG.debug("Expand-R");
					// left is C(u1,...,un)
					int i = 1;
					AdvanceType cp = new AdvanceType();
					cp.type = rel.left.type;
					cp.typeURI = rel.left.typeURI;
					
					for (AdvanceType at : rel.left.typeArguments) {
						// create fresh type ti
						AdvanceType rt = new AdvanceType();
						rt.typeVariableName = "T" + i;
						rt.typeVariable = new AdvanceTypeVariable();
						rt.typeVariable.name = rt.typeVariableName;
						
						cp.typeArguments.add(rt);
						
						// add u1 >= t1 
						relations.push(new TypeRelation(at, rt, rel.wire));
						
						i++;
					}
					// replace all alpha with the new composite type
					replaceTypes(relations, substitution, rel.right, cp);
					// add alpha -> C(t1,...,tn) as substitution
					substitution.add(new TypeSubstitution(rel.right, cp, true, rel.wire));
					
				} else {
					// replace Y by X on the stack and in existing substitution
					AdvanceType right = rel.right;
					LOG.debug("Step 3: Left something else, Right identifier");
					
					replaceTypes(relations, substitution, right, rel.left);
					substitution.add(new TypeSubstitution(right, rel.left, false, rel.wire));
				}
			} else
			if (rel.left.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE 
				&& rel.right.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE
				&& rel.left.typeArguments.size() == rel.right.typeArguments.size()
			) {
				// type constructors are basically parametric types
				// compare base type
				XRelation xr = rel.left.type.compareTo(rel.right.type);
				if (xr == XRelation.EQUAL || xr == XRelation.EXTENDS) {
					LOG.debug("Step 5: Type constructors");
					Iterator<AdvanceType> leftIt = rel.left.typeArguments.iterator();
					Iterator<AdvanceType> rightIt = rel.right.typeArguments.iterator();
					while (leftIt.hasNext() && rightIt.hasNext()) {
						AdvanceType t1 = leftIt.next();
						AdvanceType t2 = rightIt.next();
						TypeRelation cr = new TypeRelation(t1, t2, rel.wire);
						relations.push(cr);
						LOG.debug("Adding new relation " + cr);
					}
				} else {
					LOG.debug("Base type mismatch in " + rel);
					error.add(new TypeMismatchError(rel.wire)); // todo the wire
				}
			} else {
				XRelation xr = SchemaParser.compare(rel.left.type, rel.right.type); // FIXME not sure
				if (xr == XRelation.EQUAL || xr == XRelation.EXTENDS) {
					LOG.debug("Left extends|equals right " + rel);
				} else {
					LOG.debug("Type mismatch in " + rel);
					error.add(new TypeMismatchError(rel.wire)); // todo the wire
					break;
				}
			}
			LOG.debug("SUBS: " + substitution.toString());
		}
		return substitution;
	}
	/**
	 * Replace types in the relations and substitutions recursively.
	 * @param relations the available relations
	 * @param substitution the available substitutions
	 * @param oldType the type to replace
	 * @param newType the new type to assign
	 */
	private static void replaceTypes(Deque<TypeRelation> relations,
			List<TypeSubstitution> substitution, AdvanceType oldType,
			AdvanceType newType) {
		for (TypeRelation tr : relations) {
			if (tr.left == oldType) {
				tr.left = newType;
			} else {
				replaceInnerType(tr.left, oldType, newType);
			}
			if (tr.right == oldType) {
				tr.right = newType;
			} else {
				replaceInnerType(tr.left, oldType, newType);
			}
		}
		for (TypeSubstitution ts : substitution) {
			if (ts.right == oldType) {
				ts.right = newType;
			} else {
				replaceInnerType(ts.right, oldType, newType);
			}
		}
	}
	/**
	 * Replace an inner type reference recursively if exists.
	 * @param left the target type to check for the type arguments
	 * @param oldType the old type to replace
	 * @param newType the new type
	 */
	private static void replaceInnerType(AdvanceType left, AdvanceType oldType,
			AdvanceType newType) {
		Deque<AdvanceType> stack = Lists.newLinkedList();
		stack.add(left);
		while (!stack.isEmpty()) {
			AdvanceType t = stack.pop();
			int i = 0;
			for (AdvanceType ta : Lists.newArrayList(t.typeArguments)) {
				if (ta == oldType) {
					t.typeArguments.set(i, newType);
				} else {
					stack.addAll(ta.typeArguments);
				}
				i++;
			}
		}
	}
	/**
	 * <p>Type inference using the algorithm described by</p>
	 * <p>Francois Pottier: Type inference in presence of subtyping: from theory to practice</p>.
	 * @param relations the available type relations as extracted from the program.
	 * @param error the errors discovered.
	 */
	public static void pottierInference(Deque<TypeRelation> relations, List<AdvanceCompilationError> error) {
		// C up typevar -> term
		// keeps track of the current upper bound (lower bound in Java type terms) for a type variable
		Map<AdvanceType, AdvanceType> upperBounds = Maps.newHashMap();
		// keeps track of the current lower bound (upper bound in Java type terms) for a type variable
		// C down typevar -> term
		Map<AdvanceType, AdvanceType> lowerBounds = Maps.newHashMap();
		// keeps track of the boundary relations of type variables
		List<TypeRelation> reflexiveRelations = Lists.newArrayList();
		
		while (!relations.isEmpty()) {
			TypeRelation tr = relations.pop();
			// if both sides are type variables
			if (tr.left.getKind() == AdvanceTypeKind.VARIABLE_TYPE && tr.right.getKind() == AdvanceTypeKind.VARIABLE_TYPE) {
				// if left <= right is in reflexiveRelations, do nothing
				if (!containsRelation(reflexiveRelations, tr.left, tr.right)) {
					
					AdvanceType alphaDown = lowerBounds.get(tr.left);
					AdvanceType betaUp = upperBounds.get(tr.right);
					
					// e.g., (alpha', beta') = reflexiveRelations 
					for (TypeRelation refl : Lists.newArrayList(reflexiveRelations)) {
						// add clauses for each  (x <= left) as (x <= right) | (alpha' <= alpha) -> (alpha' <= beta)
						if (refl.right == tr.left) {
							reflexiveRelations.add(new TypeRelation(refl.left, tr.right, tr.wire));
							
							// adjust alpha's upper bound to the union with its current bound
							AdvanceType alpha2Upper = upperBounds.get(refl.left);
							AdvanceType betaUpper = upperBounds.get(tr.right);
							if (alpha2Upper != null && betaUpper != null) {
								AdvanceType u = union(alpha2Upper, betaUpper);
								if (u != null) {
									upperBounds.put(refl.left, u);
								} else {
									// no union is possible this is a type error
									error.add(new TypeMismatchError(tr.wire));
									break;
								}
							}
							
						}
						// add clauses for each  (right <= y) as (left <= y) | (beta <= beta') -> (alpha <= beta')
						if (refl.left == tr.right) {
							reflexiveRelations.add(new TypeRelation(tr.left, refl.right, tr.wire));
							
							// adjust beta' lower bound to the intersection with its current bound if present
							
							AdvanceType beta2Lower = lowerBounds.get(refl.right); // type or bottom
							AdvanceType alphaLower = lowerBounds.get(tr.left); // type or bottom
							if (beta2Lower != null && alphaLower != null) {
								lowerBounds.put(refl.right, intersection(beta2Lower, alphaLower));
							}
						}
					}
					subc(alphaDown, betaUp, tr.wire, relations);
				}
			} else
			if (tr.left.getKind() == AdvanceTypeKind.VARIABLE_TYPE) {
				// alpha <= tau
				AdvanceType alphaUp = upperBounds.get(tr.left);
				AdvanceType alphaDown = lowerBounds.get(tr.left);
				if (alphaUp == null || !containsType(alphaUp, tr.right)) {
					for (TypeRelation refl : Lists.newArrayList(reflexiveRelations)) {
						// add clauses for each  (x <= left) as (x <= right) | (alpha' <= alpha) -> (alpha' <= beta)
						if (refl.right == tr.left) {
							// adjust alpha's upper bound to the union with its current bound
							AdvanceType alpha2Upper = upperBounds.get(refl.left);
							AdvanceType betaUpper = upperBounds.get(tr.right);
							if (alpha2Upper != null && betaUpper != null) {
								AdvanceType u = union(alpha2Upper, betaUpper);
								if (u != null) {
									upperBounds.put(refl.left, u);
								} else {
									// no union is possible this is a type error
									error.add(new TypeMismatchError(tr.wire));
									break;
								}
							}
							
						}
					}					
				}
				subc(alphaDown, tr.right, tr.wire, relations);
			} else
			if (tr.right.getKind() == AdvanceTypeKind.VARIABLE_TYPE) {
				// tau <= beta
				AdvanceType betaDown = lowerBounds.get(tr.right);
				AdvanceType betaUp = upperBounds.get(tr.right);
				if (betaDown == null || !containsType(betaDown, tr.left)) {
					// e.g., (alpha', beta') = reflexiveRelations 
					for (TypeRelation refl : Lists.newArrayList(reflexiveRelations)) {
						// add clauses for each  (right <= y) as (left <= y) | (beta <= beta') -> (alpha <= beta')
						if (refl.left == tr.right) {
							reflexiveRelations.add(new TypeRelation(tr.left, refl.right, tr.wire));
							
							// adjust beta' lower bound to the intersection with its current bound if present
							
							AdvanceType beta2Lower = lowerBounds.get(refl.right); // type or bottom
							AdvanceType alphaLower = lowerBounds.get(tr.left); // type or bottom
							if (beta2Lower != null && alphaLower != null) {
								lowerBounds.put(refl.right, intersection(beta2Lower, alphaLower));
							}
						}
					}				
				}
				subc(tr.left, betaUp, tr.wire, relations);
			}
		}
	}
	/**
	 * Compute a substitution for the constraint based on its structure.
	 * @param left the left type
	 * @param right the right type
	 * @param wire the original wire
	 * @param relations the relations to add new constraints
	 */
	static void subc(AdvanceType left, AdvanceType right, AdvanceBlockBind wire, Deque<TypeRelation> relations) {
		if (left != null && right != null) {
			if (left.getKind() != AdvanceTypeKind.PARAMETRIC_TYPE && right.getKind() != AdvanceTypeKind.PARAMETRIC_TYPE) {
				relations.add(new TypeRelation(left, right, wire));
			} else
			if (left.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE && right.getKind() != AdvanceTypeKind.PARAMETRIC_TYPE) {
				for (AdvanceType at : left.typeArguments) {
					AdvanceType fresh = new AdvanceType();
					fresh.typeVariableName = "T";
					fresh.typeVariable = new AdvanceTypeVariable();
					fresh.typeVariable.name = "T";
					subc(at, fresh, wire, relations);
				}
			} else
			if (right.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE && left.getKind() != AdvanceTypeKind.PARAMETRIC_TYPE) {
				for (AdvanceType at : left.typeArguments) {
					AdvanceType fresh = new AdvanceType();
					fresh.typeVariableName = "T";
					fresh.typeVariable = new AdvanceTypeVariable();
					fresh.typeVariable.name = "T";
					subc(fresh, at, wire, relations);
				}
			} else {
				if (left.typeArguments.size() == right.typeArguments.size()) {
					XRelation xr = SchemaParser.compare(left.type, right.type);
					if (xr == XRelation.EQUAL || xr == XRelation.EXTENDS) {
						Iterator<AdvanceType> ta1 = left.typeArguments.iterator();
						Iterator<AdvanceType> ta2 = right.typeArguments.iterator();
						while (ta1.hasNext() && ta2.hasNext()) {
							AdvanceType t1 = ta1.next();
							AdvanceType t2 = ta2.next();
							subc(t1, t2, wire, relations);
						}
					} else {
						// TODO signal base type error
					}
				} else {
					// TODO signal error on mismatching base types
				}
			}
			
		}
	}
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
}
