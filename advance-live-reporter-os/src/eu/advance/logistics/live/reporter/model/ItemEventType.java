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
 * The item event type enumeration.
 * @author karnokd, 2013.09.23.
 */
public enum ItemEventType {
	/** Created. */
	CREATED,
	/** Scanned at the collection depot. */
	SOURCE_SCAN,
	/** Declared. */
	DECLARED,
	/** Arrived at the hub. */
	HUB_ARRIVE,
	/** Entered a warehouse. */
	WAREHOUSE_ENTER,
	/** Scanned off a vehicle. */
	SCAN_OFF,
	/** Scanned onto a vehicle. */
	SCAN_ON,
	/** Left the warehouse. */
	WAREHOUSE_LEAVE,
	/** Left the hub. */
	HUB_LEAVE,
	/** Arrived at delivery depot. */
	DESTINATION_SCAN
}