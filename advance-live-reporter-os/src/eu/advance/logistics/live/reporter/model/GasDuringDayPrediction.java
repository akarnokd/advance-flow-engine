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

import org.joda.time.DateMidnight;
import org.joda.time.LocalTime;

/**
 * Galassify during-day prediction.
 * @author karnokd, 2013.09.23.
 *
 */
public class GasDuringDayPrediction {
	/** The hub. */
	public String hub;
	/** The depot. */
	public String depot;
	/** The day. */
	public DateMidnight day;
	/** The time. */
	public LocalTime time;
	/** Is inbound? */
	public boolean inbound;
	/** Service level. */
	public String service;
	/** Unit of measure. */
	public String unit;
	/** The day offset of the prediction relative to day. */
	public int dayOffset;
	/** The current value. */
	public double current;
	/** The remaining value. */
	public double remaining;
	/** The actual value. */
	public double actual;
	/** The attribute values as XML used when predicting. */
	public String attributeValues;
}
