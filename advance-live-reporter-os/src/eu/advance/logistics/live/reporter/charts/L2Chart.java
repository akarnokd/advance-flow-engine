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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * L2Chart is the main class for making JSON for the warehouse level 2 chart.
 * @author csirobi
 */
public final class L2Chart {
	/** Utility class. */
	private L2Chart() { }
	/**
	 * Comparator inner class for the relative ordering.
	 * @author csirobi
	 */
	private static class RelativeComparator implements Comparator<L2StorageChartData> {
		@Override
		public int compare(L2StorageChartData o1, L2StorageChartData o2) {
			return -1 * Double.compare(o1.relFullness, o2.relFullness);
		}
	}

	/**
	 * Comparator inner class for the storage id (number) ordering.
	 * @author csirobi
	 */
	private static class NumberComparator implements Comparator<L2StorageChartData> {
		@Override
		public int compare(L2StorageChartData o1, L2StorageChartData o2) {
			return Long.compare(o1.id, o2.id);
		}
	}

	/**
	 * Comparator inner class for the relative ordering, when the option is L or R.
	 * @author csirobi
	 */
	private static class TempComparator implements Comparator<TempLROption>	{
		@Override
		public int compare(TempLROption o1, TempLROption o2) {
			return -1 * Double.compare(o1.sumRelFullness, o2.sumRelFullness);
		}
	}

	/**
	 * Inner helper class for the relative ordering, when the option is L or R.
	 * @author csirobi
	 */
	private static class TempLROption {
		/** The index. */
		private int index;
		/** The relative fullness. */
		private double sumRelFullness;
		/**
		 * Constructor, initializes the fields.
		 * @param i the index
		 * @param s the total relative fullness
		 */
		public TempLROption(int i, double s) {
			this.index = i;
			this.sumRelFullness = s;
		}
	}
	/**
	 * Create a chart JSON object.
	 * @param ws the warehouse switch
	 * @param storageRawMap the raw data map
	 * @return the JSON object
	 */
	public static JSONObject getJSONtoChart(
			WarehouseSwitch ws, Map<L2DisplaySide, List<L2StorageRawData>> storageRawMap) {
		Map<L2DisplaySide, List<L2StorageChartData>> storageChartMap = new LinkedHashMap<L2DisplaySide, List<L2StorageChartData>>();

		for (L2DisplaySide dsKey : L2DisplaySide.values()) {
			List<L2StorageChartData> storageChartList = new ArrayList<L2StorageChartData>();
			for (L2StorageRawData storageRaw : storageRawMap.get(dsKey)) {
				storageRaw.normalizeByCap();
				storageChartList.add(ChartProcess.getL2StorageChartData(storageRaw));
			}
			storageChartMap.put(dsKey, storageChartList);
		}

		L2Chart.sortStorageChartMap(ws, storageChartMap);

		JSONObject result = new JSONObject();
		result.put("direction", L2Chart.createDirectionJSON(ws));
		for (L2DisplaySide dsKey : L2DisplaySide.values()) {
			result.put(dsKey.getInfo(),  L2Chart.createDisplaySideJSON(storageRawMap.get(dsKey), storageChartMap.get(dsKey)));
		}

		return result;
	}
	/**
	 * Sorts the storage chart.
	 * @param ws the warehouse switch
	 * @param storageChartMap the storage chart map
	 */
	private static void sortStorageChartMap(WarehouseSwitch ws, Map<L2DisplaySide, List<L2StorageChartData>> storageChartMap) {
		switch (ws.getL2WarehouseOption()) {
		case A:
		case B:
			switch (ws.getStorageOrder()) {
			case RELATIVE:
				for (L2DisplaySide dsKey : L2DisplaySide.values()) {
					Collections.sort(storageChartMap.get(dsKey), new L2Chart.RelativeComparator());
				}
				break;
			case NUMBER:
				for (L2DisplaySide dsKey : L2DisplaySide.values()) {
					Collections.sort(storageChartMap.get(dsKey), new L2Chart.NumberComparator());
				}
				break;
			default:
			}
			break;
		case LEFT:
		case RIGHT:
			switch (ws.getStorageOrder()) {
			case RELATIVE:
				for (L2DisplaySide dsKey : L2DisplaySide.values()) {
					Collections.sort(storageChartMap.get(dsKey), new L2Chart.NumberComparator());
				}
				
				List<L2StorageChartData> leftStroageChartList  = storageChartMap.get(L2DisplaySide.LEFT);
				List<L2StorageChartData> rightStorageChartList = storageChartMap.get(L2DisplaySide.RIGHT);

				if (leftStroageChartList.size() == rightStorageChartList.size()) {
					List<L2Chart.TempLROption> tempLROptionList = new ArrayList<L2Chart.TempLROption>();

					for (int i = 0; i < leftStroageChartList.size(); i++) {
						tempLROptionList.add(new L2Chart.TempLROption(i, leftStroageChartList.get(i).relFullness + rightStorageChartList.get(i).relFullness));
					}
					Collections.sort(tempLROptionList, new L2Chart.TempComparator());

					for (L2DisplaySide dsKey : L2DisplaySide.values()) {
						List<L2StorageChartData> relativeStorageChartList = new ArrayList<L2StorageChartData>();
						for (L2Chart.TempLROption tempItem : tempLROptionList) {
							relativeStorageChartList.add(storageChartMap.get(dsKey).get(tempItem.index));
						}
						storageChartMap.remove(dsKey);
						storageChartMap.put(dsKey, relativeStorageChartList);
					}
				}

				break;
			case NUMBER:
				for (L2DisplaySide dsKey : L2DisplaySide.values()) {
					Collections.sort(storageChartMap.get(dsKey), new L2Chart.NumberComparator());
				}
				break;
			default:
			}
			break;
		default:
		}

	}
	/**
	 * Create the direction JSON object.
	 * @param ws the warehouse switch
	 * @return the JSON object
	 */
	private static JSONObject createDirectionJSON(WarehouseSwitch ws) {
		JSONObject result = new JSONObject();

		for (L2DisplaySide dsKey : L2DisplaySide.values()) {
			JSONObject dsRecord = new JSONObject();
			switch (ws.getL2WarehouseOption()) {
			case A:
			case B:
				dsRecord.put("align", WarehouseSide.values()[dsKey.ordinal()].name().toLowerCase());
				dsRecord.put("warehouseInfo", ws.getWarehouse() + ws.getWarehouseType().name());
				break;
			case LEFT:
				dsRecord.put("align", WarehouseSide.LEFT.toString().toLowerCase());
				dsRecord.put("warehouseInfo", ws.getWarehouse() + WarehouseType.values()[dsKey.ordinal()].name());
				break;
			case RIGHT:
				dsRecord.put("align", WarehouseSide.RIGHT.toString().toLowerCase());
				dsRecord.put("warehouseInfo", ws.getWarehouse() + WarehouseType.values()[dsKey.ordinal()].name());
				break;
			default:
			}
			dsRecord.put("at_hub", L2TimeState.NOW_AT_HUB.getMessage());
			dsRecord.put("coming", L2TimeState.COMING_UP.getMessage());

			result.put(dsKey.getInfo(), dsRecord);
		}

		return result;
	}

