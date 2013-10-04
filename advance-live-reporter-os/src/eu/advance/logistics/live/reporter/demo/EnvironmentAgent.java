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
package eu.advance.logistics.live.reporter.demo;

import eu.advance.logistics.live.reporter.model.Depot;
import eu.advance.logistics.live.reporter.model.Hub;
import eu.advance.logistics.live.reporter.model.ItemEventType;
import eu.advance.logistics.live.reporter.model.LorryPosition;
import eu.advance.logistics.live.reporter.model.ScanType;
import eu.advance.logistics.live.reporter.model.StorageArea;
import eu.advance.logistics.live.reporter.model.Vehicle;
import eu.advance.logistics.live.reporter.model.Warehouse;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import hu.akarnokd.reactive4java.base.Action0;
import hu.akarnokd.reactive4java.base.Pair;
import hu.akarnokd.utils.database.DB;

import java.awt.Point;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base environment for the agents.
 * @author karnokd, 2013.10.02.
 */
public class EnvironmentAgent {
	/** The log. */
	private static final Logger LOG = LoggerFactory.getLogger(EnvironmentAgent.class);
	/** The database connection. */
	protected final DB db;
	/** The map of depots. */
	private final TLongObjectMap<DepotAgent> depotAgents = new TLongObjectHashMap<>();
	/** The map of vehicles. */
	private final Map<String, VehicleAgent> vehicleAgents = new HashMap<>();
	/** The map of warehouses. */
	private final Map<String, WarehouseAgent> warehouseAgents = new HashMap<>();
	/** The map of hub agents. */
	private final TLongObjectMap<HubAgent> hubAgents = new TLongObjectHashMap<>();
	/** The event sequence. */
	private final PriorityQueue<Pair<DateTime, Action0>> events = new PriorityQueue<>(256, new CompareFirst());
	/** Time to load one item. */
	public static final int LOAD_SECONDS = 10;
	/** Time to unload one item. */
	public static final int UNLOAD_SECONDS = 10;
	/** Max wait in seconds in case of an empty hub/depot. */
	public static final int MAX_LOAD_WAIT = 60 * 60;
	/** The idle wait in seconds. */
	public static final int IDLE_WAIT = 5 * 60;
	/** Indication that no further consignments will be created. */
	public boolean endOfConsignments;
	/** Consignments not introduced yet. */
	public final Set<ConsItem> pendingConsignments = new HashSet<>();
	/**
	 * Constructor.
	 * @param db the database
	 */
	public EnvironmentAgent(DB db) {
		this.db = db;
	}
	/**
	 * The event loop.
	 */
	public void eventLoop() {
		int i = 1;
		while (!events.isEmpty()) {
			Pair<DateTime, Action0> e = events.remove();
			e.second.invoke();
			if (i % 1000 == 0) {
				try {
					db.commit();
				} catch (SQLException ex) {
					LOG.error(ex.toString(), ex);
				}
			}
			i++;
		}
	}
	/**
	 * Add a new event to the queue.
	 * @param timestamp the timestamp
	 * @param action the action
	 */
	public void add(DateTime timestamp, Action0 action) {
		events.add(Pair.of(timestamp, action));
	}
	/**
	 * Create hub agents.
	 * @param hubs the sequence of hubs
	 */
	public void setHubs(Iterable<Hub> hubs) {
		hubAgents.clear();
		for (Hub h : hubs) {
			HubAgent ha = new HubAgent();
			ha.id = h.id;
			hubAgents.put(ha.id, ha);
		}
	}
	/**
	 * Create warehouse agents for hubs.
	 * @param warehouses the warehouses
	 */
	public void setWarehouses(Iterable<Warehouse> warehouses) {
		warehouseAgents.clear();
		for (Warehouse wh : warehouses) {
			HubAgent ha = hubAgents.get(wh.hub);
			
			WarehouseAgent wa = new WarehouseAgent();
			wa.hub = wh.hub;
			wa.id = wh.warehouse;
			
			ha.warehouses.add(wa);
			
			warehouseAgents.put(wa.id, wa);
		}
	}
	/**
	 * Set the storage areas insode warehouses.
	 * @param storages the storage areas
	 */
	public void setStorageAreas(Iterable<StorageArea> storages) {
		for (StorageArea sa : storages) {
			WarehouseAgent wa = warehouseAgents.get(sa.warehouse);
			
			wa.depots.add(sa.depot);
		}
	}
	/**
	 * Create the depot agents.
	 * @param depots the depots
	 */
	public void setDepots(Iterable<Depot> depots) {
		depotAgents.clear();
		for (Depot d : depots) {
			DepotAgent da = new DepotAgent();
			da.id = d.id;
			
			depotAgents.put(da.id, da);
		}
		
	}
	/**
	 * Create the vehicle agents.
	 * @param vehicles the vehicle
	 */
	public void setVehicles(Iterable<Vehicle> vehicles) {
		vehicleAgents.clear();
		for (Vehicle v : vehicles) {
			DepotAgent da = depotAgents.get(v.depot);
			
			VehicleAgent va = new VehicleAgent();
			va.id = v.vehicleId;
			va.depot = da;
			va.capacity = v.capacity;
			
			da.onSite.add(va);
			da.vehicles.add(va);
			
			vehicleAgents.put(va.id, va);
		}
	}
	/**
	 * Set the lorry positions in warehouses.
	 * @param lorryPositions the lorry position sequence
	 */
	public void setLorryPositions(Iterable<LorryPosition> lorryPositions) {
		for (LorryPosition lp : lorryPositions) {
			WarehouseAgent wa = warehouseAgents.get(lp.warehouse);
			wa.positions.add(lp);
		}
	}
	/**
	 * Returns the depot position.
	 * @param depot the depot
	 * @return the 
	 */
	static Point depotLocation(long depot) {
		int x = (int)(16 + 16 * ((depot - 1) % 5));
		int y = (int)(10 + 10 * ((depot - 1) / 5));
		
		return new Point(x, y);
	}
	/**
	 * Returns the position of the hub.
	 * @param hub the hub id
	 * @return the position
	 */
	static Point hubLocation(long hub) {
		return new Point((int)(hub * 33), 30);
	}
	/**
	 * Returns the position of a postcode.
	 * @param postcode the postcode in form 'XA00'
	 * @return the position
	 */
	static Point postcodeLocation(long postcode) {
		int c = (int)((postcode - 1) / 100);
		int n = (int)((postcode - 1) % 100);
		
		return new Point(n, c * 3);
	}
	/**
	 * Returns the distance between a postcode and a depot.
	 * @param postcode the postcode 
	 * @param depot the depot
	 * @return the distance
	 */
	public double postcodeDepotDistance(long postcode, long depot) {
		Point pc = postcodeLocation(postcode);
		Point pd = depotLocation(depot);
		return pc.distance(pd);
	}
	/**
	 * The distance between a hub and depot.
	 * @param depot the depot
	 * @param hub the hub
	 * @return the distance
	 */
	public double depotHubDistance(long depot, long hub) {
		Point pd = depotLocation(depot);
		Point ph = hubLocation(hub);
		return pd.distance(ph);
	}
	/**
	 * Computes the arrival time of an item travelling from the postcode to the depot.
	 * @param now the current time
	 * @param postcode the postcode
	 * @param depot the depot
	 * @return the arrival time
	 */
	public DateTime postcodeDepotTravel(DateTime now, long postcode, long depot) {
		double d = postcodeDepotDistance(postcode, depot) * 60 * 1.5;
		
		return now.plusSeconds((int)d);
	}
	/**
	 * Computes the arrival time of a vehicle, from depot to hub.
	 * @param now the current time
	 * @param depot the depot
	 * @param hub the hub
	 * @return the arrival time
	 */
	public DateTime depotHubTravel(DateTime now, long depot, long hub) {
		double d = depotHubDistance(depot, hub) * 60 * 1.5;
		
		return now.plusSeconds((int)d);
	}
	/**
	 * Stores an event for a particulare item.
	 * @param itemId the item identifier
	 * @param consignmentId the parent consignment id
	 * @param timestamp the event timestamp
	 * @param event the event
	 */
	public void event(long itemId, long consignmentId, DateTime timestamp, ItemEventType event) {
		try {
			db.update("INSERT IGNORE INTO `events` VALUES (?, ?, ?, ?)", 
					consignmentId, itemId, timestamp, event.ordinal());
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	/**
	 * Record a scan onto vehicle event in the database tables.
	 * @param va the vehicle agent
	 * @param item the consignment item
	 * @param when the current time
	 * @param onto scan onto the vehicle
	 */
	public void depotScanVehicle(VehicleAgent va, ConsItem item, DateTime when, boolean onto) {
		ItemEventType iet = onto ? ItemEventType.SOURCE_SCAN : ItemEventType.DESTINATION_SCAN;
		event(item.id, item.consignmentId, when, iet);
		try {
			int st = (onto ? ScanType.SOURCE : ScanType.DESTINATION).ordinal();
			db.update("INSERT IGNORE INTO scans VALUES (?, ?, ?, ?, ?, ?)",
					item.consignmentId, item.id, st,
					va.depot.id, when, va.id
			);
			db.update("INSERT IGNORE INTO scans_history VALUES (?, ?, ?, ?, ?, ?)",
					item.consignmentId, item.id, st,
					va.depot.id, when, va.id
			);
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	/**
	 * Declare the contents of the entire vehicle.
	 * @param va the vehicle
	 * @param when the current time
	 */
	public void declare(VehicleAgent va, DateTime when) {
		try {
			for (ConsItem ci : va.contents) {
				event(ci.id, ci.consignmentId, when, ItemEventType.DECLARED);
				
				db.update("UPDATE consignments SET declared = ? WHERE id = ? ", when, ci.consignmentId);
				
				db.update("UPDATE consignments_history SET declared = ? WHERE id = ? ", when, ci.consignmentId);
				
				db.update("INSERT IGNORE INTO vehicle_declared VALUES (?, ?, ?)", va.id, when, ci.externalId);
			}
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	/**
	 * Record a vehicle enter-leave the hub.
	 * @param va the vehicle
	 * @param when the current time
	 * @param enter enter?
	 */
	public void hubVehicleEnterLeave(VehicleAgent va, DateTime when, boolean enter) {
		try {
			for (ConsItem ci : va.contents) {
				event(ci.id, ci.consignmentId, when, enter ? ItemEventType.HUB_ARRIVE : ItemEventType.HUB_LEAVE);
			}
			if (enter) {
				va.sessionId = va.hub.id + "_" + when.toString("yyyyMMdd'_'HHmmss") + "_" + va.id;

				db.update("INSERT IGNORE INTO vehicle_sessions VALUES (?, ?, ?, ?, ?)",
						va.sessionId, va.id, va.hub.id, when, DateTime.class);
			} else {
				db.update("UPDATE vehicle_sessions SET leave_timestamp = ? WHERE session_id = ? ", when, va.sessionId);
			}
			
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	/**
	 * Record a vehicle enter/leave a warehouse.
	 * @param va the vehicle
	 * @param when the current time
	 * @param enter is enter?
	 */
	public void warehouseEnterLeave(VehicleAgent va, DateTime when, boolean enter) {
		try {
			long scanId = db.insertAuto("INSERT INTO vehicle_scan (session_id, scan_timestamp, warehouse, entry) VALUES (?, ?, ?, ?)",
					va.sessionId, when, va.warehouse.id, enter);
			
			for (ConsItem ci : va.contents) {
				event(ci.id, ci.consignmentId, when, enter ? ItemEventType.WAREHOUSE_ENTER : ItemEventType.WAREHOUSE_LEAVE);
				
				int st = (enter ? ScanType.WAREHOUSE_ENTER : ScanType.WAREHOUSE_LEAVE).ordinal();
				db.update("INSERT IGNORE INTO scans VALUES (?, ?, ?, ?, ?, ?)",
						ci.consignmentId, ci.id, st,
						va.hub.id + " " + va.warehouse.id, when, va.id);

				db.update("INSERT IGNORE INTO scans_history VALUES (?, ?, ?, ?, ?, ?)",
						ci.consignmentId, ci.id, st,
						va.hub.id + " " + va.warehouse.id, when, va.id);
				
				db.update("INSERT IGNORE INTO vehicle_items VALUES (?, ?)", scanId, ci.externalId);
			}
			
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}		
	}
	/**
	 * Record the scan of the item onto/off a vehicle.
	 * @param va the vehicle
	 * @param ci the item
	 * @param when the current time
	 * @param onto the direction
	 */
	public void warehouseItemScan(VehicleAgent va, ConsItem ci, DateTime when, boolean onto) {
		try {
			event(ci.id, ci.consignmentId, when, onto ? ItemEventType.SCAN_ON : ItemEventType.SCAN_OFF);
			
			int st = (onto ? ScanType.WAREHOUSE_MANUAL_LOAD : ScanType.WAREHOUSE_MANUAL_UNLOAD).ordinal();
			db.update("INSERT IGNORE INTO scans VALUES (?, ?, ?, ?, ?, ?)",
					ci.consignmentId, ci.id, st,
					va.hub.id + " " + va.warehouse.id, when, va.id);

			db.update("INSERT IGNORE INTO scans_history VALUES (?, ?, ?, ?, ?, ?)",
					ci.consignmentId, ci.id, st,
					va.hub.id + " " + va.warehouse.id, when, va.id);
			
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}		
		
	}
	/**
	 * Add a new item.
	 * @param item the item
	 */
	public void newItem(final ConsItem item) {
		pendingConsignments.add(item);
		final DateTime when = item.consignment.created;
		add(when, new Action0() {
			@Override
			public void invoke() {
				pendingConsignments.remove(item);
				final DepotAgent da = depotAgents.get(item.consignment.collectionDepot);
				da.toDeliver.add(item);
				event(item.id, item.consignmentId, when, ItemEventType.CREATED);
				if (!da.onSite.isEmpty()) {
					VehicleAgent va = Collections.max(da.onSite, VehicleAgent.CAPACITY_CONTENTS);
					dispatchDepotVehicle(va, when);
				}
			}
		});
	}
	/**
	 * Leave for the hub.
	 * @param va the vehicle
	 * @param when the current time
	 */
	public void leaveForHub(final VehicleAgent va, DateTime when) {
		LOG.info(va.id + " DEPOT " + va.depot.id + " LEAVE [" + va.contents.size() + "] @ " + when.toString("yyyy-MM-dd HH:mm:ss"));
		declare(va, when);
		va.atDepot = false;
		va.waiting = null;
		va.depot.onSite.remove(va);
		final DateTime arrive = depotHubTravel(when, va.depot.id, va.hubId);
		add(arrive, new Action0() {
			@Override
			public void invoke() {
				enterHub(va, arrive);
			}
		});
	}
	/**
	 * Enter the hub.
	 * @param va the vehicle.
	 * @param when the current time
	 */
	public void enterHub(VehicleAgent va, DateTime when) {
		LOG.info(va.id + " HUB ENTER [" + va.contents.size() + "] @ " + when.toString("yyyy-MM-dd HH:mm:ss"));
		va.atHub = true;
		va.hub = hubAgents.get(va.hubId);
		va.hub.onSite.add(va);
		
		hubVehicleEnterLeave(va, when, true);
		
		dispatchHubVehicle(va, when);
	}
	/**
	 * Dispatch a vehicle based on its contents.
	 * @param va the vehicle
	 * @param when the current time
	 */
	public void dispatchHubVehicle(final VehicleAgent va, final DateTime when) {
		if (va.sourceContent()) {
			WarehouseAgent wa = findUnloadWarehouse(va);
			if (wa != null) {
				enterWarehouse(va, wa, when);
				unload(va, when);
			} else {
				// try again later
				final DateTime next = when.plusSeconds(IDLE_WAIT);
				dispatchHubWait(va, next);
			}
		} else
		if (va.destinationContent() && va.isFull()) {
			leaveHub(va, when);
		} else
		if (va.destinationContent() || va.isEmpty()) {
			WarehouseAgent wa = findLoadWarehouse(va);
			if (wa != null) {
				enterWarehouse(va, wa, when);
				load(va, when, true);
			} else {
				if (va.waiting == null) {
					va.waiting = when;
				}
				if (Seconds.secondsBetween(va.waiting, when).getSeconds() > MAX_LOAD_WAIT) {
					va.waiting = null;
					leaveHub(va, when);
				} else {
					final DateTime next = when.plusSeconds(IDLE_WAIT);
					dispatchHubWait(va, next);
				}
			}
		}
	}
	/**
	 * Leave the hub.
	 * @param va the vehicle
	 * @param when the current time
	 */
	public void leaveHub(final VehicleAgent va, final DateTime when) {
		LOG.info(va.id + " HUB LEAVE [" + va.contents.size() + "] @ " + when.toString("yyyy-MM-dd HH:mm:ss"));
		hubVehicleEnterLeave(va, when, false);

		final DateTime depotArrive = depotHubTravel(when, va.depot.id, va.hub.id);

		va.hub.onSite.remove(va);
		
		va.hub = null;
		va.atHub = false;
		va.sessionId = null;
		
		add(depotArrive, new Action0() {
			@Override
			public void invoke() {
				depotArrive(va, depotArrive);
			}
		});
	}
	/**
	 * Vehicle arrived at a depot.
	 * @param va the vehicle
	 * @param when the time
	 */
	public void depotArrive(VehicleAgent va, DateTime when) {
		LOG.info(va.id + " DEPOT " + va.depot.id + " ENTER [" + va.contents.size() + "] @ " + when.toString("yyyy-MM-dd HH:mm:ss"));
		va.atDepot = true;
		va.depot.onSite.add(va);
		
		for (ConsItem ci : va.contents) {
			depotScanVehicle(va, ci, when, false);
		}
		va.contents.clear();
		
		dispatchDepotVehicle(va, when);
	}
	/**
	 * Dispatch the depot vehicle.
	 * @param va the vehicle
	 * @param when the current time
	 */
	public void dispatchDepotVehicle(final VehicleAgent va, DateTime when) {
		if (!va.atDepot) {
			return;
		} else
		if (va.isFull()) {
			leaveForHub(va, when);
		} else
		if (!va.depot.toDeliver.isEmpty()) {
			int limit = va.limit();
			for (ConsItem ci : new ArrayList<>(va.depot.toDeliver)) {
				if (limit > 0) {
					va.hubId = ci.consignment.hub;
					va.contents.add(ci);
					va.depot.toDeliver.remove(ci);
					depotScanVehicle(va, ci, when, true);
					limit--;
				}
			}
			final DateTime wait = when.plusSeconds(LOAD_SECONDS);
			dispatchDepotWait(va, wait);
		} else
		if (itemsForDepotExist(va.depot.id) || va.sourceContent()) {
			if (va.waiting == null) {
				va.waiting = when;
			}
			if (endOfConsignments || Seconds.secondsBetween(va.waiting, when).getSeconds() > MAX_LOAD_WAIT) {
				leaveForHub(va, when);
			} else {
				final DateTime wait = when.plusSeconds(IDLE_WAIT);
				dispatchDepotWait(va, wait);
			}
		} else
		if (!endOfConsignments) {
			final DateTime wait = when.plusSeconds(IDLE_WAIT);
			dispatchDepotWait(va, wait);
		}
	}
	/**
	 * Wait for some time and run a dispatch in the depot.
	 * @param va the vehicle
	 * @param wait the time to wait
	 */
	protected void dispatchDepotWait(final VehicleAgent va, final DateTime wait) {
		add(wait, new Action0() {
			@Override
			public void invoke() {
				dispatchDepotVehicle(va, wait);
			}
		});
	}
	/**
	 * Check if other depots, in-transit vehicles or the hub contains items for the target depot.
	 * @param depot the target depot
	 * @return true if items need to be fetched from hub
	 */
	public boolean itemsForDepotExist(long depot) {
		for (ConsItem ci : pendingConsignments) {
			if (ci.consignment.deliveryDepot == depot) {
				return true;
			}
		}
		for (DepotAgent da : depotAgents.valueCollection()) {
			for (ConsItem ci : da.toDeliver) {
				if (ci.consignment.deliveryDepot == depot) {
					return true;
				}
			}
		}
		for (VehicleAgent va : vehicleAgents.values()) {
			for (ConsItem ci : va.contents) {
				if (ci.consignment.deliveryDepot == depot) {
					return true;
				}
			}
		}
		for (WarehouseAgent wa : warehouseAgents.values()) {
			if (wa.storageAreas.containsKey(depot)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Wait until the given time and dispatch the vehicle again.
	 * @param va the vehicle
	 * @param next the wait end time
	 */
	protected void dispatchHubWait(final VehicleAgent va, final DateTime next) {
		add(next, new Action0() {
			@Override
			public void invoke() {
				dispatchHubVehicle(va, next);
			}
		});
	}
	/**
	 * Enter the warehouse.
	 * @param va the vehicle
	 * @param wa the warehouse
	 * @param when the current time
	 */
	public void enterWarehouse(VehicleAgent va, WarehouseAgent wa, DateTime when) {
		va.warehouse = wa;
		va.position = wa.freeSlot();
		wa.vehicles.add(va);
		
		warehouseEnterLeave(va, when, true);
		LOG.debug(va.id + " WAREHOUSE " + wa.id + " ENTER [" + va.contents.size() + "] @ " + when.toString("yyyy-MM-dd HH:mm:ss"));
	}
	/**
	 * Schedule the unloading of items.
	 * @param va the vehicle
	 * @param when current time
	 */
	public void unload(final VehicleAgent va, DateTime when) {
		DateTime first = when.plusSeconds(va.position.enterTime);
		for (final ConsItem ci : va.contents) {
			if (va.warehouse.depots.contains(ci.consignment.deliveryDepot)) {
				
				final DateTime dt = first;
				add(first, new Action0() {
					@Override
					public void invoke() {
						va.warehouse.storageAreas.put(ci.consignment.deliveryDepot, ci);
						va.contents.remove(ci);
						
						warehouseItemScan(va, ci, dt, false);
					}
				});
				
				first = first.plusSeconds(UNLOAD_SECONDS);
			}
		}
		final DateTime dt = first;
		add(first, new Action0() {
			@Override
			public void invoke() {
				unloadComplete(va, dt);
			}
		});
	}
	/**
	 * Called when the vehicle was unloaded successfully.
	 * @param va the vehicle
	 * @param dt the current time
	 */
	public void unloadComplete(final VehicleAgent va, DateTime dt) {
		if (!va.sourceContent() && va.warehouse.storageAreas.containsKey(va.depot.id)) {
			load(va, dt, false);
		} else {
			leaveWarehouse(va, dt);
		}
	}
	/**
	 * Leave the warehouse.
	 * @param va the vehicle
	 * @param when the time
	 */
	protected void leaveWarehouse(final VehicleAgent va, DateTime when) {
		final DateTime leave = when.plusSeconds(va.position.leaveTime);
		add(leave, new Action0() {
			@Override
			public void invoke() {
				LOG.debug(va.id + " WAREHOUSE " + va.warehouse.id + " LEAVE [" + va.contents.size() + "] @ " + leave.toString("yyyy-MM-dd HH:mm:ss"));
				warehouseEnterLeave(va, leave, false);

				va.warehouse.vehicles.remove(va);
				va.warehouse = null;
				va.position = null;
				
				dispatchHubVehicle(va, leave);
			}
		});
	}
	/**
	 * Load onto the vehicle.
	 * @param va the vehicle
	 * @param when the current time
	 * @param waitEnter wait for vehicle enter?
	 */
	public void load(final VehicleAgent va, DateTime when, boolean waitEnter) {
		DateTime dt = when;
		if (waitEnter) {
			dt = dt.plusSeconds(va.position.enterTime);
		}
		Collection<ConsItem> cs = va.warehouse.storageAreas.get(va.depot.id);
		int limit = va.limit();
		if (limit == 0 || cs.isEmpty()) {
			leaveWarehouse(va, dt);
		} else {
			final ConsItem ci = cs.iterator().next();
			if (va.warehouse.storageAreas.remove(va.depot.id, ci)) {
				dt = dt.plusSeconds(LOAD_SECONDS);
				final DateTime fdt = dt;
				add(dt, new Action0() {
					@Override
					public void invoke() {
						warehouseItemScan(va, ci, fdt, true);
						va.contents.add(ci);
						load(va, fdt, false);
					}
				});
			}
		}
	}
	/**
	 * Find a warehouse which can accept most of the contents of the vehicle.
	 * @param va the vehicle
	 * @return the best warehouse, null if all are occupied
	 */
	public WarehouseAgent findUnloadWarehouse(VehicleAgent va) {

		WarehouseAgent wres = null;
		int bestWres = 0;
		
		for (WarehouseAgent wa : va.hub.warehouses) {
			if (wa.vehicles.size() < wa.positions.size()) {
				int contentThere = 0;
				int contentVa = 0;
				for (ConsItem ci : va.contents) {
					contentThere += wa.storageAreas.get(ci.consignment.deliveryDepot).size();
					contentVa += wa.depots.contains(ci.consignment.deliveryDepot) ? 1 : 0;
				}
				
				if (contentVa > 0) {
					if (wres == null || bestWres > contentThere) {
						wres = wa;
						bestWres = contentThere;
					}
				}
			}
		}
		
		return wres;
	}
	/**
	 * Find a warehouse with the most content.
	 * @param va the vehicle
	 * @return the warehouse or null if no item can be found
	 */
	public WarehouseAgent findLoadWarehouse(VehicleAgent va) {
		WarehouseAgent result = null;
		int fill = 0;
		for (WarehouseAgent wa : va.hub.warehouses) {
			if (wa.vehicles.size() < wa.positions.size()) {
				int cnt = wa.storageAreas.get(va.depot.id).size();
				if (cnt > 0 && (result == null || cnt > fill)) {
					fill = cnt;
					result = wa;
				}
			}
		}
		return result;
	}
	/**
	 * Indicate that no more consignments will be created.
	 */
	public void consignmentsEnd() {
		endOfConsignments = true;
	}
	/**
	 * Check if unprocessed items remained.
	 * @return true if unprocessed items remained
	 */
	public boolean hasUnporcessed() {
		boolean r = false;
		for (WarehouseAgent wa : warehouseAgents.values()) {
			if (!wa.storageAreas.isEmpty()) {
				LOG.warn(wa.id + ": " + wa.storageAreas.size());
				r = true;
			}
		}
		for (DepotAgent da : depotAgents.valueCollection()) {
			if (!da.toDeliver.isEmpty()) {
				LOG.warn(da.id + ": " + da.toDeliver.size());
				r = true;
			}
		}
		for (VehicleAgent va : vehicleAgents.values()) {
			if (!va.isEmpty()) {
				LOG.warn(va.id + ": " + va.contents.size());
				r = true;
			}
		}
		return r;
	}
}
