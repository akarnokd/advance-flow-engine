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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;

import eu.advance.logistics.live.reporter.db.MasterDB;
import eu.advance.logistics.live.reporter.model.HubDepotInfo;
import eu.advance.logistics.live.reporter.model.UOM;
import eu.advance.logistics.live.reporter.model.Warehouse;




/**
 * The extended WarehouseSwitch is for storing the actual user's settings at tablet. 
 * @author csirobi, 2013.02.04.
 */

public class WarehouseSwitch {
	/** The warehouse name. */
	private String warehouse;
	/** The warehouse display mode option. */
	private WarehouseOption l2warehouseOption;
	/** The storage area order. */
	private StorageAreaOrder storageOrder;

	/** Level 3 warehouse display mode option. */
	private WarehouseOption l3warehouseOption;
	/** Level 3 selected storage area ID. */
	private long l3SelectedStorageId;
	/** Level 3 warehouse name. */
	private String l3warehouse;

	/** The hub id. */
	private final long hubId;
	/** The user. */
	private final String user;
	/** The map of warehouse pairs. */
	private final Map<String, String> warehousePairMap;

	/**
	 * Constructor.
	 * @param hubId the hub
	 * @param warehouse the starting warehouse
	 * @param user the user name
	 */
	public WarehouseSwitch(long hubId, String user) {
		this.hubId = hubId;
		this.user = user;
		this.warehousePairMap = this.createWarehousePairMap();
		this.warehouse = this.getFirstWarehousePairTop();

		this.l2warehouseOption = WarehouseOption.A;
		this.storageOrder = StorageAreaOrder.PHYSICAL;

		this.l3warehouseOption = WarehouseOption.LEFT;    
		this.l3SelectedStorageId = 0;
		this.l3warehouse = this.warehouse;
	}

	/**
	 * Creates map for the warehouse pairs.
	 * @return map of the warehouse pairs
	 */
	private Map<String, String> createWarehousePairMap() {
		Map<String, String> wPair = new LinkedHashMap<>();
		List<Warehouse> list = MasterDB.warehouses(this.hubId);

		if (list.isEmpty() == false) {
			for (Warehouse item: list) {
				if ((wPair.containsKey(item.warehouse) == false) && (wPair.containsValue(item.warehouse) == false)) {
					String pair = Strings.isNullOrEmpty(item.pair) ? "" : item.pair;
					wPair.put(item.warehouse, pair);
				}
			}
		}

		return wPair;
	}

	/**
	 * Returns the map of the warehouse pairs.
	 * @return the map of the warehouse pairs
	 */
	public Map<String, String> getWarehousePairMap() {
		return this.warehousePairMap;
	}

	/**
	 * Returns the pair of the warehouse.
	 * @return the pair of the warehouse
	 */
	public String getPair() {
		String res = "";

		if (warehousePairMap.containsKey(this.warehouse)) {
			res = warehousePairMap.get(this.warehouse);
		}
		else if(warehousePairMap.containsValue(this.warehouse))
		{
			for(String keyItem : warehousePairMap.keySet())
			{
				if(warehousePairMap.get(keyItem).equals(this.warehouse))
				{
					res = keyItem;
				}
			}
		}

		return res;
	}

	/**
	 * Returns the top member of the first warehouse pair to initialize the warehouse. 
	 * @return the top member of the first warehouse pair
	 */
	public String getFirstWarehousePairTop()
	{
		String res = "";
		if(this.warehousePairMap.isEmpty() == false)
		{
			res = this.warehousePairMap.keySet().iterator().next();
		}

		return res;
	}

	/**
	 * Returns the top member of the actual warehouse pair.
	 * @return the top member of the actual warehouse pair
	 */
	public String getWarehousePairTop()
	{
		String res = "";

		if(warehousePairMap.containsKey(this.warehouse))
		{
			res = this.warehouse;
		}
		else if(warehousePairMap.containsValue(this.warehouse))
		{
			for(String keyItem : warehousePairMap.keySet())
			{
				if(warehousePairMap.get(keyItem).equals(this.warehouse))
				{
					res = keyItem;
				}
			}
		}

		return res;
	}

	/**
	 * Returns the bottom member of the actual warehouse pair.
	 * @return the bottom member of the actual warehouse pair
	 */
	public String getWarehousePairBottom()
	{
		String res = "";

		if(this.warehousePairMap.containsKey(this.warehouse))
		{
			res = this.warehousePairMap.get(this.warehouse);
		}
		else if(this.warehousePairMap.containsValue(this.warehouse))
		{
			res = this.warehouse;
		}

		return res;
	}

	/**
	 * Returns the hub of this warehouse.
	 * @return the hub id
	 */
	public long hubId() {
		return hubId;
	}

	/**
	 * Sets the actual warehosue.
	 * @param warehouse the actual warehosue
	 */
	public void setWarehouse(String warehouse)
	{
		this.warehouse = warehouse;
	}

