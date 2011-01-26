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

import java.util.Date;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DatePicker;

/**
 * The {@link DateTimePicker} combines a {@link DatePicker} and a
 * {@link TimePicker}.
 */

public class DateTimePicker extends Composite {
	/** The date picker. */
	private DatePicker datePicker;
	/** The time picker. */
	private TimePicker timePicker;
	/** Handler. */
	private ValueChangeHandler<Date> datePickerChangeHandler = new ValueChangeHandler<Date>() {
		@Override
		public void onValueChange(ValueChangeEvent<Date> event) {
			timePicker.setDate(event.getValue());
		}
	};
	/** Handler. */
	private ValueChangeHandler<Date> timePickerChangeHandler = new ValueChangeHandler<Date>() {

		@Override
		public void onValueChange(ValueChangeEvent<Date> event) {
			datePicker.setValue(event.getValue());
		}
	};

	/**
	 * Creates a {@link TimePicker} instance using the current date as initial
	 * value.
	 * @param use24Hours use 24 hour display?
	 */
	public DateTimePicker(boolean use24Hours) {
		this(new DatePicker(), new TimePicker(use24Hours));
	}

	/**
	 * @param datePicker
	 *            the {@link DatePicker} to be used
	 * @param timePicker
	 *            the {@link TimePicker} to be used
	 */
	public DateTimePicker(DatePicker datePicker, TimePicker timePicker) {
		this.datePicker = datePicker;
		this.timePicker = timePicker;
		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		verticalPanel.add(datePicker);
		datePicker.setWidth("100%");
		verticalPanel.add(timePicker);
		timePicker.addValueChangeHandler(timePickerChangeHandler);
		datePicker.addValueChangeHandler(datePickerChangeHandler);
		initWidget(verticalPanel);
	}

	/**
	 * @return the entered date
	 */
	public Date getDate() {
		return timePicker.getDateTime();
	}

	/**
	 * @return the {@link DatePicker}
	 */
	public DatePicker getDatePicker() {
		return datePicker;
	}

	/**
	 * @return the {@link TimePicker}
	 */
	public TimePicker getTimePicker() {
		return timePicker;
	}

	/**
	 * @return Gets whether this widget is enabled
	 */
	public boolean isEnabled() {
		return timePicker.isEnabled();
	}

	/**
	 * Sets whether this widget is enabled.
	 * 
	 * @param enabled
	 *            true to enable the widget, false to disable it
	 */
	public void setEnabled(boolean enabled) {
		timePicker.setEnabled(enabled);
	}
}
