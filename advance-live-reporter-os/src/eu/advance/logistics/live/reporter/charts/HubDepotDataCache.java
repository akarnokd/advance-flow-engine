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

package eu.advance.logistics.live.reporter.charts;

import eu.advance.logistics.live.reporter.db.ConsignmentDB;
import eu.advance.logistics.live.reporter.model.ConsignmentSummary;
import eu.advance.logistics.live.reporter.model.ServiceLevel;
import gnu.trove.TLongCollection;
import gnu.trove.map.TLongDoubleMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongDoubleHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongDoubleProcedure;
import gnu.trove.procedure.TLongProcedure;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import org.joda.time.ReadableDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Callables;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

/**
 * Class that caches hub and depot info and periodically
 * refreshes them.
 * @author akarnokd, 2013.05.07.
 */
public class HubDepotDataCache {
	/** The summary for hubs and dates. */
	protected final LoadingCache<List<Object>, TLongObjectMap<ConsignmentSummary>> summaryCache;
	/** The summary for hubs, depots and dates. */
	protected final LoadingCache<List<Object>, TLongObjectMap<ConsignmentSummary>> summaryDepotCache;
	/** The warehouse items cache. */
	protected final LoadingCache<List<Object>, TLongObjectMap<EnumMap<ServiceLevel, TLongSet>>> warehouseItemsCache;
	/** The item floorspace cache. */
	protected final Cache<Long, Double> itemFloorspaceCache;
	/** The refresh executor. */
	protected final ExecutorService refresh;
	/** The logger. */
	protected static final Logger LOGGER = LoggerFactory.getLogger(HubDepotDataCache.class);
	/**
	 * The asynchronous cache loader.
	 * @author karnokd, 2013.05.07.
	 *
	 * @param <T> the result type
	 */
	public abstract static class AsyncCacheLoader<K, T> extends CacheLoader<K, T> {
		/** The refresh executor. */
		protected final ExecutorService refresh;
		/**
		 * Constructor.
		 * @param refresh the refresh executor
		 */
		public AsyncCacheLoader(ExecutorService refresh) {
			this.refresh = refresh;
		}
		@Override
		@GwtIncompatible("Futures")
		public ListenableFuture<T> reload(
				final K key,
				T oldValue)
				throws Exception {
			ListenableFutureTask<T> task = ListenableFutureTask.create(new
					Callable<T>() {
				@Override
				public T call()
						throws Exception {
					return load(key);
				}
			});
			refresh.execute(task);
			return task;
		}
	}
	/**
	 * Constructor, sets up the timer.
	 */
	public HubDepotDataCache() {
		
		ThreadPoolExecutor exec = new ThreadPoolExecutor(0, 1, 2, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		exec.allowCoreThreadTimeOut(true);
		refresh = exec;
		
		summaryCache = CacheBuilder.newBuilder()
				.maximumSize(30)
				.refreshAfterWrite(30, TimeUnit.SECONDS)
				.build(new AsyncCacheLoader<List<Object>, TLongObjectMap<ConsignmentSummary>>(refresh) {
					@Override
					public TLongObjectMap<ConsignmentSummary> load(
							List<Object> key) throws Exception {
						TLongObjectMap<ConsignmentSummary> consignmentStatus = ConsignmentDB.consignmentStatus((Long)key.get(0), 
								null, null, (ReadableDateTime)key.get(1));
						return consignmentStatus;
					}
				});
		summaryDepotCache = CacheBuilder.newBuilder()
				.maximumSize(200)
				.refreshAfterWrite(30, TimeUnit.SECONDS)
				.build(new AsyncCacheLoader<List<Object>, TLongObjectMap<ConsignmentSummary>>(refresh) {
					@Override
					public TLongObjectMap<ConsignmentSummary> load(
							List<Object> key) throws Exception {
						TLongSet depotsIn = (TLongSet)key.get(1);
						TLongSet depotsOut = (TLongSet)key.get(2);
						TLongObjectMap<ConsignmentSummary> consignmentStatus = ConsignmentDB.consignmentStatus(
								(Long)key.get(0), 
								depotsIn, depotsOut, (ReadableDateTime)key.get(3));
						return consignmentStatus;
					}
				});
		warehouseItemsCache = CacheBuilder.newBuilder()
				.maximumSize(200)
				.refreshAfterWrite(30, TimeUnit.SECONDS)
				.build(new AsyncCacheLoader<List<Object>, TLongObjectMap<EnumMap<ServiceLevel, TLongSet>>>(refresh) {
					@Override
					public TLongObjectMap<EnumMap<ServiceLevel, TLongSet>> load(
							List<Object> key) throws Exception {
						TLongObjectMap<EnumMap<ServiceLevel, TLongSet>> result = new TLongObjectHashMap<>();
						long hubId = (Long)key.get(0);
						String sname = (String)key.get(1);
						ReadableDateTime when = (ReadableDateTime)key.get(2);
						ExecutorService exec = Executors.newCachedThreadPool();
						ConsignmentDB.itemsInWarehouse(hubId, sname, when, result, exec);
						exec.shutdown();
						return result;
					}
				});
		itemFloorspaceCache = CacheBuilder.newBuilder()
				.maximumSize(50000)
				.build();
	}
//	/**
//	 * Creates or returns the single per application
//	 * cache instance.
//	 * @param sr the servlet request object
//	 * @return the hub-depot cache
//	 */
//	public static HubDepotDataCache get(HttpServletRequest sr) {
//		ServletContext ctx = sr.getSession().getServletContext();
//		return get(ctx);
//	}
	/**
	 * Creates or returns the single per application
	 * cache instance.
	 * @param ctx the servlet context object
	 * @return the hub-depot cache
	 */
	public static HubDepotDataCache get(ServletContext ctx) {
		synchronized (ctx) {
			HubDepotDataCache result = (HubDepotDataCache)ctx.getAttribute("HUB_DEPOT_DATA_CACHE");
			if (result == null) {
				result = new HubDepotDataCache();
				ctx.setAttribute("HUB_DEPOT_DATA_CACHE", result);
			}
			return result;
		}
	}
	/**
	 * Retrieves the hub status.
	 * @param hub the hub id
	 * @param when the target date
	 * @return the status map
	 */
	public TLongObjectMap<ConsignmentSummary> getHubStatus(long hub, ReadableDateTime when) {
		try {
			return summaryCache.get(Arrays.<Object>asList(hub, when));
		} catch (ExecutionException ex) {
			ex.printStackTrace();
			return new TLongObjectHashMap<ConsignmentSummary>();
		}
	}
	/**
	 * Returns the depot status.
	 * @param hub the hub
	 * @param depotsIn the set of depots of source
	 * @param depotsOut the set of depots of destination
	 * @param when the target datetime
	 * @return the target
	 */
	public TLongObjectMap<ConsignmentSummary> getHubDepotStatus(long hub, 
			TLongSet depotsIn, TLongSet depotsOut, ReadableDateTime when) {
		try {
			return summaryDepotCache.get(Arrays.<Object>asList(hub, depotsIn, depotsOut, when));
		} catch (ExecutionException ex) {
			ex.printStackTrace();
			return new TLongObjectHashMap<>();
		}
	}
	/**
	 * Returns the items in the given hub and warehouse at the requested time.
	 * @param hub the hub
	 * @param warehouse the warehouse name
	 * @param when the date time
	 * @return the warehouse contents
	 */
	public TLongObjectMap<EnumMap<ServiceLevel, TLongSet>> getItemsInWarehouse(long hub, String warehouse,
			ReadableDateTime when) {
		try {
			return warehouseItemsCache.get(Arrays.<Object>asList(hub, warehouse, when));
		} catch (ExecutionException ex) {
			ex.printStackTrace();
			return new TLongObjectHashMap<>();
		}
	}
	/**
	 * Returns a map of item floorspace for the given set of item ids.
	 * @param itemIds the item ids
	 * @return the map
	 */
	public TLongDoubleMap getItemFloorspace(TLongCollection itemIds) {
		final TLongDoubleMap map = new TLongDoubleHashMap();
		
		final TLongSet missing = new TLongHashSet();
		
		itemIds.forEach(new TLongProcedure() {
			@Override
			public boolean execute(long value) {
				try {
					Double v = itemFloorspaceCache.get(value, Callables.returning(Double.NaN));
					if (v.isNaN()) {
						missing.add(value);
					} else {
						map.put(value, v);
					}
				} catch (ExecutionException ex) {
					missing.add(value);
				}
				return true;
			}
		});
		
		if (!missing.isEmpty()) {
			LOGGER.debug("Fetching uncached floorspace for items: " + missing.size());
			TLongDoubleMap fetch = ConsignmentDB.itemFloorspace(missing);
			fetch.forEachEntry(new TLongDoubleProcedure() {
				@Override
				public boolean execute(long a, double b) {
					itemFloorspaceCache.put(a, b);
					return true;
				}
			});
			
			map.putAll(fetch);
		}
		
		
		return map;
	}
}
