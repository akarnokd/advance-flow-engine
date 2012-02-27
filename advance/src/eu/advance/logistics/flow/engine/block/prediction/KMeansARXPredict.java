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

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * The prediction calculator for the K-means ARX model for a single group.
 * @author karnokd, 2012.02.27.
 */
@Block(id = "KMeansARXPredict", 
category = "prediction", scheduler = "CPU", 
description = "Predicts the subsequent values of an ARX model.")
public class KMeansARXPredict extends AdvanceBlock {
	/** The input of timestamp-value pairs. */
	@Input("advance:collection<advance:timedvalue>")
	protected static final String DATA = "data";
	/** The ARX model. */
	@Input("advance:arxmodel")
	protected static final String MODEL = "model";
	/** The lenght of the horizon. */
	@Input("advance:integer")
	protected static final String HORIZON = "horizon";
	/** The collection of predicted values for the next timestamps. */
	@Input("advance:collection<advance:real>")
	protected static final String PREDICTION = "prediction";
	@Override
	protected void invoke() {
		
		int horizon = getInt(HORIZON);
		
		Map<DateTime, Integer> dateset = Maps.newHashMap();
		
		Map<Integer, Double> values = Maps.newHashMap();
		for (XElement e : resolver().getItems(get(DATA))) {
			try {
				DateTime dt = new DateTime(resolver().getTimestamp(e.childElement("timestamp")).getTime());
				double v = resolver().getDouble(e.childElement("value"));
	
				Integer idx = dateset.get(dt);
				
				if (idx == null) {
					idx = dateset.size();
					dateset.put(dt, idx);
				}
				
				Double d = values.get(idx);
				if (d == null) {
					d = v;
				} else {
					d += v;
				}
				values.put(idx, d);
				
			} catch (ParseException ex) {
				log(ex);
			}
		}

		final int[] tf = new int[dateset.size()];
		
		double[] xs = new double[dateset.size()];
		List<DateTime> dts = Lists.newArrayList(dateset.keySet());
		Collections.sort(dts);
		for (DateTime dt : dts) {
			Integer idx = dateset.get(dt);
			xs[idx] = values.get(idx);
			tf[idx] = (int)(dt.getMillis() / (24L * 60 * 60 * 1000)); 
		}

		XElement xm = get(MODEL);
		
		int p = xm.getInt("model-order");
		int m = 5;
		
		ArxModel arxModel = new ArxModel(p, m, new Func2<Integer, Integer, Double>() {
			@Override
			public Double invoke(Integer param1, Integer param2) {
				int dow = KMeansARXLearn.dayOfWeek(tf[param1]);
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
		
		double[] forecasts = arxModel.forecastAll(xs, horizon);
		
		List<XElement> result = Lists.newArrayList();
		for (double d : forecasts) {
			result.add(resolver().create(d));
		}
		dispatch(PREDICTION, result);
	}

}
