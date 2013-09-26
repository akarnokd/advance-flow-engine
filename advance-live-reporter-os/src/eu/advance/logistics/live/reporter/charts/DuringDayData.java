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

import hu.akarnokd.reactive4java.interactive.Interactive;
import hu.akarnokd.utils.collection.AggregatorHashMap4;
import hu.akarnokd.utils.collection.AggregatorMap1;
import hu.akarnokd.utils.collection.AggregatorMap4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLStreamException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.Iterables;

import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.live.reporter.model.ChartView;
import eu.advance.logistics.live.reporter.model.DuringAmountStatus;
import eu.advance.logistics.live.reporter.model.ServiceLevel;
import eu.advance.logistics.live.reporter.model.UOM;

/**
 * DuringDayData record stores data for during-day chart.
 * @author csirobi, 2013.06.24.
 */
public class DuringDayData {
	/** Quarter of an hour in minutes. */
	public static final int QUARTER_HOUR = 15;

	/** Chart view based on hub or depot user. */
	public ChartView chartView;
	/** Type of hub/depot. */
	public TypeStatus type;
	/** Id of hub/depot. */
	public long id;
	/** Unit of hub/depot. */
	public UOM unit;
	/** Direction of depot. */
	public OrientStatus orient;
	/** Display name of hub/depot. */
	public String name;
	/** Business-as-usual-scale of hub/depot. */
	public int busAsUsSc;
	/** Aggregator map for orient, time, amount, snip and bardata. */
	public final AggregatorMap4<OrientStatus, DateTime, DuringAmountStatus, ServiceLevel, BarData> itemsInOut;
	/** The default during-day configuration resource. */
	private static final String DURING_DAY = "/duringday.xml";
	/** The default first time slot to display. */
	private static DateTime firstTime;
	/** The default last time slot to display. */
	private static DateTime lastTime;
	/** The default number of time slot vertical line. */
	private static int quarterLine;
	/** The time format for during-day. */
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("HH:mm:ss");
	static {
		try {
			XElement xDuring = XElement.parseXML(DuringDayData.class.getResource(DURING_DAY));

			try {
				firstTime = DateTime.parse(xDuring.childValue("firstTime"), TIME_FORMAT);
				if (firstTime.getHourOfDay() < 9) {
					firstTime = new DateTime().withHourOfDay(9).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
				}
			} catch (IllegalArgumentException e) {
				firstTime = new DateTime().withHourOfDay(9).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
			}
			try {
				lastTime = DateTime.parse(xDuring.childValue("lastTime"), TIME_FORMAT);
				if (20 < lastTime.getHourOfDay()) {
					lastTime = new DateTime().withHourOfDay(20).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
				}
			} catch (IllegalArgumentException e) {
				lastTime = new DateTime().withHourOfDay(20).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
			}

			quarterLine = Integer.parseInt(xDuring.childValue("timeSlot"));

		} catch (IllegalArgumentException | IOException | XMLStreamException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Constructor with the switch source.
	 * @param hubDepotSwitch the switch source
	 */
	public DuringDayData(HubDepotSwitch hubDepotSwitch) {
		this.chartView = hubDepotSwitch.getChartView();
		this.type = hubDepotSwitch.getTypeStatus();
		this.id = hubDepotSwitch.getHubDepotInfo().id;
		this.unit = hubDepotSwitch.getUnit();
		this.orient = hubDepotSwitch.convertDuringOrient();
		this.name = this.orient.getMessage(this.chartView) + hubDepotSwitch.getHubDepotInfo().getHubDepotInfo(this.chartView);
		this.busAsUsSc = hubDepotSwitch.getHubDepotInfo().busAsUsualScale.get(hubDepotSwitch.getUnit());

		this.itemsInOut = new AggregatorHashMap4<>();
	}

	/**
	 * Normalize value of items based on total coordinate.
	 * @param coord bar coordinate
	 */
	public void normalizeBy(BarCoordinate coord) {
		GraphDecimal ratio = coord.totalCoord;

		for (BarData bd : itemsInOut.values4()) {
			bd.normalize(ratio);
		}
	}

	/**
	 * Checking the business-as-usual-scale to be bigger than the items data.
	 */
	public void checkBaus() {
		for (AggregatorMap1<ServiceLevel, BarData> bdo : itemsInOut.values3()) {
			BarData p = bdo.getValue(ServiceLevel.ALL, null);
			if (p != null) {
				int inner = p.value.intValue();
				if (inner > this.busAsUsSc) {
					this.busAsUsSc = inner;
				}
			}
		}
	}
	/**
	 * Counts the number of quarter-hour between the two input dates.
	 * @param fromTime the start date
	 * @param toTime the end date
	 * @return the number of quarter-hour
	 */
	private int countQuarterDistance(DateTime fromTime, DateTime toTime) {
		int fromTimeInMins = fromTime.getHourOfDay() * 60 + fromTime.getMinuteOfHour();

		int toTimeInMins = toTime.getHourOfDay() * 60 + toTime.getMinuteOfHour();

		return ((toTimeInMins - fromTimeInMins) / DuringDayData.QUARTER_HOUR);
	}

	/**
	 * Returns the total number of quarter-hour slots which are shown in the
	 * during-day chart.
	 * @return the number of quarter-hour slots
	 */
	public int getQuarterSlots() {
		return this.countQuarterDistance(firstTime, lastTime);
	}

	/**
	 * Returns the first time slot of items hashMap data.
	 * @return the first time slot of items hashMap data
	 */
	public int getQuarterChartFrom() {
		int res = 0;

		Iterable<DateTime> ks = itemsInOut.keys2();
		Iterable<DateTime> ksm = Interactive.min(ks);
		DateTime firstDataTime = Iterables.getFirst(ksm, null);
		if (firstDataTime != null) {
			res = this.countQuarterDistance(firstTime, firstDataTime);
		}

		return res;
	}

	/**
	 * Returns number of the quarter-hour vertical line.
	 * @return number of the quarter-hour vertical line
	 */
	public int getQuarterLine() {
		return quarterLine;
	}

	/**
	 * Returns list of hours which are shown as the label of x-axis.
	 * @return list of hours
	 */
	public List<String> getDisplayHourList() {
		ArrayList<String> result = new ArrayList<String>();

		int firstHour = firstTime.getHourOfDay();

		int lastHour = lastTime.getHourOfDay();

		for (int i = firstHour; i < lastHour + 1; i++) {
			result.add(this.getTimeString(firstTime.withHourOfDay(i)));
		}

		return result;
	}

	/**
	 * Returns the input time as a string based on the time is o'clock or not.
	 * @param time input time
	 * @return the input time as a string
	 */
	public String getTimeString(DateTime time) {
		if (time.getMinuteOfHour() != 0) {
			return time.toString("h:mma", Locale.ENGLISH);
		}
		return time.toString("ha", Locale.ENGLISH);
	}

}
