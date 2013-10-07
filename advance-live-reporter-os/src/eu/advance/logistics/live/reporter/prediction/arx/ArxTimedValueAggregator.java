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
package eu.advance.logistics.live.reporter.prediction.arx;

import eu.advance.logistics.live.reporter.model.TimedValue;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

/**
 * The time series aggregator.
 * @author karnokd, 2013.05.03.
 */
public class ArxTimedValueAggregator {
	/** The aggregated map of values for each day. */
	public final TObjectDoubleMap<DateMidnight> dayValues = new TObjectDoubleHashMap<>();
	/** The minimum date in the dayValues. */
	public DateMidnight minDate;
	/** The maximum date in the dayValues. */
	public DateMidnight maxDate;
	/** 
	 * The time series matrix where each row is a group (unused) and each column
	 * is a day. 
	 */
	public double[][] timeSeriesMatrix;
	/** The precomputed day of week for each column in the time series matrix above. */
	public final TIntList dayOfWeek = new TIntArrayList();
	/** The individual days in the columns. */
	private List<DateMidnight> days;
	/** The holidays. */
	private final Set<DateMidnight> holidays;
	/** Ignore weekends? */
	private final boolean ignoreWeekends;
	/**
	 * Constructor, sets the ignore dates.
	 * @param holidays the holidays to ignore
	 * @param ignoreWeekends ignore weekends?
	 */
	public ArxTimedValueAggregator(Set<DateMidnight> holidays, boolean ignoreWeekends) {
		this.holidays = holidays;
		this.ignoreWeekends = ignoreWeekends;
	}
	/**
	 * Adds a timed value to this aggregator.
	 * @param tv the timed value
	 */
	public void add(TimedValue tv) {
		dayValues.adjustOrPutValue(tv.day, tv.value, tv.value);
		if (minDate == null || minDate.compareTo(tv.day) > 0) {
			minDate = tv.day;
		}
		if (maxDate == null || maxDate.compareTo(tv.day) < 0) {
			maxDate = tv.day;
		}
	}
	/**
	 * Add all elements from the sequence.
	 * @param tvs the sequence
	 */
	public void addAll(Iterable<? extends TimedValue> tvs) {
		for (TimedValue tv : tvs) {
			add(tv);
		}
	}
	/**
	 * Build the time series matrix and day of week array.
	 */
	public void build() {
		days = new ArrayList<>(dayValues.size());
		DateMidnight dt = minDate;
		while (dt.compareTo(maxDate) <= 0) {
			if (!holidays.contains(dt)) {
				int dow = dt.getDayOfWeek();
				if (!ignoreWeekends || (dow != DateTimeConstants.SATURDAY && dow != DateTimeConstants.SUNDAY)) {
					days.add(dt);
					dayOfWeek.add(dow - 1);
				}
			}
			dt = dt.plusDays(1);
		}
		timeSeriesMatrix = new double[1][days.size()];
		int i = 0;
		for (DateMidnight d : days) {
			timeSeriesMatrix[0][i] = dayValues.get(d);
			i++;
		}
	}
	/**
	 * Adds the additional number of days after
	 * the maximum date for forecasting purposes.
	 * @param horizon the number of days
	 * @return the new days
	 */
	public List<DateMidnight> addForecastDays(int horizon) {
		DateMidnight dm = maxDate;
		int d = horizon;
		List<DateMidnight> horizonDays = new ArrayList<>();
		while (d > 0) {
			dm = dm.plusDays(1);
			if (!holidays.contains(dm)) {
				int dow = dm.getDayOfWeek();
				if (!ignoreWeekends 
						|| (dow != DateTimeConstants.SATURDAY 
						&& dow != DateTimeConstants.SUNDAY)) {
					d--;
					dayOfWeek.add(dow - 1);
					horizonDays.add(dm);
				}
			}
		}
		return horizonDays;
	}
}