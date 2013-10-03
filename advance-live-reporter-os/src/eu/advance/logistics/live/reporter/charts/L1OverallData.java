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
import java.util.LinkedHashMap;
import java.util.Map;

import eu.advance.logistics.live.reporter.model.ItemStatus;

/**
 * L1OverallData stores overall data of selected warehouse for the upper chart in the warehouse level 1. 
 * @author csirobi
 */
public class L1OverallData {
	/** Name of the warehouse. */
	public String warehouse;
	/** Half part of business-as-usual-scale(floorspace) of hub. */
	public int busAsUsSc;
	/** HashMap for items. */
	public final Map<WarehouseServiceLevel, Map<ItemStatus, BarData>> items;
	/**
	 * Constructor, initializes the fields.
	 * @param warehouseSwitch the warehouse switch
	 */
	public L1OverallData(WarehouseSwitch warehouseSwitch) {
		this.warehouse = warehouseSwitch.getWarehouse();
		this.busAsUsSc = warehouseSwitch.getFloorspaceScale();
		this.items = new LinkedHashMap<>();
	}

	/**
	 * Get that item statuses which are displayed in the UI.
	 * @return EnumSet of the used item statuses
	 */
	public static EnumSet<ItemStatus> getDisplayItems()	{
		EnumSet<ItemStatus> result = EnumSet.of(ItemStatus.LEFT_HUB_TODAY,
				ItemStatus.AT_HUB,
				ItemStatus.DECLARED,
				ItemStatus.SCANNED,
				ItemStatus.CREATED);

		return result;
	}

	/**
	 * Normalize value of items based on total coordinate.
	 * @param coord the coordinates
	 */
	public void normalizeBy(BarCoordinate coord) {
		GraphDecimal ratio = coord.totalCoord;

		for (WarehouseServiceLevel sKey : WarehouseServiceLevel.values()) {
			for (ItemStatus pKey : ItemStatus.values())	{
				BarData bd = this.items.get(sKey).get(pKey);
				if (bd != null) {
					bd.normalize(ratio);
				}
			}
		}
	}

	/**
	 * Checking the business-as-usual-scale(floorspace) to be bigger than the sum items data.
	 */
	public void checkBaus()	{
		GraphDecimal value = new GraphDecimal(0);

		for (ItemStatus pKey : ItemStatus.values()) {
			value = value.add(new GraphDecimal(this.items.get(WarehouseServiceLevel.ALL).get(pKey).value));
		}

		if (value.intValue() > this.busAsUsSc) {
			this.busAsUsSc = value.intValue();
		}
	}


}
