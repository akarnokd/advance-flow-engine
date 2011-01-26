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
import java.util.List;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * The {@link Spinner} provide two arrows for in- and decreasing values. A
 * linked {@link SpinnerListener}
 */
public class Spinner {
	/** Field. */
	private static final int INITIAL_SPEED = 7;

	/** Field. */
	private final Image decrementArrow = new Image();
	/** Field. */
	private final Image incrementArrow = new Image();

	/** Field. */
	private List<SpinnerListener> spinnerListeners = new ArrayList<SpinnerListener>();
	/** Field. */
	private int step, minStep, maxStep, initialSpeed = 7;
	/** Field. */
	private long value, min, max;
	/** Field. */
	private boolean increment;
	/** Field. */
	private boolean constrained;
	/** Field. */
	private boolean enabled = true;

	/** Hold-down spinning timer. */
	private final Timer timer = new Timer() {
		private int counter = 0;
		private int speed = 7;

		@Override
		public void cancel() {
			super.cancel();
			speed = initialSpeed;
			counter = 0;
		}

		@Override
		public void run() {
			counter++;
			if (speed <= 0 || counter % speed == 0) {
				speed--;
				counter = 0;
				if (increment) {
					increase();
				} else {
					decrease();
				}
			}
			if (speed < 0 && step < maxStep) {
				step += 1;
			}
		}
	};
	/** Mouse down handler. */
	private MouseDownHandler mouseDownHandler = new MouseDownHandler() {

		@Override
		public void onMouseDown(MouseDownEvent event) {
			if (enabled) {
				Image sender = (Image) event.getSource();
				if (sender == incrementArrow) {
					increment = true;
					increase();
				} else {
					increment = false;
					decrease();
				}
				timer.scheduleRepeating(30);
			}
		}
	};
	/** Mouse out handler. */
	private MouseOutHandler mouseOutHandler = new MouseOutHandler() {
		@Override
		public void onMouseOut(MouseOutEvent event) {
			if (enabled) {
				cancelTimer((Widget) event.getSource());
			}
		}
	};
	/** Mouse Up handler. */
	private MouseUpHandler mouseUpHandler = new MouseUpHandler() {
		@Override
		public void onMouseUp(MouseUpEvent event) {
			if (enabled) {
				cancelTimer((Widget) event.getSource());
			}
		}
	};

	/**
	 * @param spinner
	 *            the widget listening to the arrows
	 * @param value
	 *            initial value
	 */
	public Spinner(SpinnerListener spinner, long value) {
		this(spinner, value, 0, 0, 1, 99, false);
	}

	/**
	 * @param spinner
	 *            the widget listening to the arrows
	 * @param value
	 *            initial value
	 * @param min
	 *            min value
	 * @param max
	 *            max value
	 */
	public Spinner(SpinnerListener spinner, long value, long min, long max) {
		this(spinner, value, min, max, 1, 99, true);
	}

	/**
	 * @param spinner
	 *            the widget listening to the arrows
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
	public Spinner(SpinnerListener spinner, long value, long min, long max,
			int minStep, int maxStep) {
		this(spinner, value, min, max, minStep, maxStep, true);
	}


	/**
	 * @param spinner
	 *            the widget listening to the arrows
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
	 *            determines if min and max value will take effect
	 */
	public Spinner(SpinnerListener spinner, long value, long min, long max,
			int minStep, int maxStep, boolean constrained) {
		super();
		spinnerListeners.add(spinner);
		this.value = value;
		this.constrained = constrained;
		this.step = minStep;
		this.minStep = minStep;
		this.maxStep = maxStep;
		this.min = min;
		this.max = max;
		this.initialSpeed = INITIAL_SPEED;
		incrementArrow.addMouseUpHandler(mouseUpHandler);
		incrementArrow.addMouseDownHandler(mouseDownHandler);
		incrementArrow.addMouseOutHandler(mouseOutHandler);
		incrementArrow.setUrl("images/arrowUp.png");
		decrementArrow.addMouseUpHandler(mouseUpHandler);
		decrementArrow.addMouseDownHandler(mouseDownHandler);
		decrementArrow.addMouseOutHandler(mouseOutHandler);
		decrementArrow.setUrl("images/arrowDown.png");
		fireOnValueChanged();
	}