	/**
	 * Create a display side JSON array.
	 * @param storageRawList the raw list
	 * @param storageChartList the storage chart
	 * @return the JSON array
	 */
	private static JSONArray createDisplaySideJSON(
			List<L2StorageRawData> storageRawList, 
			List<L2StorageChartData> storageChartList) {
		JSONArray result = new JSONArray();
		int i = 0;

		for (L2StorageChartData storageChart : storageChartList) {
			JSONObject storageRecord = new JSONObject();

			L2StorageRawData storageRaw = storageRawList.get(i);
			String ss = storageRaw.warehouse + "_" + storageRaw.type + "_" + storageRaw.side.name();
			storageRecord.put("warehouseLayout", ss.toLowerCase());
			storageRecord.put("id", storageChart.id);
			storageRecord.put("overColor", storageChart.bgColor.get(L2TimeState.OVERALL).getColor());

			for (L2TimeState timeState : L2StorageChartData.getUsedBarTime()) {
				JSONObject timeRecord = new JSONObject();
				timeRecord.put("standardNormal",  storageChart.barLength.get(timeState).get(WarehouseServiceLevel.STANDARD).getLength().toPlainString());
				timeRecord.put("priorityNormal", storageChart.barLength.get(timeState).get(WarehouseServiceLevel.PRIORITY_SPECIAL).getLength().toPlainString());
				timeRecord.put("bgColor", storageChart.bgColor.get(timeState).getColor());
				storageRecord.put(timeState.getInfo(), timeRecord);
			}
			result.add(storageRecord);
			i++;
		}

		return result;
	}
}
