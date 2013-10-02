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
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import org.joda.time.DateTime;

/**
 * The base environment for the agents.
 * @author karnokd, 2013.10.02.
 */
public class EnvironmentAgent {
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
		while (!events.isEmpty()) {
			Pair<DateTime, Action0> e = events.remove();
			e.second.invoke();
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
			HubAgent ha = new HubAgent(this);
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
			
			WarehouseAgent wa = new WarehouseAgent(this);
			wa.hub = wh.hub;
			wa.warehouse = wh.warehouse;
			
			ha.warehouses.add(wa);
			
			warehouseAgents.put(wa.warehouse, wa);
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
			DepotAgent da = new DepotAgent(this);
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
			
			VehicleAgent va = new VehicleAgent(this);
			va.id = v.vehicleId;
			va.depot = v.depot;
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
	 * Stores an event for a particulare item.
	 * @param itemId the item identifier
	 * @param consignmentId the parent consignment id
	 * @param timestamp the event timestamp
	 * @param event the event
	 */
	public void event(long itemId, long consignmentId, DateTime timestamp, ItemEventType event) {
		try {
			db.update("INSERT INGORE INTO events VALUES (?, ?, ?, ?)", itemId, consignmentId, timestamp, event.ordinal());
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
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
	 * Add a new item.
	 * @param item the item
	 */
	public void newItem(final ConsItem item) {
		
		event(item.id, item.consignmentId, item.consignment.created, ItemEventType.CREATED);
		
		final DateTime depotArrive = postcodeDepotTravel(item.consignment.created, item.consignment.collectionPostcode, item.consignment.collectionDepot);
		
		final DepotAgent da = depotAgents.get(item.consignment.collectionDepot);
		
		add(depotArrive, new Action0() {
			@Override
			public void invoke() {
				da.itemArrived(depotArrive, item);
			}
		});
	}
	/**
	 * Register a collection scan.
	 * @param vehicleId the vehicle identifier
	 * @param when the scan time
	 * @param item the item
	 */
	public void collectionScan(String vehicleId, DateTime when, ConsItem item) {
		event(item.id, item.consignmentId, when, ItemEventType.SOURCE_SCAN);
	
		try {
			db.update("INSERT IGNORE INTO scans VALUES (?, ?, ?, ?, ?, ?, ?)",
					item.consignmentId, item.id, ScanType.SOURCE.ordinal(),
					item.consignment.collectionDepot, true, when, vehicleId);

			db.update("INSERT IGNORE INTO scans_history VALUES (?, ?, ?, ?, ?, ?, ?)",
					item.consignmentId, item.id, ScanType.SOURCE.ordinal(),
					item.consignment.collectionDepot, true, when, vehicleId);
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	/**
	 * Declare a pallet on a vehicle.
	 * @param vehicleId the vehicle identifier
	 * @param when the time of the declaration
	 * @param externalItemId the item identifier
	 */
	public void declare(String vehicleId, DateTime when, String externalItemId) {
		int iidx = externalItemId.indexOf("I");
		long consId = Long.parseLong(externalItemId.substring(1, iidx));
//		long itemId = Long.parseLong(externalItemId.substring(iidx + 1));
		
		try {
			db.update("INSERT IGNORE INTO vehicle_declared VALUES (?, ?, ?)", vehicleId, when, externalItemId);
			db.update("UPDATE consignments SET declared = ? WHERE id = ?", when, consId);
			db.update("UPDATE consignments_history SET declared = ? WHERE id = ?", when, consId);
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	/**
	 * Vehicle arrived at hub.
	 * @param now the current time
	 * @param hub the hub
	 * @param va the vehicle
	 */
	public void hubArrive(final DateTime now, final long hub, final VehicleAgent va) {
		va.sessionId = hub + "_" + now.toString("yyyyMMdd'_'HHmmss") + "_" + va.id;
		va.atHub = true;
		
		for (ConsItem ci : va.contents) {
			event(ci.id, ci.consignmentId, now, ItemEventType.HUB_ARRIVE);
		}
		
		HubAgent ha = hubAgents.get(va.targetHub);
		ha.vehiclesOnSite.add(va);
		ha.loadUnload(now);
	}
	/**
	 * Register the contents of the vehicle entering/leaving its warehouse.
	 * @param va the vehicle
	 * @param now the current time
	 * @param enter is enter?
	 */
	public void warehouseScan(VehicleAgent va, DateTime now, boolean enter) {
		try {
			long sid = db.insertAuto("INSERT INTO vehicle_scan (session_id, scan_timestamp, warehouse, entry) VALUES (?, ?, ?, ?)",
					va.sessionId, now, va.warehouse, enter
			);
			for (ConsItem ci : va.contents) {
				event(ci.id, ci.consignmentId, now, ItemEventType.WAREHOUSE_ENTER);
				db.update("INSERT INTO vehicle_items VALUES (?, ?) ", sid, ci.externalId);
				
				ScanType st = enter ? ScanType.WAREHOUSE_ENTER : ScanType.WAREHOUSE_LEAVE;
				
				db.update("INSERT IGNORE INTO scans VALUES (?, ?, ?, ?, ?, ?)",
						ci.consignmentId, ci.id, st,
						va.targetHub + " " + va.warehouse, now, va.id);

				db.update("INSERT IGNORE INTO scans_history VALUES (?, ?, ?, ?, ?, ?)",
						ci.consignmentId, ci.id, st,
						va.targetHub + " " + va.warehouse, now, va.id);

			}
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	/**
	 * Indicate that no more consignments will appear on the given day.
	 * @param now the current time
	 */
	public void consignmentsEnd(DateTime now) {
		// TODO
		for (DepotAgent da : depotAgents.valueCollection()) {
			
		}
	}
}
