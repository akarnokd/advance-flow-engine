/*
 * Copyright 2010-2012 The Advance EU 7th Framework project consortium
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

package eu.advance.logistics.model;

import java.io.Serializable;
import java.util.List;

/**
 * A delivery order between two parties.
 * @author karnok, 2010.12.15.
 * @version $Revision 1.0$
 * @param <Source> the source location partner object, e.g., Client, Warehouse&lt;Carrier&gt;, 
 * Warehouse&lt;Distributor&gt;
 * @param <Destination> the destination partner object, e.g., Client, Warehouse&lt;Carrier&gt;, 
 * Warehouse&lt;Distributor&gt;
 */
public class Delivery<Source extends Serializable, Destination extends Serializable> 
implements Serializable {
	/** */
	private static final long serialVersionUID = 4303541067192395834L;
	/** The unique identifier. */
	public long id;
	/** The carrying truck. */
	public Truck truck;
	/** The starting address. */
	public Address sourceAddress;
	/** The source partner object. */
	public Source source;
	/** The finishing address. */
	public Address destinationAddress;
	/** The destination partner object. */
	public Destination destination;
	/** The pallets delivered. */
	public List<Pallet> pallets;
	/** The departure timestamp, null if not in transit. */
	public Long departureTimestamp;
	/** The arrival timestamp, null if still in transit. */
	public Long arrivalTimestamp;
}
