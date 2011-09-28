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
import hu.akarnokd.reactive4java.util.DefaultScheduler;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceCreateModifyInfo;
import eu.advance.logistics.flow.engine.api.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.impl.LocalDataStore;
import eu.advance.logistics.flow.engine.model.AdvanceBlockRegistryEntry;
import eu.advance.logistics.flow.engine.model.SchedulerPreference;
import eu.advance.logistics.flow.engine.util.KeystoreManager;
import eu.advance.logistics.flow.engine.util.ReactiveEx;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * The engine configuration record.
 * @author karnokd, 2011.09.23.
 */
public class AdvanceEngineConfig {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceEngineConfig.class);
	/** The block registry. */
	public String blockRegistry;
	/** The block resolver. */
	public AdvanceBlockResolver blockResolver;
	/** The schema resolver. */
	public AdvanceSchemaResolver schemaResolver;
	/** A JDBC based datastore datasource. */
	protected AdvanceJDBCDataSource jdbcDataStore;
	/** The local datastore object. */
	protected LocalDataStore localDataStore;
	/** The local keystores. */
	public final Map<String, AdvanceKeyStore> keystores = Maps.newHashMap();
	/** The scheduler mappings. */
	protected final EnumMap<SchedulerPreference, Scheduler> schedulerMap = new EnumMap<SchedulerPreference, Scheduler>(SchedulerPreference.class);
	/** The backing executor services to allow peaceful shutdown. */
	protected final EnumMap<SchedulerPreference, ExecutorService> schedulerMapExecutors = new EnumMap<SchedulerPreference, ExecutorService>(SchedulerPreference.class);
	/**
	 * Create the lookup.
	 * @param blockRegistry The block registry to use
	 */
	protected void initBlockRegistry(String blockRegistry) {
		Map<String, AdvanceBlockRegistryEntry> blocks = Maps.newHashMap();
		try {
			for (AdvanceBlockRegistryEntry e : AdvanceBlockRegistryEntry.parseRegistry(
					XElement.parseXML(blockRegistry))) {
				blocks.put(e.id, e);
			}
		} catch (XMLStreamException ex) {
			throw new IllegalArgumentException(ex);
		} catch (IOException ex) {
			throw new IllegalArgumentException(ex);
		}
		blockResolver = new AdvanceBlockResolver(blocks);
	}
	
	/**
	 * Get a specific scheduler.
	 * @param pref the preferred scheduler type
	 * @return the scheduler
	 */
	@NonNull
	public Scheduler get(@NonNull SchedulerPreference pref) {
		return schedulerMap.get(pref);
	}
	/**
	 * Initialize the configuration from the given config XML tree.
	 * @param configXML the configuration tree
	 */
	public void initialize(XElement configXML) {
		// load blocks.
		for (XElement br : configXML.childrenWithName("block-registry")) {
			initBlockRegistry(br.get("file"));
		}
		// initialize schedulers
		schedulerMap.clear();
		schedulerMapExecutors.clear();
		initSchedulers(configXML.childrenWithName("scheduler"));
		// load schema locations
		List<String> schemas = Lists.newArrayList();
		for (XElement xs : configXML.childrenWithName("schemas")) {
			schemas.add(xs.get("location"));
		}
		schemaResolver = new AdvanceSchemaResolver(schemas);
		
		// initialize keystores
		for (XElement xks : configXML.childrenWithName("keystore")) {
			AdvanceKeyStore e = new AdvanceKeyStore();
			e.name = xks.get("name");
			e.location = xks.get("file");

			e.password = AdvanceCreateModifyInfo.getPassword(xks, "password");
			
			// initialize keystore if nonexistent
			File f = new File(e.location);
			if (!f.canRead()) {
				KeystoreManager mgr = new KeystoreManager();
				mgr.create();
				mgr.save(e.location, e.password);
			}
			
			e.createdAt = new Date(f.lastModified());
			e.createdBy = "?";
			e.modifiedAt = new Date(f.lastModified());
			e.modifiedBy = "?";
			keystores.put(e.name, e);
		}
		// initialize datastore
		XElement ds = configXML.childElement("datastore");
		jdbcDataStore = new AdvanceJDBCDataSource();
		
		jdbcDataStore.driver = ds.get("driver");
		jdbcDataStore.url = ds.get("url");
		jdbcDataStore.password = AdvanceCreateModifyInfo.getPassword(ds, "password");
		
		if ("LOCAL".equals(jdbcDataStore.driver)) {
			localDataStore = new LocalDataStore();
			if (jdbcDataStore.password != null) {
				localDataStore.loadEncrypted(jdbcDataStore.url, jdbcDataStore.password);
			} else {
				localDataStore.load(jdbcDataStore.url);
			}
		} else {
			jdbcDataStore.user = ds.get("user");
			jdbcDataStore.schema = ds.get("schema");
			jdbcDataStore.poolSize = ds.getInt("poolsize");
		}
	}
	/**
	 * Terminate and close everything.
	 */
	public void close() {
		if (localDataStore != null) {
			if (jdbcDataStore.password != null) {
				localDataStore.saveEncrypted(jdbcDataStore.url, jdbcDataStore.password);
			} else {
				localDataStore.save(jdbcDataStore.url);
			}
		}
		for (ExecutorService s : schedulerMapExecutors.values()) {
			s.shutdown();
		}
		for (ExecutorService s : schedulerMapExecutors.values()) {
			try {
				s.awaitTermination(0, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ex) {
				LOG.warn(ex.toString(), ex);
			}
		}
		
	}
	/**
	 * Initialize the schedulers from the configuration.
	 * @param schedulerConfigs the sequence of configurations
	 */
	protected void initSchedulers(Iterable<XElement> schedulerConfigs) {
		initNowScheduler();
		for (XElement sc : schedulerConfigs) {
			createScheduler(SchedulerPreference.valueOf(sc.get("type")), sc.get("concurrency"), 
					sc.get("priority"));
		}
	}
	/**
	 * Create a scheduler pool with the given concurrency and priority settings.
	 * @param sp the scheduler preference constant
	 * @param concurrency the concurrency number or ALL_CORES
	 * @param priority the priority percent value (0..100) or IDLE, VERY_LOW, LOW, NORMAL, ABOVE_NORMAL, HIGH, VERY_HIGH, MAX
	 */
	protected void createScheduler(
			SchedulerPreference sp,
			String concurrency, String priority) {
		int n = Runtime.getRuntime().availableProcessors();
		if (!"ALL_CORES".equals(concurrency)) {
			n = Integer.parseInt(concurrency);
		}
		int p = Thread.NORM_PRIORITY;
		if ("IDLE".equals(priority)) {
			p = Thread.MIN_PRIORITY;
		} else
		if ("MAX".equals(priority)) {
			p = Thread.MAX_PRIORITY;
		} else
		if ("VERY_LOW".equals(priority)) {
			p = Thread.MIN_PRIORITY + 1;
		} else
		if ("LOW".equals(priority)) {
			p = Thread.NORM_PRIORITY - 1;
		} else
		if ("NORMAL".equals(priority)) {
			p = Thread.NORM_PRIORITY;
		} else
		if ("ABOVE_NORMAL".equals(priority)) {
			p = Thread.NORM_PRIORITY + 1;
		} else
		if ("HIGH".equals(priority)) {
			p = Thread.MAX_PRIORITY - 2;
		} else
		if ("VERY_HIGH".equals(priority)) {
			p = Thread.MAX_PRIORITY - 1;
		} else {
			p = Thread.MIN_PRIORITY + Integer.parseInt(priority) * (Thread.MAX_PRIORITY - Thread.MIN_PRIORITY) / 100;
		}
		
		final int prio = p;
		
		ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(n, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setPriority(prio);
				return t;
			}
		});
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
		schedulerMap.put(sp, new DefaultScheduler(scheduler));
		schedulerMapExecutors.put(sp, scheduler);
	}
	/**
	 * Create the NOW scheduler.
	 */
	protected void initNowScheduler() {
		// ------------------------------------------------------
		// Create the current thread scheduler
		schedulerMap.put(SchedulerPreference.NOW, new Scheduler() {
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
}
