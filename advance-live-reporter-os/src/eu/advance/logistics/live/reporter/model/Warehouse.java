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

import hu.akarnokd.utils.database.SQLResult;

/**
 * A warehouse record class.
 * @author karnokd, 2013.09.23.
 */
public class Warehouse {
	/** Hub. */
	public long hub;
	/** Warehouse name. */
	public String warehouse;
	/** X coordinate inside the hub. */
	public double x;
	/** Y coordinate inside the hub. */
	public double y;
	/** Width. */
	public double width;
	/** Height. */
	public double height;
	/** The angle. */
	public double angle;
	/** Number of forklifts. */
	public int forklifts;
	/** The pair warehouse. */
	public String pair;
	/** Default selector. */
	public static final SQLResult<Warehouse> SELECT = new SQLResult<Warehouse>() {
		@Override
		public Warehouse invoke(ResultSet t) throws SQLException {
			Warehouse r = new Warehouse();
			r.hub = t.getLong(1);
			r.warehouse = t.getString(2);
			r.x = t.getDouble(3);
			r.y = t.getDouble(4);
			r.width = t.getDouble(5);
			r.height = t.getDouble(6);
			r.angle = t.getDouble(7);
			r.forklifts = t.getInt(8);
			r.pair = t.getString(9);
			return r;
		}
	};
}
