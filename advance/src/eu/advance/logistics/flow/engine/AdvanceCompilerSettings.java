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

import hu.akarnokd.reactive4java.base.Scheduler;

import java.util.List;
import java.util.Map;

import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.ds.AdvancePools;
import eu.advance.logistics.flow.engine.model.rt.AdvanceSchedulerPreference;

/**
 * @author akarnokd, 2011.11.08.
 *
 */
public class AdvanceCompilerSettings {
	/** The default schema locations. */
	public List<String> defaultSchemas;
	/** The default blocks. */
	public Map<String, AdvanceBlockResolver> defaultBlocks;
	/** The map of various schedulers. */
	public Map<AdvanceSchedulerPreference, Scheduler> schedulers;
	/** The unchecked datastore. */
	public AdvanceDataStore datastore;
	/** The connection pools manager. */
	public AdvancePools pools;
	/** The plugin manager. */
	public AdvancePluginManager pluginManager;
}
