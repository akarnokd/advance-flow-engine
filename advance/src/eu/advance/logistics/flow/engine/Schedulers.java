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

package eu.advance.logistics.flow.engine;

import hu.akarnokd.reactive4java.base.Scheduler;
import hu.akarnokd.reactive4java.reactive.Reactive;
import hu.akarnokd.reactive4java.util.DefaultScheduler;

import java.io.Closeable;
import java.util.EnumMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.util.ReactiveEx;

/**
 * Container for various schedulers used to run the ADVANCE block bodies.
 * @author karnokd, 2011.06.30.
 */
public class Schedulers {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(Schedulers.class);
	/** The scheduler mappings. */
	final EnumMap<SchedulerPreference, Scheduler> map;
	/**
	 * Initialize the various schedulers.
	 */
	public Schedulers() {
		map = new EnumMap<SchedulerPreference, Scheduler>(SchedulerPreference.class);

		// --------------------------------------------------------------
		// use or create a CPU-count sized scheduler
		if (Reactive.getDefaultScheduler().getClass().equals(DefaultScheduler.class)) {
			map.put(SchedulerPreference.CPU, Reactive.getDefaultScheduler());
		} else {
			map.put(SchedulerPreference.CPU, new DefaultScheduler());
		}
		
		// --------------------------------------------------------------
		// create a single thread pool
		ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
		scheduler.setKeepAliveTime(1, TimeUnit.SECONDS);
		scheduler.allowCoreThreadTimeOut(true);
		
		try {
			java.lang.reflect.Method m = scheduler.getClass().getMethod("setRemoveOnCancelPolicy", Boolean.TYPE);
			m.invoke(scheduler, true);
		} catch (java.lang.reflect.InvocationTargetException ex) {
			
		} catch (NoSuchMethodException e) {
		} catch (SecurityException e) {
		} catch (IllegalAccessException e) {
		} catch (IllegalArgumentException e) {
		}
		map.put(SchedulerPreference.SEQUENTIAL, new DefaultScheduler(scheduler));

		// ------------------------------------------------------
		// Create a large pool
		
		scheduler = new ScheduledThreadPoolExecutor(0);
		scheduler.setMaximumPoolSize(1024); // FIXME how big?
		scheduler.setKeepAliveTime(1, TimeUnit.SECONDS);
		scheduler.allowCoreThreadTimeOut(true);
		
		try {
			java.lang.reflect.Method m = scheduler.getClass().getMethod("setRemoveOnCancelPolicy", Boolean.TYPE);
			m.invoke(scheduler, true);
		} catch (java.lang.reflect.InvocationTargetException ex) {
			
		} catch (NoSuchMethodException e) {
		} catch (SecurityException e) {
		} catch (IllegalAccessException e) {
		} catch (IllegalArgumentException e) {
		}
		map.put(SchedulerPreference.IO, new DefaultScheduler(scheduler));

		// ------------------------------------------------------
		// Create the current thread scheduler
		map.put(SchedulerPreference.NOW, new Scheduler() {

			@Override
			public Closeable schedule(Runnable run) {
				run.run();
				return ReactiveEx.emptyCloseable();
			}

			@Override
			public Closeable schedule(Runnable run, long delay, TimeUnit unit) {
				try {
					unit.sleep(delay);
					run.run();
				} catch (InterruptedException ex) {
					LOG.info(ex.toString(), ex);
				}
				return ReactiveEx.emptyCloseable();
			}

			@Override
			public Closeable schedule(Runnable run, long initialDelay,
					long betweenDelay, TimeUnit unit) {
				try {
					unit.sleep(initialDelay);
					while (!Thread.currentThread().isInterrupted()) {
						run.run();
						unit.sleep(betweenDelay);
					}
				} catch (InterruptedException ex) {
					LOG.info(ex.toString(), ex);
				} catch (CancellationException ex) {
					LOG.debug(ex.toString(), ex);
				}
				return ReactiveEx.emptyCloseable();
			}
			
		});
	}
	/**
	 * Get a specific scheduler.
	 * @param pref the preferred scheduler type
	 * @return the scheduler
	 */
	@NonNull
	public Scheduler get(@NonNull SchedulerPreference pref) {
		return map.get(pref);
	}
}
