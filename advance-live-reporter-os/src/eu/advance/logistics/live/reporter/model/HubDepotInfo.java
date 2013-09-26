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

import java.util.EnumMap;


/**
 * HubDepotInfo inner class represents the concrete node of hub or depot.  
 * @author csirobi, 2013.04.22.
 */
public class HubDepotInfo {
	/** Id of the hub/depot. */
	public long id;
	/** Name of the hub/depot. */
	public String name;
	/** Indicate if this is a hub. */
	public boolean isHub;
	/** Business-as-usual-scale of the hub/depot based on the unit status. */
	public EnumMap<UOM, Integer> busAsUsualScale = new EnumMap<>(UOM.class);
	/**
	 * Returns the info name.
	 * @return the info name
	 */
	public String getHubDepotInfo() {
		return isHub ? "Overall" : id + ", " + name;
	}
	/**
	 * Returns the info name based on the chart view.
	 * @param chartView the chart view
	 * @return the info name
	 */
	public String getHubDepotInfo(ChartView chartView) {
		String s = "";
		if (isHub) {
			s = "Overall";
		} else {
			switch (chartView) {
			case HUB_USER:	{
				s = this.id + ", " + this.name;
				break;
			}
			case DEPOT_USER: {
				s = this.name;
				break;
			}
			default:
			}
		}
		return s;
	}
	/**
	 * Set all values to the same.
	 * @param max the common max value
	 */
	public void setAll(int max) {
		for (UOM u : UOM.values()) {
			busAsUsualScale.put(u, max);
		}
	}
}