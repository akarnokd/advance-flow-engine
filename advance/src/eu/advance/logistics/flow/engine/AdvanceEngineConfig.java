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

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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
import eu.advance.logistics.flow.engine.model.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.AdvanceSchemaResolver;
import eu.advance.logistics.flow.engine.model.UnresolvableSchemaURIException;
import eu.advance.logistics.flow.engine.util.KeystoreManager;
import eu.advance.logistics.flow.engine.util.ReactiveEx;
import eu.advance.logistics.flow.engine.xml.typesystem.SchemaParser;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XType;

/**
 * The engine configuration record.
 * @author karnokd, 2011.09.23.
 */
public class AdvanceEngineConfig implements AdvanceSchemaResolver {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceEngineConfig.class);
	/** The block registry. */
	public String blockRegistry;
	/** The block descriptions. */
	protected final Map<String, AdvanceBlockRegistryEntry> blocks = Maps.newHashMap();
	/**
	 * The list of local schema locations.
	 */
	public final List<String> schemas = Lists.newArrayList();
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
	 * Resolve a schema URI link.
	 * @param schemaURI the schema URI.
	 * @return the parsed schema
	 */
	@Override
	public XType resolve(URI schemaURI) {
		String s = schemaURI.getScheme(); 
		if ("advance".equals(s)) {
			String u = schemaURI.getSchemeSpecificPart();
			for (String schemaDir : schemas) {
				File f = new File(schemaDir + "/" + u + ".xsd");
				if (f.exists()) {
					try {
						return resolveSchemaLoad(f.toURI().toURL(), schemaURI);
					} catch (MalformedURLException ex) {
						LOG.error(f.toString(), ex);
						throw new UnresolvableSchemaURIException(schemaURI, ex);
					}
				}
			}
		}
		if ("res".equals(s)) {
			String u = schemaURI.getSchemeSpecificPart();
			if (!u.startsWith("/")) {
				u = "/" + u;
			}
			URL url = getClass().getResource(u);
			if (url != null) {
				return resolveSchemaLoad(url, schemaURI);
			}
		} else
		if ("http".equals(s) || "https".equals(s) || "ftp".equals(s) || "file".equals(s)) {
			try {
				URL url = schemaURI.toURL();
				return resolveSchemaLoad(url, schemaURI);
			} catch (MalformedURLException ex) {
				LOG.error(schemaURI.toString(), ex);
				throw new UnresolvableSchemaURIException(schemaURI, ex);
			}
		}
		LOG.error(schemaURI.toString());
		throw new UnresolvableSchemaURIException(schemaURI);
	}
	/**
	 * Perform the retrieval and parsing of the schema file.
	 * @param url the URL to load from
	 * @param schemaURI the original URI
	 * @return the parsed schema
	 */
	protected XType resolveSchemaLoad(URL url, URI schemaURI) {
		try {
			BufferedInputStream bin = new BufferedInputStream(url.openStream());
			try {
				return SchemaParser.parse(XElement.parseXML(bin), "schemas");
			} finally {
				bin.close();
			}
		} catch (XMLStreamException ex) {
			LOG.error(ex.toString(), ex);
			throw new UnresolvableSchemaURIException(schemaURI, ex);
		} catch (IOException ex) {
			LOG.error(ex.toString(), ex);
			throw new UnresolvableSchemaURIException(schemaURI, ex);
		}
	}
	/**
	 * Create the lookup.
	 * @param blockRegistry The block registry to use
	 */
	public void initBlockRegistry(String blockRegistry) {
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
	}
	/**
	 * Locate the block description in the repository (e.g., block-repository.xml).
	 * @param id the block identifier
	 * @return the block
	 */
	public AdvanceBlockRegistryEntry lookup(@NonNull String id) {
		return blocks.get(id);
	}
	/**
	 * Create a concrete block by using the given settings.
	 * @param gid the global block id
	 * @param parent the parent composite block
	 * @param name the level block identifier
	 * @return the new block instance 
	 */
	public AdvanceBlock create(int gid, AdvanceCompositeBlock parent, String name) {
		AdvanceBlockRegistryEntry e = blocks.get(name);
		try {
			Class<?> clazz = Class.forName(e.clazz);
			if (AdvanceBlock.class.isInstance(clazz)) {
				try {
					Constructor<?> c = clazz.getConstructor(
							Integer.TYPE, 
							AdvanceCompositeBlock.class, 
							String.class, 
							SchedulerPreference.class);
					return AdvanceBlock.class.cast(c.newInstance(gid, parent, name, e.scheduler));
				} catch (NoSuchMethodException ex) {
					LOG.error("Missing constructor of {int, AdvanceCompositeBlock, String, SchedulerPreference}", ex);
				} catch (SecurityException ex) {
					LOG.error(ex.toString(), ex);
				} catch (InstantiationException ex) {
					LOG.error(ex.toString(), ex);
				} catch (IllegalAccessException ex) {
					LOG.error(ex.toString(), ex);
				} catch (IllegalArgumentException ex) {
					LOG.error(ex.toString(), ex);
				} catch (InvocationTargetException ex) {
					LOG.error(ex.toString(), ex);
				}
			} else {
				LOG.error("Block " + name + " of class " + e.clazz + " is not an AdvanceBlock");
			}
		} catch (ClassNotFoundException ex) {
			LOG.error(ex.toString(), ex);
		}
		return null;
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
		blocks.clear();
		for (XElement br : configXML.childrenWithName("block-registry")) {
			initBlockRegistry(br.get("file"));
		}
		// initialize schedulers
		schedulerMap.clear();
		schedulerMapExecutors.clear();
		initSchedulers(configXML.childrenWithName("scheduler"));
		// load schema locations
		schemas.clear();
		for (XElement xs : configXML.childrenWithName("schemas")) {
			schemas.add(xs.get("location"));
		}
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
		jdbcDataStore.load(ds);
		if ("LOCAL".equals(ds.get("driver"))) {
			localDataStore = new LocalDataStore();
			if (jdbcDataStore.password != null) {
				localDataStore.loadEncrypted(jdbcDataStore.url, jdbcDataStore.password);
			} else {
				localDataStore.load(jdbcDataStore.url);
			}
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
