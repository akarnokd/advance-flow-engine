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
 * BarCoordinate is the core class for calculating and storing different ticks of coordinate.
 * @author csirobi, 2013.02.04.
 */

public class BarCoordinate {
	/** Scale. */
	public GraphDecimal scale;
	/** Tick unit. */
	public GraphDecimal tickUnit;
	/** Number of ticks. */
	public GraphDecimal noOfTick;
	/** Total coordinates. */
	public GraphDecimal totalCoord;
	/** Normal scale. */
	public GraphDecimal normalScale;
	/**
	 * Constructor. Sets values to zero.
	 */
	protected BarCoordinate() {
		this.scale = new GraphDecimal(0);
		this.tickUnit = new GraphDecimal(1);
		this.noOfTick = new GraphDecimal(0);

		this.totalCoord = new GraphDecimal(0);
		this.normalScale = new GraphDecimal(0);
	}
	/**
	 * Constructor, sets the values.
	 * @param configCap the config capacity
	 * @param maxTickUnit the maximum tick unit
	 * @param maxNoOfTick maximum number of ticks
	 */
	public BarCoordinate(int configCap, int maxTickUnit, int maxNoOfTick) {
		this();
		// Checking the filled/alive configCapacity 
		if (configCap > 0) {
			this.countTotalCoord(configCap, maxTickUnit, maxNoOfTick);
		}
	}
	/**
	 * Count the total coordinate.
	 * @param configCap the capacity
	 * @param maxTickValue the max tick value
	 * @param maxNoOfTick the maximum number of ticks
	 */
	private void countTotalCoord(int configCap, int maxTickValue, int maxNoOfTick) {
		// 1.) Count the scale
		int x = configCap, scale = 1;
		boolean devideTen = true;
		while (maxTickValue * maxNoOfTick < x) {
			devideTen = (x % 10 == 0) ? true : false;

			if (devideTen) {
				x = x / 10;
			} else {
				x = (x / 10) + 1;
			}

			scale = scale * 10;
		}
		this.scale = new GraphDecimal(scale);

		// 2.) Define the unit of every tick
		int tickUnit = 0;
		if (x % maxNoOfTick != 0) {
			tickUnit = (x / maxNoOfTick) + 1;
		} else {
			tickUnit = (x / maxNoOfTick);
		}
		this.tickUnit = new GraphDecimal(tickUnit);

		// 3.) Define the number of tick in the ruler
		int noOfTick = 0;
		if (x % tickUnit != 0) {
			noOfTick = (x / tickUnit) + 1;
		} else {
			noOfTick = (x / tickUnit);
		}
		this.noOfTick = new GraphDecimal(noOfTick);

		// 4.) Set the total value..
		this.totalCoord = this.tickUnit.multiply(this.noOfTick).multiply(this.scale);
		//.. and the normalScale
		this.normalScale = new GraphDecimal(1).divide(this.noOfTick);
	}
	/**
	 * Returns the tick scale.
	 * @return the tick scale
	 */
	protected GraphDecimal getTickScale() {
		return this.tickUnit.multiply(this.scale);
	}

}
