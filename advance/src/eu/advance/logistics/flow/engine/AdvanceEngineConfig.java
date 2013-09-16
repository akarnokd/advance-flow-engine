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

import hu.akarnokd.reactive4java.base.Scheduler;
import hu.akarnokd.reactive4java.scheduler.DefaultScheduler;
import hu.akarnokd.reactive4java.util.Closeables;
import hu.akarnokd.utils.crypto.KeystoreManager;
import hu.akarnokd.utils.xml.XNElement;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import eu.advance.logistics.flow.engine.api.BoundedPool;
import eu.advance.logistics.flow.engine.api.ds.AdvanceCreateModifyInfo;
import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.comm.JDBCConnection;
import eu.advance.logistics.flow.engine.comm.JDBCPoolManager;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.runtime.BlockRegistryEntry;
import eu.advance.logistics.flow.engine.runtime.SchedulerPreference;
import eu.advance.logistics.flow.engine.runtime.SchedulerPriority;

/**
 * The engine configuration record.
 * @author akarnokd, 2011.09.23.
 */
public class AdvanceEngineConfig {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceEngineConfig.class);
	/** The listener configuration. */
	public AdvanceListenerConfig listener;
	/** The block registry. */
	public String blockRegistry;
	/** The block resolver. */
	public Map<String, AdvanceBlockResolver<XNElement, AdvanceType, AdvanceRuntimeContext>> defaultBlocks;
	/** The schema directories. */
	public final List<String> schemas = Lists.newArrayList();
	/** A JDBC based datastore datasource. */
	private AdvanceJDBCDataSource jdbcDataSource;
	/** The local datastore object. */
	private LocalDataStore localDataStore;
	/** The JDBC data store. */
	private JDBCDataStore jdbcDataStore;
	/** The JDBC pool. */
	protected BoundedPool<JDBCConnection> jdbcPool;
	/** The local keystores. */
	public final Map<String, AdvanceKeyStore> keystores = Maps.newHashMap();
	/** The scheduler mappings. */
	public final EnumMap<SchedulerPreference, Scheduler> schedulerMap = new EnumMap<SchedulerPreference, Scheduler>(SchedulerPreference.class);
	/** The backing executor services to allow peaceful shutdown. */
	public final EnumMap<SchedulerPreference, ExecutorService> schedulerMapExecutors = new EnumMap<SchedulerPreference, ExecutorService>(SchedulerPreference.class);
	/** The working directory. */
	protected String workDir;
	/** The connection pool managers. */
	protected AdvancePools pools;
	/** The plugin manager. */
	protected AdvancePluginManager<XNElement, AdvanceType, AdvanceRuntimeContext> pluginManager;
	/** The plugin manager closeable. */
	protected Closeable pluginManagerCloseable;
	/**
	 * Create the lookup.
	 * @param blockRegistries The block registries
	 */
	protected void initBlockRegistry(Iterable<XNElement> blockRegistries) {
		Map<String, BlockRegistryEntry> blocks = Maps.newHashMap();
		try {
			InputStream in = getClass().getResourceAsStream("/block-registry.xml");
			try {
				for (BlockRegistryEntry e : BlockRegistryEntry.parseRegistry(
						XNElement.parseXML(in))) {
					blocks.put(e.id, e);
				}
			} finally {
				in.close();
			}
			for (XNElement br : blockRegistries) {
				for (BlockRegistryEntry e : BlockRegistryEntry.parseRegistry(
						XNElement.parseXML(workDir + "/" + br.get("file")))) {
					blocks.put(e.id, e);
				}
			}
		} catch (XMLStreamException ex) {
			throw new IllegalArgumentException(ex);
		} catch (IOException ex) {
			throw new IllegalArgumentException(ex);
		}
		AdvanceDefaultBlockResolver<XNElement, AdvanceType, AdvanceRuntimeContext> br = 
				new AdvanceDefaultBlockResolver<XNElement, AdvanceType, AdvanceRuntimeContext>(blocks);
		defaultBlocks = Maps.newHashMap();
		for (String blockId : blocks.keySet()) {
			defaultBlocks.put(blockId, br);
		}
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
	 * @param workDir the working directory to resolve relative configuration entries
	 */
	public void initialize(XNElement configXML, String workDir) {
		this.workDir = workDir;
		// load listeners
		listener = new AdvanceListenerConfig();
		listener.load(configXML.childElement("listener"));

		initBlockRegistry(configXML.childrenWithName("block-registry"));
		// initialize schedulers
		schedulerMap.clear();
		schedulerMapExecutors.clear();
		initSchedulers(configXML.childrenWithName("scheduler"));
		// load schema locations
		schemas.clear();
		for (XNElement xs : configXML.childrenWithName("schemas")) {
			schemas.add(workDir + "/" + xs.get("location"));
		}
		
		// initialize keystores
		for (XNElement xks : configXML.childrenWithName("keystore")) {
			AdvanceKeyStore e = new AdvanceKeyStore();
			e.name = xks.get("name");
			e.location = workDir + "/" + xks.get("file");

			e.password(AdvanceCreateModifyInfo.getPassword(xks, "password"));
			
			// initialize keystore if nonexistent
			File f = new File(e.location);
			if (!f.canRead()) {
				KeystoreManager mgr = new KeystoreManager();
				mgr.create();
				mgr.save(e.location, e.password());
			}
			
			e.createdAt = new Date(f.lastModified());
			e.createdBy = "?";
			e.modifiedAt = new Date(f.lastModified());
			e.modifiedBy = "?";
			keystores.put(e.name, e);
		}
		// initialize datastore
		XNElement ds = configXML.childElement("datastore");
		jdbcDataSource = new AdvanceJDBCDataSource();
		
		jdbcDataSource.driver = ds.get("driver");
		jdbcDataSource.url = ds.get("url");
		jdbcDataSource.password(AdvanceCreateModifyInfo.getPassword(ds, "password"));
		
		if ("LOCAL".equals(jdbcDataSource.driver)) {
			localDataStore = new LocalDataStore(workDir);
			if (jdbcDataSource.password() != null) {
				localDataStore.loadEncrypted(workDir + "/" + jdbcDataSource.url, jdbcDataSource.password());
			} else {
				localDataStore.load(workDir + "/" + jdbcDataSource.url);
			}
		} else {
			jdbcDataSource.user = ds.get("user");
			jdbcDataSource.schema = ds.get("schema");
			jdbcDataSource.poolSize = ds.getInt("poolsize");
			
			jdbcPool = new BoundedPool<JDBCConnection>(jdbcDataSource.poolSize, 
					new JDBCPoolManager(jdbcDataSource));
			jdbcDataStore = new JDBCDataStore(null, jdbcPool); // TODO JDBC update
		}
		pools = new AdvancePools(new AdvancePoolCreator(datastore()));
		
		String pluginDir = workDir + "/plugins"; 
		XNElement xpluginDir = configXML.childElement("plugins");
		if (xpluginDir != null) {
			pluginDir = workDir + "/" + xpluginDir.content;
		}
		pluginManager = new AdvancePluginManager<XNElement, AdvanceType, AdvanceRuntimeContext>(pluginDir);
		pluginManager.run();
		pluginManagerCloseable = get(SchedulerPreference.IO).schedule(pluginManager, 5, 5, TimeUnit.SECONDS);
	}
	/**
	 * Terminate and close everything.
	 */
	public void close() {
		Closeables.closeSilently(pluginManagerCloseable);
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
		if (localDataStore != null) {
			if (jdbcDataSource.password() != null) {
				localDataStore.saveEncrypted(workDir + "/" + jdbcDataSource.url, jdbcDataSource.password());
			} else {
				localDataStore.save(workDir + "/" + jdbcDataSource.url);
			}
		} else
		if (jdbcPool != null) {
			try {
				jdbcPool.close();
			} catch (IOException ex) {
				LOG.error(ex.toString(), ex);
			}
		}
		pluginManager = null;
	}
	/**
	 * Initialize the schedulers from the configuration.
	 * @param schedulerConfigs the sequence of configurations
	 */
	protected void initSchedulers(Iterable<XNElement> schedulerConfigs) {
		initNowScheduler();
		for (XNElement sc : schedulerConfigs) {
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
		if (priority.length() > 0 && Character.isLetter(priority.charAt(0))) {
			SchedulerPriority ep = SchedulerPriority.valueOf(priority);
			p = ep.priority;
		} else {
			// priority in percent
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
				return Closeables.emptyCloseable();
			}

			@Override
			public Closeable schedule(Runnable run, long delay, TimeUnit unit) {
				try {
					unit.sleep(delay);
					run.run();
				} catch (InterruptedException ex) {
					LOG.info(ex.toString(), ex);
				}
				return Closeables.emptyCloseable();
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
				return Closeables.emptyCloseable();
			}
			
		});
	}
	/** @return the datastore object. */
	public AdvanceDataStore datastore() {
		return localDataStore != null ? localDataStore : jdbcDataStore;
	}
	/**
	 * Returns the global connection pool manager.
	 * @return the connection pool manager.
	 */
	public AdvancePools pools() {
		return pools;
	}
	/**
	 * Creates a compiler settings based on this configuration.
	 * @return a compiler settings based on this configuration.
	 */
	public AdvanceCompilerSettings<XNElement, AdvanceType, AdvanceRuntimeContext> createCompilerSettings() {
		AdvanceCompilerSettings<XNElement, AdvanceType, AdvanceRuntimeContext> compilerSettings = 
				new AdvanceCompilerSettings<XNElement, AdvanceType, AdvanceRuntimeContext>();
		compilerSettings.defaultSchemas = schemas; 
		compilerSettings.defaultBlocks = defaultBlocks; 
		compilerSettings.schedulers = schedulerMap;
		compilerSettings.context = new AdvanceRuntimeContext();
		compilerSettings.context.datastore = datastore();
		compilerSettings.context.pools = pools();
		compilerSettings.pluginManager = pluginManager;
		compilerSettings.resolver = new AdvanceData();
		compilerSettings.typeFunctions = new AdvanceTypeFunctions();
		return compilerSettings;
	}
}
