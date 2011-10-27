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

import hu.akarnokd.reactive4java.base.Pair;
import hu.akarnokd.reactive4java.base.Scheduler;
import hu.akarnokd.reactive4java.reactive.Observer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.advance.logistics.flow.engine.api.AdvanceFlowCompiler;
import eu.advance.logistics.flow.engine.api.AdvanceFlowExecutor;
import eu.advance.logistics.flow.engine.error.ConstantOutputError;
import eu.advance.logistics.flow.engine.error.DestinationToCompositeInputError;
import eu.advance.logistics.flow.engine.error.DestinationToCompositeOutputError;
import eu.advance.logistics.flow.engine.error.DestinationToOutputError;
import eu.advance.logistics.flow.engine.error.MissingBlockError;
import eu.advance.logistics.flow.engine.error.MissingDestinationError;
import eu.advance.logistics.flow.engine.error.MissingDestinationPortError;
import eu.advance.logistics.flow.engine.error.MissingSourceError;
import eu.advance.logistics.flow.engine.error.MissingSourcePortError;
import eu.advance.logistics.flow.engine.error.MultiInputBindingError;
import eu.advance.logistics.flow.engine.error.SourceToCompositeInputError;
import eu.advance.logistics.flow.engine.error.SourceToCompositeOutputError;
import eu.advance.logistics.flow.engine.error.SourceToInputBindingError;
import eu.advance.logistics.flow.engine.model.AdvanceCompilationError;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockBind;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockReference;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.fd.AdvanceConstantBlock;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.model.fd.AdvanceTypeKind;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockPort;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockRegistryEntry;
import eu.advance.logistics.flow.engine.model.rt.AdvanceCompilationResult;
import eu.advance.logistics.flow.engine.model.rt.AdvancePort;
import eu.advance.logistics.flow.engine.model.rt.AdvanceSchedulerPreference;
import eu.advance.logistics.flow.engine.util.Triplet;
import eu.advance.logistics.flow.engine.xml.typesystem.SchemaParser;
import eu.advance.logistics.flow.engine.xml.typesystem.XRelation;
import eu.advance.logistics.flow.engine.xml.typesystem.XType;

/**
 * The ADVANCE block compiler which turns the the flow description into runnable advance blocks.
 * @author akarnokd, 2011.06.27.
 */
