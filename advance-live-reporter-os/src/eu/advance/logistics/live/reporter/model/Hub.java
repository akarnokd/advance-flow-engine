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

import hu.akarnokd.utils.database.SQLResult;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A hub record class.
 * @author karnokd, 2013.09.17.
 */
public class Hub {
	/** The identifier. */
	public long id;
	/** The name. */
	public String name;
	/** Width. */
	public double width;
	/** Height. */
	public double height;
	/** Select all hub fields. */
	public static final SQLResult<Hub> SELECT = new SQLResult<Hub>() {
		@Override
		public Hub invoke(ResultSet rs) throws SQLException {
			Hub h = new Hub();
			h.id = rs.getLong(1);
			h.name = rs.getString(2);
			h.width = rs.getDouble(3);
			h.height = rs.getDouble(4);
			return h;
		}
	};
}
