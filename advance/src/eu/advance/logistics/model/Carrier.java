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
 * A transportation company responsible for transferring pallets.
 * @author karnok, 2010.12.15.
 * @version $Revision 1.0$
 */
public class Carrier implements Serializable {
	/** */
	private static final long serialVersionUID = -3524414605401070376L;
	/** The carrier identifier. */
	public long id;
	/** The carrier's human readable name. */
	public String name;
	/** Where to send the bills. */
	public Address billingAddress;
	/** The list of warehouses. */
	public List<Warehouse<Carrier>> warehouses;
	/** The list of trucks. */
	public List<Truck> trucks;
	/** The list of clients. */
	public List<Client> clients;
}
