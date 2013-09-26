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
 * The extended DepotCoord stores number of closed and alive ticks for coordinate.
 * @author csirobi, 2013.02.04.
 */

public class DepotCoord extends BarCoordinate {
	/** Store number of closed ticks. */
	public GraphDecimal noOfClosedTick;
	/** Store number of alive ticks. */
	public GraphDecimal noOfAliveTick;
	/**
	 * Constructor, sets the fields.
	 * @param sumData the summary data
	 * @param maxTickUnit the max tick unit
	 * @param maxNoOfTick themax numbero fo ticks
	 */
	public DepotCoord(SumData sumData, int maxTickUnit, int maxNoOfTick) {
		super(sumData.busAsUsSc, maxTickUnit, maxNoOfTick);

		switch(sumData.chartView) {
		case HUB_USER: {
			this.setNoOfClosedTick(sumData);
			this.setNoOfAliveTick(sumData);
			break;
		}
		case DEPOT_USER: {
			this.noOfClosedTick = new GraphDecimal(0);
			// Count: (totalCoord) DIV (tickUnit * scale)
			int tickScale = super.getTickScale().intValue();
			this.noOfAliveTick = new GraphDecimal((super.totalCoord.intValue() / tickScale));
			break;
		}
		default:
		}
	}
	/**
	 * Sets the number of closed ticks.
	 * @param sumData the summary data
	 */
	private void setNoOfClosedTick(SumData sumData)	{
		// Count: (sumData.maxLeftHub()) DIV (tickUnit * scale)
		int leftHub = sumData.maxLeftHub(true).intValue();
		int tickScale = super.getTickScale().intValue();
		this.noOfClosedTick = new GraphDecimal((leftHub / tickScale));
	}
	/**
	 * Sets the number of alive ticks.
	 * @param sumData the summary data
	 */
	private void setNoOfAliveTick(SumData sumData) {
		// Count: (totalCoord - sumData.maxLeftHub()) DIV (tickUnit * scale)
		int alive = super.totalCoord.subtract(sumData.maxLeftHub(true)).intValue();
		int tickScale = super.getTickScale().intValue();
		this.noOfAliveTick = new GraphDecimal((alive / tickScale));
	}
}
