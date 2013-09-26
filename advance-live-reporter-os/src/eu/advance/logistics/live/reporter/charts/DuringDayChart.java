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

import hu.akarnokd.reactive4java.interactive.Interactive;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.joda.time.DateTime;

import eu.advance.logistics.live.reporter.model.DuringAmountStatus;
import eu.advance.logistics.live.reporter.model.DuringDayDetailsInfo;
import eu.advance.logistics.live.reporter.model.ServiceLevel;

/**
 * DuringDayChart is the main class for making JSON for during-day chart.
 * @author csirobi, 2013.06.24.
 */
public final class DuringDayChart {
	/** Maximum value of every tick. */
	private static final int MAX_TICK_VALUE = 4;
	/** Maximum number of the ticks in the ruler. */
	private static final int MAX_NO_OF_TICK = 15;
	/** Helper class. */
	private DuringDayChart() { }
	/**
	 * Returns the json object of the during-day chart.
	 * @param duringData data source
	 * @return the json object of the during-day chart
	 */
	public static JSONObject getDuringDayJSON(DuringDayData duringData) {
		duringData.checkBaus();

		BarCoordinate coord = new BarCoordinate(duringData.busAsUsSc, MAX_TICK_VALUE, MAX_NO_OF_TICK);
		duringData.normalizeBy(coord);

		JSONObject result = new JSONObject();
		result.put("direction", DuringDayChart.createDirectionJSON(duringData));
		result.put("status", DuringDayChart.createStatusJSON());
		result.put("xcoord", DuringDayChart.createXCoordJSON(duringData));
		result.put("ycoord", DuringDayChart.createYCoordJSON(coord));
		result.put("bars", DuringDayChart.createBarJSON(duringData));

		return result;
	}

	/**
	 * Returns the "direction" part of the json object.
	 * @param duringData data source
	 * @return the "direction" part of the json object
	 */
	private static JSONObject createDirectionJSON(DuringDayData duringData) {
		JSONObject result = new JSONObject();

		result.put("chartView", duringData.chartView.name().toLowerCase());
		result.put("id", duringData.id);
		result.put("name", duringData.name);
		result.put("type", duringData.type.getInfo());
		result.put("unit", duringData.unit.getInfo());
		result.put("orient", duringData.orient.getInfo());

		return result;
	}

	/**
	 * Returns the "status" part of the json object.
	 * @return the "status" part of the json object
	 */
	private static JSONObject createStatusJSON() {
		JSONObject result = new JSONObject();
		JSONArray resultArray;
		ServiceLevel[] snipOrder = { ServiceLevel.STANDARD, ServiceLevel.PRIORITY, ServiceLevel.SPECIAL };

		resultArray = new JSONArray();
		for (DuringDayDetailsInfo infoKey : DuringDayDetailsInfo.values()) {
			JSONObject record = new JSONObject();
			record.put("info", infoKey.getInfo());
			record.put("message", infoKey.getMessage());
			resultArray.add(record);
		}
		result.put("legendInfo", resultArray);

		resultArray = new JSONArray();
		for (ServiceLevel sKey : snipOrder) {
			JSONObject record = new JSONObject();
			record.put("info", sKey.name().toLowerCase());
			record.put("message", sKey.getMessage());
			resultArray.add(record);
		}
		result.put("legendSnip", resultArray);

		return result;
	}

	/**
	 * Returns the "xcoord" part of the json object.
	 * @param duringData data source
	 * @return the "xcoord" part of the json object
	 */
	private static JSONObject createXCoordJSON(DuringDayData duringData) {
		JSONObject result = new JSONObject();
		JSONArray timeRec = new JSONArray();

		result.put("quarterSlots", duringData.getQuarterSlots());
		result.put("quarterChartFrom", duringData.getQuarterChartFrom());
		result.put("quarterChartWidth", DuringDayChart.countQuarterChartWidth(duringData));
		result.put("quarterLine", duringData.getQuarterLine());

		for (String ss : duringData.getDisplayHourList()) {
			timeRec.add(ss);
		}

		result.put("displayHours", timeRec);

		return result;
	}

	/**
	 * Returns the "ycoord" part of the json object.
	 * @param coord bar coordinate data source
	 * @return the "ycoord" part of the json object
	 */
	private static JSONObject createYCoordJSON(BarCoordinate coord) {
		JSONObject result = new JSONObject();

		result.put("scale", coord.scale.toPlainString());
		result.put("tickValue", coord.tickUnit.toPlainString());
		result.put("noOfTick", coord.noOfTick.toPlainString());

		result.put("totalCoord", coord.totalCoord.toPlainString());
		result.put("normalScale", coord.normalScale.toPlainString());

		return result;
	}

	/**
	 * Returns the "bars" part of the json object.
	 * @param duringData data source
	 * @return the "bars" part of the json object
	 */
	private static JSONArray createBarJSON(DuringDayData duringData) {
		JSONArray resultArray = new JSONArray();
		ServiceLevel[] snipOrder = {ServiceLevel.SPECIAL, ServiceLevel.PRIORITY, ServiceLevel.STANDARD, ServiceLevel.ALL};

		JSONObject timeRec, snipRec;
		JSONArray amountRec;

		if (duringData.itemsInOut.get(duringData.orient).size() > 0) {

			Iterable<DateTime> ks = duringData.itemsInOut.get(duringData.orient).keys();
			Iterable<DateTime> orderedKs = Interactive.orderBy(ks);

			int quarterChartWidth = DuringDayChart.countQuarterChartWidth(duringData);
			int numOfSlot = 0; 

			for (DateTime dateKey : orderedKs) {
				if (numOfSlot <= quarterChartWidth) {
					timeRec = new JSONObject();
					timeRec.put("time", duringData.getTimeString(dateKey));

					for (DuringAmountStatus dasKey : DuringAmountStatus.values()) {
						amountRec = new JSONArray();
						for (ServiceLevel sKey : snipOrder) {
							String val = "0";
							String nval = "0";

							BarData bd = duringData.itemsInOut.getValue(duringData.orient, dateKey, dasKey, sKey, null);
							if (bd != null) {
								val = bd.value.toPlainString();
								nval = bd.normalValue.toPlainString();
							}

							snipRec = new JSONObject();
							snipRec.put("info", sKey.name().toLowerCase());
							snipRec.put("value", val);
							snipRec.put("normalValue", nval);
							amountRec.add(snipRec);
						}
						timeRec.put(dasKey.name().toLowerCase(), amountRec);
					}
					resultArray.add(timeRec);
					numOfSlot++;
				}

			}
		}
		return resultArray;
	}

	/**
	 * Count the real width of the chart in quarter-hour slots.
	 * @param duringData data source
	 * @return width of the chart in quarter-hour slots
	 */
	private static int countQuarterChartWidth(DuringDayData duringData) {
		int quarterSlots = duringData.getQuarterSlots();
		int quarterChartFrom = duringData.getQuarterChartFrom();

		int quarterChartWidth = ((quarterSlots - quarterChartFrom) < duringData.itemsInOut.get(duringData.orient).size() - 1) 
				? quarterSlots - quarterChartFrom 
				: duringData.itemsInOut.get(duringData.orient).size() - 1;  

		return quarterChartWidth;
	}

}
