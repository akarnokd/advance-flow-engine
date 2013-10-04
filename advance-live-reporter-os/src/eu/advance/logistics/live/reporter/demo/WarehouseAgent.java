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

import eu.advance.logistics.live.reporter.model.LorryPosition;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * A warehouse managing items.
 * @author karnokd, 2013.10.01.
 */
public class WarehouseAgent {
	/** The hub. */
	public long hub;
	/** The warehouse. */
	public String id;
	/** The depots and the current contents. */
	public final Multimap<Long, ConsItem> storageAreas = HashMultimap.create();
	/** The target depots maintained by the warehouse. */
	public final TLongSet depots = new TLongHashSet();
	/** List of vehicles in the warehouse. */
	public final List<VehicleAgent> vehicles = new ArrayList<>();
	/** The lorry positions. */
	public final List<LorryPosition> positions = new ArrayList<>();
	/**
	 * Locates the first free vehicle slot.
	 * @return the vehicle slot or null if full
	 */
	public LorryPosition freeSlot() {
		List<LorryPosition> candidates = new ArrayList<>(positions);
		
		for (VehicleAgent va : vehicles) {
			candidates.remove(va.position);
		}
		
		if (candidates.isEmpty()) {
			return null;
		}
		return candidates.get(0);
	}
}