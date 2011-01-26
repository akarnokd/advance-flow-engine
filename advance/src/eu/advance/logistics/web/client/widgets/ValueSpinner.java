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

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A {@link ValueSpinner} is a combination of a {@link TextBox} and a
 * {@link Spinner} to allow spinning <h3>CSS Style Rules</h3>.
 * <ul class='css'>
 * <li>.gwt-ValueSpinner { primary style }</li>
 * <li>.gwt-ValueSpinner .textBox { the textbox }</li>
 * <li>.gwt-ValueSpinner .arrows { the spinner arrows }</li>
 * </ul>
 */
public class ValueSpinner extends HorizontalPanel {
	/** Default stylename. */
	private static final String STYLENAME_DEFAULT = "gwt-ValueSpinner";
	/** The spinner. */
	private Spinner spinner;
	/** The textbox. */
	private final TextBox valueBox = new TextBox();
	/** The spinner listener. */
	private SpinnerListener spinnerListener = new SpinnerListener() {
		@Override
		public void onSpinning(long value) {
			if (getSpinner() != null) {
				getSpinner().setValue(value, false);
			}
			valueBox.setText(formatValue(value));
		}
	};
	/** The keypress handler. */
	private KeyPressHandler keyPressHandler = new KeyPressHandler() {

		@Override
		public void onKeyPress(KeyPressEvent event) {
			int index = valueBox.getCursorPos();
			String previousText = valueBox.getText();
			String newText;
			if (valueBox.getSelectionLength() > 0) {
				newText = previousText.substring(0, valueBox.getCursorPos())
						+ event.getCharCode()
						+ previousText.substring(valueBox.getCursorPos()
								+ valueBox.getSelectionLength(), previousText
								.length());
			} else {
				newText = previousText.substring(0, index)
						+ event.getCharCode()
						+ previousText.substring(index, previousText.length());
			}
			valueBox.cancelKey();
			try {
				long newValue = parseValue(newText);
				if (spinner.isConstrained()
						&& (newValue > spinner.getMax() || newValue < spinner
								.getMin())) {
					return;
				}
				spinner.setValue(newValue, true);
			} catch (Exception e) {
				// valueBox.cancelKey();
			}
		}
	};

	/**
	 * @param value
	 *            initial value
	 */
	public ValueSpinner(long value) {
		this(value, 0, 0, 1, 99, false);
	}

	/**
	 * @param value
	 *            initial value
	 * @param min
	 *            min value
	 * @param max
	 *            max value
	 */
	public ValueSpinner(long value, int min, int max) {
		this(value, min, max, 1, 99, true);
	}

	/**
	 * @param value
	 *            initial value
	 * @param min
	 *            min value
	 * @param max
	 *            max value
	 * @param minStep
	 *            min value for stepping
	 * @param maxStep
	 *            max value for stepping
	 */
	public ValueSpinner(long value, int min, int max, int minStep, int maxStep) {
		this(value, min, max, minStep, maxStep, true);
	}

	/**
	 * @param value
	 *            initial value
	 * @param min
	 *            min value
	 * @param max
	 *            max value
	 * @param minStep
	 *            min value for stepping
	 * @param maxStep
	 *            max value for stepping
	 * @param constrained
	 *            if set to false min and max value will not have any effect
	 */
	public ValueSpinner(long value, int min, int max, int minStep, int maxStep,
			boolean constrained) {
		super();
		setStylePrimaryName(STYLENAME_DEFAULT);
		spinner = new Spinner(spinnerListener, value, min, max, minStep,
				maxStep, constrained);
		valueBox.setStyleName("textBox");
		valueBox.addKeyPressHandler(keyPressHandler);
		setVerticalAlignment(ALIGN_MIDDLE);
		add(valueBox);
		VerticalPanel arrowsPanel = new VerticalPanel();
		arrowsPanel.setStylePrimaryName("arrows");
		arrowsPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		arrowsPanel.add(spinner.getIncrementArrow());
		arrowsPanel.add(spinner.getDecrementArrow());
		add(arrowsPanel);
	}

	/**
	 * @return the Spinner used by this widget
	 */
	public Spinner getSpinner() {
		return spinner;
	}

	/**
	 * @return the SpinnerListener used to listen to the {@link Spinner} events
	 */
	public SpinnerListener getSpinnerListener() {
		return spinnerListener;
	}

	/**
	 * @return the TextBox used by this widget
	 */
	public TextBox getTextBox() {
		return valueBox;
	}

	/**
	 * @return whether this widget is enabled.
	 */
	public boolean isEnabled() {
		return spinner.isEnabled();
	}

	/**
	 * Sets whether this widget is enabled.
	 * 
	 * @param enabled
	 *            true to enable the widget, false to disable it
	 */
	public void setEnabled(boolean enabled) {
		spinner.setEnabled(enabled);
		valueBox.setEnabled(enabled);
	}

	/**
	 * @param value
	 *            the value to format
	 * @return the formatted value
	 */
	protected String formatValue(long value) {
		return String.valueOf(value);
	}

	/**
	 * @param value
	 *            the value to parse
	 * @return the parsed value
	 */
	protected long parseValue(String value) {
		return Long.valueOf(value);
	}
}
