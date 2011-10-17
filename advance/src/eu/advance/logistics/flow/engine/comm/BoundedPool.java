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

package eu.advance.logistics.flow.engine.comm;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.concurrent.GuardedBy;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A bounded object pool. 
 * @author akarnokd, 2011.10.06.
 * @param <T> the pooled object type
 */
public class BoundedPool<T> implements Pool<T> {
	/** The available pool of objects. */
	protected final BlockingQueue<T> objects;
	/** The storage of all created objects. */
	@GuardedBy("allObjects")
	protected final Set<T> allObjects = Sets.newHashSet();
	/** The target pool size. */
	protected final int poolSize;
	/** The manager of the pool objects. */
	protected final PoolManager<T> manager;
	/**
	 * Creates a pool with the given size.
	 * @param poolSize the pool size
	 * @param manager the pool manager
	 */
	public BoundedPool(int poolSize, 
			PoolManager<T> manager) {
		this.poolSize = poolSize;
		this.manager = manager;
		
		objects = new LinkedBlockingQueue<T>(poolSize);
	}
	@Override
	public T get() throws Exception {
		synchronized (allObjects) {
			if (allObjects.size() < poolSize) {
				T obj = manager.create();
				allObjects.add(obj);
				return obj;
			}
		}
		T obj = objects.take();
		if (manager.verify(obj)) {
			return obj;
		}
		// create a new one instead
		synchronized (allObjects) {
			allObjects.remove(obj);
			obj = manager.create();
			allObjects.add(obj);
			return obj;
		}
	}
	/**
	 * Return a pool object.
	 * @param obj the object to return
	 */
	@Override
	public void put(T obj) {
		synchronized (allObjects) {
			if (!allObjects.contains(obj)) {
				throw new IllegalArgumentException("obj is not managed by this pool");
			}			
		}
		objects.add(obj);
	}
	@Override
	public void close() throws IOException {
		List<Exception> exc = Lists.newArrayList();
		synchronized (allObjects) {
			for (T obj : allObjects) {
				try {
					manager.close(obj);
				} catch (Exception ex) {
					exc.add(ex);
				}
			}
			allObjects.clear();
		}
		if (exc.size() > 0) {
			throw new MultiIOException(exc);
		}
	}
}
