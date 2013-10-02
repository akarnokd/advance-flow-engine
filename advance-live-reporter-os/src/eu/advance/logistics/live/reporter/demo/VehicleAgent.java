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

import hu.akarnokd.reactive4java.base.Action0;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.joda.time.DateTime;

import eu.advance.logistics.live.reporter.model.ItemEventType;

/**
 * A vehicle delivering items to and from hub.
 * @author karnokd, 2013.10.01.
 */
public class VehicleAgent {
	/** The id. */
	public String id;
	/** The owner/target depot. */
	public long depot;
	/** The vehicle capacity. */
	public int capacity;
	/** The target hub, if moving towards it. */
	public Long targetHub;
	/** The session identifier. */
	public String sessionId;
	/** In a warehouse if not null. */
	public String warehouse;
	/** In a lorry position if not null. */
	public Integer position;
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
	/** The environment. */
	private EnvironmentAgent env;
	/**
	 * Constructor, sets the environment.
	 * @param env the environment
	 */
	public VehicleAgent(EnvironmentAgent env) {
		this.env = env;
	}
	/**
	 * Try loading a consignment item into the vehicle at the depot.
	 * @param now the current time
	 * @param item the item to load
	 * @return true if loaded successfully
	 */
	public boolean tryLoadInDepot(DateTime now, ConsItem item) {
		if (atDepot) {
			if (targetHub == null) {
				targetHub = item.consignment.hub;
				add(now, item);
				if (contents.size() >= capacity) {
					departToHub(now);
				}
				return true;
			} else
			if (targetHub == item.consignment.hub) {
				if (contents.size() < capacity) {
					add(now, item);
					if (contents.size() >= capacity) {
						departToHub(now);
					}
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Depart the vehicle towards the hub.
	 * @param now the time
	 */
	public void departToHub(DateTime now) {
		atDepot = false;
		for (ConsItem ci : contents) {
			env.event(ci.id, ci.consignmentId, now, ItemEventType.DECLARED);
			env.declare(id, now, ci.externalId);
		}
		final DateTime hubArrive = env.depotHubTravel(now, depot, targetHub);
		env.add(hubArrive, new Action0() {
			@Override
			public void invoke() {
				env.hubArrive(hubArrive, targetHub, VehicleAgent.this);
			}
		});
	}
	/**
	 * Add a consignment item.
	 * @param now the current time
	 * @param item the item to add
	 */
	public void add(DateTime now, ConsItem item) {
		contents.add(item);
		env.collectionScan(id, now, item);
	}
	/**
	 * Check if the vehicle contains items from the collection depot.
	 * @return true if contains items from collection depot
	 */
	public boolean needsUnload() {
		for (ConsItem ci : contents) {
			if (ci.consignment.collectionDepot == depot) {
				return true;
			}
		}
		return false;
	}
}