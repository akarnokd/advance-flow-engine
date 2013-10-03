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
package eu.advance.logistics.live.reporter.demo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import eu.advance.logistics.live.reporter.model.LorryPosition;

/**
 * A vehicle delivering items to and from hub.
 * @author karnokd, 2013.10.01.
 */
public class VehicleAgent {
	/** The id. */
	public String id;
	/** The owner/target depot. */
	public long depotId;
	/** The owner depot. */
	public DepotAgent depot;
	/** The vehicle capacity. */
	public int capacity;
	/** The target hub, if moving towards it. */
	public Long hubId;
	/** The hub where the vehicle is at. */
	public HubAgent hub;
	/** The session identifier. */
	public String sessionId;
	/** In a warehouse if not null. */
	public WarehouseAgent warehouse;
	/** In a lorry position if not null. */
	public LorryPosition position;
	/** Vehicle is at a hub. */
	public boolean atHub;
	/** Vehicle is at a depot. */
	public boolean atDepot = true;
	/** The contents. */
	public final List<ConsItem> contents = new ArrayList<>();
	/** Capacity comparator. */
	public static final Comparator<VehicleAgent> CAPACITY = new Comparator<VehicleAgent>() {
		@Override
		public int compare(VehicleAgent o1, VehicleAgent o2) {
			return Integer.compare(o1.capacity, o2.capacity);
		}
	};
	/** Capacity comparator. */
	public static final Comparator<VehicleAgent> CAPACITY_CONTENTS = new Comparator<VehicleAgent>() {
		@Override
		public int compare(VehicleAgent o1, VehicleAgent o2) {
			int c = Integer.compare(o1.capacity, o2.capacity);
			if (c == 0) {
				c = Integer.compare(o1.contents.size(), o2.contents.size());
			}
			return c;
		}
	};
	/**
	 * Check if the vehicle is full.
	 * @return true if vehicle is full
	 */
	public boolean isFull() {
		return contents.size() >= capacity;
	}
	/**
	 * Check if vehicle is empty.
	 * @return true if empty
	 */
	public boolean isEmpty() {
		return contents.isEmpty();
	}
	/**
	 * Check if the vehicle contains items from the source depot.
	 * @return true if source content is present
	 */
	public boolean sourceContent() {
		for (ConsItem ci : contents) {
			if (ci.consignment.collectionDepot == depotId) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Check if the vehicle contains items for the destination depot.
	 * @return true if destination content is present
	 */
	public boolean destinationContent() {
		for (ConsItem ci : contents) {
			if (ci.consignment.deliveryDepot == depotId) {
				return true;
			}
		}
		return false;
	}
}