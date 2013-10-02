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

import eu.advance.logistics.live.reporter.model.ItemEventType;
import eu.advance.logistics.live.reporter.model.LorryPosition;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import hu.akarnokd.reactive4java.base.Action0;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

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
	public String warehouse;
	/** The depots and the current contents. */
	public final Multimap<Long, ConsItem> storageAreas = HashMultimap.create();
	/** The target depots maintained by the warehouse. */
	public final TLongSet depots = new TLongHashSet();
	/** List of vehicles in the warehouse. */
	public final List<VehicleAgent> vehicles = new ArrayList<>();
	/** The lorry positions. */
	public final List<LorryPosition> positions = new ArrayList<>();
	/** The environment. */
	private EnvironmentAgent env;
	/**
	 * Constructor, sets the environment.
	 * @param env the environment
	 */
	public WarehouseAgent(EnvironmentAgent env) {
		this.env = env;
	}
	/**
	 * Try entering a warehouse.
	 * @param now the current time
	 * @param va the warehouse agent
	 * @return true if enter was successful
	 */
	public boolean tryEnter(DateTime now, VehicleAgent va) {
		if (positions.size() > vehicles.size()) {
			for (ConsItem ci : va.contents) {
				if (depots.contains(ci.consignment.deliveryDepot)) {
					enter(now, va);
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Enter a vehicle into a free position and schedule the unload.
	 * @param now the current time
	 * @param va the vehicle
	 */
	public void enter(DateTime now, final VehicleAgent va) {
		va.warehouse = warehouse;
		List<LorryPosition> lps = new ArrayList<LorryPosition>(positions);
		for (VehicleAgent va0 : vehicles) {
			for (int i = lps.size() - 1; i >= 0; i--) {
				LorryPosition lp = lps.get(i);
				if (lp.index == va0.position) {
					lps.remove(i);
				}
			}
		}
		vehicles.add(va);
		va.position = lps.get(0).index;
		DateTime unload = now.plusSeconds(lps.get(0).enterTime);

		env.warehouseScan(va, now, true);

		for (final ConsItem ci : va.contents) {
			final DateTime funload = unload;
			if (depots.contains(ci.consignment.deliveryDepot)) {
				env.add(unload, new Action0() {
					@Override
					public void invoke() {
						env.event(ci.id, ci.consignmentId, funload, ItemEventType.SCAN_OFF);
						storageAreas.put(ci.consignment.deliveryDepot, ci);
						va.contents.remove(ci);
					}
				});
			}
			unload = unload.plusSeconds(15);
		}
		final DateTime endUnload = unload;
		env.add(endUnload, new Action0() {
			@Override
			public void invoke() {
				checkLoad(endUnload, va);
			}
		});
	}
	/**
	 * Check if there is something to load onto the vehicle in the warehouse.
	 * @param now the current time
	 * @param va the vehicle
	 */
	public void checkLoad(DateTime now, VehicleAgent va) {
		if (va.contents.isEmpty()) {
			if (depots.contains(va.depot)) {
				// TODO continue
			}
		}
	}
}