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
package eu.advance.logistics.live.reporter.charts;


/**
 * The extended L3DepotStorageCoord stores the number of ticks 
 * and tick units for coordinate at the chart warehouse level 3.
 * @author csirobi
 */
public class L3DepotStorageCoord extends BarCoordinate {
	/**
	 * Constructor, sets the fields.
	 * @param totalCoord the total coordinate
	 * @param scale the scale
	 */
	public L3DepotStorageCoord(double totalCoord, double scale) {
		// Checking the depotBusAsUsSc/storageCapacities..
		if ((totalCoord > 0) && (scale > 0)) {
			// Depot business-as-usual-scale
			this.totalCoord = new GraphDecimal(totalCoord);
			// Storage capacity
			this.scale = new GraphDecimal(scale);

			// Use only simple counter...
			this.tickUnit = new GraphDecimal(1);
			int i = this.totalCoord.divide(this.scale).intValue(); 
			this.noOfTick = new GraphDecimal(i);
			this.normalScale = this.scale.divide(this.totalCoord);    
		}
	}

}
