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
package eu.advance.logistics.live.reporter.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import hu.akarnokd.utils.database.DB;
import hu.akarnokd.utils.database.SQLResult;

/**
 * A vehicle record class.
 * @author karnokd, 2013.09.23.
 */
public class Vehicle {
	/** The vehicle identifier. */
	public String vehicleId;
	/** Owner depot. */
	public long depot;
	/** Current job id if not null. */
	public Long currentJob;
	/** Current position in warehouse if not null. */
	public Integer currentPosition;
	/** The capacity in items. */
	public int capacity;
	/** Storage width. */
	public double width;
	/** Storage height. */
	public double height;
	/** Storage length. */
	public double length;
	/** Storage weight. */
	public double weight;
	/** Default select. */
	public static final SQLResult<Vehicle> SELECT = new SQLResult<Vehicle>() {
		@Override
		public Vehicle invoke(ResultSet t) throws SQLException {
			Vehicle r = new Vehicle();
			
			r.vehicleId = t.getString(1);
			r.depot = t.getLong(2);
			r.currentJob = DB.getLong(t, 3);
			r.currentPosition = DB.getInt(t, 4);
			r.capacity = t.getInt(5);
			r.width = t.getDouble(6);
			r.height = t.getDouble(7);
			r.length = t.getDouble(8);
			r.weight = t.getDouble(9);
			
			return r;
		}
	};
}