	/**
	 * @param listener
	 *            the listener to add
	 */
	public void addSpinnerListener(SpinnerListener listener) {
		spinnerListeners.add(listener);
	}

	/**
	 * @return the image representing the decreating arrow
	 */
	public Image getDecrementArrow() {
		return decrementArrow;
	}

	/**
	 * @return the image representing the increasing arrow
	 */
	public Image getIncrementArrow() {
		return incrementArrow;
	}

	/**
	 * @return the maximum value
	 */
	public long getMax() {
		return max;
	}

	/**
	 * @return the maximum spinner step
	 */
	public int getMaxStep() {
		return maxStep;
	}

	/**
	 * @return the minimum value
	 */
	public long getMin() {
		return min;
	}

	/**
	 * @return the minimum spinner step
	 */
	public int getMinStep() {
		return minStep;
	}

	/**
	 * @return the current value
	 */
	public long getValue() {
		return value;
	}

	/**
	 * @return true is min and max values are active, false if not
	 */
	public boolean isConstrained() {
		return constrained;
	}

	/**
	 * @return Gets whether this widget is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param listener
	 *            the listener to remove
	 */
	public void removeSpinnerListener(SpinnerListener listener) {
		spinnerListeners.remove(listener);
	}

	/**
	 * Sets whether this widget is enabled.
	 * 
	 * @param enabled
	 *            true to enable the widget, false to disable it
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (!enabled) {
			timer.cancel();
		}
	}

	/**
	 * @param initialSpeed
	 *            the initial speed of the spinner. Higher values mean lower
	 *            speed, default value is 7
	 */
	public void setInitialSpeed(int initialSpeed) {
		this.initialSpeed = initialSpeed;
	}

	/**
	 * @param max
	 *            the maximum value. Will not have any effect if constrained is
	 *            set to false
	 */
	public void setMax(long max) {
		this.max = max;
	}

	/**
	 * @param maxStep
	 *            the maximum step for this spinner
	 */
	public void setMaxStep(int maxStep) {
		this.maxStep = maxStep;
	}

	/**
	 * @param min
	 *            the minimum value. Will not have any effect if constrained is
	 *            set to false
	 */
	public void setMin(long min) {
		this.min = min;
	}

	/**
	 * @param minStep
	 *            the minimum step for this spinner
	 */
	public void setMinStep(int minStep) {
		this.minStep = minStep;
	}

	/**
	 * @param value
	 *            sets the current value of this spinner
	 * @param fireEvent
	 *            fires value changed event if set to true
	 */
	public void setValue(long value, boolean fireEvent) {
		this.value = value;
		if (fireEvent) {
			fireOnValueChanged();
		}
	}

	/**
	 * Decreases the current value of the spinner by subtracting current step.
	 */
	protected void decrease() {
		value -= step;
		if (constrained && value < min) {
			value = min;
			timer.cancel();
		}
		fireOnValueChanged();
	}

	/**
	 * Increases the current value of the spinner by adding current step.
	 */
	protected void increase() {
		value += step;
		if (constrained && value > max) {
			value = max;
			timer.cancel();
		}
		fireOnValueChanged();
	}
	/**
	 * Cancel the timer.
	 * @param sender the sender
	 */
	private void cancelTimer(Widget sender) {
		step = minStep;
		timer.cancel();
	}
	/**
	 * Fire value changed event.
	 */
	private void fireOnValueChanged() {
		for (SpinnerListener listener : spinnerListeners) {
			listener.onSpinning(value);
		}
	}
}

