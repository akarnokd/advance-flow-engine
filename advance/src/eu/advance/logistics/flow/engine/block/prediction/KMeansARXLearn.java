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

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Func2;
import hu.akarnokd.reactive4java.base.Pred1;
import hu.akarnokd.reactive4java.query.IterableBuilder;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
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
    /*
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
    */
	/**
	 * Aggregates a sequence of group-time-values.
	 * @author karnokd, 2012.10.29.
	 */
	public static class TimeseriesAggregator {
		/** The sparse map for each group, day to value. */
		public final Map<String, Map<DateMidnight, Double>> groupTimeValues = Maps.newHashMap();
		/** The total set of dates. */
		public final Set<DateMidnight> dates = Sets.newHashSet();
		/** The group indexes for the matrix. */
		public final BiMap<String, Integer> groupIndex = HashBiMap.create();
		/** The time series matrix. */
		public double[][] timeSeriesMatrix;
		/** The days since epoch for each column. */
		public int[] daysSinceEpoch;
		/** Filter dates. */
		public Func1<DateMidnight, Boolean> dateFilter;
		/** Filter groups. */
		public Func1<String, Boolean> groupFilter;
		/**
		 * Add a new value for the specified group and time.
		 * @param group the group
		 * @param time the time
		 * @param value the value
		 */
		public void add(String group, DateMidnight time, double value) {
			if (groupFilter == null || groupFilter.invoke(group)) {
				if (dateFilter == null || dateFilter.invoke(time)) {
					Map<DateMidnight, Double> timeValues = groupTimeValues.get(group);
					if (timeValues == null) {
						timeValues = Maps.newHashMap();
						groupTimeValues.put(group, timeValues);
					}
					Double v = timeValues.get(time);
					timeValues.put(time, v != null ? v + value : value);
					dates.add(time);
					
					if (!groupIndex.containsKey(group)) {
						groupIndex.put(group, groupIndex.size());
					}
				}
			}
		}
		/**
		 * Constructs the raw matrix from weekdays only.
		 */
		public void buildWeekdaysOnly() {
			Set<DateMidnight> dates = Sets.newHashSet(IterableBuilder.from(this.dates).where(new Pred1<DateMidnight>() { 
				@Override
				public Boolean invoke(DateMidnight dm) {
					int dow = dm.getDayOfWeek();
					return dow != DateTimeConstants.SATURDAY && dow != DateTimeConstants.SUNDAY;
				}
			}));
			
			DateMidnight min = Collections.min(dates);
			DateMidnight max = Collections.max(dates);

			Map<DateMidnight, Integer> dateIndex = Maps.newHashMap();
			// count the number of weekdays between min and max
			int times = 0;
			DateMidnight dt = min;
			while (dt.compareTo(max) <= 0) {
				int dow = dt.getDayOfWeek();
				if (dow != DateTimeConstants.SATURDAY && dow != DateTimeConstants.SUNDAY) {
					times++;
					dateIndex.put(dt, dateIndex.size());
				}
				dt = dt.plusDays(1);
			}

			int groups = groupTimeValues.size();
			
			daysSinceEpoch = new int[times];
			timeSeriesMatrix = new double[groups][times];
			
			for (DateMidnight dm : dates) {
				int dse = (int)(dm.getMillis() / (24L * 60 * 60 * 1000));
				int idx = dateIndex.get(dm);
				
				daysSinceEpoch[idx] = dse;
				
				for (Map.Entry<String, Map<DateMidnight, Double>> e : groupTimeValues.entrySet()) {
					int gidx = groupIndex.get(e.getKey());
					
					Double v = e.getValue().get(dm);
					
					if (v != null) {
						timeSeriesMatrix[gidx][idx] = v;
					}
				}
			}
		}
		/**
		 * Creates and loads the time series from the supplied timedvaluegroup sequence.
		 * @param items the items
		 * @return the built aggregator
		 */
		public static TimeseriesAggregator load(Iterable<XElement> items) {
			final TimeseriesAggregator series = new TimeseriesAggregator();
			
			for (XElement e : items) {
				try {
					DateMidnight dt = new DateMidnight(XElement.parseDateTime(e.get("timestamp")).getTime());
					String group = e.get("group");
					double v = e.getDouble("value");
					series.add(group, dt, v);
				} catch (ParseException ex) {
					
				}
			}

			series.buildWeekdaysOnly();
			
			
			return series;
		}
	}
	@Override
	protected void invoke() {

		XElement cfg = get(CONFIG);
		
		final TimeseriesAggregator series = TimeseriesAggregator.load(resolver().getItems(get(DATA)));

		final double split = cfg.getDouble("split"); //  
		int p = cfg.getInt("model-order"); // doc: n
		int modelCount = cfg.getInt("cluster-count"); // doc: K
		int maxIter = cfg.getInt("max-iteration"); // 100
		int horizon = cfg.getInt("horizon"); // 2
		int m = 5;
		boolean normalize = cfg.getBoolean("normalize"); // true

		int minSamples = (int)Math.ceil((1 + split) * p);
		
		if (series.daysSinceEpoch.length >= minSamples) {
			KMeansARX kMeans = new KMeansARX(
					series.timeSeriesMatrix, 
					(int)(series.daysSinceEpoch.length * split), 
					p, m, modelCount, maxIter, normalize, horizon,
					new Func2<Integer, Integer, Double>() {
				@Override
				public Double invoke(Integer param1, Integer param2) {
					int dow = dayOfWeek(series.daysSinceEpoch[param1]);
					return param2.intValue() == dow ? 1d : 0d;
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
				Set<Integer> rows = kMeans.getTimeseriesRowsForCluster(cl);
				for (Map.Entry<String, Integer> e : series.groupIndex.entrySet()) {
					if (rows.contains(e.getValue().intValue())) {
						classesMap.put(resolver().create(e.getKey()), resolver().create(cl));
					}
				}
			}
			
			
			dispatch(CLASSES, resolver().create(classesMap));
			dispatch(MODEL, resolver().create(models));
			
			dispatch(L1_TRAIN_ERROR, resolver().create(kMeans.getL1TrainError()));
			dispatch(L2_TRAIN_ERROR, resolver().create(kMeans.getL2TrainError()));
			dispatch(L1_TEST_ERROR, resolver().create(kMeans.getL1TestError()));
			dispatch(L2_TEST_ERROR, resolver().create(kMeans.getL2TestError()));
		} else {
			dispatch(CLASSES, resolver().create(Maps.<XElement, XElement>newHashMap()));
			dispatch(MODEL, resolver().create(Lists.<XElement>newArrayList()));

			dispatch(L1_TRAIN_ERROR, resolver().create(-1d));
			dispatch(L2_TRAIN_ERROR, resolver().create(-1d));
			dispatch(L1_TEST_ERROR, resolver().create(-1d));
			dispatch(L2_TEST_ERROR, resolver().create(-1d));
		}
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
