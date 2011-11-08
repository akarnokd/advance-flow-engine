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

package eu.advance.logistics.flow.engine.api;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockRegistryEntry;
import eu.advance.logistics.flow.engine.model.rt.AdvanceCompilationResult;

/**
 * Base interface to support compiling and verifying flows.
 * @author akarnokd, 2011.10.04.
 */
public interface AdvanceFlowCompiler {
	/**
	 * Verify the given flow.
	 * @param flow outer composite block of the dataflow.
	 * @return the compilation result.
	 */
	AdvanceCompilationResult verify(@NonNull AdvanceCompositeBlock flow);
	/**
	 * Compile the target composite block as flow. The flow
	 * should pass the verification of the {@link #verify(AdvanceCompositeBlock)} call.
	 * @param flow the outer composite block of the dataflow.
	 * @return the list of compiled concrete blocks
	 */
	List<AdvanceBlock> compile(@NonNull AdvanceCompositeBlock flow);
	/**
	 * Returns a list of supported block types.
	 * @return the block types
	 */
	@NonNull 
	List<AdvanceBlockRegistryEntry> blocks();
}