public final class AdvanceCompiler implements AdvanceFlowCompiler, AdvanceFlowExecutor {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceFlowEngine.class);
	/** The cache for schema uri to types. */
	protected Map<String, XType> schemaTypeCache = Maps.newConcurrentMap();
	/** The schema resolver. */
	protected final AdvanceSchemaResolver schemaResolver;
	/** The block resolver. */
	protected final AdvanceBlockResolver blockResolver;
	/** The map of various schedulers. */
	protected final Map<AdvanceSchedulerPreference, Scheduler> schedulers;
	/**
	 * Constructor.
	 * @param schemaResolver the schema resolver
	 * @param blockResolver the block resolver
	 * @param schedulers the schedulers for the blocks
	 */
	public AdvanceCompiler(
			AdvanceSchemaResolver schemaResolver, 
			AdvanceBlockResolver blockResolver,
			Map<AdvanceSchedulerPreference, Scheduler> schedulers) {
		this.schemaResolver = schemaResolver;
		this.blockResolver = blockResolver;
		this.schedulers = schedulers;
	}
	@Override
	public List<AdvanceBlock> compile(AdvanceCompositeBlock flow) {
		List<AdvanceBlock> result = Lists.newArrayList();
		compile(flow, result);
		return result;
	}
	@Override
	public List<AdvanceBlockRegistryEntry> blocks() {
		return Lists.newArrayList(blockResolver.blocks.values());
	}
	/**
	 * Compile the composite block.
	 * @param root the flow description
	 * @param flow the entire compiled flow model
	 */
	public void compile(
			AdvanceCompositeBlock root, 
			List<AdvanceBlock> flow
			) {
		for (AdvanceCompositeBlock cb : root.composites.values()) {
			compile(cb, flow);
		}
		// current level constants
		for (AdvanceBlockReference br : root.blocks.values()) {
			Map<String, AdvanceConstantBlock> consts = Maps.newHashMap();
			AdvanceBlockDescription bd = blockResolver.lookup(br.type);
			for (AdvanceBlockParameterDescription bdp : bd.inputs.values()) {
				ConstantOrBlock cb = walkBinding(root, br.id, bdp.id);
				if (cb != null && cb.constant != null) {
					consts.put(bdp.id, cb.constant);
				}
			}
			AdvanceBlock ab = blockResolver.create(br.id, root, br.type);
			ab.init(bd, consts);
			flow.add(ab);
		}
		// bind
		if (root.parent == null) {
			for (AdvanceBlock ab : flow) {
				for (AdvancePort p : ab.inputs) {
					if (p instanceof AdvanceBlockPort) {
						ConstantOrBlock cb = walkBinding(ab.parent, ab.id, p.name());
						for  (AdvanceBlock ab2 : flow) {
							if (ab2.parent == cb.composite && ab2.id.equals(cb.block)) {
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
	 */
	@Override
	public void run(Iterable<? extends AdvanceBlock> flow) {
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
	@Override
	public void done(Iterable<? extends AdvanceBlock> flow) {
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
	 * @param block the starting block identifier
	 * @param param the starting parameter
	 * @return the constant block or null
	 */
	ConstantOrBlock walkBinding(AdvanceCompositeBlock start, String block, String param) {
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
						break;
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
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TypeRelation) {
				TypeRelation tr = (TypeRelation) obj;
				return left == tr.left && right == tr.right;
			}
			return false;
		}
		@Override
		public int hashCode() {
			return left.hashCode() * 31 + right.hashCode();
		}
	}
	/**
	 * Verify the types along the bindings of this composite block.
	 * @param enclosingBlock the most outer block
	 * @return the compilation result
	 */
	@Override
	public AdvanceCompilationResult verify(
			AdvanceCompositeBlock enclosingBlock) {
		AdvanceCompilationResult result = new AdvanceCompilationResult();
		
		LinkedList<TypeRelation> relations = Lists.newLinkedList();
		
		LinkedList<AdvanceCompositeBlock> blockRecursion = Lists.newLinkedList();
		blockRecursion.add(enclosingBlock);
		
		Map<Triplet<AdvanceCompositeBlock, String, String>, AdvanceType> compositePortTypes = Maps.newHashMap();
		
		schemaTypeCache.clear();
		
		
		while (!blockRecursion.isEmpty()) {
			AdvanceCompositeBlock cb = blockRecursion.removeFirst();
			blockRecursion.addAll(cb.composites.values());

			if (!verifyBlocks(result.errors, cb)) {
				continue;
			}

			// verify bindings
			List<AdvanceBlockBind> validBindings = Lists.newArrayList();
			verifyBindings(result.errors, cb, validBindings);
			
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
						at2.type = schemaResolver.resolve(constblock.typeURI);
						typeMemory.put(bb.sourceBlock, Collections.singletonMap("", at2));
						tr.left = at2;
					} else {
						tr.left = at.get("");
					}
				} else
				if (cb.blocks.containsKey(bb.sourceBlock)) {
					Map<String, AdvanceType> at = typeMemory.get(bb.sourceBlock);
					if (at == null) {
						AdvanceBlockDescription block = blockResolver.lookup(cb.blocks.get(bb.sourceBlock).type).copy();
						at = Maps.newHashMap();
						for (AdvanceBlockParameterDescription bpd : block.inputs.values()) {
							resolve(bpd.type);
							at.put(bpd.id, bpd.type);
						}
						for (AdvanceBlockParameterDescription bpd : block.outputs.values()) {
							resolve(bpd.type);
							at.put(bpd.id, bpd.type);
						}
						typeMemory.put(bb.sourceBlock, at);
					}
					tr.left = at.get(bb.sourceParameter);
					// add bounds of left if any
					if (tr.left.typeVariable != null) {
						for (AdvanceType t : tr.left.typeVariable.bounds) {
							if (tr.left.typeVariable.isUpperBound) {
								relations.add(new TypeRelation(t, tr.left, bb));
							} else {
								relations.add(new TypeRelation(tr.left, t, bb));
							}
						}
					}
				} else
				if (cb.composites.containsKey(bb.sourceBlock)) {
					AdvanceCompositeBlock cb1 = cb.composites.get(bb.sourceBlock);
					Triplet<AdvanceCompositeBlock, String, String> typePort = 
							Triplet.of(cb1, "", bb.sourceParameter);
					AdvanceType at = compositePortTypes.get(typePort);
					if (at == null) {
						// construct a simple unbounded type variable
						at = AdvanceType.fresh();
						compositePortTypes.put(typePort, at);
					}					
					tr.left = at;
				} else
				if (!bb.hasSourceBlock() && cb.inputs.containsKey(bb.sourceParameter)) {
					Triplet<AdvanceCompositeBlock, String, String> typePort = Triplet.of(cb, "", bb.sourceParameter);
					AdvanceType at = compositePortTypes.get(typePort);
					if (at == null) {
						at = AdvanceType.fresh();
						compositePortTypes.put(typePort, at);
					}					
					tr.left = at;
				}
				// evaluate destination
				if (cb.blocks.containsKey(bb.destinationBlock)) {
					Map<String, AdvanceType> at = typeMemory.get(bb.destinationBlock);
					if (at == null) {
						AdvanceBlockDescription block = blockResolver.lookup(cb.blocks.get(bb.destinationBlock).type).copy();
						at = Maps.newHashMap();
						for (AdvanceBlockParameterDescription bpd : block.inputs.values()) {
							resolve(bpd.type);
							at.put(bpd.id, bpd.type);
						}
						for (AdvanceBlockParameterDescription bpd : block.outputs.values()) {
							resolve(bpd.type);
							at.put(bpd.id, bpd.type);
						}
						typeMemory.put(bb.destinationBlock, at);
					}
					tr.right = at.get(bb.destinationParameter);
					if (tr.right.typeVariable != null) {
						for (AdvanceType t : tr.right.typeVariable.bounds) {
							if (tr.right.typeVariable.isUpperBound) {
								relations.add(new TypeRelation(t, tr.right, bb));
							} else {
								relations.add(new TypeRelation(tr.right, t, bb));
							}
						}
					}
				} else
				if (cb.composites.containsKey(bb.destinationBlock)) {
					AdvanceCompositeBlock cb1 = cb.composites.get(bb.destinationBlock);
					Triplet<AdvanceCompositeBlock, String, String> typePort = 
							Triplet.of(cb1, "", bb.destinationParameter);
					AdvanceType at = compositePortTypes.get(typePort);
					if (at == null) {
						// construct a simple unbounded type variable
						at = AdvanceType.fresh();
						compositePortTypes.put(typePort, at);
					}					
					tr.right = at;
					
				} else
				if (!bb.hasDestinationBlock() && cb.outputs.containsKey(bb.destinationParameter)) {
					Triplet<AdvanceCompositeBlock, String, String> typePort = Triplet.of(cb, "", bb.destinationParameter);
					AdvanceType at = compositePortTypes.get(typePort);
					if (at == null) {
						at = AdvanceType.fresh();
						compositePortTypes.put(typePort, at);
					}					
					tr.right = at;
				}
				
				relations.add(tr);
			}
		}
		
		// ---------------------------------------------------------------------------------
		
		AdvanceTypeInference.infer(relations, result);
		
		if (result.wireTypes.size() > 0) {
			List<Pair<XType, URI>> baseTypes = Lists.newArrayList();
			try {
				baseTypes.add(Pair.of(schemaResolver.resolve(new URI("advance:object")), new URI("advance:object")));
				baseTypes.add(Pair.of(schemaResolver.resolve(new URI("advance:integer")), new URI("advance:integer")));
				baseTypes.add(Pair.of(schemaResolver.resolve(new URI("advance:real")), new URI("advance:real")));
				baseTypes.add(Pair.of(schemaResolver.resolve(new URI("advance:string")), new URI("advance:string")));
				baseTypes.add(Pair.of(schemaResolver.resolve(new URI("advance:boolean")), new URI("advance:boolean")));
				baseTypes.add(Pair.of(schemaResolver.resolve(new URI("advance:timestamp")), new URI("advance:timestamp")));
				
				Deque<AdvanceType> types = Lists.newLinkedList();
				for (AdvanceType at : result.wireTypes.values()) {
					types.add(at);
				}
				while (!types.isEmpty()) {
					AdvanceType t = types.removeFirst();
					if (t.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE) {
						types.addAll(t.typeArguments);
					} else {
						for (Pair<XType, URI> xt : baseTypes) {
							if (SchemaParser.compare(xt.first, t.type) == XRelation.EQUAL) {
								t.typeURI = xt.second;
								break;
							}
						}
					}
				}
			} catch (URISyntaxException ex) {
				LOG.error(ex.toString(), ex);
			}
		}
		
		return result;
	}
	/**
	 * Verify the existence of blocks within the registry.
	 * @param result the error output
	 * @param cb the composite block to verify
	 * @return false if one or more blocks are missing.
	 */
	boolean verifyBlocks(final List<? super AdvanceCompilationError> result,
			final AdvanceCompositeBlock cb) {
		boolean r = true;
		for (AdvanceBlockReference br : cb.blocks.values()) {
			if (blockResolver.lookup(br.type) == null) {
				result.add(new MissingBlockError(br.id, br.type));
				r = false;
			}
		}
		return r;
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
	void verifyBindings(final List<? super AdvanceCompilationError> result,
			final AdvanceCompositeBlock cb, 
			final List<AdvanceBlockBind> validBindings) {
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
				AdvanceBlockRegistryEntry block = blockResolver.lookup(b.type);
				if (block.inputs.containsKey(bb.sourceParameter)) {
					result.add(new SourceToInputBindingError(bb));
					continue;
				}
				inputPort = block.outputs.get(bb.sourceParameter);
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
				AdvanceBlockRegistryEntry block = blockResolver.lookup(b.type);
				if (block.outputs.containsKey(bb.destinationParameter)) {
					result.add(new DestinationToOutputError(bb));
					continue;
				}
				outputPort = block.inputs.get(bb.destinationParameter);
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
	 * Resolve the schemas in the given type recursively and in place.
	 * It caches the resolved schemas for the duration of this method
	 * @param type the type to start with
	 */
	void resolve(AdvanceType type) {
		LinkedList<AdvanceType> queue = Lists.newLinkedList();
		queue.add(type);
		while (!queue.isEmpty()) {
			AdvanceType t = queue.removeFirst();
			if (t.typeURI != null) {
				String key = t.typeURI.toString();
				XType xt = schemaTypeCache.get(key);
				if (xt == null) {
					xt = schemaResolver.resolve(t.typeURI);
					schemaTypeCache.put(key, xt);
				}
				t.type = xt;
			}
			queue.addAll(t.typeArguments);
			if (t.typeVariable != null) {
				queue.addAll(t.typeVariable.bounds);
			}
		}
	}
}
