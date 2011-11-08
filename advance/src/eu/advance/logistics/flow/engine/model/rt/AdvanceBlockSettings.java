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

package eu.advance.logistics.flow.engine.model.rt;

import hu.akarnokd.reactive4java.base.Scheduler;

import java.util.Map;

import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;

/**
 * The record for initializing a block.
 * @author akarnokd, 2011.11.08.
 */
public class AdvanceBlockSettings {
	/** The block ID. */
	public String id;
	/** The available schedulers. */
	public Map<AdvanceSchedulerPreference, Scheduler> schedulers;
	/** The parent composite block in the flow. */
	public AdvanceCompositeBlock parent;
	/** The block description from the block registry. */
	public AdvanceBlockRegistryEntry description;
	/** Default constructor. */
	public AdvanceBlockSettings() {
		
	}
	/**
	 * Copy constructor.
	 * @param other the other settings
	 */
	public AdvanceBlockSettings(AdvanceBlockSettings other) {
		this.id = other.id;
		this.schedulers = other.schedulers;
		this.parent = other.parent;
		this.description = other.description;
	}
	/** @return The preferred scheduler. */
	public AdvanceSchedulerPreference preferredScheduler() {
		return description.scheduler;
	}
}
