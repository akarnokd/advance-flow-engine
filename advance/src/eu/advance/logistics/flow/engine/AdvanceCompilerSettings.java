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

import eu.advance.logistics.flow.engine.inference.Type;
import eu.advance.logistics.flow.engine.inference.TypeFunctions;
import eu.advance.logistics.flow.engine.runtime.DataResolver;
import eu.advance.logistics.flow.engine.runtime.SchedulerPreference;

/**
 * The compiler settings.
 * @author akarnokd, 2011.11.08.
 * @param <T> the runtime type of the dataflow
 * @param <X> the type system type
 * @param <C> the context object to pass into the blocks
 */
public class AdvanceCompilerSettings<T, X extends Type, C> {
	/** The default schema locations. */
	public List<String> defaultSchemas;
	/** The default blocks. */
	public Map<String, AdvanceBlockResolver<T, X, C>> defaultBlocks;
	/** The map of various schedulers. */
	public Map<SchedulerPreference, Scheduler> schedulers;
	/** The plugin manager. */
	public AdvancePluginManager<T, X, C> pluginManager;
	/** The context object. */
	public C context;
	/** The data resolver. */
	public DataResolver<T> resolver;
	/** The type functions. */
	public TypeFunctions<X> typeFunctions;
}
