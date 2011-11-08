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

package eu.advance.logistics.flow.engine.comm;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A pool manager which may return an unlimited amount of objects.
 * <p>The pool doesn't keep track of the returned objects.</p>
 * @author akarnokd, 2011.10.06.
 * @param <T> the pooled object type
 */
public class UnlimitedPool<T> implements Pool<T> {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(UnlimitedPool.class);
	/** The pool manager object. */
	protected final PoolManager<T> manager;
	/**
	 * Constructor. Initializes the pool manager.
	 * @param manager the pool manager
	 */
	public UnlimitedPool(PoolManager<T> manager) {
		this.manager = manager;
	}
	@Override
	public void close() throws IOException {
		// NO op
	}

	@Override
	public T get() throws Exception {
		return manager.create();
	}

	@Override
	public void put(T obj) {
		try {
			manager.close(obj);
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
		}
	}

}
