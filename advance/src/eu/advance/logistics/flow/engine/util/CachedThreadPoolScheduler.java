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

package eu.advance.logistics.flow.engine.util;

import hu.akarnokd.reactive4java.base.Scheduler;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * The scheduler uses a cached thread pool executor (via {@link Executors#newCachedThreadPool()}) as its backing thread pool.
 * Delayed executions are performed via the TimeUnit.sleep() methods on the pool thread (consuming resources).
 * To stop a repeating schedule, throw a CancellationException.
 * @author akarnokd, 2011.10.05.
 */
public class CachedThreadPoolScheduler implements Scheduler {
	/** The thread pool. */
	protected final ExecutorService pool;
	/**
	 * Constructor. Initializes the backing thread pool
	 */
	public CachedThreadPoolScheduler() {
		pool = Executors.newCachedThreadPool();
	}
	@Override
	public Closeable schedule(Runnable run) {
		final Future<?> f = pool.submit(run);
		return new Closeable() {
			@Override
			public void close() throws IOException {
				f.cancel(true);
			}
		};
	}

	@Override
	public Closeable schedule(final Runnable run, final long delay, final TimeUnit unit) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					unit.sleep(delay);
					run.run();
				} catch (InterruptedException ex) {
					// ignore and quit
				} catch (CancellationException ex) {
					// ignored
				}
			}
		};
		return schedule(task);
	}

	@Override
	public Closeable schedule(final Runnable run, final long initialDelay,
			final long betweenDelay, final TimeUnit unit) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					unit.sleep(initialDelay);
					while (!Thread.currentThread().isInterrupted()) {
						run.run();
						unit.sleep(betweenDelay);
					}
				} catch (InterruptedException ex) {
					// ignore and quit
				}
			}
		};
		return schedule(task);
	}

}
