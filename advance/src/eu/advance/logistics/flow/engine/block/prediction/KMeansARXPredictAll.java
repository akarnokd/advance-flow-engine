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

import hu.akarnokd.reactive4java.base.Action1;
import hu.akarnokd.reactive4java.base.Func2;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.util.Triplet;
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
		
		final TimeGroupSeriesMapper series = new TimeGroupSeriesMapper();
		
		series.map(resolver().getItems(get(DATA)), resolver());
		
		final double[][] allData = new double[series.rowset.size()][series.dateset.size()];
		
		series.createMatrix(new Action1<Triplet<Integer, Integer, Double>>() {
			@Override
			public void invoke(Triplet<Integer, Integer, Double> value) {
				allData[value.first][value.second] = value.third;
			}
		});

		final Map<Integer, String> modelReverseMap = Maps.newHashMap();
		
		for (Map.Entry<XElement, XElement> e : resolver().getMap(get(CLASS)).entrySet()) {
			String key = resolver().getString(e.getKey());
			int value = resolver().getInt(e.getValue());
			modelReverseMap.put(value, key);
		}

		Map<XElement, XElement> result = Maps.newHashMap();
		int j = 0;
		for (XElement xm : resolver().getItems(get(MODEL))) {
			
			String group = modelReverseMap.get(j);
			
			int midx = series.rowset.get(group);
			
			int p = xm.getInt("model-order");
			int m = 5;
			
			ArxModel arxModel = new ArxModel(p, m, new Func2<Integer, Integer, Double>() {
				@Override
				public Double invoke(Integer param1, Integer param2) {
					int dow = KMeansARXLearn.dayOfWeek(series.daysSinceEpoch.get(param1));
					return dow == param2.intValue() ? 1d : 0d;
				}
			});

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
			
			double[] forecasts = arxModel.forecastAll(allData[midx], horizon);
	
			List<XElement> forecastList = Lists.newArrayList();
			for (double d : forecasts) {
				forecastList.add(resolver().create(d));
			}
			
			result.put(resolver().create(group), resolver().create(forecastList));
			
			j++;
		}
		
		dispatch(PREDICTION, resolver().create(result));
	}

}
