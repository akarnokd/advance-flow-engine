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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A scheduler which creates separate new threads for each task.
 * To stop a repeating schedule, throw a CancellationException.
 * @author akarnokd, 2011.10.05.
 */
public class NewThreadScheduler implements Scheduler {
	/** The tread names. */
	@NonNull
	private String name = "NewThreadScheduler";
	/** Counts the new threads started. */
	private final AtomicInteger counter = new AtomicInteger();
	/**
	 * Constructor. The threads are named as {@code NewThreadScheduler-#}
	 */
	public NewThreadScheduler() {
	}
	/**
	 * Constructor.
	 * @param name the name prefix used when creating new threads.
	 */
	public NewThreadScheduler(@NonNull String name) {
		this.name = name;
	}
	@Override
	public Closeable schedule(Runnable run) {
		final Thread t = new Thread(run, name + "-" + counter.incrementAndGet());
		t.start();
		return new Closeable() {
			@Override
			public void close() throws IOException {
				t.interrupt();
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
