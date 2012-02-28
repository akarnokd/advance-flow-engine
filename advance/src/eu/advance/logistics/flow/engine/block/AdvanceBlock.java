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

package eu.advance.logistics.flow.engine.block;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.api.core.Pool;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.runtime.Block;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * The generic ADVANCE block.
 * @author akarnokd, 2011.06.22.
 */
public abstract class AdvanceBlock extends Block<XElement, AdvanceType, AdvanceRuntimeContext> {
	/**
	 * Convenience method to ask for a specific type of pooled objects.
	 * @param <T> the pooled object type
	 * @param clazz the class of the object
	 * @param id the identifier within the pooled objects
	 * @return the pool or null if no such pool exists
	 * @throws Exception if the pool initialization fails
	 */
	@Nullable
	public <T> Pool<T> getPool(@NonNull Class<T> clazz, @NonNull String id) throws Exception {
		return this.settings.context.pools.get(clazz, id);
	}
}
