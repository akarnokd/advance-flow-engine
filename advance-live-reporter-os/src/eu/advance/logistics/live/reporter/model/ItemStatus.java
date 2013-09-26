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
 * Item status enumeration.
 * @author karnokd, 2013.09.23.
 */
public enum ItemStatus {
	/** Item was created. */
	CREATED("created", "Created"),
	/** Item was scanned at the source depot. */
	SCANNED("scanned", "Scanned"),
	/** Item was declared. */
	DECLARED("declared", "Declared"),
	/** Item is at the hub. */
	AT_HUB("at_hub", "At hub"),
	/** Left the hub. */
	LEFT_HUB("left_hub", "Left hub"),
	/** Left the hub today. */
	LEFT_HUB_TODAY("left_hub", "Left hub"),
	/** Predicted remaining. */
	PREDICTED("predicted", "Predicted");
	/** The code. */
	private final String info;
	/** The display text. */
	private final String message;
	/**
	 * Constructor, sets the code and display text.
	 * @param info the code
	 * @param message the display text
	 */
	ItemStatus(String info, String message) {
		this.info = info;
		this.message = message;
	}
	/**
	 * Returns the code.
	 * @return the code
	 */
	public String getInfo() {
		return info;
	}
	/**
	 * Returns the display text.
	 * @return the display text
	 */
	public String getMessage() {
		return message;
	}
}
