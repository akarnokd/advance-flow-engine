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
import java.util.Comparator;
import java.util.EnumSet;

/**
 * Dimensions and properties of a storage area inside a warehouse.
 * @author karnokd, 2013.09.23.
 */
public class StorageArea {
	/** The hub. */
	public long hub;
	/** The warehouse. */
	public String warehouse;
	/** The side of the warehouse. */
	public StorageSide side;
	/** The index. */
	public int index;
	/** The destination depot. */
	public long depot;
	/** The width of the storage area. */
	public double width;
	/** The height of the storage area. */
	public double height;
	/** The nominal capacity. */
	public int capacity;
	/** The set of service levels that can be put into this storage. */
	public final EnumSet<ServiceLevel> services = EnumSet.noneOf(ServiceLevel.class);
	/** Default selector. */
	public static final SQLResult<StorageArea> SELECT = new SQLResult<StorageArea>() {
		@Override
		public StorageArea invoke(ResultSet t) throws SQLException {
			StorageArea r = new StorageArea();
			
			r.hub = t.getLong(1);
			r.warehouse = t.getString(2);
			r.side = StorageSide.values()[t.getInt(3)];
			r.index = t.getInt(4);
			r.depot = t.getLong(5);
			r.width = t.getDouble(6);
			r.height = t.getDouble(7);
			
			int flags = t.getInt(8);
			r.flags(flags);
			r.capacity = t.getInt(9);
			
			return r;
		}
	};
	/**
	 * Creates a copy of this object.
	 * @return the copy
	 */
	public StorageArea copy() {
		StorageArea r = new StorageArea();
		
		r.hub = hub;
		r.warehouse = warehouse;
		r.side = side;
		r.index = index;
		r.depot = depot;
		r.width = width;
		r.height = height;
		r.services.addAll(services);
		r.capacity = capacity;
		
		return r;
	}
	/**
	 * Returns the flags enumset as integer.
	 * @return the flags
	 */
	public int flags() {
		int r = 0;
		for (ServiceLevel sl : services) {
			r |= 1 << sl.ordinal();
		}
		return r;
	}
	/**
	 * Sets the flags enum set from integer bitflags.
	 * @param flags the flags
	 */
	public void flags(int flags) {
		services.clear();
		ServiceLevel[] slv = ServiceLevel.values();
		for (int i = 0; i < slv.length; i++) {
			if ((flags & (1 << i)) != 0) {
				services.add(slv[i]);
			}
		}
	}
	/** Index reverse comparator. */
	public static final Comparator<StorageArea> INDEX_REVERSE = new Comparator<StorageArea>() {
		@Override
		public int compare(StorageArea o1, StorageArea o2) {
			return Integer.compare(o2.index, o1.index);
		}
	};
}
