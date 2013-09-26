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

import eu.advance.logistics.live.reporter.db.MLDB;
import eu.advance.logistics.live.reporter.db.MasterDB;
import eu.advance.logistics.live.reporter.model.ConsignmentSummary;
import eu.advance.logistics.live.reporter.model.ItemStatus;
import eu.advance.logistics.live.reporter.model.MlPrediction;
import eu.advance.logistics.live.reporter.model.ServiceLevel;
import eu.advance.logistics.live.reporter.model.StorageArea;
import eu.advance.logistics.live.reporter.model.UOM;
import eu.advance.logistics.live.reporter.model.Warehouse;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.map.TLongDoubleMap;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TLongDoubleHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.procedure.TLongDoubleProcedure;
import gnu.trove.procedure.TLongObjectProcedure;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import hu.akarnokd.reactive4java.base.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalTime;
import org.joda.time.ReadableDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data provider for the warehouse summary and detail views.
 * @author karnokd, 2013.04.29.
 */
public final class WarehouseSummary {
	/** Helper class. */
	private WarehouseSummary() { }
	/** The logger. */
	protected static final Logger LOGGER = LoggerFactory.getLogger(WarehouseSummary.class);
	/**
	 * Returns a set of depots appearing the given warehouse
	 * independent of type (A, B) and side (LEFT, RIGHT).
	 * @param hubId the hub identifier
	 * @param warehouse the warehouse name
	 * @return the set of depots
	 */
	public static TLongSet depotsInWarehouse(long hubId, String warehouse) {
		TLongSet storageDepots = new TLongHashSet();
		List<Warehouse> warehouses = MasterDB.warehouses(hubId);
		for (Warehouse s : warehouses) {
			if (warehouse.equals(s.warehouse) || warehouse.equals(s.pair)) {
				List<StorageArea> areas = MasterDB.storageAreas(hubId, s.warehouse);
				for (StorageArea b : areas) {
					storageDepots.add(b.depot);
				}
			}
		}
		return storageDepots;
	}
	/**
	 * Sets the overall quantities.
	 * @param overall the output
	 * @param hubId the hub id
	 * @param when the current time
	 * @param cache the data cache
	 */
	public static void setOverall(
			L1OverallData overall, 
			long hubId, 
			ReadableDateTime when,
			HubDepotDataCache cache) {
		TLongSet storageDepots = depotsInWarehouse(hubId, overall.warehouse);
		
		TLongObjectMap<ConsignmentSummary> consignmentStatus = 
				cache.getHubDepotStatus(hubId, null, storageDepots, when);
		
		EnumMap<WarehouseServiceLevel, Aggregates> perLevel = new EnumMap<>(WarehouseServiceLevel.class);
		for (WarehouseServiceLevel sl : WarehouseServiceLevel.values()) {
			perLevel.put(sl, new Aggregates());
		}
		EnumMap<ServiceLevel, List<WarehouseServiceLevel>> serviceToSnip = new EnumMap<>(ServiceLevel.class);
		serviceToSnip.put(ServiceLevel.STANDARD, new ArrayList<WarehouseServiceLevel>(Arrays.asList(WarehouseServiceLevel.ALL)));
		serviceToSnip.put(ServiceLevel.PRIORITY, new ArrayList<WarehouseServiceLevel>(Arrays.asList(WarehouseServiceLevel.ALL)));
		serviceToSnip.put(ServiceLevel.SPECIAL, new ArrayList<WarehouseServiceLevel>(Arrays.asList(WarehouseServiceLevel.ALL)));

		serviceToSnip.get(ServiceLevel.STANDARD).addAll(Arrays.asList(WarehouseServiceLevel.STANDARD));
		serviceToSnip.get(ServiceLevel.PRIORITY).addAll(Arrays.asList(WarehouseServiceLevel.PRIORITY_SPECIAL));
		serviceToSnip.get(ServiceLevel.SPECIAL).addAll(Arrays.asList(WarehouseServiceLevel.PRIORITY_SPECIAL));
		
		for (ConsignmentSummary cs : consignmentStatus.valueCollection()) {
			double cscreated = 0;
			double csscanned = 0;
			double csdeclared = 0;
			double csathub = 0;
			double cslefthub = 0;

			csathub = cs.floorspace(ItemStatus.AT_HUB);
			cslefthub = cs.floorspace(ItemStatus.LEFT_HUB_TODAY);
			csdeclared = cs.floorspace(ItemStatus.DECLARED);
			csscanned = cs.floorspace(ItemStatus.SCANNED);
			cscreated = cs.floorspace(ItemStatus.CREATED);
			
			for (WarehouseServiceLevel s : serviceToSnip.get(cs.level)) {
				perLevel.get(s).created += cscreated;
				perLevel.get(s).scanned += csscanned;
				perLevel.get(s).declared += csdeclared;
				perLevel.get(s).athub += csathub;
				perLevel.get(s).lefthub += cslefthub;
			}
		}
		Map<Long, Pair<LocalTime, List<MlPrediction>>> predictions = MLDB.getMLLatestPredictions(hubId, storageDepots, when.toDateTime());
		for (Pair<LocalTime, List<MlPrediction>> pds : predictions.values()) {
			for (MlPrediction pd : pds.second) {
				if (pd.inbound && pd.dayOffset == 0 && pd.unit == UOM.FLOORSPACE && pd.service != ServiceLevel.ALL) {
					for (WarehouseServiceLevel s : serviceToSnip.get(pd.service)) {
						perLevel.get(s).predicted += pd.current + pd.remaining;
					}
				}
			}
		}

		
		for (WarehouseServiceLevel sKey : WarehouseServiceLevel.values()) {
			Aggregates a = perLevel.get(sKey);
			Map<ItemStatus, BarData> p = a.create();
			overall.items.put(sKey, p);
		}    
	}
	/**
	 * Returns the warehouse capacity.
	 * @param hub the hub
	 * @param warehouse the warehouse
	 * @return the capacity
	 */
	public static double getL1WarehouseCapacity(long hub, String warehouse) {
		// FIXME cache somehow!
		Map<String, Map<WarehouseSide, Double>> warehouseCapacityMap = MasterDB.warehouseCapacityMap(MasterDB.getWarehouseLayoutMap(hub));
		
		double d = 0;
		for (WarehouseSide sKey : WarehouseSide.values()) {
			d += warehouseCapacityMap.get(warehouse).get(sKey);
		}
		return d;
		
	}
	/**
	 * Set the at hub detailed counts.
	 * @param atHub the output record
	 * @param hubId the hub id
	 * @param when the current time
	 * @param cache the data cache
	 */
	public static void setAtHubDetails(L1AtHubData atHub, long hubId, ReadableDateTime when, HubDepotDataCache cache) {

		String warehouse = atHub.warehouse;

		final EnumMap<WarehouseServiceLevel, TObjectDoubleMap<WarehouseType>> levelTypeSum = new EnumMap<>(WarehouseServiceLevel.class);
		EnumMap<WarehouseServiceLevel, EnumMap<WarehouseType, TLongIntMap>> levelTypeDepotCapacity = new EnumMap<>(WarehouseServiceLevel.class);
		final EnumMap<WarehouseServiceLevel, EnumMap<WarehouseType, TLongDoubleMap>> levelTypeDepotSum = new EnumMap<>(WarehouseServiceLevel.class);

		getWarehouseContents(hubId, when, warehouse, levelTypeSum, levelTypeDepotCapacity, levelTypeDepotSum, cache);
		
		for (WarehouseServiceLevel sKey : WarehouseServiceLevel.values())	{
			
			Map<WarehouseType, L1AtHubData.L1Warehouse> stotal = new LinkedHashMap<>();
			Map<WarehouseType, L1AtHubData.L1StorageArea>  bworst = new LinkedHashMap<>();

			for (WarehouseType st : WarehouseType.values()) {
				stotal.put(st, atHub.new L1Warehouse((int)levelTypeSum.get(sKey).get(st), getL1WarehouseCapacity(hubId, warehouse)));
				
				/** The aggregator procedure. */
				class MaxRec implements TLongDoubleProcedure {
					long maxDepot;
					double maxValue;
					@Override
					public boolean execute(long a, double b) {
						if (b > maxValue) {
							maxValue = b;
							maxDepot = a;
						}
						return true;
					}
				}
				
				
				MaxRec mrp = new MaxRec();
				
				TLongDoubleMap map = levelTypeDepotSum.get(sKey).get(st);
				map.forEachEntry(mrp);
				
				int capacity = levelTypeDepotCapacity.get(sKey).get(st).get(mrp.maxDepot);
				bworst.put(st, atHub.new L1StorageArea(mrp.maxDepot, (int)mrp.maxValue, capacity));
			}

			atHub.warehouses.put(sKey, stotal);
			atHub.worstStorageAreas.put(sKey, bworst);
		}    

	}
	/**
	 * Retrieves the warehouse contents.
	 * @param hubId the hub identifier
	 * @param when the current date
	 * @param warehouse the warehouse name
	 * @param levelTypeSum the output total quantity per service level and warehouse type 
	 * @param levelTypeDepotCapacity the capacity per service level, warehouse type and depot
	 * @param levelTypeDepotSum the floorspace per service level, warehouse type and depot
	 * @param cache the data cache
	 */
	protected static void getWarehouseContents(
			long hubId,
			ReadableDateTime when,
			String warehouse,
			final EnumMap<WarehouseServiceLevel, TObjectDoubleMap<WarehouseType>> levelTypeSum,
			EnumMap<WarehouseServiceLevel, EnumMap<WarehouseType, TLongIntMap>> levelTypeDepotCapacity,
			final EnumMap<WarehouseServiceLevel, EnumMap<WarehouseType, TLongDoubleMap>> levelTypeDepotSum,
			HubDepotDataCache cache) {
		final EnumMap<ServiceLevel, List<WarehouseServiceLevel>> serviceToSnip = new EnumMap<>(ServiceLevel.class);
		for (ServiceLevel sl : ServiceLevel.values()) {
			serviceToSnip.put(sl, new ArrayList<WarehouseServiceLevel>(Arrays.asList(WarehouseServiceLevel.ALL)));
		}
		serviceToSnip.get(ServiceLevel.STANDARD).addAll(Arrays.asList(WarehouseServiceLevel.STANDARD));
		serviceToSnip.get(ServiceLevel.PRIORITY).addAll(Arrays.asList(WarehouseServiceLevel.PRIORITY_SPECIAL));
		serviceToSnip.get(ServiceLevel.SPECIAL).addAll(Arrays.asList(WarehouseServiceLevel.PRIORITY_SPECIAL));

		EnumMap<WarehouseType, TLongObjectMap<EnumMap<ServiceLevel, TLongSet>>> warehouseTypeMap = new EnumMap<>(WarehouseType.class);
		TLongSet itemIds = new TLongHashSet();
		List<Warehouse> warehouses = MasterDB.warehouses(hubId);
		

		for (WarehouseServiceLevel sn : WarehouseServiceLevel.values()) {
			levelTypeSum.put(sn, new TObjectDoubleHashMap<WarehouseType>());
			EnumMap<WarehouseType, TLongDoubleMap> enm = new EnumMap<>(WarehouseType.class);
			for (WarehouseType st : WarehouseType.values()) {
				enm.put(st, new TLongDoubleHashMap());
			}
			levelTypeDepotSum.put(sn, enm);
			
			EnumMap<WarehouseType, TLongIntMap> enm2 = new EnumMap<>(WarehouseType.class);
			for (WarehouseType st : WarehouseType.values()) {
				enm2.put(st, new TLongIntHashMap());
			}
			
			levelTypeDepotCapacity.put(sn, enm2);
		}
		
		for (Warehouse s : warehouses) {
			if (warehouse.equals(s.warehouse)) {
				
				List<Pair<String, WarehouseType>> whs = new ArrayList<>();
				
				TLongObjectMap<EnumMap<ServiceLevel, TLongSet>> out1 = cache.getItemsInWarehouse(hubId, s.warehouse, when);
				warehouseTypeMap.put(WarehouseType.A, out1);
				whs.add(Pair.of(s.warehouse, WarehouseType.A));

				if (s.pair != null) {
					TLongObjectMap<EnumMap<ServiceLevel, TLongSet>> out2 = cache.getItemsInWarehouse(hubId, s.pair, when);
					warehouseTypeMap.put(WarehouseType.B, out2);
					whs.add(Pair.of(s.pair, WarehouseType.B));
				} else {
					warehouseTypeMap.put(WarehouseType.B, new TLongObjectHashMap<EnumMap<ServiceLevel, TLongSet>>());
				}
						
				for (Pair<String, WarehouseType> wh : whs) {
					List<StorageArea> bs = MasterDB.splitStorageAreas(MasterDB.storageAreas(hubId, wh.first));
					for (StorageArea b : bs) {
						EnumSet<WarehouseServiceLevel> ssn = EnumSet.noneOf(WarehouseServiceLevel.class);
						for (ServiceLevel sl : b.services) {
							for (WarehouseServiceLevel sn : serviceToSnip.get(sl))  {
								ssn.add(sn);
							}
						}
						for (WarehouseServiceLevel sn : ssn) {
							levelTypeDepotCapacity.get(sn).get(wh.second).adjustOrPutValue((short)b.depot, b.capacity, b.capacity);
						}
					}
					// extract all item ids
					for (EnumMap<ServiceLevel, TLongSet> m1 : out1.valueCollection()) {
						for (TLongSet m2 : m1.values()) {
							itemIds.addAll(m2);
						}
					}
				}				
				break;
			}
		}
		
		final TLongDoubleMap itemFloorspaces = cache.getItemFloorspace(itemIds);

		// aggregate to total and per depot
		for (Map.Entry<WarehouseType, TLongObjectMap<EnumMap<ServiceLevel, TLongSet>>> e : warehouseTypeMap.entrySet()) {
			final WarehouseType st = e.getKey();
			e.getValue().forEachEntry(new TLongObjectProcedure<EnumMap<ServiceLevel, TLongSet>>() {
				@Override
				public boolean execute(long a,
						EnumMap<ServiceLevel, TLongSet> b) {
					for (Map.Entry<ServiceLevel, TLongSet> e2 : b.entrySet()) {
						for (WarehouseServiceLevel sn : serviceToSnip.get(e2.getKey())) {
							TLongIterator it = e2.getValue().iterator();
							while (it.hasNext()) {
								long pid = it.next();
								double v = itemFloorspaces.get(pid);
								levelTypeSum.get(sn).adjustOrPutValue(st, v, v);
								levelTypeDepotSum.get(sn).get(st).adjustOrPutValue(a, v, v);
							}
						}
					}
					return true;
				}
			});
		}
	}
	/**
	 * Retrieves item statuses per warehouse type, depot and status.
	 * @param hubId the target hub id
	 * @param when the current time
	 * @param warehouse the warehouse name, determines the destination depot filter
	 * @param out the result per warehouse type, depot, service level to aggregated statuses.
	 * @param cache the data cache
	 */
	protected static void itemStatusesAimedAtWarehouse(
			long hubId, ReadableDateTime when, String warehouse,
			TLongObjectMap<EnumMap<WarehouseServiceLevel, Aggregates>> out,
			HubDepotDataCache cache) {
		
		TLongSet storageDepots = new TLongHashSet();
		List<StorageArea> areas = MasterDB.storageAreas(hubId, warehouse);
		for (StorageArea b : areas) {
			if (storageDepots.add(b.depot)) {
				
				EnumMap<WarehouseServiceLevel, Aggregates> perLevel = new EnumMap<>(WarehouseServiceLevel.class);
				for (WarehouseServiceLevel sl : WarehouseServiceLevel.values()) {
					perLevel.put(sl, new Aggregates());
				}

				out.put((short)b.depot, perLevel);
			}
		}
		TLongObjectMap<ConsignmentSummary> consignmentStatus = 
				cache.getHubDepotStatus(hubId, null, storageDepots, when);
		
		EnumMap<ServiceLevel, List<WarehouseServiceLevel>> serviceToSnip = new EnumMap<>(ServiceLevel.class);
		for (ServiceLevel sl : ServiceLevel.values()) {
			serviceToSnip.put(sl, new ArrayList<WarehouseServiceLevel>(Arrays.asList(WarehouseServiceLevel.ALL)));
		}
		serviceToSnip.get(ServiceLevel.STANDARD).addAll(Arrays.asList(WarehouseServiceLevel.STANDARD));
		serviceToSnip.get(ServiceLevel.PRIORITY).addAll(Arrays.asList(WarehouseServiceLevel.PRIORITY_SPECIAL));
		serviceToSnip.get(ServiceLevel.SPECIAL).addAll(Arrays.asList(WarehouseServiceLevel.PRIORITY_SPECIAL));
		
		for (ConsignmentSummary cs : consignmentStatus.valueCollection()) {
			
			double cscreated = 0;
			double csscanned = 0;
			double csdeclared = 0;
			double csathub = 0;
			double cslefthub = 0;

			csathub = cs.floorspace(ItemStatus.AT_HUB);
			cslefthub = cs.floorspace(ItemStatus.LEFT_HUB_TODAY);
			csdeclared = cs.floorspace(ItemStatus.DECLARED);
			csscanned = cs.floorspace(ItemStatus.SCANNED);
			cscreated = cs.floorspace(ItemStatus.CREATED);
			
			EnumMap<WarehouseServiceLevel, Aggregates> perLevel = out.get(cs.deliveryDepot);
			
			for (WarehouseServiceLevel s : serviceToSnip.get(cs.level)) {
				perLevel.get(s).created += cscreated;
				perLevel.get(s).scanned += csscanned;
				perLevel.get(s).declared += csdeclared;
				perLevel.get(s).athub += csathub;
				perLevel.get(s).lefthub += cslefthub;
			}
		}
	}
	/**
	 * Fills in a detailed map about warehouse item statuses.
	 * @param storageRawMap the map to fill in
	 * @param hubId the hub id
	 * @param when the current time
	 * @param warehouse the current warehouse name
	 * @param cache the data cache
	 */
	public static void warehouseDetails(
			Map<L2DisplaySide, List<L2StorageRawData>> storageRawMap,
			long hubId, ReadableDateTime when, String warehouse,
			HubDepotDataCache cache) {
		
		final EnumMap<WarehouseServiceLevel, TObjectDoubleMap<WarehouseType>> levelTypeSum = new EnumMap<>(WarehouseServiceLevel.class);
		EnumMap<WarehouseServiceLevel, EnumMap<WarehouseType, TLongIntMap>> levelTypeDepotCapacity = new EnumMap<>(WarehouseServiceLevel.class);

		
		final EnumMap<WarehouseServiceLevel, EnumMap<WarehouseType, TLongDoubleMap>> levelTypeDepotSum = new EnumMap<>(WarehouseServiceLevel.class);
		TLongObjectMap<EnumMap<WarehouseServiceLevel, Aggregates>> depotLevelAggregates = new TLongObjectHashMap<>();

		getWarehouseContents(hubId, when, warehouse, levelTypeSum, levelTypeDepotCapacity, levelTypeDepotSum, cache);
		itemStatusesAimedAtWarehouse(hubId, when, warehouse, depotLevelAggregates, cache);
		
		for (L2DisplaySide dsKey : L2DisplaySide.values()) {
			for (L2StorageRawData storageRawItem : storageRawMap.get(dsKey))	{
				
				EnumMap<WarehouseServiceLevel, Aggregates> forDepot = depotLevelAggregates.get((short)storageRawItem.id);
				
				// Fill the concrete storageRawItem
				for (WarehouseServiceLevel sKey : L2StorageRawData.getUsedServices()) {
					Aggregates a = forDepot.get(sKey);
					
					Map<ItemStatus, BarData> items = new LinkedHashMap<ItemStatus, BarData>();
					for (ItemStatus pKey : L2StorageRawData.getUsedItemStatus()) {
						if (pKey == ItemStatus.AT_HUB) {
							double v = getInStorageFloorspace(levelTypeDepotSum, storageRawItem.type, storageRawItem.id, sKey);
							items.put(pKey, new BarData(v));
						} else {
							items.put(pKey, new BarData(a.value(pKey)));
						}
					}
					storageRawItem.items.put(sKey, items);
				}
			}
		}
		
	}
	/**
	 * Returns the in storage floorspace value, considering there might be no data available.
	 * @param levelTypeDepotSum the per level, type and depot keyed value
	 * @param type the warehouse type (A, B)
	 * @param depot the depot number
	 * @param sKey the service level
	 * @return the value
	 */
	private static double getInStorageFloorspace(
			final EnumMap<WarehouseServiceLevel, EnumMap<WarehouseType, TLongDoubleMap>> levelTypeDepotSum,
			WarehouseType type, long depot, WarehouseServiceLevel sKey) {
		EnumMap<WarehouseType, TLongDoubleMap> enumMap = levelTypeDepotSum.get(sKey);
		if (enumMap != null) {
			TLongDoubleMap tShortDoubleMap = enumMap.get(type);
			if (tShortDoubleMap != null) {
				return tShortDoubleMap.get(depot);
			}
		}
		return 0d;
	}
	/**
	 * Set a depot level warehouse details list.
	 * @param dataList the output datalist
	 * @param hubId the hub id
	 * @param when the current time
	 * @param warehouse the warehouse
	 * @param cache the data cache
	 */
	public static void setDepotDetails(List<L3DepotStorageData> dataList,
			long hubId, ReadableDateTime when, String warehouse,
			HubDepotDataCache cache) {

		final EnumMap<WarehouseServiceLevel, TObjectDoubleMap<WarehouseType>> levelTypeSum = new EnumMap<>(WarehouseServiceLevel.class);
		EnumMap<WarehouseServiceLevel, EnumMap<WarehouseType, TLongIntMap>> levelTypeDepotCapacity = new EnumMap<>(WarehouseServiceLevel.class);

		
		final EnumMap<WarehouseServiceLevel, EnumMap<WarehouseType, TLongDoubleMap>> levelTypeDepotSum = new EnumMap<>(WarehouseServiceLevel.class);
		TLongObjectMap<EnumMap<WarehouseServiceLevel, Aggregates>> depotLevelAggregates = new TLongObjectHashMap<>();

		getWarehouseContents(hubId, when, warehouse, levelTypeSum, levelTypeDepotCapacity, levelTypeDepotSum, cache);
		itemStatusesAimedAtWarehouse(hubId, when, warehouse, depotLevelAggregates, cache);

		ItemStatus[] futureStatuses = {
				ItemStatus.DECLARED, ItemStatus.SCANNED, ItemStatus.CREATED, ItemStatus.PREDICTED	
		};
		
		for (L3DepotStorageData depotStorageItem : dataList) {
			for (WarehouseServiceLevel sKey : WarehouseServiceLevel.values())	{

				EnumMap<WarehouseServiceLevel, Aggregates> forDepot = depotLevelAggregates.get(depotStorageItem.depot);

				Map<ItemStatus, BarData> futureP = new LinkedHashMap<>();
				
				for (ItemStatus ps : futureStatuses) {
					futureP.put(ps, new BarData(forDepot.get(sKey).value(ps)));
				}
				
				depotStorageItem.futureItems.put(sKey, futureP);

				Map<WarehouseType, BarData> atHubP = new LinkedHashMap<>();
				
				for  (WarehouseType st : WarehouseType.values()) {
					atHubP.put(st, new BarData(getInStorageFloorspace(levelTypeDepotSum, st, depotStorageItem.depot, sKey)));
				}
				
				depotStorageItem.atHubItems.put(sKey, atHubP);
			}
		}
		
	}
}
