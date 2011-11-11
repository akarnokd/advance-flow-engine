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

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * The demo datastore to manage pallets, bays, trucks and statistics.
 * @author akarnokd, 2011.11.11.
 */
public final class DemoDatastore {
	/** The singleton instance. */
	private static DemoDatastore instance;
	/** The number of destinations. */
	private int maxDestinations = 5;
    /** The tracks the contents of various destinations. */
	private final Multimap<Integer, XElement> bays = LinkedListMultimap.create();
	/** Singleton class. Use {@code instance()}. */
	private DemoDatastore() {
		
	}
	/**
	 * Creates or retunrs the single instance of this datastore.
	 * @return the datastore
	 */
	public static synchronized DemoDatastore instance() {
		if (instance == null) {
			instance = new DemoDatastore();
		}
		return instance;
	}
	/**
	 * The number of max destinations.
	 * @return the number of max destination
	 */
	public synchronized int getMaxDestinations() {
		return maxDestinations;
	}
	/**
	 * Set the number of max destinations.
	 * @param n the new number of destinations
	 */
	public synchronized void setMaxDestinations(int n) {
		this.maxDestinations = n;
	}
	/**
	 * Puts the pallet into the apropriate bay.
	 * @param pallet the pallet to add
	 * @return the target bay number
	 */
	public synchronized int addToBay(XElement pallet) {
		int bay = pallet.getInt("destination");
		bays.put(bay, pallet.copy());
		return bay;
	}
	/**
	 * Returns the number of pallets in a bay.
	 * @param bay the bay index
	 * @return the number of pallets in a bay
	 */
	public synchronized int bayCount(int bay) {
		return bays.get(bay).size();
	}
	/**
	 * Remove all pallets from a specific bay.
	 * @param bay the bay index
	 */
	public synchronized void emptyBay(int bay) {
		bays.get(bay).clear();
	}
	/**
	 * Empty all bays.
	 */
	public synchronized void emptyAllBays() {
		bays.clear();
	}
	/**
	 * Remove random pallets from the bay.
	 * @param bay the bay index
	 * @param count the count
	 */
	public synchronized void removeFromBay(int bay, int count) {
		Random rnd = new Random();
		for (int i = 0; i < count; i++) {
			Collection<XElement> b = bays.get(bay);
			int which = rnd.nextInt(b.size());
			Iterator<XElement> it = b.iterator();
			int j = 0;
			while (it.hasNext()) {
				it.next();
				if (j == which) {
					it.remove();
					break;
				}
				j++;
			}
		}
	}
}
