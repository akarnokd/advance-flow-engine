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
import hu.akarnokd.utils.crypto.BCrypt;
import hu.akarnokd.utils.database.DB;
import hu.akarnokd.utils.sequence.SequenceUtils;

import java.awt.Point;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalTime;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import eu.advance.logistics.live.reporter.model.Consignment;
import eu.advance.logistics.live.reporter.model.Depot;
import eu.advance.logistics.live.reporter.model.Hub;
import eu.advance.logistics.live.reporter.model.Item;
import eu.advance.logistics.live.reporter.model.ItemEventType;
import eu.advance.logistics.live.reporter.model.Postcode;
import eu.advance.logistics.live.reporter.model.ServiceLevel;
import eu.advance.logistics.live.reporter.model.StorageArea;
import eu.advance.logistics.live.reporter.model.UserView;
import eu.advance.logistics.live.reporter.model.Vehicle;
import eu.advance.logistics.live.reporter.model.Warehouse;

/**
 * Prepares the database with demo data.
 * @author karnokd, 2013.09.24.
 */
public final class Demo {
	/** Demo. */
	private Demo() { }
	/**
	 * The demo program.
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		try (DB db = DB.connect()) {
			createMaster(db);

			db.commit();

			ConsignmentCreator cc = new ConsignmentCreator(db);
			cc.init();
			cc.run();
			cc.done();
			
			db.commit();
		}
	}
	/** 
	 * Create the basic hubs and depots.
	 * @param db the database connection
	 * @throws SQLException on error
	 */
	private static void createMaster(DB db) throws SQLException {
		// create hubs
		int nHubs = 2;
		for (int i = 1; i <= nHubs; i++) {
			db.update("INSERT IGNORE INTO hubs VALUES (?, ?, ?, ?)", i, "Hub " + i, 1000, 1000);
		}
		
		// create depots
		int nDepots = 20;
		for (int i = 1; i <= nDepots; i++) {
			db.update("INSERT IGNORE INTO depots VALUES (?, ?) ", i, "Depot " + i);
			
			int nVehicles = (int)Math.ceil(5d * i / nDepots);
			
			for (int j = 1; j <= nVehicles; j++) {
				db.update("INSERT IGNORE INTO vehicles VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
					"D" + i + "V" + j, i, Integer.class, Integer.class, 50, 4, 4, 15, 4000
				);
			}
		}
		
		// create warehouses per hub
		int nWarehouses = 4;
		int nLorryPositions = 4;
		
		Random rnd = new Random(0);
		
		for (int i = 1; i <= nHubs; i++) {
			for (int j = 1; j <= nWarehouses; j++) {
				int x = 500 + (j % 2 == 1 ? -100 : 100);
				int y = 500 + (j % 2 == 1 ? -150 : 150);
				int wi = ((i - 1) * nWarehouses + j);
				db.update("INSERT IGNORE INTO warehouses VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", 
						i, "Warehouse " + wi, x, y, 60, 100, 0, rnd.nextInt(5) + 5, "Warehouse " + (wi % 2 == 0 ? wi - 1 : wi + 1));
				
				int sa = (nDepots / 2) * ((j - 1) / 2);
				for (int k = 0; k < nDepots / 2; k++) {
					db.update("INSERT IGNORE INTO storage_areas VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
							i, "Warehouse " + wi, k % 2, k / 2, sa + k + 1, 20, 1.5, 0, 30
							);
				}
				for (int k = 0; k < nLorryPositions; k++) {
					int lx = k % 2 == 0 ? 23 : 32;
					int ly = k / 2 == 0 ? 10 : 60;
					
					int et = k / 2 == 0 ? 15 : 30;
					int lt = k / 2 == 0 ? 30 : 15;
					
					db.update("INSERT IGNORE INTO lorry_positions VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ",
						i, "Warehouse " + wi, k, lx, ly, 5, 20, et, lt);
				}
			}
		}
		
		
		// create hub, warehouse and depot users
		for (int i = 1; i <= nHubs; i++) {
			db.update("INSERT IGNORE INTO users (name, hub, depot, admin, default_view, password) VALUES (?, ?, ?, ?, ?, ?)", 
					"admin" + i, 1, Long.class, true, UserView.HUB.ordinal(), BCrypt.hashpw("admin" + i, BCrypt.gensalt()));

			db.update("INSERT IGNORE INTO users (name, hub, depot, admin, default_view, password) VALUES (?, ?, ?, ?, ?, ?)", 
					"hub" + i, 1, Long.class, false, UserView.HUB.ordinal(), BCrypt.hashpw("hub" + i, BCrypt.gensalt()));

			db.update("INSERT IGNORE INTO users (name, hub, depot, admin, default_view, password) VALUES (?, ?, ?, ?, ?, ?)", 
					"warehouse" + i, 1, Long.class, true, UserView.WAREHOUSE.ordinal(), BCrypt.hashpw("warehouse" + i, BCrypt.gensalt()));
		}

		for (int i = 1; i <= nDepots; i++) {
			db.update("INSERT IGNORE INTO users (name, hub, depot, admin, default_view, password) VALUES (?, ?, ?, ?, ?, ?)", 
				"depot" + i, 1, i, false, UserView.HUB.ordinal(), BCrypt.hashpw("depot" + i, BCrypt.gensalt()));
		}
		
		
		// create holidays
		int nStartYear = 2013;
		int nEndYear = 2020;
		for (int y = nStartYear; y <= nEndYear; y++) {
			db.update("INSERT IGNORE INTO holidays VALUES (?)", new DateMidnight(y, 1, 1));
			db.update("INSERT IGNORE INTO holidays VALUES (?)", new DateMidnight(y, 12, 25));
			db.update("INSERT IGNORE INTO holidays VALUES (?)", new DateMidnight(y, 12, 26));
			db.update("INSERT IGNORE INTO holidays VALUES (?)", new DateMidnight(y, 12, 31));
		}
		
		int nPostcodes = 2000;
		for (int i = 0; i < nPostcodes; i++) {
			String pcg = "X" + (char)('A' + (i / 100));
			
			String pc = String.format("%s%02d", pcg, i % 100);
			
			db.update("INSERT IGNORE INTO postcodes VALUES (?, ?, ?) ",
					i + 1, pc, pcg);
		}
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
	 * The consignment item with back-reference to the consignment itself.
	 * @author karnokd, 2013.10.01.
	 */
	static class ConsItem extends Item {
		/** The parent consignment. */
		public Cons consignment;
	}
	/** The created consignment. */
	static class Cons extends Consignment {
		/** The consignment items. */
		public final List<ConsItem> items = new ArrayList<>();
	}
	/**
	 * The base agent class.
	 * @author karnokd, 2013.10.01.
	 */
	static class BaseAgent {
		/** The database connection. */
		DB db;
		/**
		 * Set the database connection.
		 * @param db the database connection
		 */
		public void setDb(DB db) {
			this.db = db;
		}
	}
	/**
	 * A depot manager "agent".
	 * @author karnokd, 2013.10.01.
	 *
	 */
	static class DepotAgent extends BaseAgent {
		/** Depot identifier. */
		public long id;
		/** The items to deliver. */
		public final List<ConsItem> toDelivery = new ArrayList<>();
		/** The items to dispatch. */
		public final List<ConsItem> toDispatch = new ArrayList<>();
		/** The list of vehicles. */
		public final List<VehicleAgent> vehicles = new ArrayList<>();
		
	}
	/**
	 * A vehicle delivering items to and from hub.
	 * @author karnokd, 2013.10.01.
	 */
	static class VehicleAgent extends BaseAgent {
		/** The id. */
		public String id;
		/** The owner/target depot. */
		public long depot;
		/** The vehicle capacity. */
		public int capacity;
		/** The target hub, if moving towards it. */
		public Long targetHub;
		/** The session identifier. */
		public String sessionId;
		/** In a warehouse if not null. */
		public String warehouse;
		/** In a lorry position if not null. */
		public Integer position;
		/** The contents. */
		public final List<ConsItem> contents = new ArrayList<>();
	}
	/**
	 * The hub agent.
	 * @author karnokd, 2013.10.01.
	 */
	static class HubAgent extends BaseAgent {
		/** Identifier. */
		public long id;
		/** List of vehicles on site. */
		public final List<VehicleAgent> vehiclesOnSite = new ArrayList<>();
		/** List of vehicles. */
		public final List<WarehouseAgent> warehouses = new ArrayList<>();
	}
	/**
	 * A warehouse managing items.
	 * @author karnokd, 2013.10.01.
	 */
	static class WarehouseAgent extends BaseAgent {
		/** The hub. */
		public long hub;
		/** The warehouse. */
		public String warehouse;
		/** The depots and the current contents. */
		public final Multimap<Long, ConsItem> storageAreas = HashMultimap.create();
		/** List of vehicles on site. */
		public final List<VehicleAgent> vehicles = new ArrayList<>();
	}
	/**
	 * Consignment creator.
	 * @author karnokd, 2013.10.01.
	 *
	 */
	static class ConsignmentCreator {
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
		/** The map of depots. */
		private final TLongObjectMap<DepotAgent> depotAgents = new TLongObjectHashMap<>();
		/** The map of vehicles. */
		private final TLongObjectMap<VehicleAgent> vehicleAgents = new TLongObjectHashMap<>();
		/** The map of warehouses. */
		private final Map<String, WarehouseAgent> warehouseAgents = new HashMap<>();
		/** The map of hub agents. */
		private final TLongObjectMap<HubAgent> hubAgents = new TLongObjectHashMap<>();
		/**
		 * Constructor, sets the database connection.
		 * @param db the database connection
		 */
		public ConsignmentCreator(DB db) {
			this.db = db;
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
					Point dd = depotLocation(d.id);
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
				Point dd = depotLocation(d.id);
				for (Hub h : hubs) {
					Point hp = hubLocation(h.id);
					double dist1 = hp.distance(dd);
					if (hmin == null || dist > dist1) {
						hmin = h;
						dist = dist1;
					}
				}
				depotClosestHub.put(d.id, hmin);
			}

		}
		/**
		 * Generate.
		 * @throws SQLException ingored
		 */
		public void run() throws SQLException {
			DateMidnight startDay = new DateMidnight(2013, 7, 1);
			DateMidnight endDay = new DateMidnight(2013, 12, 31);
			
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
		}
		/**
		 * Generate a day.
		 * @param day the day
		 * @throws SQLException ignored
		 * @return the generated consignments
		 */
		List<Cons> generate(DateMidnight day) throws SQLException {
			List<Cons> r = new ArrayList<>();
			LocalTime lt = new LocalTime(7, 0);
			int dy = day.getDayOfYear();
			int dm = day.getDayOfMonth();
			int dw = day.getDayOfWeek();
			int k = 0;
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
					cs.created = day.toDateTime().withTime(lt.getHourOfDay(), lt.getMinuteOfHour(), lt.getSecondOfMinute(), lt.getMillisOfSecond());
					cs.itemCount = nf + nh + nq;
					cs.collectionPostcode = pc.id;
					cs.collectionDepot = postcodeClosestDepot.get(pc.id).id;
					cs.hub = depotClosestHub.get(cs.collectionDepot).id;
					cs.deliveryPostcode = dpc.id;
					cs.deliveryDepot = postcodeClosestDepot.get(dpc.id).id;
					
					int sli = ((dm + k) % 30);
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
						item.length = 1;
						item.width = 1;
						item.height = 1;
						cs.items.add(item);
					}
					for (int i = 0; i < nh; i++) {
						ConsItem item = new ConsItem();
						item.consignment = cs;
						item.id = ++itemId;
						item.length = 1;
						item.width = 1;
						item.height = 0.5;
						cs.items.add(item);
					}
					for (int i = 0; i < nq; i++) {
						ConsItem item = new ConsItem();
						item.consignment = cs;
						item.id = ++itemId;
						item.length = 1;
						item.width = 1;
						item.height = 0.25;
						cs.items.add(item);
					}
				}
				
