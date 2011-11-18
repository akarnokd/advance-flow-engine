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

package eu.advance.logistics.flow.engine.runtime;

import hu.akarnokd.reactive4java.base.Scheduler;

import java.util.Map;

import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockReference;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;

/**
 * The record for initializing a block.
 * @author akarnokd, 2011.11.08.
 * @param <T> the runtime flow data type
 * @param <C> the runtime context
 */
public class BlockSettings<T, C> {
	/** The block ID. */
	public String id;
	/** The available schedulers. */
	public Map<SchedulerPreference, Scheduler> schedulers;
	/** The parent composite block in the flow. */
	public AdvanceCompositeBlock parent;
	/** The block description from the block registry. */
	public BlockRegistryEntry description;
	/** The block reference as specified in the flow. */
	public AdvanceBlockReference instance;
	/** Constant parameters. */
	public Map<String, T> constantValues;
	/** The runtime context. */
	public C context;
	/** The realm where this block runs. */
	public String realm;
	/** The data resolver. */
	public DataResolver<T> resolver;
	/** Default constructor. */
	public BlockSettings() {
		
	}
	/**
	 * Copy constructor.
	 * @param other the other settings
	 */
	public BlockSettings(BlockSettings<T, C> other) {
		this.id = other.id;
		this.schedulers = other.schedulers;
		this.parent = other.parent;
		this.description = other.description;
		this.context = other.context;
	}
	/** @return The preferred scheduler. */
	public SchedulerPreference preferredScheduler() {
		return description.scheduler;
	}
}
