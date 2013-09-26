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

/**
 * Helper enum for the time states at the chart in the warehouse level 2.
 * @author csirobi
 */
public enum L2TimeState {
	/** Now at hub. */
	NOW_AT_HUB("at_hub", "At hub now"),
	/** Coming up. */
	COMING_UP("coming", "Coming up"),
	/** Overall. */
	OVERALL("overall", "overall");
	/** The display key. */
	private String info;
	/** The message. */
	private String message;
	/**
	 * Constructor, sets the fields.
	 * @param info the display key
	 * @param message the message
	 */
	private L2TimeState(String info, String message) {
		this.info = info;
		this.message = message;
	}
	/** 
	 * Returns the display key.
	 * @return the display key
	 */
	public String getInfo()	{
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
