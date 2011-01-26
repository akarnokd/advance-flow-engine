/*
 * Copyright 2010-2012 The Advance EU 7th Framework project consortium
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
/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.advance.logistics.web.client.widgets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * {@link TimePicker} widget to enter the time part of a date using spinners.
 * karnokd: patched the event bug
 */

public class TimePicker extends Composite implements
		HasValueChangeHandlers<Date> {
	/**
	 * A time spinner.
	 * @author karnokd
	 * 
	 */
	private class TimeSpinner extends ValueSpinner {
		/** The date and time format. */
		private DateTimeFormat dateTimeFormat;
		/**
		 * Constructor.
		 * @param date the date
		 * @param dateTimeFormat the date format
		 * @param step the step
		 */
		public TimeSpinner(Date date, DateTimeFormat dateTimeFormat, int step) {
			super(date.getTime());
			this.dateTimeFormat = dateTimeFormat;
			getSpinner().setMinStep(step);
			getSpinner().setMaxStep(step);
			// Refresh value after dateTimeFormat is set
			getSpinner().setValue(date.getTime(), true);
		}
		/**
		 * Format a value.
		 * @param value the value
		 * @return the formatted string
		 */
		@Override
		protected String formatValue(long value) {
			if (dateTimeFormat != null) {
				return dateTimeFormat.format(new Date(value));
			}
			return "";
		}
		/**
		 * Parse a value.
		 * @param value the string
		 * @return the long date
		 */
		@Override
		protected long parseValue(String value) {
			Date parsedDate = new Date(dateInMillis);
			dateTimeFormat.parse(value, 0, parsedDate);
			return parsedDate.getTime();
		}
	}
	/** Constants. */
	private static final int SECOND_IN_MILLIS = 1000;
	/** Constants. */
	private static final int MINUTE_IN_MILLIS = 60 * 1000;
	/** Constants. */
	private static final int HOUR_IN_MILLIS = 60 * 60 * 1000;
	/** Constants. */
	private static final int HALF_DAY_IN_MS = 12 * 60 * 60 * 1000;
	/** Constants. */
//	private static final int DAY_IN_MS = 24 * 60 * 60 * 1000;
	/** The list of spinners. */
	private List<TimeSpinner> timeSpinners = new ArrayList<TimeSpinner>();
	/** The current time. */
	private long dateInMillis;
	/** Is enabled. ?*/
	private boolean enabled = true;
	/** The spinner listener. */
	private SpinnerListener listener = new SpinnerListener() {
		@Override
		public void onSpinning(long value) {
			long oldValue = dateInMillis;
			dateInMillis = value;
			ValueChangeEvent.fireIfNotEqual(TimePicker.this,
					new Date(oldValue), new Date(value));
		};
	};

	/**
	 * @param use24Hours
	 *            if set to true the {@link TimePicker} will use 24h format
	 */
	public TimePicker(boolean use24Hours) {
		this(new Date(System.currentTimeMillis()), use24Hours);
	}

	/**
	 * @param date
	 *            the date providing the initial time to display
	 * @param use24Hours
	 *            if set to true the {@link TimePicker} will use 24h format
	 */
	public TimePicker(Date date, boolean use24Hours) {
		this(date, use24Hours ? null : DateTimeFormat.getFormat("aa"),
				use24Hours ? DateTimeFormat.getFormat("HH") : DateTimeFormat
						.getFormat("hh"), DateTimeFormat.getFormat("mm"),
				DateTimeFormat.getFormat("ss"));
	}

	/**
	 * @param date
	 *            the date providing the initial time to display
	 * @param amPmFormat
	 *            the format to display AM/PM. Can be null to hide AM/PM field
	 * @param hoursFormat
	 *            the format to display the hours. Can be null to hide hours
	 *            field
	 * @param minutesFormat
	 *            the format to display the minutes. Can be null to hide minutes
	 *            field
	 * @param secondsFormat
	 *            the format to display the seconds. Can be null to seconds
	 *            field
	 * 
	 */
	public TimePicker(Date date, DateTimeFormat amPmFormat,
			DateTimeFormat hoursFormat, DateTimeFormat minutesFormat,
			DateTimeFormat secondsFormat) {
		this.dateInMillis = date.getTime();
		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setStylePrimaryName("gwt-TimePicker");
		if (amPmFormat != null) {
			TimeSpinner amPmSpinner = new TimeSpinner(date, amPmFormat,
					HALF_DAY_IN_MS);
			timeSpinners.add(amPmSpinner);
			horizontalPanel.add(amPmSpinner);
		}
		if (hoursFormat != null) {
			TimeSpinner hoursSpinner = new TimeSpinner(date, hoursFormat,
					HOUR_IN_MILLIS);
			timeSpinners.add(hoursSpinner);
			horizontalPanel.add(hoursSpinner);
		}
		if (minutesFormat != null) {
			TimeSpinner minutesSpinner = new TimeSpinner(date, minutesFormat,
					MINUTE_IN_MILLIS);
			timeSpinners.add(minutesSpinner);
			horizontalPanel.add(minutesSpinner);
		}
		if (secondsFormat != null) {
			TimeSpinner secondsSpinner = new TimeSpinner(date, secondsFormat,
					SECOND_IN_MILLIS);
			timeSpinners.add(secondsSpinner);
			horizontalPanel.add(secondsSpinner);
		}
		for (TimeSpinner timeSpinner : timeSpinners) {
			for (TimeSpinner nestedSpinner : timeSpinners) {
				if (nestedSpinner != timeSpinner) {
					timeSpinner.getSpinner().addSpinnerListener(
							nestedSpinner.getSpinnerListener());
				}
			}
			timeSpinner.getSpinner().addSpinnerListener(listener);
		}
		initWidget(horizontalPanel);
	}
	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<Date> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	/**
	 * @return the date specified by this {@link TimePicker}
	 */
	public Date getDateTime() {
		return new Date(dateInMillis);
	}

	/**
	 * @return Gets whether this widget is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}
	/** The helper formatters. */
	private DateTimeFormat dtfDate = DateTimeFormat.getFormat("yyyy-MM-dd");
	/** The helper formatters. */
	private DateTimeFormat dtfTime = DateTimeFormat.getFormat("HH:mm:ss.SSS");
	/** The helper formatters .*/
	private DateTimeFormat dtfBoth = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/**
	 * @param date
	 *            the date to be set. Only the date part will be set, the time
	 *            part will not be affected
	 */
	public void setDate(Date date) {
		// Only change the date part, leave time part untouched
		String newDateOnly = dtfDate.format(date);
		String oldTimeOnly = dtfTime.format(new Date(dateInMillis));
		
		dateInMillis = dtfBoth.parse(newDateOnly + " " + oldTimeOnly).getTime();
		for (TimeSpinner spinner : timeSpinners) {
			spinner.getSpinner().setValue(dateInMillis, false);
		}
	}

	/**
	 * @param date
	 *            the date to be set. Both date and time part will be set
	 */
	public void setDateTime(Date date) {
		dateInMillis = date.getTime();
		for (TimeSpinner spinner : timeSpinners) {
			spinner.getSpinner().setValue(dateInMillis, true);
		}
	}

	/**
	 * Sets whether this widget is enabled.
	 * 
	 * @param enabled
	 *            true to enable the widget, false to disable it
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		for (TimeSpinner spinner : timeSpinners) {
			spinner.setEnabled(enabled);
		}
	}
}
