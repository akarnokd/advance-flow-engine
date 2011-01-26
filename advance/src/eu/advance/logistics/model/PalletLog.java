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

/**
 * The pallet transfer log.
 * @author karnok, 2010.12.15.
 * @version $Revision 1.0$
 */
public class PalletLog implements Serializable {
	/** */
	private static final long serialVersionUID = -610208648788193953L;
	/** The timestamp. */
	public long timestamp;
	/** The location object, e.g., Client, Warehouse&lt;Carrier&gt;, Delivery, Warehouse&lt;Distributor&gt;. */
	public Object location;
}
