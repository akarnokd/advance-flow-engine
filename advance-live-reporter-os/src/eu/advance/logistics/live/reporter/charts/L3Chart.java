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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import eu.advance.logistics.live.reporter.model.ItemStatus;

/**
 * L3Chart is the main class for making JSON for the warehouse level 3 chart.
 * @author csirobi
 */
public final class L3Chart {
	/** Utility class. */
	private L3Chart() { }
	/**
	 * Generate JSON object.
	 * @param ws the warehouse switch
	 * @param depotStorageDataList the depot-storage data.
	 * @return the JSON object
	 */
	public static JSONObject getJSONtoChart(WarehouseSwitch ws, List<L3DepotStorageData> depotStorageDataList) {
		HashMap<Long, L3DepotStorageCoord> depotStorageCoordMap = new LinkedHashMap<>();
		L3DepotStorageCoord depotStorageCoord;

		for (L3DepotStorageData dataItem : depotStorageDataList) {
			depotStorageCoord = new L3DepotStorageCoord(dataItem.depotBusAsUsSc, dataItem.getFullStorageCapacity());
			depotStorageCoordMap.put(dataItem.depot, depotStorageCoord);
			dataItem.normalizeBy(depotStorageCoord);

			ChartProcess.getL3RelFullness(dataItem);
		}

		L3Chart.sortDepotStorageDataList(ws, depotStorageDataList);    

		JSONObject result = new JSONObject();
		result.put("status", L3Chart.createStatusJSON(ws));
		JSONArray charts = new JSONArray();
		for (L3DepotStorageData dataItem : depotStorageDataList) {
			JSONObject oneChart = new JSONObject();
			L3DepotStorageCoord coord = depotStorageCoordMap.get(dataItem.depot);

			oneChart.put("direction", L3Chart.createDirectionJSON(dataItem));
			oneChart.put("coordinate", L3Chart.createCoordJSON(coord));
			oneChart.put("bars", L3Chart.createBarsJSON(dataItem));
			charts.add(oneChart);
		}
		result.put("charts", charts);

		return result;
	}

	/**
	 * Sorts the depot-storage data list.
	 * @param ws the warehouse switch
	 * @param depotStorageDataList the data list
	 */
	private static void sortDepotStorageDataList(WarehouseSwitch ws, List<L3DepotStorageData> depotStorageDataList) {
		switch (ws.getStorageOrder()) {
		case RELATIVE:
			Collections.sort(depotStorageDataList, new Comparator<L3DepotStorageData>()
					{
				@Override
				public int compare(L3DepotStorageData o1, L3DepotStorageData o2) {
					return -1 * Double.compare(o1.relFullness, o2.relFullness);
				}
			});

			break;
		case NUMBER:
			Collections.sort(depotStorageDataList, new Comparator<L3DepotStorageData>()
					{
				@Override
				public int compare(L3DepotStorageData o1, L3DepotStorageData o2) {
					return Long.compare(o1.depot, o2.depot);
				}
			});

			break;
		default:
		}
	}
	/**
	 * Create the status JSON object.
	 * @param ws the warehouse switch
	 * @return the JSON object
	 */
	private static JSONObject createStatusJSON(WarehouseSwitch ws) {
		JSONObject result = new JSONObject();

		result.put("jumpStorageId", ws.getL3SelectedStorageId());

		JSONArray inner = new JSONArray();
		for (ItemStatus keyItem : L3DepotStorageData.DISPLAY_FUTURE_STATUS) {
			JSONObject statusRecord = new JSONObject();
			statusRecord.put("info", keyItem.getInfo());
			statusRecord.put("message", keyItem.getMessage());
			//Note: keyItem.ordinal() is the original ordinal, not the futureStatus..
			inner.add(statusRecord);
		}
		result.put("future", inner);

		inner = new JSONArray();
		for (L3WarehouseInfo keyItem : L3WarehouseInfo.values()) {
			JSONObject statusRecord = new JSONObject();
			statusRecord.put("info", keyItem.name().toLowerCase());
			statusRecord.put("message", keyItem.getMessage());
			inner.add(keyItem.ordinal(), statusRecord);      
		}
		result.put("athub", inner);
		return result;
	}
	/**
	 * Create the direction JSON object.
	 * @param data the data source
	 * @return the created JSON object
	 */
	private static JSONObject createDirectionJSON(L3DepotStorageData data) {
		JSONObject result = new JSONObject();
		result.put("storageId", data.depot);
		result.put("depotName", data.depotName);
		result.put("warehouseName", data.warehouse);

		return result;
	}
	/**
	 * Create the coordinates JSON object.
	 * @param coord the data source
	 * @return the created JSON object
	 */
	private static JSONObject createCoordJSON(L3DepotStorageCoord coord) {
		JSONObject result = new JSONObject();
		result.put("scaleInfo", "storage cap.");
		result.put("tickUnit", coord.tickUnit.toPlainString());
		result.put("noOfTotalTick", coord.noOfTick.toPlainString());
		result.put("normalScale", coord.normalScale.toPlainString());
		return result;
	}

	/**
	 * Create the bars JSON array.
	 * @param data the source data
	 * @return the created JSON array
	 */
	private static JSONArray createBarsJSON(L3DepotStorageData data) {
		L3WarehouseInfo[] infoOrder = { L3WarehouseInfo.WAREHOUSE_A, L3WarehouseInfo.WAREHOUSE_B };
		JSONArray result = new JSONArray();

		JSONObject inner, itemRec, whRec;
		JSONArray innerArr;

		for (WarehouseServiceLevel sKey: WarehouseServiceLevel.values()) {
			inner = new JSONObject();
			inner.put("unit", sKey.getMessage());

			// Generate for Future
			innerArr = new JSONArray();
			for (ItemStatus pKey: L3DepotStorageData.DISPLAY_FUTURE_STATUS) {
				itemRec = new JSONObject();
				itemRec.put("info", pKey.getInfo());
				itemRec.put("value", data.futureItems.get(sKey).get(pKey).value.toPlainString());
				itemRec.put("normalValue", data.futureItems.get(sKey).get(pKey).normalValue.toPlainString());
				innerArr.add(itemRec);
			}
			inner.put("future", innerArr);

			// Generate for AtHub
			innerArr = new JSONArray();
			for (WarehouseType tKey : WarehouseType.values())	{
				whRec = new JSONObject();
				whRec.put("info", infoOrder[tKey.ordinal()].name().toLowerCase());
				whRec.put("value", data.atHubItems.get(sKey).get(tKey).value.toPlainString());
				whRec.put("normalValue", data.atHubItems.get(sKey).get(tKey).normalValue.toPlainString());
				innerArr.add(whRec);
			}
			inner.put("athub", innerArr);
			result.add(inner);
		}

		return result;

	}
}
