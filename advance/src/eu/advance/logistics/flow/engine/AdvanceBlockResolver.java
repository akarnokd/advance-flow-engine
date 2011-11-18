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

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.runtime.Block;
import eu.advance.logistics.flow.engine.runtime.BlockRegistryEntry;

/**
 * The base interface for resolving blocks.
 * @author akarnokd, 2011.11.17.
 * @param <T> the runtime type of the dataflow
 * @param <X> the type system type
 * @param <C> the runtime context
 */
public interface AdvanceBlockResolver<T, X, C> {
	/**
	 * Locate the block description in the repository (e.g., block-repository.xml).
	 * @param id the block identifier
	 * @return the block
	 */
	BlockRegistryEntry lookup(@NonNull String id);
	/**
	 * Create a concrete block by using the given settings.
	 * @param id the block type identifier
	 * @return the new block instance 
	 */
	Block<T, X, C> create(@NonNull String id);
	/** 
	 * Returns a list of supported block ids.
	 * @return the list of supported block ids 
	 */
	List<String> blocks();
}