	/**
	 * Set the level 2 warehouse option.
	 * @param option the option
	 */
	public void setL2WarehouseOption(String option) {
		this.l2warehouseOption = WarehouseOption.valueOf(option.toUpperCase());
	}
	/**
	 * Set the level 3 warehouse option.
	 * @param option the option
	 */
	public void setL3WarehouseOption(String option) {
		this.l3warehouseOption = WarehouseOption.valueOf(option.toUpperCase());
	}
	/**
	 * Set the storage area order.
	 * @param order the order
	 */
	public void setStorageAreaOrder(String order) {
		this.storageOrder = StorageAreaOrder.valueOf(order.toUpperCase());

	}
	/**
	 * Select storage area id.
	 * @param storageId the storage area identifier
	 */
	public void setL3SelectedStorageId(long storageId) {
		this.l3SelectedStorageId = storageId;
	}

	/**
	 * Sets the level 3 warehouse.
	 * @param warehouse the level 3 warehouse
	 */
	public void setL3Warehouse(String warehouse)
	{
		this.l3warehouse = warehouse;
	}

	/**
	 * @return the warehouse
	 */
	public String getWarehouse() {
		return this.warehouse;
	}
	/**
	 * @return the level 2 warehouse option
	 */
	public WarehouseOption getL2WarehouseOption()	{
		return this.l2warehouseOption;
	}
	/**
	 * @return the level 3 warehouse option
	 */
	public WarehouseOption getL3WarehouseOption()	{
		return this.l3warehouseOption;
	}
	/**
	 * @return the storage area order
	 */
	public StorageAreaOrder getStorageOrder() {
		return this.storageOrder;
	}
	/**
	 * @return the warehouse type
	 */
	public WarehouseType getWarehouseType() {
		WarehouseType type;

		switch (this.l2warehouseOption) {
		case A:
			type = WarehouseType.A;
			break;
		case B:
			type = WarehouseType.B;
			break;
		default:
			type = null;
		}
		return type;
	}
	/**
	 * @return the level 2 warehouse side
	 */
	public WarehouseSide getL2WarehouseSide()	{
		return WarehouseSwitch.warehouseSideFromOption(this.l2warehouseOption);
	}
	/**
	 * @return the level 3 warehouse side
	 */
	public WarehouseSide getL3WarehouseSide()	{
		return WarehouseSwitch.warehouseSideFromOption(this.l3warehouseOption);
	}
	/**
	 * Set the warehouse side from a warehouse option.
	 * @param option the option
	 * @return the side
	 */
	public static WarehouseSide warehouseSideFromOption(WarehouseOption option) {
		WarehouseSide side;

		switch (option) {
		case LEFT:
			side = WarehouseSide.LEFT;
			break;
		case RIGHT:
			side = WarehouseSide.RIGHT;
			break;
		default:
			side = null;
		}
		return side;
	}

	/**
	 * Get the opposite side of the input WarehouseSide.
	 * @param warehouseSide actual warehouse side
	 * @return the opposite side
	 */
	public static WarehouseSide oppositeWarehouseSide(WarehouseSide warehouseSide) {
		WarehouseSide side;
		switch (warehouseSide) {
		case LEFT:
			side = WarehouseSide.RIGHT;
			break;
		case RIGHT:
			side = WarehouseSide.LEFT;
			break;
		default:
			side = null;
		}
		return side;
	}

	/**
	 * @return the level 3 selected storage id
	 */
	public long getL3SelectedStorageId() {
		return this.l3SelectedStorageId;
	}

	/**
	 * Returns the Level 3 warehouse.
	 * @return the Level 3 warehouse
	 */
	public String getL3Warehouse()
	{
		return this.l3warehouse;
	}

