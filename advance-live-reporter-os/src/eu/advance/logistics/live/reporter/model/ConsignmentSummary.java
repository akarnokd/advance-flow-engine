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

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import org.joda.time.DateTime;

/**
 * The consignment record for the summary pages.
 * @author karnokd, 2013.04.26.
 */
public class ConsignmentSummary {
	/** The tracking id. */
	public long id;
	/** The created timestamp. */
	public DateTime created;
	/** The declared timestamp. */
	public DateTime declared;
	/** The service level. */
	public ServiceLevel level;
	/** Number of items. */
	public int itemCount;
	/** The collection depot. */
	public long collectionDepot;
	/** The delivery depot. */
	public long deliveryDepot;
	/** The set of item ids of this consignment. */
	public final TLongObjectMap<ItemSummary> itemIds = new TLongObjectHashMap<>();
	/**
	 * Adds the item to the item map.
	 * @param item the item summary record
	 */
	public void add(ItemSummary item) {
		itemIds.put(item.id, item);
	}
	/**
	 * Set the state of the given item id. 
	 * @param itemId the item id
	 * @param status the new status
	 */
	public void setState(long itemId, ItemStatus status) {
		ItemSummary ps = itemIds.get(itemId);
		if (ps != null) {
			ps.updateStatus(status);
		}
	}
	/**
	 * Set the state of the given item id. 
	 * @param itemId the item id
	 * @param item the new status
	 */
	public void setState(long itemId, ItemSummary item) {
		ItemSummary ps = itemIds.get(itemId);
		if (ps == null) {
			itemIds.put(itemId, item);
		} else {
			if (ps.status.compareTo(item.status) < 0) {
				itemIds.put(itemId, item);
			}
		}
	}
	/**
	 * Returns the state of the item.
	 * @param itemId the item id
	 * @return the state or null if unknown
	 */
	public ItemSummary getState(long itemId) {
		return itemIds.get(itemId);
	}
	/**
	 * Returns the total floorspace of the items in the specified status.
	 * @param status the target status
	 * @return the value
	 */
	public double floorspace(ItemStatus status) {
		double result = 0d;
		for (ItemSummary is : itemIds.valueCollection()) {
			if (is.status == status) {
				result += is.floorspace();
			}
		}
		return result;
	}
	/**
	 * Returns the total price units of the items in the specified status.
	 * @param status the target status
	 * @return the value
	 */
	public double priceUnit(ItemStatus status) {
		double result = 0d;
		for (ItemSummary is : itemIds.valueCollection()) {
			if (is.status == status) {
				result += is.priceUnit();
			}
		}
		return result;
	}
	/**
	 * Returns the total item count in the specified status.
	 * @param status the target status
	 * @return the value
	 */
	public double itemCount(ItemStatus status) {
		double result = 0d;
		for (ItemSummary is : itemIds.valueCollection()) {
			if (is.status == status) {
				result += 1;
			}
		}
		return result;
	}
}
