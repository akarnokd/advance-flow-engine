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

import eu.advance.logistics.live.reporter.model.ServiceLevel;

/**
 * Warehouse service levels.
 * @author karnokd, 2013.09.25.
 */
public enum WarehouseServiceLevel {
	/** Standard items. */
	STANDARD("Standard"),
	/** Priority and special items. */
	PRIORITY_SPECIAL("Priority + Special"),
	/** All items. */
	ALL("All");
	/** The display text. */
	private final String message;
	/** 
	 * Constructor, sets the display text.
	 * @param message the display text
	 */
	WarehouseServiceLevel(String message) {
		this.message = message;
	}
	/**
	 * Returns the display text.
	 * @return the display text
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * Converts a consignment service level into warehouse service level enum.
	 * @param sl the service level to convert
	 * @return the converted service level
	 */
	public static WarehouseServiceLevel convert(ServiceLevel sl) {
		switch (sl) {
		case ALL:
			return ALL;
		case STANDARD:
			return STANDARD;
		case PRIORITY:
		case SPECIAL:
			return PRIORITY_SPECIAL;
		default:
			throw new IllegalArgumentException("" + sl);
		}
	}
}
