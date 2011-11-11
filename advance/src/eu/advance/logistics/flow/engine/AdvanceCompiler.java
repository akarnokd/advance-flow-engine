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

import hu.akarnokd.reactive4java.base.Pair;
import hu.akarnokd.reactive4java.base.Scheduler;
import hu.akarnokd.reactive4java.reactive.Observer;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.api.AdvanceFlowCompiler;
import eu.advance.logistics.flow.engine.api.AdvanceFlowExecutor;
import eu.advance.logistics.flow.engine.api.core.AdvanceData;
import eu.advance.logistics.flow.engine.error.ConstantOutputError;
import eu.advance.logistics.flow.engine.error.DestinationToCompositeInputError;
import eu.advance.logistics.flow.engine.error.DestinationToCompositeOutputError;
import eu.advance.logistics.flow.engine.error.DestinationToOutputError;
import eu.advance.logistics.flow.engine.error.HasTypes;
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
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockSettings;
import eu.advance.logistics.flow.engine.model.rt.AdvanceCompilationResult;
import eu.advance.logistics.flow.engine.model.rt.AdvancePort;
import eu.advance.logistics.flow.engine.model.rt.AdvanceSchedulerPreference;
import eu.advance.logistics.flow.engine.util.Triplet;
import eu.advance.logistics.flow.engine.xml.typesystem.XRelation;
import eu.advance.logistics.flow.engine.xml.typesystem.XSchema;
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
	/** Thec compiler settings. */
	protected final AdvanceCompilerSettings settings;
	/** The predefined list of base types. */
	protected final List<Pair<XType, URI>> baseTypes = Lists.newArrayList();
	/**
	 * Constructor.
	 * @param settings the compiler settings
	 */
	public AdvanceCompiler(AdvanceCompilerSettings settings) {
		this.settings = settings;
		for (URI u : AdvanceData.BASE_TYPES) {
			baseTypes.add(Pair.of(schemaResolver().resolve(u), u));
		}
	}
	@Override
	public List<AdvanceBlock> compile(AdvanceCompositeBlock flow) {
		List<AdvanceBlock> result = Lists.newArrayList();
		compile(flow, result);
		return result;
	}
	@Override
	public List<AdvanceBlockRegistryEntry> blocks() {
		return Lists.newArrayList(blockResolver().blocks.values());
	}
	/**
	 * Returns the block resolver.
	 * @return the block resolver
	 */
	public AdvanceBlockResolver blockResolver() {
		return settings.blockResolver;
	}
	/**
	 * Returns the schema resolver.
	 * @return the schema resolver
	 */
	public AdvanceSchemaResolver schemaResolver() {
		return settings.schemaResolver;
	}
	/**
	 * Returns the schedulers.
	 * @return the schedulers
	 */
	public Map<AdvanceSchedulerPreference, Scheduler> schedulers() {
		return settings.schedulers;
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
		List<AdvanceBlock> currentLevelBlocks = Lists.newArrayList();
		try {
			for (AdvanceCompositeBlock cb : root.composites.values()) {
				compile(cb, flow);
			}
			// current level blocks
			for (AdvanceBlockReference br : root.blocks.values()) {
				Map<String, AdvanceConstantBlock> consts = Maps.newHashMap();
				AdvanceBlockDescription bd = blockResolver().lookup(br.type);
				for (AdvanceBlockParameterDescription bdp : bd.inputs.values()) {
					ConstantOrBlock cb = walkBinding(root, br.id, bdp.id);
					if (cb != null && cb.constant != null) {
						consts.put(bdp.id, cb.constant);
					}
				}
				AdvanceBlockSettings blockSettings = new AdvanceBlockSettings();
				blockSettings.id = br.id;
				blockSettings.parent = root;
				blockSettings.schedulers = schedulers();
				blockSettings.datastore = this.settings.datastore;
				blockSettings.pools = this.settings.pools;
				
				AdvanceBlock ab = blockResolver().create(blockSettings, br.type);
				ab.init(consts);
				
				flow.add(ab);
				currentLevelBlocks.add(ab);
			}
			// bind
			if (root.parent == null) {
				for (AdvanceBlock ab : flow) {
					for (AdvancePort p : ab.inputs.values()) {
						if (p instanceof AdvanceBlockPort) {
							ConstantOrBlock cb = walkBinding(ab.parent(), ab.id(), p.name());
							if (cb != null) {
								for  (AdvanceBlock ab2 : flow) {
									if (ab2.parent() == cb.composite && ab2.id().equals(cb.block)) {
										((AdvanceBlockPort) p).connect(ab2.getOutput(cb.param));
										break;
									}
								}
							}
						}
					}
				}
			}
		} catch (RuntimeException ex) {
			LOG.error(ex.toString(), ex);
			// terminate blocks
			for (AdvanceBlock b : currentLevelBlocks) {
				try {
					b.done();
				} catch (Throwable t) {
					LOG.error(t.toString(), t);
				}
			}
			throw ex;
		}
	}
	/**
	 * Run the flow graph.
	 * @param flow the list of blocks
	 */
	@Override
	public void run(Iterable<? extends AdvanceBlock> flow) {
		// arm
		try {
			List<Observer<Void>> notifycations = Lists.newLinkedList();
			for (AdvanceBlock ab : flow) {
				notifycations.add(ab.run());
			}
			// notify
			for (Observer<Void> n : notifycations) {
				n.next(null);
			}
		} catch (RuntimeException ex) {
			LOG.error(ex.toString(), ex);
			for (AdvanceBlock b : flow) {
				try {
					b.done();
				} catch (Throwable t) {
					LOG.error(t.toString(), t);
				}
			}
			throw ex;
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
	@Nullable
	ConstantOrBlock walkBinding(AdvanceCompositeBlock start, String block, String param) {
		while (!Thread.currentThread().isInterrupted()) {
			boolean foundBinding = false;
			for (AdvanceBlockBind bb : start.bindings) {
				if (bb.destinationBlock.equals(block) && bb.destinationParameter.equals(param)) {
					foundBinding = true;
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
			if (!foundBinding) {
				break;
			}
		}
		return null;
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

			if (!verifyBlocks(result, cb)) {
				continue;
			}

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
						at2.type = schemaResolver().resolve(constblock.typeURI);
						typeMemory.put(bb.sourceBlock, Collections.singletonMap("", at2));
						tr.left = at2;
					} else {
						tr.left = at.get("");
					}
				} else
				if (cb.blocks.containsKey(bb.sourceBlock)) {
					Map<String, AdvanceType> at = typeMemory.get(bb.sourceBlock);
					if (at == null) {
						AdvanceBlockDescription block = blockResolver().lookup(cb.blocks.get(bb.sourceBlock).type).copy();
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
						AdvanceBlockDescription block = blockResolver().lookup(cb.blocks.get(bb.destinationBlock).type).copy();
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
		
		AdvanceTypeInference typeInference = new AdvanceTypeInference(relations);
		result.add(typeInference.infer());
		if (result.wireTypes().size() > 0) {
				
			Deque<AdvanceType> types = Lists.newLinkedList();
			for (AdvanceType at : result.wireTypes()) {
				types.add(at);
			}
			for (AdvanceCompilationError e : result.errors()) {
				if (e instanceof HasTypes) {
					Iterables.addAll(types, ((HasTypes)e).types());
				}
			}
			while (!types.isEmpty()) {
				AdvanceType t = types.removeFirst();
				if (t.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE) {
					types.addAll(t.typeArguments);
				} else 
				if (t.getKind() == AdvanceTypeKind.CONCRETE_TYPE) {
					for (Pair<XType, URI> xt : baseTypes) {
						if (XSchema.compare(xt.first, t.type) == XRelation.EQUAL) {
							t.typeURI = xt.second;
							break;
						}
					}
				}
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
	boolean verifyBlocks(final AdvanceCompilationResult result,
			final AdvanceCompositeBlock cb) {
		boolean r = true;
		for (AdvanceBlockReference br : cb.blocks.values()) {
			if (blockResolver().lookup(br.type) == null) {
				result.addError(new MissingBlockError(br.id, br.type));
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
	void verifyBindings(final AdvanceCompilationResult result,
			final AdvanceCompositeBlock cb, 
			final List<AdvanceBlockBind> validBindings) {
		Set<List<Object>> bindingMemory = Sets.newHashSet();
		Set<List<Object>> sameOutputMemory = Sets.newHashSet();
		for (AdvanceBlockBind bb : cb.bindings) {
			// locate input port object
			Object input = null;
			Object inputPort = null;
			
			if (!bb.hasSourceBlock() && cb.outputs.containsKey(bb.sourceParameter)) {
				result.addError(new SourceToCompositeOutputError(bb));
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
				AdvanceBlockRegistryEntry block = blockResolver().lookup(b.type);
				if (block.inputs.containsKey(bb.sourceParameter)) {
					result.addError(new SourceToInputBindingError(bb));
					continue;
				}
				inputPort = block.outputs.get(bb.sourceParameter);
			} else
			if (cb.composites.containsKey(bb.sourceBlock)) {
				AdvanceCompositeBlock cb1 = cb.composites.get(bb.sourceBlock);
				input = cb1;
				if (cb1.inputs.containsKey(bb.sourceParameter)) {
					result.addError(new SourceToCompositeInputError(bb));
					continue;
				}
				inputPort = cb1.outputs.get(bb.sourceParameter);
			}
			
			
			if (input == null) {
				result.addError(new MissingSourceError(bb));
				continue;
			}
			if (inputPort == null) {
				result.addError(new MissingSourcePortError(bb));
				continue;
			}
			
			Object output = null;
			Object outputPort = null;
			
			// check if destination is a constant
			if (cb.constants.containsKey(bb.destinationBlock)) {
				result.addError(new ConstantOutputError(bb));
				continue;
			}
			if (!bb.hasDestinationBlock() && cb.inputs.containsKey(bb.destinationParameter)) {
				result.addError(new DestinationToCompositeInputError(bb));
				continue;
			}
			if (!bb.hasDestinationBlock() && cb.outputs.containsKey(bb.destinationParameter)) {
				output = cb;
				outputPort = cb.outputs.get(bb.destinationParameter);
			} else
			if (cb.blocks.containsKey(bb.destinationBlock)) {
				AdvanceBlockReference b = cb.blocks.get(bb.destinationBlock);
				output = b;
				AdvanceBlockRegistryEntry block = blockResolver().lookup(b.type);
				if (block.outputs.containsKey(bb.destinationParameter)) {
					result.addError(new DestinationToOutputError(bb));
					continue;
				}
				outputPort = block.inputs.get(bb.destinationParameter);
			} else
			if (cb.composites.containsKey(bb.destinationBlock)) {
				AdvanceCompositeBlock cb1 = cb.composites.get(bb.destinationBlock);
				output = cb1;
				if (cb1.outputs.containsKey(bb.destinationParameter)) {
					result.addError(new DestinationToCompositeOutputError(bb));
					continue;
				}
				outputPort = cb1.inputs.get(bb.destinationParameter);
			}
			
			if (output == null) {
				result.addError(new MissingDestinationError(bb));
				continue;
			}
			if (outputPort == null) {
				result.addError(new MissingDestinationPortError(bb));
				continue;
			}

			if (!sameOutputMemory.add(Arrays.asList(output, outputPort))) {
				result.addError(new MultiInputBindingError(bb));
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
					xt = schemaResolver().resolve(t.typeURI);
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
