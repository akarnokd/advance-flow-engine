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
import hu.akarnokd.reactive4java.reactive.Observer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.util.Triplet;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * K-means algorithm for classifying multiple time series.
 * @author karnokd, 2012.02.27.
 */
@Block(id = "KMeansARXLearn", 
category = "prediction", scheduler = "CPU", 
description = "K-means algorithm for classifying multiple time series.")
public class KMeansARXLearn extends AdvanceBlock {
	/** The model configuration. */
	@Input("advance:kmeans_arx_config")
	protected static final String CONFIG = "config";
	/** The collection of timed value group. */
    @Input("advance:collection<advance:timedvaluegroup>")
    protected static final String DATA = "data";
    /** The learned ARX models. */
    @Output("advance:collection<advance:arxmodel>")
    protected static final String MODEL = "model";
    /** Training error. */
    @Output("advance:real")
    protected static final String L1_TRAIN_ERROR = "L1TrainError";
    /** Training error. */
    @Output("advance:real")
    protected static final String L2_TRAIN_ERROR = "L2TrainError";
    /** Test error. */
    @Output("advance:real")
    protected static final String L1_TEST_ERROR = "L1TestError";
    /** Test error. */
    @Output("advance:real")
    protected static final String L2_TEST_ERROR = "L2TestError";
    /** The class index of the groups. */
    @Output("advance:map<advance:string, advance:integer>")
    protected static final String CLASSES = "classes";
    /** The Configuration. */
    protected AtomicReference<XElement> config = new AtomicReference<XElement>();
    
    @Override
    public Observer<Void> run() {
    	
    	RunObserver obs = new RunObserver();
    	
		observeInput(CONFIG, new Action1<XElement>() {
			@Override
			public void invoke(XElement value) {
				config.set(value);
			}
		});
		
		observeInput(DATA, new Action1<XElement>() {
			@Override
			public void invoke(XElement value) {
				params.put(DATA, value);
				KMeansARXLearn.this.invoke();
			}
		}, obs);
  	
    	return obs;
    }
    
	@Override
	protected void invoke() {

		XElement cfg = config.get();
		
		final TimeGroupSeriesMapper series = new TimeGroupSeriesMapper();
		
		series.map(resolver().getItems(get(DATA)), resolver());
		
		int sampleCount = series.datamap.size();
		int timeCount = series.dateset.size();
		
		final int split = (int)(timeCount * cfg.getDouble("split")); //  
		int p = cfg.getInt("model-order"); // doc: n
		int modelCount = cfg.getInt("cluster-count"); // doc: K
		int maxIter = cfg.getInt("max-iteration"); // 100
		int horizon = cfg.getInt("horizon"); // 2
		int m = 5;
		boolean normalize = cfg.getBoolean("normalize"); // true

		
		// convert to matrix
		final double[][] allData = new double[sampleCount][timeCount];
		
		series.createMatrix(new Action1<Triplet<Integer, Integer, Double>>() {
			@Override
			public void invoke(Triplet<Integer, Integer, Double> value) {
				allData[value.first][value.second] = value.third;
			}
		});
		
		
		KMeansARX kMeans = new KMeansARX(allData, split, 
				p, m, modelCount, maxIter, normalize, horizon,
				new Func2<Integer, Integer, Double>() {
			@Override
			public Double invoke(Integer param1, Integer param2) {
				int dow = dayOfWeek(series.daysSinceEpoch.get(param1));
				return dow == param2.intValue() ? 1d : 0d;
			}
		});
		
		kMeans.solve();

		List<XElement> models = Lists.newArrayList();
		
		for (ArxModel md : kMeans.models()) {
			
			XElement xm = new XElement("arxmodel");
			xm.set("model-order", md.getP());
			xm.set("external-order", md.getM());
			
			for (double ac : md.getArCoefficients()) {
				xm.add("model-coefficient").content = Double.toString(ac);
			}
			for (double uc : md.getUCoefficients()) {
				xm.add("external-coefficient").content = Double.toString(uc);
			}
			
			models.add(xm);
		}

		int[] classes = kMeans.getClasses();

		Map<XElement, XElement> classesMap = Maps.newHashMap();
		
		for (int cl : classes) {
			for (Map.Entry<String, Integer> e : series.rowset.entrySet()) {
				if (e.getValue().intValue() == cl) {
					classesMap.put(resolver().create(e.getKey()), resolver().create(cl));
				}
			}
		}
		
		
		dispatch(CLASSES, resolver().create(classesMap));
		
		dispatch(MODEL, resolver().create(models));
		dispatch(L1_TRAIN_ERROR, kMeans.getL1TrainError());
		dispatch(L2_TRAIN_ERROR, kMeans.getL2TrainError());
		dispatch(L1_TEST_ERROR, kMeans.getL1TestError());
		dispatch(L2_TEST_ERROR, kMeans.getL2TestError());
		
	}
	/**
	 * Compute the current day of week (0 - Monday, 6 - Sunday) from the given days since the unix epoch.
	 * @param daysSinceEpoch the days since the unix epoch
	 * @return the day of week
	 */
	public static int dayOfWeek(int daysSinceEpoch) {
		return (3 + (daysSinceEpoch % 7)) % 7;
	}
}
