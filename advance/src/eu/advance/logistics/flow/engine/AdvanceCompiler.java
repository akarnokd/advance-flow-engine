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

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.advance.logistics.flow.model.AdvanceBlockBind;
import eu.advance.logistics.flow.model.AdvanceBlockDescription;
import eu.advance.logistics.flow.model.AdvanceBlockParameterDescription;
import eu.advance.logistics.flow.model.AdvanceBlockReference;
import eu.advance.logistics.flow.model.AdvanceCompositeBlock;
import eu.advance.logistics.flow.model.AdvanceConstantBlock;

/**
 * The ADVANCE block compiler which turns the the flow description into runnable advance blocks.
 * @author karnokd, 2011.06.27.
 */
public final class AdvanceCompiler {
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
	 */
	public static void run(List<AdvanceBlock> flow) {
		// arm
		List<Observer<Void>> notifycations = Lists.newLinkedList();
		for (AdvanceBlock ab : flow) {
			notifycations.add(ab.run());
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
}
