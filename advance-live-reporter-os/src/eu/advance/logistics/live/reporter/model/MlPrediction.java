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
package eu.advance.logistics.live.reporter.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.DateMidnight;
import org.joda.time.LocalTime;

/**
 * The during day prediction record class.
 * @author karnokd, 2013.09.23.
 */
public class MlPrediction {
	/** The hub. */
	public long hub;
	/** The depot. */
	public long depot;
	/** The target day. */
	public DateMidnight day;
	/** The target time. */
	public LocalTime time;
	/** The day of prediction relative to day. */
	public int dayOffset;
	/** The service level. */
	public ServiceLevel service;
	/** Is inbound? */
	public boolean inbound;
	/** Unit of measure. */
	public UOM unit;
	/** The current declared. */
	public double current;
	/** The remaining declared. */
	public double remaining;
	/** The actual declared. */
	public double actual;
	/**
	 * Map the sequence of predictions according to local time.
	 * @param src the source sequence
	 * @return the mapped predictions
	 */
	public static Map<LocalTime, List<MlPrediction>> map(Iterable<? extends MlPrediction> src) {
		Map<LocalTime, List<MlPrediction>> r = new TreeMap<>();
		for (MlPrediction mp : src) {
			List<MlPrediction> list = r.get(mp.time);
			if (list == null) {
				list = new ArrayList<>();
				r.put(mp.time, list);
			}
			list.add(mp);
		}
		return r;
	}
}
