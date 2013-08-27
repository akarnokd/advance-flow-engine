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
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.advance.logistics.flow.engine.runtime.Block;
import eu.advance.logistics.flow.engine.runtime.Port;

/**
 * Represents the runtime values of a realm.
 * @author karnokd, 2012.03.12.
 * @param <T> the runtime type of the dataflow
 * @param <X> the type system type
 * @param <C> the runtime context
 */
public class AdvanceRealmRuntime<T, X, C> {
	/** The runtime blocks. */
	public final List<Block<T, X, C>> blocks = Lists.newArrayList();
	/** The map of global input ports. */
	public final Map<String, List<Port<T, X>>> inputs = Maps.newHashMap();
	/** The map which specifies the type of the input (either the inferred or declared). */
	public final Map<String, X> inputTypes = Maps.newHashMap();
	/** The map of global output ports. */
	public final Map<String, Port<T, X>> outputs = Maps.newHashMap();
	/** The map which specifies the type of the output (either the inferred or declared). */
	public final Map<String, X> outputTypes = Maps.newHashMap();
}