				lt = lt.plusSeconds(15);
				k++;
			}
			return r;
		}
		/**
		 * Save the consignments and items.
		 * @param seq the sequence
		 * @throws SQLException on error
		 */
		void saveCons(Iterable<Cons> seq) throws SQLException {
			for (Cons cs : seq) {
				db.update("INSERT IGNORE INTO consignments VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
						cs.id, cs.created, DateTime.class, 
						cs.hub, cs.collectionDepot, cs.collectionPostcode, 
						cs.deliveryDepot, cs.deliveryPostcode, 
						cs.service.ordinal(), cs.itemCount, String.class);

				db.update("INSERT IGNORE INTO consignments_history VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
						cs.id, cs.created, DateTime.class, 
						cs.hub, cs.collectionDepot, cs.collectionPostcode, 
						cs.deliveryDepot, cs.deliveryPostcode, 
						cs.service.ordinal(), cs.itemCount, String.class);

				for (Item item : cs.items) {
					db.update("INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?) ",
							item.id, cs.id, String.class, item.width, item.height, item.length);
					db.update("INSERT IGNORE INTO items_history VALUES (?, ?, ?, ?, ?, ?) ",
							item.id, cs.id, String.class, item.width, item.height, item.length);
					
					db.update("INSERT IGNORE INTO events VALUES (?, ?, ?, ?)",
							item.id, cs.id, cs.created, ItemEventType.CREATED.ordinal());
				}
			}
		}
		/**
		 * Returns the distance between a postcode and a depot.
		 * @param postcode the postcode 
		 * @param depot the depot
		 * @return the distance
		 */
		public double postcodeDepotDistance(long postcode, long depot) {
			Point pc = postcodeLocation(postcodeMap.get(postcode).code);
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
		 * Complete.
		 * @throws SQLException ignored
		 */
		public void done() throws SQLException {
			
		}
	}
}
