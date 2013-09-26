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
 * The depot record-class.
 * @author karnokd, 2013.09.23.
 */
public class Depot {
	/** Identifier. */
	public long id;
	/** Name. */
	public String name;
	/** Default selector. */
	public static final SQLResult<Depot> SELECT = new SQLResult<Depot>() {
		@Override
		public Depot invoke(ResultSet rs) throws SQLException {
			Depot d = new Depot();
			d.id = rs.getLong(1);
			d.name = rs.getString(2);
			return d;
		}
	};
}
