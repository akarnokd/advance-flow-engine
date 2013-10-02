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
 * Lorry load/unload position in a warehouse.
 * @author karnokd, 2013.09.23.
 */
public class LorryPosition {
	/** Hub. */
	public long hub;
	/** Warehouse. */
	public String warehouse;
	/** The position index. */
	public int index;
	/** X coordinate. */
	public double x;
	/** Y coordinate. */
	public double y;
	/** Width. */
	public double width;
	/** Height. */
	public double height;
	/** Seconds to enter into the position. */
	public int enterTime;
	/** Seconds to leave the position and the warehouse. */
	public int leaveTime;
	/** Default select. */
	public static final SQLResult<LorryPosition> SELECT = new SQLResult<LorryPosition>() {
		@Override
		public LorryPosition invoke(ResultSet t) throws SQLException {
			LorryPosition r = new LorryPosition();
			
			r.hub = t.getLong(1);
			r.warehouse = t.getString(2);
			r.index = t.getInt(3);
			r.x = t.getDouble(4);
			r.y = t.getDouble(5);
			r.width = t.getDouble(6);
			r.height = t.getDouble(7);
			r.enterTime = t.getInt(8);
			r.leaveTime = t.getInt(9);
			
			return r;
		}
	};
}
