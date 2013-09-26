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

import java.text.NumberFormat;
import java.util.Locale;


/**
 * BarData is the core class for storing the number of any units and the normalized value of them.
 * @author csirobi, 2013.02.04.
 */

public class BarData {
	/** Value. */
	public GraphDecimal value;
	/** Normalized value. */
	public GraphDecimal normalValue;
	/** The raw value. */
	public double raw;
	/**
	 * Constructor, sets the value to zero.
	 */
	public BarData() {
		this(0);
	}
	/**
	 * Constructor with int value.
	 * @param value the value
	 */
	public BarData(int value) {
		raw = value;
		this.value = new GraphDecimal(value).divide(new GraphDecimal(1));
		this.normalValue = new GraphDecimal(0);
	}
	/**
	 * Set the value from the raw.
	 */
	public void setFromRaw() {
		this.value = new GraphDecimal(raw).divide(new GraphDecimal(1));
	}
	/**
	 * Constructor with double value.
	 * @param value the value
	 */
	public BarData(double value) {
		raw = value;
		this.value = new GraphDecimal(value).divide(new GraphDecimal(1));
		this.normalValue = new GraphDecimal(0);
	}

	/**
	 * Normalize value based on the ratio.
	 * If ratio is zero, the normalValue = 0. 
	 * @param ratio the ratio decimal
	 */
	public void normalize(GraphDecimal ratio) {
		if (ratio.compareTo(new GraphDecimal(0)) != 0) {
			this.normalValue = this.value.divide(ratio);
		} else {
			this.normalValue = new GraphDecimal(0);
		}
	}

	/**
	 * Warehouse Layout 3: Normalize value based on the different storage area capacity and
	 * the normal scale of the total coordinate.
	 * Normal scale means a normalized unit of the storage area capacity.
	 * @param capacity storage area capacity based on warehouseType and the itemStatus
	 * @param normalScale normalized scale of the total coordinate
	 */
	public void normalizeWarehouseL3(GraphDecimal capacity, GraphDecimal normalScale) {
		if (capacity.compareTo(new GraphDecimal(0)) != 0) {
			this.normalValue = this.value.divide(capacity).multiply(normalScale);
		} else {
			this.normalValue = new GraphDecimal(0);
		}
	}

	/**
	 * Get percentage of value based on the ratio.
	 * If ratio is zero, the result is "--".
	 * @param ratio the ratio
	 * @return the percentage string
	 */
	protected String getPercent(GraphDecimal ratio) {
		NumberFormat f = NumberFormat.getPercentInstance(Locale.ENGLISH);
		f.setMinimumFractionDigits(0);
		f.setMaximumFractionDigits(0);

		String result = "";
		if (ratio.compareTo(new GraphDecimal(0)) != 0) {
			result = f.format(this.value.divide(ratio).doubleValue());
		} else {
			result = "--";
		}
		return result;
	}

	@Override
	public String toString() {
		return "" + value;
	}
}
