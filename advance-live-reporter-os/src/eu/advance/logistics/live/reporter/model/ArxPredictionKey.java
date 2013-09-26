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

import java.util.Objects;


/**
 * The key record for the ARX input data.
 * @author karnokd, 2013.05.03.
 */
public final class ArxPredictionKey {
	/** The service level. */
	public final ServiceLevel serviceLevel;
	/** The unit status. */
	public final UOM unit;
	/** The depot. */
	public final long depot;
	/** The precomputed hash. */
	private final int h;
	/**
	 * Constructor, sets the fields.
	 * @param serviceLevel the service leve
	 * @param unit the unit
	 * @param depot the depot
	 */
	public ArxPredictionKey(ServiceLevel serviceLevel, UOM unit,
			long depot) {
		this.serviceLevel = serviceLevel;
		this.unit = unit;
		this.depot = depot;
		this.h = Objects.hash(serviceLevel, unit, depot);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ArxPredictionKey) {
			ArxPredictionKey o = (ArxPredictionKey) obj;
			return serviceLevel == o.serviceLevel
					&& unit == o.unit
					&& depot == o.depot;
			
		}
		return false;
	}
	@Override
	public int hashCode() {
		return h;
	}
	@Override
	public String toString() {
		return "{ " + serviceLevel + ", " + unit + ", " + depot + " }";
	}
}
