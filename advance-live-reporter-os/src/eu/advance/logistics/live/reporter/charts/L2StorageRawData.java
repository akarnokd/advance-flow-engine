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
 * L2StorageRawData stores different items for the selected warehouse.
 * @author csirobi
 */
public class L2StorageRawData {
	/** Name of the warehouse. */
	public String warehouse;
	/** The warehouse type. */
	public WarehouseType type;
	/** Side of the warehouse. */
	public WarehouseSide side;
	/** Id of the storage. */
	public long id;
	/** Storage area capacity based on the warehouse type and the warehouse service level. */
	public final Map<WarehouseType, Map<WarehouseServiceLevel, Double>> storageCapacityMap;

	/** HashMap for items. */
	public final Map<WarehouseServiceLevel, Map<ItemStatus, BarData>> items;
	
	/**Warehouse services which are used in this level.*/
	public static final WarehouseServiceLevel[] USED_SERVICES = {
	  WarehouseServiceLevel.STANDARD,
	  WarehouseServiceLevel.PRIORITY_SPECIAL
	};
	/**Item statuses which are used in this level.*/
	public static final ItemStatus[] USED_ITEMS = {
	  ItemStatus.AT_HUB,
    ItemStatus.DECLARED,
    ItemStatus.SCANNED,
    ItemStatus.CREATED
	};
	
	/**
	 * Constructor.
	 */
	public L2StorageRawData() {
		this.storageCapacityMap = new HashMap<>();
		this.items = new LinkedHashMap<>();
	}

	/**
	 * Normalize items data based on the floorspace capacity of the storage.
	 */
	public void normalizeByCap() {
		BarData bd;
		double capacity;

		for (WarehouseServiceLevel sKey : L2StorageRawData.USED_SERVICES) {
			// For at_hub: count the warehouse type + warehouse snip specific capacity.
			bd = this.items.get(sKey).get(ItemStatus.AT_HUB);
			capacity = this.storageCapacityMap.get(this.type).get(sKey);
			bd.normalize(new GraphDecimal(capacity));

			// For future items: count the warehouse type + warehouse snip specific capacity.
			capacity = this.storageCapacityMap.get(WarehouseType.A).get(sKey) + this.storageCapacityMap.get(WarehouseType.B).get(sKey);
			for (ItemStatus pKey : EnumSet.of(ItemStatus.DECLARED, ItemStatus.SCANNED, ItemStatus.CREATED)) {
				bd = this.items.get(sKey).get(pKey);
				bd.normalize(new GraphDecimal(capacity));
			}
		}

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "L2StorageRawData [warehouse=" + warehouse + ", type=" + type
				+ ", side=" + side + ", id=" + id + "]";
	}
	
}
