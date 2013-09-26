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

/**
 * Helper enum of details info for the during-day chart.
 * @author csirobi, 2013.06.24.
 */
public enum DuringDayDetailsInfo {
	/** Predicted total. */
	PREDICTED_TOTAL("pred_total", "Predicted total"),
	/** Predicted visible. */
	PREDICTED_VISIBLE("pred_visible", "Predicted visible"),
	/** Actual total. */
	ACTUAL_TOTAL("act_total", "Actual total"),
	/** Actual visible. */
	ACTUAL_VISIBLE("act_visible", "Actual visible");

	/** The display key. */
	private String info;
	/** The message. */
	private String message;
	/**
	 * Constructor, initializes the fields.
	 * @param info the display key
	 * @param message the message
	 */
	private DuringDayDetailsInfo(String info, String message) {
		this.info = info;
		this.message = message;
	}

	/**
	 * Returns the info.
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}
	/**
	 * Returns the message.
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

}
