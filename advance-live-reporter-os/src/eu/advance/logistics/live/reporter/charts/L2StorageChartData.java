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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * L2StorageChartData stores chart data for warehouse level 2 chart. 
 * These data are calculated from L2StorageRawData.
 * @author csirobi
 */
public class L2StorageChartData {
	/** Id of the storage. */
	public long id;
	/** Relative fullness. */
	public double relFullness;
	/** HashMap for bar length of chart. */
	public final Map<L2TimeState, Map<WarehouseServiceLevel, BarLength>> barLength;
	/** HashMap for background color of chart. */
	public final Map<L2TimeState, BgColor>  bgColor;
	
	/**Time states which are used in this level.*/
	public static final L2TimeState[] USED_BAR_TIME = {
	  L2TimeState.NOW_AT_HUB,
	  L2TimeState.COMING_UP
	};
	/**Warehouse services which are used in this level.*/
	public static final WarehouseServiceLevel[] USED_SERVICES = {
	  WarehouseServiceLevel.STANDARD,
	  WarehouseServiceLevel.PRIORITY_SPECIAL
	};
	
	/**
	 * Constructor, initializes the fields.
	 */
	public L2StorageChartData() {
		this.barLength = new LinkedHashMap<>();
		this.bgColor = new LinkedHashMap<>();
	}

	/**
	 * BarLength inner helper class for storing the bar length. 
	 * @author csirobi
	 */
	public static class BarLength {
		/** The length value .*/
		private GraphDecimal length;
		/**
		 * Constructor.
		 */
		public BarLength() {
			this(0);
		}
		/**
		 * Double constructor.
		 * @param value the value
		 */
		public BarLength(double value) {
			this.length = new GraphDecimal(value).divide(new GraphDecimal(1));
		}
		/**
		 * Set the length.
		 * @param value the new length
		 */
		public void setLength(double value)	{
			this.length = new GraphDecimal(value).divide(new GraphDecimal(1));
		}
		/**
		 * Returns the length as GraphDecimal.
		 * @return the length
		 */
		public GraphDecimal getLength() {
			return this.length;
		}
	}

	/**
	 * BgColor inner helper class for storing the backgpround color. 
	 * @author csirobi
	 */
	public static class BgColor	{
		/** Maximum color component. */
		private static final int MAX_COLOR = 255;
		/** Red. */
		private double rColor;
		/** Green. */
		private double gColor;
		/** Blue. */
		private double bColor;
		/**
		 * Constructor. Sets the color to black.
		 */
		public BgColor() {
			this(0, 0, 0);
		}
		/**
		 * Constructor with color components.
		 * @param r the red
		 * @param g the green
		 * @param b the blue
		 */
		public BgColor(double r, double g, double b) {
			this.rColor = r;
			this.gColor = g;
			this.bColor = b;
		}
		/**
		 * Sets the color component from a length 3 list.
		 * @param colorList the color list
		 */
		public void setColor(List<Double> colorList) {
			this.rColor = colorList.get(0);
			this.gColor = colorList.get(1);
			this.bColor = colorList.get(2);
		}
		/**
		 * Returns the color name as CSS command.
		 * @return the color
		 */
		public String getColor() {
			return "rgb(" + this.up(this.rColor) + ", " + this.up(this.gColor) + ", " + this.up(this.bColor) + ")";
		}
		/**
		 * Scale up the color component to 0-255.
		 * @param c the color
		 * @return the scaled color
		 */
		private int up(double c) {
			return (int) (c * BgColor.MAX_COLOR);
		}
	}
}
