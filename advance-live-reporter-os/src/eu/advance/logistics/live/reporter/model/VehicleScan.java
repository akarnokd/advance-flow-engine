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
package eu.advance.logistics.live.reporter.model;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;

/**
 * A vehicle scan record.
 * @author karnokd, 2013.09.23.
 *
 */
public class VehicleScan {
	/** The scan unique id. */
	public long scanId;
	/** The vehicle session. */
	public String sessionId;
	/** Scan timestamp. */
	public DateTime timestamp;
	/** Warehouse. */
	public String warehouse;
	/** On enter? */
	public boolean enter;
	/** Set of item external identifiers. */
	public final Set<String> externalIds = new HashSet<>();
}
