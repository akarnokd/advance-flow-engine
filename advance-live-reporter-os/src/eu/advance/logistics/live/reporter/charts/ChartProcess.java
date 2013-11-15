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

import hu.akarnokd.utils.xml.XElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import eu.advance.logistics.live.reporter.model.ItemStatus;

/**
 * ChartProcess XML parser and helper class for 
 * calculating the background color and bar length of warehouse L2.
 * @author csirobi, 2013.04.22.
 */
public final class ChartProcess {
	/** Helper class. */
	private ChartProcess() { }
	/**
	 * ParamWeight inner helper class for storing params loaded from XML.
	 * @author csirobi, 2013.04.22.
	 */
	public static class ParamWeight	{
		/** Weight. */
		public double tau;
		/** Weight. */
		public double lambda;
		/** Weight. */
		public double delta;
		/** Weight. */
		public double wm;
		/** Weight. */
		public double ws;
		/** Weight. */
		public double we;
		/** Weight. */
		public double wn;
		/** Weight. */
		public double wp;
		/** Weight. */
		public double aprm;
		/** Weight. */
		public double a;
		/** Weight. */
		public double b;
	}
	/** Parameter file. */
	private static final String FILE_NAME = "/l2params.xml";
	/** The parameter values. */
	private static final HashMap<L2TimeState, ParamWeight> PW_VALUES = new LinkedHashMap<L2TimeState, ParamWeight>();

