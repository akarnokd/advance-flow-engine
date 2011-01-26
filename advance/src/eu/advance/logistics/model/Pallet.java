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
 * The base transportation unit.
 * @author karnok, 2010.12.15.
 * @version $Revision 1.0$
 */
public class Pallet implements Serializable {
	/** */
	private static final long serialVersionUID = 3134996541816225477L;
	/** The pallet's unique identifier. */
	public long id;
	/** The delivery priority. */
	public Priority priority;
	/** The height of the pallet. */
	public double heigth;
	/** Other pallets may be placed over this one? */
	public boolean stackable;
	/** The source of the pallet. */
	public Client sourceClient;
	/** The pickup address of the pallet. */
	public Address sourceAddress;
	/** The destination company. */
	public Client destinationClient;
	/** The end destination of the pallet. */
	public Address destinationAddress;
	/** When was this pallet created. */
	public long createTimestamp;
	/** The pallet's time-location log. */
	public List<PalletLog> logs;
}
