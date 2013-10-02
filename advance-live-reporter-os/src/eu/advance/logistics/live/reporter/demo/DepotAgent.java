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
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;

import eu.advance.logistics.live.reporter.model.ItemEventType;

/**
 * A depot manager "agent".
 * @author karnokd, 2013.10.01.
 *
 */
public class DepotAgent {
	/** Depot identifier. */
	public long id;
	/** The items to deliver. */
	public final List<ConsItem> toDeliver = new ArrayList<>();
	/** The list of own vehicles. */
	public final List<VehicleAgent> vehicles = new ArrayList<>();
	/** The vehicles on site. */
	public final List<VehicleAgent> onSite = new ArrayList<>();
	/** The environment. */
	private EnvironmentAgent env;
	/**
	 * Constructor, sets the environment.
	 * @param env the environment
	 */
	public DepotAgent(EnvironmentAgent env) {
		this.env = env;
	}
	/**
	 * Item arrived from the collection postcode.
	 * @param now the current time
	 * @param item the item
	 */
	public void itemArrived(DateTime now, ConsItem item) {
		toDeliver.add(item);
		checkLoadVehicle(now);
	}
	/**
	 * Vehicle arrived at the depot.
	 * @param now the arrival time
	 * @param va the vehicle
	 */
	public void vehicleArrived(DateTime now, VehicleAgent va) {
		for (ConsItem item : va.contents) {
			env.event(item.id, item.consignmentId, now, ItemEventType.DESTINATION_SCAN);
		}
		va.contents.clear();
		va.targetHub = null;
		va.atDepot = true;
		onSite.add(va);
		checkLoadVehicle(now);
	}
	/**
	 * Check if vehicles can be loaded.
	 * @param now the current time
	 */
	public void checkLoadVehicle(DateTime now) {
		if (!onSite.isEmpty()) {
			for (ConsItem ci : new ArrayList<>(toDeliver)) {
				List<VehicleAgent> vas = new ArrayList<>(onSite);
				Collections.sort(vas, VehicleAgent.CAPACITY);
				for (VehicleAgent va : vas) {
					if (va.tryLoadInDepot(now, ci)) {
						toDeliver.remove(ci);
					}
				}
			}
		}
	}
}