	static {
		try {
			XElement xPwValues = XElement.parseXML(ChartProcess.class.getResource(FILE_NAME));
			for (XElement xTimeState : xPwValues.childrenWithName("timeState"))	{
				L2TimeState timeState = L2TimeState.valueOf(xTimeState.get("id").toUpperCase());
				ParamWeight pw = new ParamWeight();

				String ss = xTimeState.childValue("tau");
				pw.tau = (ss != null) ? Double.parseDouble(ss) : -1;
				ss = xTimeState.childValue("lambda");
				pw.lambda = (ss != null) ? Double.parseDouble(ss) : -1;
				ss = xTimeState.childValue("delta");
				pw.delta = (ss != null) ? Double.parseDouble(ss) : -1;
				ss = xTimeState.childValue("wm");
				pw.wm = (ss != null) ? Double.parseDouble(ss) : -1;
				ss = xTimeState.childValue("ws");
				pw.ws = (ss != null) ? Double.parseDouble(ss) : -1;
				ss = xTimeState.childValue("we");
				pw.we = (ss != null) ? Double.parseDouble(ss) : -1;
				ss = xTimeState.childValue("wn");
				pw.wn = (ss != null) ? Double.parseDouble(ss) : -1;
				ss = xTimeState.childValue("wp");
				pw.wp = (ss != null) ? Double.parseDouble(ss) : -1;

				switch(timeState) {
				case NOW_AT_HUB:
				case COMING_UP:
					pw.aprm = pw.tau / (1 - pw.tau);
					pw.a = pw.lambda / ((pw.lambda * 2.0) - 1.0);
					pw.b = 9.0 / (pw.delta - 1.0);
					break;
				case OVERALL:
					pw.a = pw.lambda / ((pw.lambda * 2.0) - 1.0);
					pw.b = 9.0 / (pw.delta - 1.0);
					break;
				default:
				}

				ChartProcess.PW_VALUES.put(timeState, pw);
			}

		} catch (IllegalArgumentException | IOException | XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Main class for calculating the background color and bar length of warehouse level 2.
	 * @param storageRaw warehouse level 2 storageRaw storage raw data 
	 * @return storage chart data
	 */
	public static L2StorageChartData getL2StorageChartData(L2StorageRawData storageRaw) {
		L2StorageChartData storageChart = new L2StorageChartData();
		storageChart.id = storageRaw.id;

		HashMap<L2TimeState, Double> maxInput = new LinkedHashMap<L2TimeState, Double>();

		// I.) For NOW_AT_HUB & COMING_UP
		for (L2TimeState timeState : L2StorageChartData.USED_BAR_TIME) {
			// 1.) Count bar length...
			Map<WarehouseServiceLevel, L2StorageChartData.BarLength> lengthMap = new LinkedHashMap<>();
			Map<WarehouseServiceLevel, Double> weightInput = ChartProcess.getWeightInput(timeState, storageRaw);      
			//.. for STANDARD & PRIORITY
			for (WarehouseServiceLevel wsl : L2StorageChartData.USED_SERVICES) {
				L2StorageChartData.BarLength barLength = new L2StorageChartData.BarLength();
				barLength.setLength(ChartProcess.countBarLength(timeState, weightInput.get(wsl).doubleValue()));
				lengthMap.put(wsl, barLength);
			}
			storageChart.barLength.put(timeState, lengthMap);

			// 2.) Count bar color
			maxInput.put(timeState, ChartProcess.getMaxInput(weightInput));
			L2StorageChartData.BgColor bgColor = new L2StorageChartData.BgColor();
			bgColor.setColor(ChartProcess.countBarColor(timeState, maxInput.get(timeState).doubleValue()));
			storageChart.bgColor.put(timeState, bgColor);
		}

		// II.) For OVERALL, sum...
		double wX0 = ChartProcess.PW_VALUES.get(L2TimeState.OVERALL).wn * maxInput.get(L2TimeState.NOW_AT_HUB);
		double wX1 = ChartProcess.PW_VALUES.get(L2TimeState.OVERALL).wp * maxInput.get(L2TimeState.COMING_UP);
		storageChart.relFullness = (wX0 + wX1);    
		//.. and count bar color
		L2StorageChartData.BgColor bgColor = new L2StorageChartData.BgColor();
		bgColor.setColor(ChartProcess.countBarColor(L2TimeState.OVERALL, (wX0 + wX1)));
		storageChart.bgColor.put(L2TimeState.OVERALL, bgColor);

		return storageChart;
	}


	// 
	/**
	 * Count weighted input for barLength(), maximum() at STANDARD & PRIORITY at level 2.
	 * @param timeState the time state
	 * @param storageRaw the raw data
	 * @return the state map
	 */
	private static Map<WarehouseServiceLevel, Double> getWeightInput(
			L2TimeState timeState, L2StorageRawData storageRaw) {
		Map<WarehouseServiceLevel, Double> weightInput = new LinkedHashMap<>();

		switch(timeState) {
		case NOW_AT_HUB:
			// for STANDARD & PRIORITY
			for (WarehouseServiceLevel wsl : L2StorageChartData.USED_SERVICES) {
				double x = storageRaw.items.get(wsl).get(ItemStatus.AT_HUB).normalValue.doubleValue();
				weightInput.put(wsl, x);

			}
			break;
		case COMING_UP:
			for (WarehouseServiceLevel wsl : L2StorageChartData.USED_SERVICES) {
				double m = ChartProcess.PW_VALUES.get(timeState).wm 
						* storageRaw.items.get(wsl).get(ItemStatus.DECLARED).normalValue.doubleValue();
				double s = ChartProcess.PW_VALUES.get(timeState).ws 
						* storageRaw.items.get(wsl).get(ItemStatus.SCANNED).normalValue.doubleValue();
				double e = ChartProcess.PW_VALUES.get(timeState).we 
						* storageRaw.items.get(wsl).get(ItemStatus.CREATED).normalValue.doubleValue();
				double x = m + s + e;

				weightInput.put(wsl, x);
			}
			break;
		default:
		}

		return weightInput;
	}


	/**
	 * Count the maximum value of weighted input map for level 2 and 3.
	 * @param weightInput the weight map
	 * @return maximum
	 */
	private static double getMaxInput(Map<WarehouseServiceLevel, Double> weightInput) {
		double result = (
				weightInput.get(WarehouseServiceLevel.STANDARD).doubleValue() > weightInput.get(WarehouseServiceLevel.PRIORITY_SPECIAL).doubleValue()) 
				? weightInput.get(WarehouseServiceLevel.STANDARD).doubleValue() : weightInput.get(WarehouseServiceLevel.PRIORITY_SPECIAL).doubleValue();

				return result;
	}

	/**
	 * Count bar length for level 2.
	 * @param timeState the time state
	 * @param x the X coordinate
	 * @return the bar length
	 */
	private static double countBarLength(L2TimeState timeState, double x) {
		double aprm = ChartProcess.PW_VALUES.get(timeState).aprm;

		return aprm * x / (aprm * x + 1);
	}

	/**
	 * Count bar color for level 2.
	 * @param timeState time state
	 * @param x value
	 * @return list of color components
	 */
	private static List<Double> countBarColor(L2TimeState timeState, double x) {
		List<Double> colorList = new ArrayList<Double>();
		double a = ChartProcess.PW_VALUES.get(timeState).a;
		double b = ChartProcess.PW_VALUES.get(timeState).b;

		double tmp = (x < 1.0) ? ((a - 1) * x) / (a - x) : 1.0 / (b * x + 1.0 - b);
		double r = 1.0 - (tmp - 1) * (tmp - 1);
		double g = (x < 1.0) ? 0.6 * (1.0 - tmp * tmp) : 0.0;
		colorList.add(r);
		colorList.add(g);
		colorList.add(0.0);
		
		return colorList;     
	}

	/**
	 * Main class for calculating the relative fullness for wsl level 3 chart.
	 * @param depotStorageData  wsl level 3 depot/storage data
	 */
	public static void getL3RelFullness(L3DepotStorageData depotStorageData) {
		double maxSA = 0, maxSB = 0, maxComing = 0;

		for (L2TimeState timeState : EnumSet.of(L2TimeState.NOW_AT_HUB, L2TimeState.COMING_UP))	{
			switch (timeState) {
			case NOW_AT_HUB:
				Map<WarehouseServiceLevel, Double> weightInput;
				weightInput = ChartProcess.getWeightInput(timeState, WarehouseType.A, depotStorageData);
				maxSA = ChartProcess.getMaxInput(weightInput);

				weightInput = ChartProcess.getWeightInput(timeState, WarehouseType.B, depotStorageData);
				maxSB = ChartProcess.getMaxInput(weightInput);

				break;
			case COMING_UP:
				weightInput = ChartProcess.getWeightInput(timeState, null, depotStorageData);
				maxComing   = ChartProcess.getMaxInput(weightInput);

				break;
			default:
			}
		}

		double wX0SA = ChartProcess.PW_VALUES.get(L2TimeState.OVERALL).wn * maxSA;
		double wX0SB = ChartProcess.PW_VALUES.get(L2TimeState.OVERALL).wn * maxSB;
		double wX1   = ChartProcess.PW_VALUES.get(L2TimeState.OVERALL).wp * maxComing;

		depotStorageData.relFullness = (wX0SA + wX0SB + wX1);
	}
	/**
	 * Count the weight input for level 3.
	 * @param timeState the time state
	 * @param type the warehouse type
	 * @param depotStorageData the data
	 * @return the weight map
	 */
	private static Map<WarehouseServiceLevel, Double> getWeightInput(
			L2TimeState timeState, WarehouseType type, L3DepotStorageData depotStorageData) {
		Map<WarehouseServiceLevel, Double> weightInput = new LinkedHashMap<>();

		switch (timeState) {
		case NOW_AT_HUB:
			for (WarehouseServiceLevel sKey : EnumSet.of(WarehouseServiceLevel.STANDARD, WarehouseServiceLevel.PRIORITY_SPECIAL)) {
				double s = depotStorageData.atHubItems.get(sKey).get(type).normalValue.doubleValue();
				weightInput.put(sKey, s);
			}
			break;
		case COMING_UP:
			for (WarehouseServiceLevel sKey : EnumSet.of(WarehouseServiceLevel.STANDARD, WarehouseServiceLevel.PRIORITY_SPECIAL)) {
				double m =  ChartProcess.PW_VALUES.get(timeState).wm 
						* depotStorageData.futureItems.get(sKey).get(ItemStatus.DECLARED).normalValue.doubleValue();
				double s = ChartProcess.PW_VALUES.get(timeState).ws 
						* depotStorageData.futureItems.get(sKey).get(ItemStatus.SCANNED).normalValue.doubleValue();
				double e = ChartProcess.PW_VALUES.get(timeState).we 
						* depotStorageData.futureItems.get(sKey).get(ItemStatus.CREATED).normalValue.doubleValue();
				double x = m + s + e;
				weightInput.put(sKey, x);
			}
			break;
		default:
		}

		return weightInput;
	}

}
