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

import java.util.HashMap;

import eu.advance.logistics.live.reporter.model.ItemStatus;
import eu.advance.logistics.live.reporter.model.ServiceLevel;

/**
 * The extended HubCoord stores number of total and alive ticks for coordinate.
 * @author csirobi, 2013.02.04.
 */

public class HubCoord extends BarCoordinate {
	/** Store number of total ticks for every snip. */
	public HashMap<ServiceLevel, GraphDecimal> noOfTotalTick;
	/** Store number of alive ticks. */
	public GraphDecimal noOfAliveTick;  
	/**
	 * Constructor, sets the fields.
	 * @param sumData the summary data
	 * @param maxTickUnit the maximum tick unit
	 * @param maxNoOfTick the maximum numer op ticks
	 */
	public HubCoord(SumData sumData, int maxTickUnit, int maxNoOfTick) {
		super(sumData.busAsUsSc, maxTickUnit, maxNoOfTick);

		this.noOfTotalTick = new HashMap<ServiceLevel, GraphDecimal>();
		for (ServiceLevel sKey : ServiceLevel.values()) {
			this.setNoOfTotalTick(sKey, sumData);
		}
		this.setNoOfAliveTick(sumData);
	}
	/**
	 * Set the number of total ticks.
	 * @param snip the service level
	 * @param sumData the summary data
	 */
	private void setNoOfTotalTick(ServiceLevel snip, SumData sumData) {
		// Count: (totalCoord - sumData.maxLeftHub() + leftHub) DIV (tickUnit * scale)
		int total = super.totalCoord.subtract(sumData.maxLeftHub(true))
				.add(sumData.items.get(snip).get(OrientStatus.SINGLE).get(ItemStatus.LEFT_HUB_TODAY).value).intValue();
		int tickScale = super.getTickScale().intValue();
		this.noOfTotalTick.put(snip, new GraphDecimal((total / tickScale)));
	}
	/**
	 * Set the number of available ticks.
	 * @param sumData the summary data
	 */
	private void setNoOfAliveTick(SumData sumData) {
		// Count: (totalCoord - sumData.maxLeftHub()) DIV (tickUnit * scale)
		int alive = super.totalCoord.subtract(sumData.maxLeftHub(true)).intValue();
		int tickScale = super.getTickScale().intValue();
		this.noOfAliveTick = new GraphDecimal((alive / tickScale));
	}
}
