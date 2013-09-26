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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import eu.advance.logistics.live.reporter.model.ItemStatus;

/**
 * L1Chart is the main class for making JSON for the warehouse level 1 chart.
 * @author csirobi
 */
public final class L1Chart {
	/** Helper class. */
	private L1Chart() { }
	/** Overall chart: Maximum unit of every tick. */
	private static final int OVERALL_MAX_TICK_UNIT = 4;
	/** Overall chart: Maximum number of the ticks in the ruler. */
	private static final int OVERALL_MAX_NO_OF_TICK = 12;
	/** At_hub chart : Maximum unit of every tick. */
	private static final int ATHUB_MAX_TICK_UNIT = 1;
	/** At_hub chart : Maximum number of the ticks in the ruler. */
	private static final int ATHUB_MAX_NO_OF_TICK = 2;

	/**
	 * Convert data to JSON object.
	 * @param overData the overview data
	 * @param atHubData the at hub data
	 * @return the JSON object
	 */
	public static JSONObject getJSONtoChart(L1OverallData overData, L1AtHubData atHubData) {
	  overData.checkBaus();
	  
		BarCoordinate overallCoord = new BarCoordinate(overData.busAsUsSc, OVERALL_MAX_TICK_UNIT, OVERALL_MAX_NO_OF_TICK);
		overData.normalizeBy(overallCoord);
		BarCoordinate atHubCoord = new BarCoordinate(ATHUB_MAX_NO_OF_TICK, ATHUB_MAX_TICK_UNIT, ATHUB_MAX_NO_OF_TICK);
		atHubData.normalizeBy(atHubCoord);

		JSONObject result = new JSONObject();
		result.put("status", L1Chart.createStatusJSON());
		result.put("coordinate", L1Chart.createCoordJSON(overallCoord, atHubCoord));
		result.put("bars", L1Chart.createBarsJSON(overData, atHubData));

		return result;
	}
	/**
	 * Create a status JSON object.
	 * @return the JSON object
	 */
	private static JSONObject createStatusJSON() {
		JSONObject result = new JSONObject();

		JSONArray inner = new JSONArray();
		for (ItemStatus keyItem : L1OverallData.getDisplayItems()) {
			JSONObject statusRecord = new JSONObject();
			statusRecord.put("info", keyItem.getInfo());
			statusRecord.put("message", keyItem.getMessage());
			inner.add(keyItem.ordinal(), statusRecord);      
		}
		result.put("overall", inner);

		inner = new JSONArray();
		for (L1WarehouseInfo keyItem : L1WarehouseInfo.values()) {
			JSONObject statusRecord = new JSONObject();
			statusRecord.put("info", keyItem.name().toLowerCase());
			statusRecord.put("message", keyItem.getMessage());
			inner.add(keyItem.ordinal(), statusRecord);      
		}
		result.put("athub", inner);


		return result;
	}
	/**
	 * Create coordinates JSON object.
	 * @param overallCoord the overview data
	 * @param atHubCoord the coordinates data
	 * @return the JSON object
	 */
	private static JSONObject createCoordJSON(BarCoordinate overallCoord, BarCoordinate atHubCoord)	{
		JSONObject result = new JSONObject();

		JSONObject inner = new JSONObject();
		inner.put("scale", overallCoord.scale.toPlainString());
		inner.put("tickUnit", overallCoord.tickUnit.toPlainString());
		inner.put("noOfTotalTick", overallCoord.noOfTick.toPlainString());
		inner.put("normalScale", overallCoord.normalScale.toPlainString());
		inner.put("logicalTotalCoord", overallCoord.totalCoord.toPlainString());
		result.put("overall", inner);

		inner = new JSONObject();
		inner.put("scaleInfo", "nominal cap.");
		inner.put("tickUnit", atHubCoord.tickUnit.toPlainString());
		inner.put("noOfTotalTick", atHubCoord.noOfTick.toPlainString());
		inner.put("normalScale", atHubCoord.normalScale.toPlainString());
		result.put("athub", inner);

		return result;
	}
	/**
	 * Create the bars JSON array.
	 * @param overData the overview data
	 * @param atHubData the hub data
	 * @return the JSON array
	 */
	private static JSONArray createBarsJSON(L1OverallData overData, L1AtHubData atHubData) {
		L1WarehouseInfo[] infoOrder = { L1WarehouseInfo.A_TOTAL, L1WarehouseInfo.A_WORST, L1WarehouseInfo.B_TOTAL, L1WarehouseInfo.B_WORST};
		String[] atHubType = { "warehouse", "storage" };

		JSONArray result = new JSONArray();

		JSONObject inner, itemRec, whRec;
		JSONArray innerArr;

		for (WarehouseServiceLevel sKey: WarehouseServiceLevel.values()) {
			inner = new JSONObject();
			inner.put("unit", sKey.getMessage());

			// Generate for Overall
			innerArr = new JSONArray();
			for (ItemStatus pKey: L1OverallData.getDisplayItems()) {
				itemRec = new JSONObject();
				itemRec.put("info", pKey.getInfo());
				itemRec.put("value", overData.items.get(sKey).get(pKey).value.toPlainString());
				itemRec.put("normalValue", overData.items.get(sKey).get(pKey).normalValue.toPlainString());
				innerArr.add(itemRec);
			}
			inner.put("overall", innerArr);

			// Generate for AtHub
			innerArr = new JSONArray(); int j = 0;
			for (WarehouseType tKey : WarehouseType.values())	{
				for (int i = 0; i < atHubType.length; i++) {
					whRec = new JSONObject();
					whRec.put("type", atHubType[i]);
					whRec.put("info", infoOrder[j].name().toLowerCase());
					if (i == 0)	{
						whRec.put("percent", atHubData.warehouses.get(sKey).get(tKey).getPercent());
						whRec.put("value", atHubData.warehouses.get(sKey).get(tKey).value.toPlainString());
						whRec.put("normalValue", atHubData.warehouses.get(sKey).get(tKey).normalValue.toPlainString());
					} else {
						whRec.put("id", atHubData.worstStorageAreas.get(sKey).get(tKey).id);
						whRec.put("percent", atHubData.worstStorageAreas.get(sKey).get(tKey).getPercent());
						whRec.put("value", atHubData.worstStorageAreas.get(sKey).get(tKey).value.toPlainString());
						whRec.put("normalValue", atHubData.worstStorageAreas.get(sKey).get(tKey).normalValue.toPlainString());
					}
					innerArr.add(whRec); j++;
				}
			}
			inner.put("athub", innerArr);

			result.add(inner);
		}

		return result;
	}
}
