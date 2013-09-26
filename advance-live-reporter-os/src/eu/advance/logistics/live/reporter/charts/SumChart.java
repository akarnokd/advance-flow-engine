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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import eu.advance.logistics.live.reporter.model.ChartView;
import eu.advance.logistics.live.reporter.model.ItemStatus;
import eu.advance.logistics.live.reporter.model.ServiceLevel;

/**
 * The SumChart main class for making JSON.
 * @author csirobi, 2013.02.04.
 */
public final class SumChart {
	/** Helper class. */
	private SumChart() { }
	/** Maximum unit of every tick. */
	private static final int MAX_TICK_UNIT = 4;
	/** Maximum number of the ticks in the ruler. */
	private static final int MAX_NO_OF_TICK = 25;
	/**
	 * Create the summary JSON object.
	 * @param sumData the data source
	 * @return the created JSON object
	 */
	public static JSONObject getSummaryJSON(SumData sumData) {
	  sumData.checkBaus();
	  
		BarCoordinate coord = null;
		switch (sumData.type) {
		case HUB:
			coord = new HubCoord(sumData, MAX_TICK_UNIT, MAX_NO_OF_TICK);
			break;
		case DEPOT:
		  // Count coord included based on chartView
			coord = new DepotCoord(sumData, MAX_TICK_UNIT, MAX_NO_OF_TICK);
			break;
		default:
			throw new IllegalStateException();
		}

		sumData.normalizeBy(coord);

		JSONObject result = new JSONObject();
		result.put("direction", SumChart.createDirectionJSON(sumData));
		result.put("status", SumChart.createStatusJSON());
		result.put("coordinate", SumChart.createCoordJSON(coord, sumData));
		result.put("bars", SumChart.createBarsJSON(coord, sumData));

		return result;
	}

	/**
	 * Create the direction JSON object.
	 * @param sumData the data source
	 * @return the created JSON object
	 */
	private static JSONObject createDirectionJSON(SumData sumData) {
		JSONObject result = new JSONObject();
		JSONArray orientType = new JSONArray();

		result.put("chartView", sumData.chartView.name().toLowerCase());
		result.put("type", sumData.type.getInfo());
		result.put("unit", sumData.unit.getInfo());
		result.put("normalZeroX", (sumData.chartView == ChartView.HUB_USER) ? sumData.maxLeftHub(false).toPlainString() : "0");

		switch (sumData.type) {
		case HUB:
			orientType.add(OrientStatus.SINGLE.getInfo());
			break;
		case DEPOT:
			orientType.add(OrientStatus.ORIGIN.getInfo());
			orientType.add(OrientStatus.DESTIN.getInfo());
			break;
		default:
			throw new IllegalStateException();
		}
		result.put("orient", orientType);  

		return result;
	}

	/**
	 * Create the coordinate JSON object.
	 * @param coord the coordinate object
	 * @param sumData the data source
	 * @return the created JSON object
	 */
	private static JSONObject createCoordJSON(BarCoordinate coord, SumData sumData) {
		JSONObject result = new JSONObject();

		result.put("scale", coord.scale.toPlainString());
		result.put("tickValue", coord.tickUnit.toPlainString());

		switch(sumData.type) {
		case HUB:
			JSONArray tickArray = new JSONArray();
			for (ServiceLevel sKey : ServiceLevel.values())	{
				tickArray.add(((HubCoord)coord).noOfTotalTick.get(sKey).toPlainString());
			}
			result.put("noOfTotalTick", tickArray);
			result.put("noOfAliveTick", ((HubCoord)coord).noOfAliveTick.toPlainString());
			break;
		case DEPOT:
			result.put("noOfClosedTick", ((DepotCoord)coord).noOfClosedTick.toPlainString());
			result.put("noOfAliveTick", ((DepotCoord)coord).noOfAliveTick.toPlainString());
			break;
		default:
			throw new IllegalStateException();
		}

		result.put("normalScale", coord.normalScale.toPlainString());
		result.put("logicalTotalCoord", coord.totalCoord.toPlainString());

		return result; 
	}
	/** The relevant item statuses. */
	private static final ItemStatus[] ITEM_STATUSES = {
		ItemStatus.CREATED,
		ItemStatus.SCANNED,
		ItemStatus.DECLARED,
		ItemStatus.AT_HUB,
		ItemStatus.LEFT_HUB_TODAY,
		ItemStatus.PREDICTED
	};

	/**
	 * Create the status JSON array.
	 * @return the created JSON array
	 */
	private static JSONArray createStatusJSON()	{
		JSONArray resultArray = new JSONArray();

		for (ItemStatus keyItem : ITEM_STATUSES) {
			JSONObject statusRecord = new JSONObject();
			statusRecord.put("info", keyItem.getInfo());
			statusRecord.put("message", keyItem.getMessage());
			resultArray.add(statusRecord);      
		}

		return resultArray;
	}

	/**
	 * Create the bars JSON array.
	 * @param coord the coordinate object
	 * @param bar the data source
	 * @return the created JSON array
	 */
	private static JSONArray createBarsJSON(BarCoordinate coord, SumData bar) {
		
		JSONArray result = new JSONArray();

		JSONObject typeRec, itemRec;
		JSONArray orientRec;
		EnumSet<OrientStatus> orientKeys = bar.getOrientKeys();

		for (ServiceLevel uKey: ServiceLevel.values()) {
			typeRec = new JSONObject();
			typeRec.put("unit", uKey.getMessage());
			typeRec.put("name", bar.name);
			typeRec.put("id", bar.id);
			for (OrientStatus oKey : orientKeys) {
				orientRec = new JSONArray();
				for (ItemStatus pKey: ITEM_STATUSES) {
					itemRec = new JSONObject();
					itemRec.put("info", pKey.getInfo());
					BarData bd = bar.items.get(uKey).get(oKey).get(pKey);
					if (bd != null) {
						itemRec.put("value", bd.value.toPlainString());
						itemRec.put("normalValue", bd.normalValue.toPlainString());
					} else {
						itemRec.put("value", "0");
						itemRec.put("normalValue", "0");
					}
					orientRec.add(itemRec);
				}
				typeRec.put(oKey.getInfo(), orientRec);
			}
			result.add(typeRec);
		}

		return result;

	}
}
