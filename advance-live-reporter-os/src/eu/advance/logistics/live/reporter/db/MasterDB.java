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
package eu.advance.logistics.live.reporter.db;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import hu.akarnokd.utils.database.DB;
import hu.akarnokd.utils.database.SQLInvoke;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateMidnight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.live.reporter.charts.StorageAreaInfo;
import eu.advance.logistics.live.reporter.charts.WarehouseServiceLevel;
import eu.advance.logistics.live.reporter.charts.WarehouseSide;
import eu.advance.logistics.live.reporter.charts.WarehouseType;
import eu.advance.logistics.live.reporter.model.Depot;
import eu.advance.logistics.live.reporter.model.Hub;
import eu.advance.logistics.live.reporter.model.HubDepotInfo;
import eu.advance.logistics.live.reporter.model.ServiceLevel;
import eu.advance.logistics.live.reporter.model.StorageArea;
import eu.advance.logistics.live.reporter.model.StorageSide;
import eu.advance.logistics.live.reporter.model.UOM;
import eu.advance.logistics.live.reporter.model.Warehouse;

/**
 * The database manager for common base model classes.
 * @author karnokd, 2013.09.23.
 */
public final class MasterDB {
	/** Database class. */
	private MasterDB() { }
	/** The logger. */
	private static final Logger LOG = LoggerFactory.getLogger(MasterDB.class);
	/**
	 * Returns the maximum values for the hub summary.
	 * @param hub the target hub id
	 * @param userId the user identifier
	 * @return the summary value maximum
	 */
	public static HubDepotInfo getHubSummaryValueMaxes(long hub, String userId) {
		final HubDepotInfo result = new HubDepotInfo();
		result.isHub = true;
		result.setAll(20000);
		try (DB db = DB.connect()) {
			db.query("SELECT unit, value FROM hub_diagram_scales WHERE name = ? AND hub = ? ",
			new SQLInvoke() {
				@Override
				public void invoke(ResultSet t) throws SQLException {
					UOM unit = UOM.values()[t.getInt(1)];
					result.busAsUsualScale.put(unit, t.getInt(2));
				}
			}, userId, hub
			);
		} catch (IOException | SQLException ex) {
			LOG.error(ex.toString(), ex);
		}
		return result;
	}
	/**
	 * Returns the maximum values for the depot summary.
	 * @param hub the hub reference
	 * @param depot the target depot id
	 * @param userId the user
	 * @return the summary value maximum
	 */
	public static HubDepotInfo getDepotSummaryValueMaxes(long hub, long depot, String userId) {
		final HubDepotInfo result = new HubDepotInfo();
		result.id = depot;
		result.setAll(300);
		try (DB db = DB.connect()) {
			db.query("SELECT name FROM depots WHERE id = ? ", new SQLInvoke() {
				@Override
				public void invoke(ResultSet t) throws SQLException {
					result.name = t.getString(1);
				}
			}, depot);
			db.query("SELECT a.unit, a.value, b.name " 
					+ "FROM depot_diagram_scales a, depots b "
					+ "WHERE a.hub = ? AND a.depot = ? AND a.name = ? AND a.depot = b.id ",
					new SQLInvoke() {
						@Override
						public void invoke(ResultSet t) throws SQLException {
							UOM unit = UOM.values()[t.getInt(1)];
							result.busAsUsualScale.put(unit, t.getInt(2));
						}
					}, hub, depot, userId
			);
		} catch (IOException | SQLException ex) {
			LOG.error(ex.toString(), ex);
		}
		return result;
	}
	/**
	 * Returns an ordered list of depots with their depot information record.
	 * @param hub the hub identifier
	 * @param userId the user name
	 * @return the list
	 */
	public static List<HubDepotInfo> getDepotsList(long hub, String userId) {
		final Map<Long, HubDepotInfo> map = new LinkedHashMap<>();
		
		try (DB db = DB.connect()) {
			db.query("SELECT id, name FROM depots ", new SQLInvoke() {
				@Override
				public void invoke(ResultSet t) throws SQLException {
					HubDepotInfo hdi = new HubDepotInfo();
					hdi.id = t.getLong(1);
					hdi.name = t.getString(2);
					for (UOM u : UOM.values()) {
						hdi.busAsUsualScale.put(u, 100);
					}
					map.put(hdi.id, hdi);
				}
			});
			db.query("SELECT a.depot, a.unit, a.value, b.name " 
					+ "FROM depot_diagram_scales a, depots b "
					+ "WHERE a.hub = ? AND a.name = ? AND a.depot = b.id ",
					new SQLInvoke() {
						@Override
						public void invoke(ResultSet t) throws SQLException {
							long depot = t.getLong(1);
							UOM unit = UOM.values()[t.getInt(2)];
							HubDepotInfo hdi = map.get(depot);
							if (hdi == null) {
								hdi = new HubDepotInfo();
								hdi.id = depot;
								hdi.name = t.getString(4);
								map.put(depot, hdi);
							}
							hdi.busAsUsualScale.put(unit, t.getInt(3));
						}
					}, hub, userId
			);
		} catch (IOException | SQLException ex) {
			LOG.error(ex.toString(), ex);
		}
		
		List<HubDepotInfo> result = new ArrayList<>(map.values());

		Collections.sort(result, new Comparator<HubDepotInfo>() {
			@Override
			public int compare(HubDepotInfo o1, HubDepotInfo o2) {
				return Long.compare(o1.id, o2.id);
			}
		});

		return result;
	}
	/**
	 * Returns a set of holidays.
	 * @return the set of days
	 */
	public static Set<DateMidnight> holidays() {
		final Set<DateMidnight> result = new HashSet<>();
		try (DB db = DB.connect()) {
			db.query("SELECT holiday FROM holidays", new SQLInvoke() {
				@Override
				public void invoke(ResultSet t) throws SQLException {
					result.add(new DateMidnight(t.getTimestamp(1).getTime()));
				}
			});
		} catch (IOException | SQLException ex) {
			LOG.error(ex.toString(), ex);
		}
		return result;
	}
	/**
	 * Set a new value to the given hub's unit summary representation.
	 * @param hub the target hub
	 * @param user the user name
	 * @param unit the unit type
	 * @param value the new maximum value
	 */
	public static void setHubSummaryValueMax(long hub, String user, UOM unit, int value) {
		try (DB db = DB.connect()) {
			db.update("DELETE FROM hub_diagram_scales WHERE name = ? AND hub = ? AND unit = ?",
					user, hub, unit.ordinal());
			db.update("INSERT INTO hub_diagram_scales VALUES (?, ?, ?, ?)", 
					user, hub, unit.ordinal(), value);
			db.commit();
		} catch (IOException | SQLException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Set a new value to the given depot's unit summary representation.
	 * @param hub the hub
	 * @param depot the target depot
	 * @param user the user name
	 * @param unit the unit type
	 * @param value the new maximum value
	 */
	public static void setDepotSummaryValueMax(long hub, long depot, String user, UOM unit, int value) {
		try (DB db = DB.connect()) {
			
			db.update("DELETE FROM depot_diagram_scales WHERE name = ? AND hub = ? AND depot = ? AND unit = ?",
					user, hub, depot, unit.ordinal());
			db.update("INSERT INTO depot_diagram_scales VALUES (?, ?, ?, ?, ?)", 
					user, hub, depot, unit.ordinal(), value);
			
			db.commit();
		} catch (IOException | SQLException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Returns a list of all depots.
	 * @return the depot list
	 */
	public static List<Depot> depots() {
		try (DB db = DB.connect()) {
			return db.query("SELECT id, name FROM depots", Depot.SELECT);
		} catch (IOException | SQLException ex) {
			ex.printStackTrace();
			return new ArrayList<>();
		}
	}
	/**
	 * Returns a list of all hubs.
	 * @return the hub list
	 */
	public static List<Hub> hubs() {
		try (DB db = DB.connect()) {
			return db.query("SELECT id, name, width, height FROM hubs ", Hub.SELECT);
		} catch (IOException | SQLException ex) {
			ex.printStackTrace();
			return new ArrayList<>();
		}
	}
	/**
	 * Returns a concrete hub.
	 * @param id the hub identifier
	 * @return the hub or null if not found
	 */
	public static Hub hub(long id) {
		try (DB db = DB.connect()) {
			return db.querySingle("SELECT id, name, width, height FROM hubs WHERE id = ? ", Hub.SELECT, id);
		} catch (IOException | SQLException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	/**
	 * Returns a concrete hub.
	 * @param name the hub name
	 * @return the hub or null if not found
	 */
	public static Hub hub(String name) {
		try (DB db = DB.connect()) {
			return db.querySingle("SELECT id, name, width, height FROM hubs WHERE name = ? ", Hub.SELECT, name);
		} catch (IOException | SQLException ex) {
			LOG.error(ex.toString(), ex);
			return null;
		}
	}
	/**
	 * Returns a list of warehouses inside a hub.
	 * @param hub the hub identifier
	 * @return the list of warehouses.
	 */
	public static List<Warehouse> warehouses(long hub) {
		try (DB db = DB.connect()) {
			return db.query("SELECT hub, warehouse, x, y, width, height, angle, forklifts, warehouse_pair FROM warehouses WHERE hub = ? ", Warehouse.SELECT, hub);
		} catch (IOException | SQLException ex) {
			LOG.error(ex.toString(), ex);
			return new ArrayList<>();
		}
	}
	/**
	 * Returns a list of warehouses inside a hub.
	 * @param hub the hub identifier
	 * @param warehouse the warehouse name
	 * @return the list of warehouses.
	 */
	public static Warehouse warehouse(long hub, String warehouse) {
		try (DB db = DB.connect()) {
			return db.querySingle("SELECT hub, warehouse, x, y, width, height, angle, forklifts, warehouse_pair FROM warehouses WHERE hub = ? AND warehouse = ? ", Warehouse.SELECT, hub, warehouse);
		} catch (IOException | SQLException ex) {
			LOG.error(ex.toString(), ex);
			return null;
		}
	}
	/**
	 * Returns the list of storage areas inside a hub/warehouse.
	 * @param hub the hub indentifier
	 * @param warehouse the warehouse name.
	 * @return the list of storage areas
	 */
	public static List<StorageArea> storageAreas(long hub, @NonNull String warehouse) {
		try (DB db = DB.connect()) {
			return db.query("SELECT hub, warehouse, side, idx, depot, width, height, service_levels, capacity "
					+ "FROM storage_areas WHERE hub = ? AND warehouse = ? "
					+ "ORDER BY hub, warehouse, side, idx ", StorageArea.SELECT, hub, warehouse);
		} catch (IOException | SQLException ex) {
			ex.printStackTrace();
			return new ArrayList<>();
		}
	}
	/**
	 * Save the storage area settings of the warehouse.
	 * @param hub the target hub
	 * @param warehouse the target warehouse
	 * @param areas the list of storage areas
	 */
	public static void saveStorageAreas(long hub, String warehouse, List<StorageArea> areas) {
		try  (DB db = DB.connect()) {
			db.update("DELETE FROM storage_areas WHERE hub = ? AND warehouse = ? ", hub, warehouse);
			for (StorageArea b : areas) {
				db.update("INSERT INTO storage_areas VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
						hub, warehouse, b.side.ordinal(), b.index,
						b.depot, 20, 1.5, b.flags(), b.capacity);
			}
			db.commit();
		} catch (IOException | SQLException ex) {
			LOG.error(ex.toString(), ex);
		}		
	}
	/**
	 * Splits the storage area slots into STANDARD and PRIORITY + SPECIAL.
	 * @param areas the input areas
	 * @return the output with new areas
	 */
	public static List<StorageArea> splitStorageAreas(List<StorageArea> areas) {
		List<StorageArea> result = new ArrayList<>();
		for (StorageArea b : areas) {
			if (b.services.isEmpty()) {
				StorageArea b0 = b.copy();
				b0.index *= 2;
				b0.capacity /= 2;
				b0.services.add(ServiceLevel.STANDARD); 
				
				StorageArea b1 = b.copy();
				b1.index *= 2;
				b1.index++;
				b1.capacity = b.capacity - b.capacity / 2;
				b1.services.add(ServiceLevel.PRIORITY);
				b1.services.add(ServiceLevel.SPECIAL);
				
				result.add(b0);
				result.add(b1);
				
			} else {
				StorageArea b0 = b.copy();
				b0.index *= 2;
				result.add(b0);
			}
		}
		return result;
	}
	/**
	 * Returns the warehouse layout map.
	 * @param hubId the target hub
	 * @return the map from warehouse to warehouse side to list of storage area information
	 */
	public static Map<String, Map<WarehouseSide, List<StorageAreaInfo>>> getWarehouseLayoutMap(
			long hubId) {
		Map<String, Map<WarehouseSide, List<StorageAreaInfo>>> result = new LinkedHashMap<>();

		List<Warehouse> warehouses = MasterDB.warehouses(hubId);
		for (Warehouse wh : warehouses) {
			HashMap<WarehouseSide, List<StorageAreaInfo>> map = new HashMap<>();
			result.put(wh.warehouse, map);
			for (WarehouseSide sd : WarehouseSide.values()) {
				map.put(sd, new ArrayList<StorageAreaInfo>());
			}
		}
		
		TLongObjectMap<StorageSide> depotSide = new TLongObjectHashMap<>();
		for (Warehouse wh : warehouses) {
			String sn = wh.warehouse;
			List<StorageArea> areas = MasterDB.splitStorageAreas(MasterDB.storageAreas(hubId, sn));

			Map<WarehouseSide, List<StorageAreaInfo>> sideMap = result.get(sn);
			if (sideMap == null) {
				sideMap = new LinkedHashMap<>();
				result.put(sn, sideMap);
			}

			for (StorageArea b : areas) {
				
				WarehouseServiceLevel sl = null;;
				if (b.services.contains(ServiceLevel.STANDARD)) {
					sl = WarehouseServiceLevel.STANDARD;
				} else {
					sl = WarehouseServiceLevel.PRIORITY_SPECIAL;
				}
				
				StorageSide side = depotSide.get(b.depot);
				if (side == null) {
					side = b.side;
					depotSide.put(b.depot, b.side);
				}
				
				List<StorageAreaInfo> list = null;
				if (side == StorageSide.LEFT) {
					list = sideMap.get(WarehouseSide.LEFT);
					if (list == null) {
						list = new ArrayList<>();
						sideMap.put(WarehouseSide.LEFT, list);
					}
				} else {
					list = sideMap.get(WarehouseSide.RIGHT);
					if (list == null) {
						list = new ArrayList<>();
						sideMap.put(WarehouseSide.RIGHT, list);
					}
				}
				mergeInto(list, b.depot, b.capacity, WarehouseType.A, sl, b.index);
			}
		}
		for (Map<WarehouseSide, List<StorageAreaInfo>> m : result.values()) {
			for (List<StorageAreaInfo> l : m.values()) {
				Collections.sort(l);
			}
		}
		
		return result;
	}
	/**
	 * Merges the storage info list with the new storage data.
	 * @param list the list
	 * @param depot the depot identifier
	 * @param capacity the capacity value
	 * @param type the warehouse type (A or B)
	 * @param sl the service level
	 * @param index the storage area's index
	 */
	protected static void mergeInto(List<StorageAreaInfo> list, 
			long depot, double capacity, WarehouseType type, WarehouseServiceLevel sl, int index) {
		StorageAreaInfo bi0 = null;
		for (StorageAreaInfo bi : list) {
			if (bi.id == depot) {
				bi0 = bi;
				break;
			}
		}
		if (bi0 == null) {
			bi0 = new StorageAreaInfo(depot, index, type);
			list.add(bi0);
		}
		bi0.add(type, sl, capacity);
	}
	/**
	 * Computes the warehouse capacity map based on the warehouse layout info.
	 * @param layoutMap the storage layout map
	 * @return the warehouse side capacity map
	 */
	public static Map<String, Map<WarehouseSide, Double>> warehouseCapacityMap(
			Map<String, Map<WarehouseSide, List<StorageAreaInfo>>> layoutMap) {
		Map<String, Map<WarehouseSide, Double>> result = new LinkedHashMap<>();
		for (String snKey : layoutMap.keySet()) {
			for (WarehouseType stype : WarehouseType.values()) {
	 			Map<WarehouseSide, Double> sd = new LinkedHashMap<>();
	 			String key = snKey.toString() + stype.toString();
	 			
	 			// FIXME somehow!
	 			System.out.println("X3 " + key);
	 			
				result.put(key, sd);
				for (WarehouseSide ssKey : WarehouseSide.values()) {
					double count = 0;
					for (StorageAreaInfo storageInfoItem : layoutMap.get(snKey).get(ssKey)) {
						count += storageInfoItem.capacity(stype);
					}
					sd.put(ssKey, count);
				}
			}
		}
		
		return result;
	}
}
