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
import java.util.List;

import org.joda.time.DateTime;

/**
 * The hub agent.
 * @author karnokd, 2013.10.01.
 */
public class HubAgent {
	/** Identifier. */
	public long id;
	/** List of vehicles on site. */
	public final List<VehicleAgent> vehiclesOnSite = new ArrayList<>();
	/** List of vehicles. */
	public final List<WarehouseAgent> warehouses = new ArrayList<>();
	/** The environment. */
	private EnvironmentAgent env;
	/**
	 * Constructor, sets the environment.
	 * @param env the environment
	 */
	public HubAgent(EnvironmentAgent env) {
		this.env = env;
	}
	/**
	 * Schedule the loading and unloading vehicles.
	 * @param now the current time
	 */
	public void loadUnload(DateTime now) {
		for (VehicleAgent va : vehiclesOnSite) {
			if (va.warehouse == null) {
				if (va.needsUnload()) {
					for (WarehouseAgent wa : warehouses) {
						if (wa.tryEnter(now, va)) {
							
						}
					}
				}
			}
		}
	}
}