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
 * Helper enum for the display sides at the chart in the warehouse level 2.
 * @author csirobi
 */
public enum L2DisplaySide {
	/** Left side. */
	LEFT("leftSide"),
	/** Right side. */
	RIGHT("rightSide");
	/** The display text. */
	private String info;
	/**
	 * Constructor, sets the display text.
	 * @param info the display text
	 */
	private L2DisplaySide(String info) {
		this.info = info;
	}
	/**
	 * Returns the display text.
	 * @return the display text
	 */
	public String getInfo() {
		return info;
	}

}
