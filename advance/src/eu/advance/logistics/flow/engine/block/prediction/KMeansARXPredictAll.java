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

package eu.advance.logistics.flow.engine.block.prediction;

import hu.akarnokd.reactive4java.base.Func2;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.prediction.KMeansARXLearn.TimeseriesAggregator;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * The prediction calculator for the K-means ARX model for multiple groups.
 * @author karnokd, 2012.02.27.
 */
@Block(id = "KMeansARXPredictAll", 
category = "prediction", scheduler = "CPU", 
description = "Predicts the subsequent values of an ARX model.")
public class KMeansARXPredictAll extends AdvanceBlock {
	/** The input of timestamp-value-group triplets. */
	@Input("advance:collection<advance:timedvaluegroup>")
	protected static final String DATA = "data";
	/** The ARX models. */
	@Input("advance:collection<advance:arxmodel>")
	protected static final String MODEL = "model";
	/** The index to tell which group belongs to which model. */
	@Input("advance:map<advance:string, advance:integer>")
	protected static final String CLASS = "class";
	/** The lenght of the horizon. */
	@Input("advance:integer")
	protected static final String HORIZON = "horizon";
	/** Map for group to prediction list. */
	@Output("advance:map<advance:string, advance:collection<advance:real>>")
	protected static final String PREDICTION = "prediction";
	@Override
	protected void invoke() {
		final int horizon = getInt(HORIZON);

		final TimeseriesAggregator series = TimeseriesAggregator.load(resolver().getItems(get(DATA)));
		
		final Map<String, Integer> groupToModel = Maps.newHashMap();
		
		for (Map.Entry<XElement, XElement> e : resolver().getMap(get(CLASS)).entrySet()) {
			String key = resolver().getString(e.getKey());
			int value = resolver().getInt(e.getValue());
			groupToModel.put(key, value);
		}
		
		// build the subsequent weekdays
		final int[] extendedDaysSinceEpoch = Arrays.copyOf(series.daysSinceEpoch, series.daysSinceEpoch.length + horizon);
		
		for (int d = series.daysSinceEpoch.length; d < extendedDaysSinceEpoch.length; d++) {
			int next = extendedDaysSinceEpoch[d - 1];
			int dow = 0;
			do {
				next++;
				dow = KMeansARXLearn.dayOfWeek(next);
			} while (dow != 5 && dow != 6);
		}
		
		
		List<XElement> models = Lists.newArrayList(resolver().getItems(get(MODEL)));

		BiMap<Integer, String> indexToGroup = series.groupIndex.inverse();
		
		Map<XElement, XElement> result = Maps.newHashMap();
		for (Map.Entry<Integer, String> ig : indexToGroup.entrySet()) {
			int didx = ig.getKey();
			
			String group = ig.getValue();
			
			int modelIndex = groupToModel.get(group);
			
			XElement xm = models.get(modelIndex);
			
			int p = xm.getInt("model-order");
			int m = 5;
			
			// create arx model
			ArxModel arxModel = new ArxModel(
					new double[0],
					p, m, new Func2<Integer, Integer, Double>() {
				@Override
				public Double invoke(Integer param1, Integer param2) {
					int dow = KMeansARXLearn.dayOfWeek(extendedDaysSinceEpoch[param1]);
					return dow == param2.intValue() ? 1d : 0d;
				}
			});

			// setup coefficients
			int i = 0;
			for (XElement ac : xm.childrenWithName("model-coefficient")) {
				arxModel.setArCoefficient(i, resolver().getDouble(ac));
				i++;
			}
			i = 0;
			for (XElement uc : xm.childrenWithName("external-coefficient")) {
				arxModel.setUCoefficient(i, resolver().getDouble(uc));
				i++;
			}
			
			// calculte forecasts
			double[] forecasts = arxModel.forecastAll(series.timeSeriesMatrix[didx], horizon);
	
			List<XElement> forecastList = Lists.newArrayList();
			for (double d : forecasts) {
				forecastList.add(resolver().create(d));
			}
			
			result.put(resolver().create(group), resolver().create(forecastList));
		}
		
		dispatch(PREDICTION, resolver().create(result));
	}

}
