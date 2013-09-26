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

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * L1AtHubData stores actual data (at hub) of selected 
 * warehouse for the bottom chart in the warehouse level 1.
 * @author csirobi
 */
public class L1AtHubData {
	/**
	 * L1Warehouse inner class represents A or B part of selected warehouse
	 * for the bottom chart in the warehouse level 1.
	 * @author csirobi
	 */
	public class L1Warehouse extends BarData	{
		/** Capacity of the warehouse, based on name & type. */
		public GraphDecimal capacity;
		/**
		 * Constructor, initializes the values.
		 * @param value the value
		 * @param capacity the storage capacity
		 */
		public L1Warehouse(int value, double capacity) {
			super(value);
			this.capacity = new GraphDecimal(capacity);
		}
		/**
		 * Normalize by capacity.
		 * @param multiplyCap the capacity value
		 */
		public void normalizeByCap(GraphDecimal multiplyCap) {
			super.normalize(this.capacity.multiply(multiplyCap));
		}
		/**
		 * Returns the fill percent.
		 * @return the fill percent
		 */
		public String getPercent() {
			return super.getPercent(capacity);
		}
	}

	/**
	 * L1StorageArea inner class represents the worst storage area in A or B part of selected warehouse
	 * for the bottom chart in the warehouse level 1.
	 * @author csirobi
	 */
	public class L1StorageArea extends BarData {
		/** Id of the worst storage. */
		public long id;
		/** Capacity of the worst storage. */
		public GraphDecimal capacity;
		/**
		 * Constructor, sets the fields.
		 * @param id the depot id
		 * @param value the current value
		 * @param capacity the maximum capacity
		 */
		public L1StorageArea(long id, int value, int capacity) {
			super(value);
			this.id = id;
			this.capacity = new GraphDecimal(capacity);
		}
		/**
		 * Mormalize by capacity.
		 * @param multiplyCap the normalization value
		 */
		public void normalizeByCap(GraphDecimal multiplyCap) {
			super.normalize(this.capacity.multiply(multiplyCap));
		}
		/**
		 * Returns the value as a percentage of capacity.
		 * @return the percentage string
		 */
		public String getPercent() {
			return super.getPercent(capacity);
		}
	}


	/** Name of the selected warehouse. */
	public String warehouse;
	/** Store warehouses for every snip for the bottom chart. */
	public final Map<WarehouseServiceLevel, Map<WarehouseType, L1Warehouse>> warehouses;
	/** Store worst storage areas for every snip for the bottom chart. */
	public final Map<WarehouseServiceLevel, Map<WarehouseType, L1StorageArea>> worstStorageAreas;
	/**
	 * Constructor, initializes the maps.
	 * @param ws the warehouse switch
	 */
	public L1AtHubData(WarehouseSwitch ws) {
		this.warehouse = ws.getWarehouse();
		this.warehouses = new LinkedHashMap<>();
		this.worstStorageAreas = new LinkedHashMap<>();
	}

	/**
	 * Normalize value of items based on double of total coordinate.
	 * @param coord the coordinate
	 */
	public void normalizeBy(BarCoordinate coord) {
		GraphDecimal multiply = coord.noOfTick;

		for (Map<?, L1Warehouse> m1 : warehouses.values()) {
			for (L1Warehouse l1w : m1.values()) {
				l1w.normalizeByCap(multiply);
			}
		}
		for (Map<?, L1StorageArea> m1 : worstStorageAreas.values()) {
			for (L1StorageArea l1b : m1.values()) {
				l1b.normalizeByCap(multiply);
			}
		}
	}
}
