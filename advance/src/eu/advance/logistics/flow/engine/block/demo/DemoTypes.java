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

package eu.advance.logistics.flow.engine.block.demo;

import hu.akarnokd.utils.xml.XNElement;
import eu.advance.logistics.flow.engine.block.AdvanceData;

/**
 * Utility class to create and manage the types of the demo.
 * @author akarnokd, 2011.11.11.
 */
public final class DemoTypes {

	/**
	 * Utility class.
	 */
	private DemoTypes() {
	}
	/**
	 * Create a full pallet with the supplied destination.
	 * @param destination the destination id
	 * @return the full pallet
	 */
	public static XNElement createFullPallet(int destination) {
		XNElement e = new XNElement("full-pallet");
		e.set("full", "true");
		e.set("destination", destination);
		return e;
	}
	/**
	 * Creates a half pallet with the supplied destination.
	 * @param destination the destination id
	 * @return the half pallet
	 */
	public static XNElement createHalfPallet(int destination) {
		XNElement e = new XNElement("half-pallet");
		e.set("half", "true");
		e.set("destination", destination);
		return e;
	}
	/**
	 * Create a truck with the supplied pallet items.
	 * @param pallets the pallet sequence
	 * @return the truck
	 */
	public static XNElement createTruck(Iterable<XNElement> pallets) {
		XNElement e = new XNElement("truck");
		AdvanceData r = new AdvanceData();
		XNElement palletCollection = AdvanceData.rename(r.create(), "pallets");
		e.add(palletCollection);
		for (XNElement p : pallets) {
			palletCollection.add(AdvanceData.rename(p, "item"));
		}
		return e;
	}
}
