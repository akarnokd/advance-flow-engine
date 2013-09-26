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
 * A consignment item.
 * @author karnokd, 2013.09.23.
 */
public class Item {
	/** Item identifier. */
	public long id;
	/** The consignment. */
	public long consignmentId;
	/** The items external identifier. */
	public String externalId;
	/** The width. */
	public double width;
	/** The height. */
	public double height;
	/** The length. */
	public double length;
	/**
	 * Computes the price unit of this item.
	 * @return the price unit
	 */
	public double priceUnit() {
		return width * height * length;
	}
	/**
	 * Computes the floor space of this item.
	 * @return the floor space
	 */
	public double floorspace() {
		return width * length;
	}
}