	/**
	 * Returns the default floorspace scale.
	 * @return the default floorspace scale
	 */
	public int getFloorspaceScale() {
		return MasterDB.getHubSummaryValueMaxes(hubId, user).busAsUsualScale.get(UOM.FLOORSPACE) / 2;
	}
	/**
	 * Get the map of storage area raw data.
	 * @return map of storage area raw data
	 */
	public Map<L2DisplaySide, List<L2StorageRawData>> getL2StorageRawMap() {
		Map<L2DisplaySide, List<L2StorageRawData>> result = new LinkedHashMap<L2DisplaySide, List<L2StorageRawData>>();

		Map<String, Map<WarehouseSide, List<StorageAreaInfo>>> warehouseLayoutMap = MasterDB.getWarehouseLayoutMap(hubId);

		Warehouse wh = MasterDB.warehouse(hubId, warehouse);

		for (L2DisplaySide displaySide : L2DisplaySide.values()) {
			List<StorageAreaInfo> storageInfoList;

			WarehouseOption sOption = getL2WarehouseOption();
			switch(sOption) {
			case A:
				storageInfoList = warehouseLayoutMap.get(warehouse).get(WarehouseSide.values()[displaySide.ordinal()]);
				break;
			case B:
				if (wh.pair != null) {
					storageInfoList = warehouseLayoutMap.get(wh.pair).get(WarehouseSide.values()[displaySide.ordinal()]);
				} else {
					storageInfoList = new ArrayList<>();
				}
				break;
			default:
				storageInfoList = warehouseLayoutMap.get(warehouse).get(getL2WarehouseSide());
				break;
			}

			List<L2StorageRawData> storageRawList = new ArrayList<L2StorageRawData>();

			for (StorageAreaInfo storageInfoItem : storageInfoList) {
				L2StorageRawData storageRaw = new L2StorageRawData();

				switch(sOption) {
				case A:
				{
					storageRaw.warehouse = warehouse;
					storageRaw.type = getWarehouseType();
					storageRaw.side = WarehouseSide.values()[displaySide.ordinal()];
					break;
				}
				case B:
				{
					storageRaw.warehouse = wh.pair;
					storageRaw.type = getWarehouseType();
					storageRaw.side = WarehouseSide.values()[displaySide.ordinal()];
					break;
				}
				case LEFT:
				case RIGHT:  
				{
					storageRaw.warehouse = (displaySide == L2DisplaySide.LEFT) ? warehouse : wh.pair;
					storageRaw.type = WarehouseType.values()[displaySide.ordinal()];
					storageRaw.side = getL2WarehouseSide();
					break;
				}
				default:
				}

				storageRaw.id = storageInfoItem.id;

				for (WarehouseType st : WarehouseType.values()) {
					Map<WarehouseServiceLevel, Double> m = new HashMap<>();
					for (WarehouseServiceLevel ss : EnumSet.of(WarehouseServiceLevel.STANDARD, WarehouseServiceLevel.PRIORITY_SPECIAL)) {
						m.put(ss, storageInfoItem.capacity(st, ss));
					}
					storageRaw.storageCapacityMap.put(st, m);
				}

				storageRawList.add(storageRaw);
			}

			result.put(displaySide, storageRawList);
		}


		return result;     
	}
	/**
	 * Based on the selected warehouse menu, get the list of the depot/storage data for warehouse level 3. 
	 * @return the list of the depot/storage data
	 */
	public List<L3DepotStorageData> getL3DepotStorageList() {
		List<L3DepotStorageData> result = new ArrayList<L3DepotStorageData>();

		Map<String, Map<WarehouseSide, List<StorageAreaInfo>>> warehouseLayoutMap = MasterDB.getWarehouseLayoutMap(hubId);

		List<StorageAreaInfo> storageInfoList = warehouseLayoutMap.get(l3warehouse).get(getL3WarehouseSide());
		for (StorageAreaInfo storageInfoItem : storageInfoList) {
			HubDepotInfo depotInfo = MasterDB.getDepotSummaryValueMaxes(hubId, storageInfoItem.id, user);
			if (depotInfo != null) {
				L3DepotStorageData depotStorageData = new L3DepotStorageData(this);
				depotStorageData.depotName = depotInfo.name;
				depotStorageData.depotBusAsUsSc = depotInfo.busAsUsualScale.get(UOM.FLOORSPACE);

				depotStorageData.depot = storageInfoItem.id;

				for (WarehouseType st : WarehouseType.values()) {
					Map<WarehouseServiceLevel, Double> m = new HashMap<>();
					double d = 0;
					for (WarehouseServiceLevel ss : EnumSet.of(WarehouseServiceLevel.STANDARD, WarehouseServiceLevel.PRIORITY_SPECIAL)) {
						m.put(ss, storageInfoItem.capacity(st, ss));
						d += storageInfoItem.capacity(st, ss);
					}
					m.put(WarehouseServiceLevel.ALL, d);
					depotStorageData.storageCapacityMap.put(st, m);
				}
				result.add(depotStorageData);
			}
		}
		return result;
	}

	/**
	 * Get the opposite storage id for the warehouse level 3 chart.
	 * @param storageId actual storage id (it may be 0, which means it is not a real id)
	 * @param prevWarehouseSide selected previuos, opposite warehouse side 
	 * @return opposite storage id or 0, if input storageId is not a real id or could not find this storage Id  
	 */
	public long getL3OppositeStorageId(int storageId, WarehouseSide prevWarehouseSide)
	{
		long result = 0;

		Map<String, Map<WarehouseSide, List<StorageAreaInfo>>> warehouseLayoutMap = MasterDB.getWarehouseLayoutMap(hubId);

		// If the storageId from the request is real..
		if (storageId != 0)	{
			List<StorageAreaInfo> prevStorageInfoList = warehouseLayoutMap.get(l3warehouse).get(prevWarehouseSide);
			int ordinal = -1;

			// Find the ordinal of this storageId
			for (int i = 0; (i < prevStorageInfoList.size()) && (ordinal == -1); i++) {
				if (prevStorageInfoList.get(i).id == storageId) {
					ordinal = i;
				}
			}

			// If the storageId is found, get the opposite storageId from the actual side
			if (ordinal > -1) {
				List<StorageAreaInfo> actualStorageInfoList =  warehouseLayoutMap.get(l3warehouse).get(getL3WarehouseSide());
				// If the size of this actual side is less than the previous one..
				if (actualStorageInfoList.size() < ordinal) {
					result = actualStorageInfoList.get(actualStorageInfoList.size() - 1).id;
				} else {
					result = actualStorageInfoList.get(ordinal).id;
				}
			} else {
				result = 0;
			}

		}

		return result;
	}
}
