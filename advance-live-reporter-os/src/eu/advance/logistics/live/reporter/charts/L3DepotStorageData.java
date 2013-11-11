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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import eu.advance.logistics.live.reporter.model.ItemStatus;

/**
 * L3DepotStorageData stores the depot and the storage area data for warehouse level 3 chart.
 * @author csirobi
 */
public class L3DepotStorageData {

	/** Name of the layer 3 warehouse. */
	public String warehouse;
	/** Side of the warehouse. */
	public WarehouseSide side;
	/** Name of depot. */
	public String depotName;
	/** The depot. */
	public long depot;
	/** Floorspace business-as-usual-scale of the depot. */
	public double depotBusAsUsSc;

	/** Storage capacity. */
	public final Map<WarehouseType, Map<WarehouseServiceLevel, Double>> storageCapacityMap;

	/** Relative fullness of the storage. */
	public double relFullness;

	/** HashMap for future items summarized to A and B warehouse. */
	public final Map<WarehouseServiceLevel, Map<ItemStatus, BarData>> futureItems;
	/** HashMap for at hub items separate to A and B warehouse. */
	public final Map<WarehouseServiceLevel, Map<WarehouseType, BarData>> atHubItems;

	/**Future item statuses which are displayed.*/
	public static final ItemStatus[] DISPLAY_FUTURE_STATUS = {
	  ItemStatus.DECLARED,
	  ItemStatus.SCANNED,
    ItemStatus.CREATED
	};
	
	/**
	 * Constructor.
	 * @param warehouseSwitch the warehouse switch
	 */
	public L3DepotStorageData(WarehouseSwitch warehouseSwitch) {
		this.warehouse = warehouseSwitch.getL3Warehouse();
		this.side = warehouseSwitch.getL3WarehouseSide();

		this.storageCapacityMap = new HashMap<>();

		this.futureItems = new LinkedHashMap<>();
		this.atHubItems = new LinkedHashMap<>();
	}

	/**
	 * Returns the combined capacity of storage (A + B).
	 * @return the capacity value
	 */
	public double getFullStorageCapacity() {
		return this.getStorageCapacity(EnumSet.of(WarehouseType.A, WarehouseType.B), WarehouseServiceLevel.ALL);
	}

	/**
	 * Get the storage capacity based on the warehouse type and the warehouse service level.
	 * @param types enum set of the warehouse type
	 * @param service warehouse service level
	 * @return the storage capacity
	 */
	private double getStorageCapacity(EnumSet<WarehouseType> types, WarehouseServiceLevel service) {
		double sum = 0;
		for (WarehouseType st : types) {
			Double d = this.storageCapacityMap.get(st).get(service);
			if (d != null) {
				sum += d;
			}
		}    
		return sum;
	}

	/**
	 * Normalize value of items based on the different storage capacity.
	 * @param coord the coordinate object
	 */
	public void normalizeBy(L3DepotStorageCoord coord) {
		GraphDecimal capacity;

		for (WarehouseServiceLevel sKey : WarehouseServiceLevel.values())	{
			// For future items: count the warehouse type + warehouse service level specific capacity.
			capacity = new GraphDecimal(this.getStorageCapacity(EnumSet.of(WarehouseType.A, WarehouseType.B), sKey));
			for (ItemStatus pKey : L3DepotStorageData.DISPLAY_FUTURE_STATUS) {
				this.futureItems.get(sKey).get(pKey).normalizeWarehouseL3(capacity, coord.normalScale);
			}

			// For at_hub: count the warehouse type + warehouse service level specific capacity.
			for (WarehouseType tKey: WarehouseType.values()) {
				capacity = new GraphDecimal(this.getStorageCapacity(EnumSet.of(tKey), sKey));
				this.atHubItems.get(sKey).get(tKey).normalizeWarehouseL3(capacity, coord.normalScale);
			}
		}
	}

}
