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

package eu.advance.logistics.live.reporter.charts;

import java.util.HashMap;
import java.util.Map;

/**
 * Storage area inner class represents the concrete node of storage area.  
 * @author csirobi, 2013.04.22.
 */
public class StorageAreaInfo implements Comparable<StorageAreaInfo> {
	/** Id of the storage. */
	public final long id;
	/** The storage area's index. */
	public final int index;
	/** The warehouse type where the storage was first encountered. */
	public final WarehouseType type;
	/** Average capacity based on the warehouse type and the warehouse service level. */
	public Map<WarehouseType, Map<WarehouseServiceLevel, Double>> avgCapacityMap;
	
	/**
	 * Constructor. Initializes the fields.
	 * @param id the storage identifier
	 * @param index the storage area's index
	 * @param stype The warehouse type where the storage was first encountered
	 */
	public StorageAreaInfo(long id, int index, WarehouseType stype) {
		this.id = id;
		this.index = index;
		this.type = stype;
		this.avgCapacityMap = new HashMap<>();
	}
	
	@Override
	public String toString() {
		return "StorageAreaInfo { id=" + id + ", avgCapacity=" + avgCapacityMap + " }";
	}
	/**
	 * Adds a capacity to the specified warehouse type and service level.
	 * @param stype the warehouse type (A or B)
	 * @param sl the service level (standard or priority)
 	 * @param capacity the capacity 
	 */
	public void add(WarehouseType stype, WarehouseServiceLevel sl, double capacity) {
		Map<WarehouseServiceLevel, Double> m = avgCapacityMap.get(stype);
		if (m == null) {
			m = new HashMap<>();
			avgCapacityMap.put(stype, m);
		}
		Double d = m.get(sl);
		if (d != null) {
			m.put(sl, d + capacity);
		} else {
			m.put(sl, capacity);
		}
	}
	/**
	 * Returns the aggregate capacity for all service types for a specific warehouse type (A or B).
	 * @param stype the warehouse type (A or B)
	 * @return the sum capacity
	 */
	public double capacity(WarehouseType stype) {
		Map<WarehouseServiceLevel, Double> m = avgCapacityMap.get(stype);
		if (m != null) {
			double r = 0;
			for (Double d : m.values()) {
				r += d;
			}
			return r;
		}
		return 0;
	}
	/**
	 * Returns the individual capacity of a warehouse type (A or B) and service level.
	 * @param stype the warehouse type (A or B)
	 * @param sl the service level
	 * @return the capacity
	 */
	public double capacity(WarehouseType stype, WarehouseServiceLevel sl) {
		Map<WarehouseServiceLevel, Double> m = avgCapacityMap.get(stype);
		if (m != null) {
			Double d = m.get(sl);
			if (d != null) {
				return d;
			}
		}		
		return 0;
	}
	@Override
	public int compareTo(StorageAreaInfo o) {
		int c = Integer.compare(index, o.index);
		if (c == 0) {
			// reverse warehouse type B first
			c = o.type.compareTo(type);
		}
		return c;
	}
}