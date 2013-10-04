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

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import hu.akarnokd.utils.database.DB;
import hu.akarnokd.utils.sequence.SequenceUtils;

import java.awt.Point;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import eu.advance.logistics.live.reporter.model.Depot;
import eu.advance.logistics.live.reporter.model.Hub;
import eu.advance.logistics.live.reporter.model.Item;
import eu.advance.logistics.live.reporter.model.LorryPosition;
import eu.advance.logistics.live.reporter.model.Postcode;
import eu.advance.logistics.live.reporter.model.ServiceLevel;
import eu.advance.logistics.live.reporter.model.StorageArea;
import eu.advance.logistics.live.reporter.model.Vehicle;
import eu.advance.logistics.live.reporter.model.Warehouse;

/**
 * Consignment creator.
 * @author karnokd, 2013.10.01.
 *
 */
public class ConsignmentCreator {
	/** The logger. */
	private static final Logger LOG = LoggerFactory.getLogger(ConsignmentCreator.class);
	/** The database connection. */
	DB db;
	/** Field. */
	private List<Postcode> postcodes;
	/** Field. */
	private TLongObjectMap<Postcode> postcodeMap;
	/** Field. */
	private List<Vehicle> vehicles;
	/** Field. */
	private Multimap<Long, Vehicle> depotVehicles;
	/** Field. */
	private List<Hub> hubs;
	/** Field. */
	private TLongObjectMap<Hub> hubMap;
	/** Field. */
	private List<Depot> depots;
	/** Field. */
	private TLongObjectMap<Depot> depotMap;
	/** Field. */
	private Set<DateMidnight> holidays;
	/** Field. */
	private List<Warehouse> warehouses;
	/** Field. */
	private Multimap<Long, Warehouse> warehouseMap;
	/** Field. */
	private List<StorageArea> storages;
	/** Field. */
	private Multimap<String, StorageArea> areas;
	/** Field. */
	private TLongObjectMap<Depot> postcodeClosestDepot;
	/** Field. */
	private TLongObjectMap<Hub> depotClosestHub;
	/** The running consignment id. */
	private long consId;
	/** The running item ids. */
	private long itemId;
	/** The environment agent. */
	private EnvironmentAgent env;
	/**
	 * Constructor, sets the database connection.
	 * @param db the database connection
	 */
	public ConsignmentCreator(DB db) {
		this.db = db;
	}
	/**
	 * Returns the position of a postcode.
	 * @param postcode the postcode in form 'XA00'
	 * @return the position
	 */
	static Point postcodeLocation(String postcode) {
		int c = postcode.charAt(1) - 'A';
		int n = Integer.parseInt(postcode.substring(2));
		
		return new Point(n, c * 3);
	}
	/**
	 * Initializes the environment.
	 * @throws SQLException ignored
	 */
	public void init() throws SQLException {
		postcodes = db.queryReadOnly("SELECT * FROM postcodes", Postcode.SELECT);
		postcodeMap = new TLongObjectHashMap<>();
		for (Postcode p : postcodes) {
			postcodeMap.put(p.id, p);
		}
		
		vehicles = db.queryReadOnly("SELECT * FROM vehicles", Vehicle.SELECT);
		depotVehicles = HashMultimap.create();
		for (Vehicle v : vehicles) {
			depotVehicles.put(v.depot, v);
		}
		
		hubs = db.queryReadOnly("SELECT * FROM hubs", Hub.SELECT);
		hubMap = new TLongObjectHashMap<>();
		for (Hub h : hubs) {
			hubMap.put(h.id, h);
		}
		
		depots = db.queryReadOnly("SELECT * FROM depots", Depot.SELECT);
		depotMap = new TLongObjectHashMap<>();
		for (Depot d : depots) {
			depotMap.put(d.id, d);
		}
		
		holidays = new HashSet<>();
		db.queryReadOnly("SELECT holiday FROM holidays", SequenceUtils.into(holidays, DB.SELECT_DATEMIDNIGHT));
		
		warehouses = db.queryReadOnly("SELECT * FROM warehouses", Warehouse.SELECT);
		warehouseMap = HashMultimap.create();
		for (Warehouse w : warehouses) {
			warehouseMap.put(w.hub, w);
		}
		
		storages = db.queryReadOnly("SELECT * FROM storage_areas", StorageArea.SELECT);
		areas = HashMultimap.create();
		for (StorageArea s : storages) {
			areas.put(s.warehouse, s);
		}
		
		postcodeClosestDepot = new TLongObjectHashMap<>();
		for (Postcode pc : postcodes) {
			double dist = 0d;
			Depot dmin = null;
			Point pcp = postcodeLocation(pc.code);
			for (Depot d : depots) {
				Point dd = EnvironmentAgent.depotLocation(d.id);
				double dist1 = dd.distance(pcp);

				if (dmin == null || dist > dist1) {
					dmin = d;
					dist = dist1;
				}
			}
			postcodeClosestDepot.put(pc.id, dmin);
		}
		
		depotClosestHub = new TLongObjectHashMap<>();
		for (Depot d : depots) {
			Hub hmin = null;
			double dist = 0d;
			Point dd = EnvironmentAgent.depotLocation(d.id);
			for (Hub h : hubs) {
				Point hp = EnvironmentAgent.hubLocation(h.id);
				double dist1 = hp.distance(dd);
				if (hmin == null || dist > dist1) {
					hmin = h;
					dist = dist1;
				}
			}
			depotClosestHub.put(d.id, hmin);
		}

		List<LorryPosition> lorryPositions = db.queryReadOnly("SELECT * FROM lorry_positions", LorryPosition.SELECT);
		
		// ----------------------------------------------------------
		
		env = new EnvironmentAgent(db);
		env.setHubs(hubs);
		env.setWarehouses(warehouses);
		env.setStorageAreas(storages);
		env.setDepots(depots);
		env.setVehicles(vehicles);
		env.setLorryPositions(lorryPositions);
		
	}
	/**
	 * Generate.
	 * @throws SQLException ingored
	 */
	public void run() throws SQLException {
		DateMidnight startDay = new DateMidnight(2013, 9, 30);
		DateMidnight endDay = new DateMidnight(2013, 9, 30);
		
		DateMidnight day = startDay;
		
		while (day.compareTo(endDay) <= 0) {
			if (!holidays.contains(day)) {
				int dow = day.getDayOfWeek();
				if (dow != DateTimeConstants.SATURDAY && dow != DateTimeConstants.SUNDAY) {
					System.out.printf("Day: %s%n", day);
					List<Cons> cons = generate(day);
					saveCons(cons);
					db.commit();
				}
			}
			
			day = day.plusDays(1);
		}
		env.consignmentsEnd();
		env.eventLoop();
		if (env.hasUnporcessed()) {
			LOG.error("Items remained!");
		}
		db.commit();
	}
	/**
	 * Generate a day.
	 * @param day the day
	 * @throws SQLException ignored
	 * @return the generated consignments
	 */
	List<Cons> generate(DateMidnight day) throws SQLException {
		List<Cons> r = new ArrayList<>();
		int lt = new LocalTime(7, 0).getMillisOfDay() / 1000;
		int dy = day.getDayOfYear();
		int dm = day.getDayOfMonth();
		int dw = day.getDayOfWeek();
		int k = 0;
		int seconds = 0;
		for (Postcode pc : postcodes) {
			if (pc.id % 5 != dw - 1) {
				int nf = 0;
				int nh = 0;
				int nq = 0;
				switch ((int)(pc.id % 3)) {
				case 0:
					nf = 1;
					break;
				case 1:
					nh = 1;
					nf = dy % 2 == 0 ? 1 : 0;
					nq = dy % 2 != 0 ? 1 : 0;
					break;
				case 2:
					nh = 2;
					break;
				default:
				}
				
				Postcode dpc = postcodes.get((k + postcodes.size() / 3) % postcodes.size());
				
				Cons cs = new Cons();
				r.add(cs);
				cs.id = ++consId;
				cs.created = day.toDateTime().plusSeconds(lt + seconds);
				cs.itemCount = nf + nh + nq;
				cs.collectionPostcode = pc.id;
				cs.collectionDepot = postcodeClosestDepot.get(pc.id).id;
				cs.hub = depotClosestHub.get(cs.collectionDepot).id;
				cs.deliveryPostcode = dpc.id;
				cs.deliveryDepot = postcodeClosestDepot.get(dpc.id).id;
				cs.externalId = "C" + cs.id;
				
				int sli = ((dm + k) % 29);
				if (sli == 0) {
					cs.service = ServiceLevel.SPECIAL;
				} else
				if (sli < 5) {
					cs.service = ServiceLevel.STANDARD;
				} else
				if (sli < 9) {
					cs.service = ServiceLevel.PRIORITY;
				} else
				if (sli < 18) {
					cs.service = ServiceLevel.STANDARD;
				} else
				if (sli < 22) {
					cs.service = ServiceLevel.PRIORITY;
				} else {
					cs.service = ServiceLevel.STANDARD;
				}
				
				for (int i = 0; i < nf; i++) {
					ConsItem item = new ConsItem();
					item.consignment = cs;
					item.id = ++itemId;
					item.consignmentId = cs.id;
					item.length = 1;
					item.width = 1;
					item.height = 1;
					item.externalId = cs.externalId + "I" + item.id;
					cs.items.add(item);
				}
				for (int i = 0; i < nh; i++) {
					ConsItem item = new ConsItem();
					item.consignment = cs;
					item.id = ++itemId;
					item.consignmentId = cs.id;
					item.length = 1;
					item.width = 1;
					item.height = 0.5;
					item.externalId = cs.externalId + "I" + item.id;
					cs.items.add(item);
				}
				for (int i = 0; i < nq; i++) {
					ConsItem item = new ConsItem();
					item.consignment = cs;
					item.id = ++itemId;
					item.consignmentId = cs.id;
					item.length = 1;
					item.width = 1;
					item.height = 0.25;
					item.externalId = cs.externalId + "I" + item.id;
					cs.items.add(item);
				}

				for (ConsItem ci : cs.items) {
					env.newItem(ci);
				}
			}
			
			
			seconds = (seconds + 20);


			k++;
		}
		return r;
	}
	/**
	 * Save the consignments and items.
	 * @param seq the sequence
	 * @return the latest creation timestamp
	 * @throws SQLException on error
	 */
	DateTime saveCons(Iterable<Cons> seq) throws SQLException {
		DateTime max = null;
		for (Cons cs : seq) {
			db.update("INSERT IGNORE INTO consignments VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
					cs.id, cs.created, DateTime.class, 
					cs.hub, cs.collectionDepot, cs.collectionPostcode, 
					cs.deliveryDepot, cs.deliveryPostcode, 
					cs.service.ordinal(), cs.itemCount, cs.externalId);

			db.update("INSERT IGNORE INTO consignments_history VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
					cs.id, cs.created, DateTime.class, 
					cs.hub, cs.collectionDepot, cs.collectionPostcode, 
					cs.deliveryDepot, cs.deliveryPostcode, 
					cs.service.ordinal(), cs.itemCount, cs.externalId);

			for (Item item : cs.items) {
				db.update("INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?) ",
						item.id, cs.id, item.externalId, item.width, item.height, item.length);
				db.update("INSERT IGNORE INTO items_history VALUES (?, ?, ?, ?, ?, ?) ",
						item.id, cs.id, item.externalId, item.width, item.height, item.length);
			}
			if (max == null || max.compareTo(cs.created) < 0) {
				max = cs.created;
			}
		}
		return max;
	}
	/**
	 * Complete.
	 * @throws SQLException ignored
	 */
	public void done() throws SQLException {
		
	}
}