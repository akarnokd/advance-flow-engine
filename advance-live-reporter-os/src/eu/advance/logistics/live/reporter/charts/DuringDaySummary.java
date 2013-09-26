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

import eu.advance.logistics.live.reporter.db.ConsignmentDB;
import eu.advance.logistics.live.reporter.db.MLDB;
import eu.advance.logistics.live.reporter.model.DuringAmountStatus;
import eu.advance.logistics.live.reporter.model.DeclaredProgress;
import eu.advance.logistics.live.reporter.model.MlPrediction;
import eu.advance.logistics.live.reporter.model.ServiceLevel;
import gnu.trove.set.TLongSet;
import hu.akarnokd.utils.collection.AggregatorMap1;
import hu.akarnokd.utils.trove.TroveUtils;

import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to create the during day summary data.
 * @author karnokd, 2013.07.04.
 */
public final class DuringDaySummary {
	/** The logger. */
	protected static final Logger LOGGER = LoggerFactory.getLogger(DuringDaySummary.class);
	/** Helper class. */
	private DuringDaySummary() { }
	/**
	 * Fill in the DuringDayData.
	 * @param duringData the output 
	 * @param now the current time
	 * @param hub the target hub
	 */
	public static void createDuringData(DuringDayData duringData, DateTime now, long hub) {
		TLongSet depots = null;
		if (duringData.type == TypeStatus.DEPOT) {
			depots = TroveUtils.singleton(duringData.id);
		}
		LOGGER.debug("DD: Loading predictions for " + now);
		List<MlPrediction> preds = MLDB.getPredictionsOnDay(hub, depots, now);
		Map<LocalTime, List<MlPrediction>> pmap = MlPrediction.map(preds);

		LOGGER.debug("DD: Loading declared items up to " + now);
		AggregatorMap1<LocalTime, List<DeclaredProgress>> mps = ConsignmentDB.getDeclaredProgress(hub, depots, now, DuringDayData.QUARTER_HOUR);

		LOGGER.debug("DD: Aggregating " + now);
		DateTime day = now.withTimeAtStartOfDay();

		LocalTime minTime = new LocalTime(9, 0);
		LocalTime endTime = new LocalTime(20, 0);
		LocalTime nt = now.toLocalTime();
		endTime = nt.compareTo(endTime) < 0 ? nt : endTime;

		NavigableSet<LocalTime> dates = new TreeSet<>();
		for (LocalTime lt : pmap.keySet()) {
			if (lt.compareTo(minTime) < 0 || lt.compareTo(endTime) > 0) {
				continue;
			}
			dates.add(lt);
		}
		for (LocalTime lt : mps.keys()) {
			if (lt.compareTo(minTime) < 0 || lt.compareTo(endTime) > 0) {
				continue;
			}
			dates.add(lt);
		}

		for (LocalTime lt : dates) {
			if (lt.compareTo(minTime) < 0 || lt.compareTo(endTime) > 0) {
				continue;
			}
			List<DeclaredProgress> list = mps.getValue(lt, null);
			if (list == null) {
				continue;
			}
			DateTime dt = day.withTime(lt.getHourOfDay(), lt.getMinuteOfHour(), lt.getSecondOfMinute(), lt.getMillisOfSecond());

			for (DeclaredProgress mp :list) {
				OrientStatus ost = null;
				if (duringData.orient == OrientStatus.SINGLE && mp.inbound) {
					ost = OrientStatus.SINGLE;
				} else {
					ost = mp.inbound ? OrientStatus.ORIGIN : OrientStatus.DESTIN;
				}

				ServiceLevel sst = mp.service;

				BarData bd = duringData.itemsInOut.getValue(ost, dt, DuringAmountStatus.ACTUAL, sst, null);
				if (bd != null) {
					duringData.itemsInOut.put(ost, dt, DuringAmountStatus.ACTUAL, sst, new BarData(mp.value(duringData.unit) + bd.raw));
				} else {
					duringData.itemsInOut.put(ost, dt, DuringAmountStatus.ACTUAL, sst, new BarData(mp.value(duringData.unit)));
				}

				sst = ServiceLevel.ALL;
				bd = duringData.itemsInOut.getValue(ost, dt, DuringAmountStatus.ACTUAL, sst, null);
				if (bd != null) {
					duringData.itemsInOut.put(ost, dt, DuringAmountStatus.ACTUAL, sst, new BarData(mp.value(duringData.unit) + bd.raw));
				} else {
					duringData.itemsInOut.put(ost, dt, DuringAmountStatus.ACTUAL, sst, new BarData(mp.value(duringData.unit)));
				}
			}
		}

		for (LocalTime lt : dates) {
			if (lt.compareTo(minTime) < 0 || lt.compareTo(endTime) > 0) {
				continue;
			}
			List<MlPrediction> list = pmap.get(lt);
			if (list == null) {
				LocalTime lt0 = lt;
				while (lt0 != null && list == null) {
					lt0 = dates.lower(lt0);
					if (lt0 != null) {
						list = pmap.get(lt0);
					}
				}
				if (list == null) {
					continue;
				}
			}
			DateTime dt = day.withTime(lt.getHourOfDay(), lt.getMinuteOfHour(), lt.getSecondOfMinute(), lt.getMillisOfSecond());

			for (MlPrediction mp : list) {
				if (mp.dayOffset == 0 && mp.unit == duringData.unit) {
					if (mp.service != ServiceLevel.ALL) {

						OrientStatus ost = null;
						if (duringData.orient == OrientStatus.SINGLE && mp.inbound) {
							ost = OrientStatus.SINGLE;
						} else {
							ost = mp.inbound ? OrientStatus.ORIGIN : OrientStatus.DESTIN;
						}

						ServiceLevel sst = mp.service;

						BarData bd = duringData.itemsInOut.getValue(ost, dt, DuringAmountStatus.PREDICTED, sst, null);
						if (bd != null) {
							duringData.itemsInOut.put(ost, dt, DuringAmountStatus.PREDICTED, sst, new BarData(mp.current + mp.remaining + bd.raw));
						} else {
							duringData.itemsInOut.put(ost, dt, DuringAmountStatus.PREDICTED, sst, new BarData(mp.current + mp.remaining));
						}

						sst = ServiceLevel.ALL;
						bd = duringData.itemsInOut.getValue(ost, dt, DuringAmountStatus.PREDICTED, sst, null);
						if (bd != null) {
							duringData.itemsInOut.put(ost, dt, DuringAmountStatus.PREDICTED, sst, new BarData(mp.current + mp.remaining + bd.raw));
						} else {
							duringData.itemsInOut.put(ost, dt, DuringAmountStatus.PREDICTED, sst, new BarData(mp.current + mp.remaining));
						}
					}
				}
			}
		}
		for (BarData bd : duringData.itemsInOut.values4()) {
			bd.setFromRaw();
		}
	}
}
