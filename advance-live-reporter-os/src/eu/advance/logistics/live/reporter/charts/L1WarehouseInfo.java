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
 * Helper enum for showing details at the bottom chart in the warehouse level 1.
 * @author csirobi
 */
public enum L1WarehouseInfo {
	/** Warehouse A total. */
	A_TOTAL("WH A total"),
	/** Warehouse A worst. */
	A_WORST("WH A worst"),
	/** Warehouse B total. */
	B_TOTAL("WH B total"),
	/** Warehouse B worst. */
	B_WORST("WH B worst");
	/** The message. */
	private String message;
	/** 
	 * Constructor, sets the message.
	 * @param message the message 
	 */
	private L1WarehouseInfo(String message) {
		this.message = message;
	}
	/**
	 * Returns the message.
	 * @return the message
	 */
	public String getMessage() {
		return this.message;
	}

}
