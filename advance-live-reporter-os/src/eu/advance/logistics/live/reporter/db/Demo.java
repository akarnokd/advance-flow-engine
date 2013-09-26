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

import hu.akarnokd.utils.crypto.BCrypt;
import hu.akarnokd.utils.database.DB;

import java.util.Random;

import org.joda.time.DateMidnight;

import eu.advance.logistics.live.reporter.model.UserView;

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
			// create hubs
			int nHubs = 2;
			for (int i = 1; i <= nHubs; i++) {
				db.update("INSERT IGNORE INTO hubs VALUES (?, ?, ?, ?)", i, "Hub " + i, 1000, 1000);
			}
			
			// create depots
			int nDepots = 20;
			for (int i = 1; i <= nDepots; i++) {
				db.update("INSERT IGNORE INTO depots VALUES (?, ?) ", i, "Depot " + i);
			}
			
			// create warehouses per hub
			int nWarehouses = 4;
			
			Random rnd = new Random(0);
			
			for (int i = 1; i <= nHubs; i++) {
				for (int j = 1; j <= nWarehouses; j++) {
					int x = 500 + (j % 2 == 1 ? -100 : 100);
					int y = 500 + (j % 2 == 1 ? -150 : 150);
					int wi = ((i - 1) * nWarehouses + j);
					db.update("INSERT IGNORE INTO warehouses VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", 
							i, "Warehouse " + wi, x, y, 60, 100, 0, rnd.nextInt(5) + 5, "Warehouse " + (wi % 2 == 0 ? wi - 1 : wi + 1));
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
			
			
			
			db.commit();
		}
	}
}
