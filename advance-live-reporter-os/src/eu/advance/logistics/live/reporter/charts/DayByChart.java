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

import java.util.Date;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import eu.advance.logistics.live.reporter.model.ServiceLevel;

/**
 * DayByChart is the main class for making JSON.
 * @author csirobi, 2013.02.04.
 */
public final class DayByChart {
	/** Helper class. */
	private DayByChart() { }
	/** Maximum value of every tick. */
	private static final int MAX_TICK_VALUE = 4;
	/** Maximum number of the ticks in the ruler. */
	private static final int MAX_NO_OF_TICK = 15;

	/**
	 * Create the JSON object.
	 * @param dayBy the data source
	 * @return the created JSON object
	 */
	public static JSONObject getDayByDayJSON(DayByData dayBy) {
	  dayBy.checkBaus();
	  
		BarCoordinate coord = new BarCoordinate(dayBy.busAsUsSc, MAX_TICK_VALUE, MAX_NO_OF_TICK);
		dayBy.normalizeBy(coord);

		JSONObject result = new JSONObject();
		result.put("direction", DayByChart.createDirectionJSON(dayBy));
		result.put("status", DayByChart.createStatusJSON());
		result.put("coordinate", DayByChart.createCoordJSON(coord));
		result.put("bars", DayByChart.createBarsJSON(dayBy));

		return result;
	}
	/**
	 * Create a direction JSON object.
	 * @param dayBy the data source
	 * @return the created JSON object
	 */
	private static JSONObject createDirectionJSON(DayByData dayBy) {
		JSONObject result = new JSONObject();
		JSONArray orientType = new JSONArray();

		result.put("chartView", dayBy.chartView.name().toLowerCase());		
		result.put("id", dayBy.id);
		result.put("name", dayBy.name);
		result.put("type", dayBy.type.getInfo());
		result.put("unit", dayBy.unit.getInfo());
		switch(dayBy.type) {
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
	 * Create the status JSON array.
	 * @return the JSON array.
	 */
	private static JSONArray createStatusJSON()	{
		JSONArray resultArray = new JSONArray();
		ServiceLevel[] unitOrder = {ServiceLevel.ALL, ServiceLevel.STANDARD, ServiceLevel.PRIORITY, ServiceLevel.SPECIAL};

		for (ServiceLevel uKey : unitOrder) {
			JSONObject record = new JSONObject();
			record.put("info", uKey.name().toLowerCase());

			if (uKey.compareTo(ServiceLevel.ALL) == 0) {
				record.put("message", "Total");
			} else {
				record.put("message", uKey.getMessage());
			}
			resultArray.add(record);
		}

		return resultArray;
	}
	/**
	 * Create a coordinate JSON object.
	 * @param coord the source data
	 * @return the created JSON object
	 */
	private static JSONObject createCoordJSON(BarCoordinate coord) {
		JSONObject result = new JSONObject();

		result.put("scale", coord.scale.toPlainString());
		result.put("tickValue", coord.tickUnit.toPlainString());
		result.put("noOfTick", coord.noOfTick.toPlainString());

		result.put("totalCoord", coord.totalCoord.toPlainString());
		result.put("normalScale", coord.normalScale.toPlainString());

		return result; 
	}

	/**
	 * Create the bars JSON array.
	 * @param dayBy the source data
	 * @return the created JSON array
	 */
	private static JSONArray createBarsJSON(DayByData dayBy) {
		JSONArray resultArray = new JSONArray();
		ServiceLevel[] unitOrder = {ServiceLevel.SPECIAL, ServiceLevel.PRIORITY, ServiceLevel.STANDARD, ServiceLevel.ALL};

		JSONObject dayRec, unitRec;
		JSONArray orientRec;

		if (dayBy.items.keySet().size() > 0) {

	    for (Date dateKey : dayBy.items.keySet()) {
	      dayRec = new JSONObject();

	      dayRec.put("day", dayBy.getDateString(dateKey));
	      
	      if (dayBy.type.compareTo(TypeStatus.DEPOT) == 0) {
          dayRec.put(OrientStatus.ORIGIN.getInfo() + "_text", "Inbound");
          dayRec.put(OrientStatus.DESTIN.getInfo() + "_text", "Outbound");
	      }

	      for (OrientStatus oKey: dayBy.getOrientKeys()) {
	        orientRec = new JSONArray();
	        for (ServiceLevel uKey : unitOrder) {
	          unitRec = new JSONObject();
	          unitRec.put("info", uKey.name().toLowerCase());
	          BarData bd = dayBy.items.get(dateKey).get(oKey).get(uKey);
	          if (bd != null) {
		          unitRec.put("value", bd.value.toPlainString());
		          unitRec.put("normalValue", bd.normalValue.toPlainString());
	          } else {
		          unitRec.put("value", "0");
		          unitRec.put("normalValue", "0");
	          }
	          orientRec.add(unitRec);
	        }
	        dayRec.put(oKey.getInfo(), orientRec);
	      }

	      resultArray.add(dayRec);      
	    }
		}


		return resultArray;
	}

}
