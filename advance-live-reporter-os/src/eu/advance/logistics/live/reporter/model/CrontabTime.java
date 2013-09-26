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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import gnu.trove.TIntCollection;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.utils.trove.TroveUtils;
import hu.akarnokd.utils.xml.XElement;

import org.joda.time.DateTime;
import org.joda.time.ReadableDateTime;

/**
 * Record that represents the time pattern to run a crontab task.
 * @author karnokd, 2013.05.27.
 */
public class CrontabTime implements Func1<ReadableDateTime, Boolean> {
	/** The set of allowed years, or null for any year. */
	public TIntSet years;
	/** The set of allowed months, or null for any month. */
	public TIntSet months;
	/** The set of allowed days, or null for any day. */
	public TIntSet days;
	/** The set of allowed weekdays, or null for any day. See DateTimeConstants for the weekday values */
	public TIntSet weekdays;
	/** The set of allowed hours, or null for any hour. */
	public TIntSet hours;
	/** The set of allowed minutes, or null for any minute. */
	public TIntSet minutes;
	/** Default constructor. */
	public CrontabTime() {
		
	}
	/**
	 * Copy constructor.
	 * @param other the other crontab time
	 */
	public CrontabTime(CrontabTime other) {
		years = other.years != null ? new TIntHashSet(other.years) : null;
		months = other.months != null ? new TIntHashSet(other.months) : null;
		days = other.days != null ? new TIntHashSet(other.days) : null;
		weekdays = other.weekdays != null ? new TIntHashSet(other.weekdays) : null;
		hours = other.hours != null ? new TIntHashSet(other.hours) : null;
		minutes = other.minutes != null ? new TIntHashSet(other.minutes) : null;
	}
	/**
	 * Loads the settings from the given XML.
	 * @param entry the source XML
	 */
	public void load(@NonNull XElement entry) {
		years = extract(entry, "years");
		months = extract(entry, "months");
		days = extract(entry, "days");
		hours = extract(entry, "hours");
		minutes = extract(entry, "minutes");
		weekdays = extract(entry, "weekdays");
	}
	/**
	 * Returns a set of allowed values for the given attribute name.
	 * @param entry the XML element
	 * @param name the attribute name
	 * @return the set
	 */
	@Nullable
	protected TIntSet extract(@NonNull XElement entry, @NonNull String name) {
		String xv = entry.get(name, null);
		if (xv != null && !"*".equals(xv)) {
			String[] xvs = xv.split("\\s*,\\s*");
			TIntSet r = new TIntHashSet();
			for (String s : xvs) {
				r.add(Integer.parseInt(s));
			}
			return r;
		}
		return null;
	}
	/**
	 * Sets an attribute with the contents of the collection if it is not null or empty.
	 * @param out the output xelement
	 * @param name the attribute name
	 * @param coll the collection
	 */
	protected void set(@NonNull XElement out, @NonNull String name, @Nullable TIntCollection coll) {
		if (TroveUtils.isNullOrEmpty(coll)) {
			out.set(name, "*");
		} else {
			out.set(name, TroveUtils.join(coll, ","));
		}
	}
	/**
	 * Saves the settings into the given XML.
	 * @param out the output XML
	 */
	public void save(@NonNull XElement out) {
		set(out, "years", years);
		set(out, "months", months);
		set(out, "days", days);
		set(out, "weekdays", weekdays);
		set(out, "hours", hours);
		set(out, "minutes", minutes);
	}
	@Override
	public Boolean invoke(ReadableDateTime rdt) {
		DateTime dt = rdt.toDateTime();
		if (!TroveUtils.isNullOrEmpty(years)) {
			if (!years.contains(dt.getYear())) {
				return false;
			}
		}
		if (!TroveUtils.isNullOrEmpty(months)) {
			if (!months.contains(dt.getMonthOfYear())) {
				return false;
			}
		}
		if (!TroveUtils.isNullOrEmpty(days)) {
			if (!days.contains(dt.getDayOfMonth())) {
				return false;
			}
		}
		if (!TroveUtils.isNullOrEmpty(weekdays)) {
			if (!weekdays.contains(dt.getDayOfWeek())) {
				return false;
			}
		}
		if (!TroveUtils.isNullOrEmpty(hours)) {
			if (!hours.contains(dt.getHourOfDay())) {
				return false;
			}
		}
		if (!TroveUtils.isNullOrEmpty(minutes)) {
			if (!minutes.contains(dt.getMinuteOfHour())) {
				return false;
			}
		}
		return true;
	}
}
