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

package eu.advance.logistics.live.reporter.cron;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateMidnight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.advance.logistics.live.reporter.db.MasterDB;
import eu.advance.logistics.live.reporter.db.PredictionDB;
import eu.advance.logistics.live.reporter.model.ArxPrediction;
import eu.advance.logistics.live.reporter.model.ArxPredictionKey;
import eu.advance.logistics.live.reporter.model.CrontabTask;
import eu.advance.logistics.live.reporter.model.CrontabTaskSettings;
import eu.advance.logistics.live.reporter.model.TimedValue;
import eu.advance.logistics.live.reporter.prediction.arx.ArxCoefficients;
import eu.advance.logistics.live.reporter.prediction.arx.ArxConfig;
import eu.advance.logistics.live.reporter.prediction.arx.ArxLearner;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.procedure.TObjectDoubleProcedure;

/**
 * Executes the learning and prediction for the ARX model.
 * @author karnokd, 2013.05.27.
 */
public class ArxRunnerTask implements CrontabTask {
	/** The logger. */
	protected static final Logger LOGGER = LoggerFactory.getLogger(ArxRunnerTask.class);
	@Override
	public void execute(CrontabTaskSettings settings) throws Exception {
		final long hub = settings.parameters.getLong("hub");
		final int historyDays = settings.parameters.getInt("days");
		final int horizon = settings.parameters.getInt("horizon");
		final int shiftStartHour = settings.parameters.getInt("shift-start");
		final boolean blacklist = settings.parameters.getBoolean("use-blacklist");
		
		DateMidnight start = settings.lastCheck.toDateMidnight();
		DateMidnight end = start.plusDays(1);
		
		PredictionDB.deleteARXPredictions(hub, null, start.toDateTime(), end.toDateTime());
		DateMidnight window = start.minusDays(historyDays);
		final ArxConfig config = new ArxConfig();
		if (blacklist) {
			config.holidays.addAll(MasterDB.holidays());
			config.ignoreWeekends = true;
		} else {
			config.ignoreWeekends = false;
		}
		
		config.horizon = horizon;
		config.modelOrder = settings.parameters.getInt("model-order", config.modelOrder);
		
		final Map<ArxPredictionKey, TObjectDoubleMap<DateMidnight>> inbound = new HashMap<>();
		final Map<ArxPredictionKey, TObjectDoubleMap<DateMidnight>> outbound = new HashMap<>();
	
		Set<ArxPredictionKey> inboundSum = new HashSet<>();
		Set<ArxPredictionKey> outboundSum = new HashSet<>();
		
		inbound.entrySet();
		outbound.entrySet();
		
		LOGGER.debug("Loading manifests for date range " + window + " to " + end);

		PredictionDB.dailyManifests(hub, window, end, shiftStartHour, inbound, outbound);

		aggregateForHub(hub, inbound, outbound, inboundSum, outboundSum);

		ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		while (start.compareTo(end) <= 0) {
			final DateMidnight fstart = start;
			final DateMidnight fwindow = window;
			exec.execute(new Runnable() {
				@Override
				public void run() {
					LOGGER.debug("Starting day " + fstart);
					final List<ArxPrediction> ps = new ArrayList<>();
					for (final Map.Entry<ArxPredictionKey, TObjectDoubleMap<DateMidnight>> e : inbound.entrySet()) {
						ps.addAll(calculate(fstart, fwindow, hub, horizon, config, e, true));
					}
					for (final Map.Entry<ArxPredictionKey, TObjectDoubleMap<DateMidnight>> e : outbound.entrySet()) {
						ps.addAll(calculate(fstart, fwindow, hub, horizon, config, e, false));
					}
					LOGGER.debug("Saving day " + fstart + " with " + ps.size() + " entries");
					PredictionDB.saveARXPredictions(ps);
				}
			});
			
			start = start.plusDays(1);
			window = start.minusDays(historyDays);
		}
		exec.shutdown();
		exec.awaitTermination(6, TimeUnit.HOURS);
	}
	/**
	 * Aggregates the inbound and outbound manifests to a hub level.
	 * @param hub the hub id used as negative key
	 * @param inbound the inbound map
	 * @param outbound the outbound map
	 * @param inboundSum the inbound sum keys
	 * @param outboundSum the outbound sum keys
	 */
	public static void aggregateForHub(
			long hub,
			final Map<ArxPredictionKey, TObjectDoubleMap<DateMidnight>> inbound,
			final Map<ArxPredictionKey, TObjectDoubleMap<DateMidnight>> outbound,
			Set<ArxPredictionKey> inboundSum, Set<ArxPredictionKey> outboundSum) {
		for (ArxPredictionKey k : new ArrayList<>(inbound.keySet())) {
			ArxPredictionKey sumKey = new ArxPredictionKey(k.serviceLevel, k.unit, -hub);
			inbound.put(sumKey, new TObjectDoubleHashMap<DateMidnight>());
			inboundSum.add(sumKey);
		}
		for (Map.Entry<ArxPredictionKey, TObjectDoubleMap<DateMidnight>> e : inbound.entrySet()) {
			ArxPredictionKey sumKey = new ArxPredictionKey(e.getKey().serviceLevel, e.getKey().unit, -hub);
			final TObjectDoubleMap<DateMidnight> sum = inbound.get(sumKey);
			
			e.getValue().forEachEntry(new TObjectDoubleProcedure<DateMidnight>() {
				@Override
				public boolean execute(DateMidnight a, double b) {
					sum.adjustOrPutValue(a, b, b);
					return true;
				}
			});
		}
		for (ArxPredictionKey k : new ArrayList<>(outbound.keySet())) {
			ArxPredictionKey sumKey = new ArxPredictionKey(k.serviceLevel, k.unit, -hub);
			outbound.put(sumKey, new TObjectDoubleHashMap<DateMidnight>());
			outboundSum.add(sumKey);
		}
		for (Map.Entry<ArxPredictionKey, TObjectDoubleMap<DateMidnight>> e : outbound.entrySet()) {
			ArxPredictionKey sumKey = new ArxPredictionKey(e.getKey().serviceLevel, e.getKey().unit, -hub);
			final TObjectDoubleMap<DateMidnight> sum = outbound.get(sumKey);
			
			e.getValue().forEachEntry(new TObjectDoubleProcedure<DateMidnight>() {
				@Override
				public boolean execute(DateMidnight a, double b) {
					sum.adjustOrPutValue(a, b, b);
					return true;
				}
			});
		}
	}
	/**
	 * Calculate the prediction for the given time window and other configuration.
	 * @param start the target day to start predicting for
	 * @param window the window start for learning
	 * @param hub the target hub
	 * @param horizon the number of days to predict
	 * @param config the learning configuration
	 * @param e the time series for a particular model
	 * @param inbound is this the inbound record?
	 * @return the predicted values
	 */
	public static List<ArxPrediction> calculate(DateMidnight start, DateMidnight window,
			long hub, int horizon, ArxConfig config,
			Map.Entry<ArxPredictionKey, TObjectDoubleMap<DateMidnight>> e, boolean inbound) {
		
		ArxPredictionKey key = e.getKey();
		
		final List<TimedValue> values = new ArrayList<>(256);
		
		final DateMidnight fwindow = window;
		final DateMidnight fstart = start;
		
		// create the subset of dates
		e.getValue().forEachEntry(new TObjectDoubleProcedure<DateMidnight>() {
			@Override
			public boolean execute(DateMidnight a, double b) {
				if (a.compareTo(fwindow) >= 0 && a.compareTo(fstart) < 0) {
					values.add(new TimedValue(a, b));
				}
				return true;
			}
		});
		// ensure the time data ends at start - 1 days
		values.add(new TimedValue(fstart.minusDays(1), 0));

		ArxCoefficients coeff = ArxLearner.learn(values, config);
		List<TimedValue> forecasts = ArxLearner.predict(config, coeff, values, horizon);

		List<ArxPrediction> ps = new ArrayList<>();
		for (TimedValue tv : forecasts) {
			ArxPrediction p = new ArxPrediction();
			p.hub = hub;
			p.depot = key.depot;
			p.dayRun = start.toDateTime();
			p.inbound = inbound;
			p.service = key.serviceLevel;
			p.unit = key.unit;
			p.dayPredict = tv.day;
			p.value = tv.value;
			ps.add(p);
		}
		return ps;
	}

}
