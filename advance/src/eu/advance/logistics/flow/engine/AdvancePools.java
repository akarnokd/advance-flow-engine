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

import hu.akarnokd.reactive4java.base.Func2;
import hu.akarnokd.reactive4java.base.MultiIOException;
import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.utils.pool.Pool;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * A common pool for various communication pools.
 * @author akarnokd, 2011.11.09.
 */
public class AdvancePools implements Closeable {
	/** The available pools. */
	final Map<Class<?>, Map<String, Pool<?>>> pools = Maps.newHashMap();
	/**
	 * Create a pool for the supplied class and configuration id or null if no such pool can be created.
	 */
	final Func2<Class<?>, String, Option<? extends Pool<?>>> creator;
	/**
	 * Constructor.
	 * @param creator the function that can create a connection pool to the supplied class and configuration id.
	 */
	public AdvancePools(Func2<Class<?>, String, Option<? extends Pool<?>>> creator) {
		this.creator = creator;
	}
	/**
	 * Retrieve a pool with the given class and configuration id.
	 * @param <T> the pooled object type
	 * @param clazz the class selecting the pooled object type
	 * @param id the configuration id
	 * @return the pool or null if no such pool can be created
	 * @throws Exception if the pool initialization threw an exception
	 */
	@Nullable
	public <T> Pool<T> get(@NonNull Class<T> clazz, @NonNull String id) throws Exception {
		synchronized (pools) {
			Map<String, Pool<?>> poolMap = pools.get(clazz);
			if (poolMap == null) {
				poolMap = Maps.newHashMap();
				pools.put(clazz, poolMap);
			}
			Pool<?> pool = poolMap.get(id);
			if (pool == null) {
				Option<? extends Pool<?>> poolOpt = creator.invoke(clazz, id);
				if (Option.isError(poolOpt)) {
					Throwable ex = Option.getError(poolOpt);
					if (ex instanceof Exception) {
						throw (Exception)ex;
					}
					throw new Exception(ex);
				} else
				if (Option.isNone(poolOpt)) {
					return null;
				}
				pool = poolOpt.value();
				poolMap.put(id, pool);
			}
			@SuppressWarnings("unchecked") Pool<T> poolT = (Pool<T>)pool;
			return poolT;
		}
	}
	@Override
	public void close() throws IOException {
		MultiIOException exc = null;
		synchronized (pools) {
			for (Map<String, Pool<?>> e : pools.values()) {
				for (Pool<?> p : e.values()) {
					try {
						p.close();
					} catch (IOException ex) {
						exc = MultiIOException.createOrAdd(exc, ex);
					}
				}
			}
			pools.clear();
		}
		if (exc != null) {
			throw exc;
		}
	}
}
