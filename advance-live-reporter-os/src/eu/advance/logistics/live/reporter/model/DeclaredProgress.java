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

import java.util.EnumMap;

import org.joda.time.DateTime;

/**
 * Record that keeps track of declared items during a day.
 * @author karnokd, 2013.07.04.
 */
public class DeclaredProgress {
	/** The target hub. */
	public long hub;
	/** The target depot. */
	public long depot;
	/** The timestamp. */
	public DateTime timestamp;
	/** The service level. */
	public ServiceLevel service;
	/** Is it inbound? */
	public boolean inbound;
	/** The progress values. */
	public final EnumMap<UOM, Double> values = new EnumMap<>(UOM.class);
	@Override
	public String toString() {
		return "DeclaredProgress [hub=" + hub + ", depot=" + depot
				+ ", timestamp=" + timestamp + ", level=" + service
				+ ", inbound=" + inbound + ", values=" + values + "]";
	}
	/**
	 * Add a value to the specified unit.
	 * @param unit the unit
	 * @param value the value to add
	 */
	public void add(UOM unit, double value) {
		Double v = values.get(unit);
		values.put(unit, v != null ? v + value : value);
	}
	/**
	 * Returns a value or zero for an unit.
	 * @param unit the unit
	 * @return the value
	 */
	public double value(UOM unit) {
		Double v = values.get(unit);
		return v != null ? v : 0d;
	}
}
