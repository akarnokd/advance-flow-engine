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

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

import eu.advance.logistics.live.reporter.model.ChartView;
import eu.advance.logistics.live.reporter.model.ItemStatus;
import eu.advance.logistics.live.reporter.model.ServiceLevel;
import eu.advance.logistics.live.reporter.model.UOM;

/**
 * SumData record stores data for summary chart.
 * @author csirobi, 2013.02.04.
 */
public class SumData {
  /** Chart view based on hub or depot user. */
  public ChartView chartView;
	/** Type of hub/depot. */	
	public TypeStatus type;
	/** Id of hub/depot. */
	public long id;
	/** Unit of hub/depot. */
	public UOM unit;
	/** Display name of hub/depot. */
	public String name;
	/** Business-as-usual-scale of hub/depot. */
	public int busAsUsSc;
	/** Store items for hub/depot. */
	public final Map<ServiceLevel, Map<OrientStatus, Map<ItemStatus, BarData>>> items;

	/**
	 * Construct a summary data with the hub-depot switch object.
	 * @param hubDepotSwitch the hub-depot switch object
	 */
	public SumData(HubDepotSwitch hubDepotSwitch) {
	  this.chartView = hubDepotSwitch.getChartView();
		this.type = hubDepotSwitch.getTypeStatus();
		this.id = hubDepotSwitch.getHubDepotInfo().id;
		this.unit = hubDepotSwitch.getUnit();
		this.name = hubDepotSwitch.getHubDepotInfo().getHubDepotInfo(this.chartView);
		this.busAsUsSc = hubDepotSwitch.getHubDepotInfo().busAsUsualScale.get(hubDepotSwitch.getUnit());

		this.items = new LinkedHashMap<>();
	}

	/**
	 * Normalize value of items based on total coordinate.
	 * @param coord the coordinate
	 */
	public void normalizeBy(BarCoordinate coord) {
		GraphDecimal ratio = coord.totalCoord;

		for (ServiceLevel sKey : ServiceLevel.values()) {
			for (OrientStatus oKey : this.getOrientKeys()) {
				for (ItemStatus pKey : ItemStatus.values()) {
					BarData bd = this.items.get(sKey).get(oKey).get(pKey);
					if (bd != null) {
						bd.normalize(ratio);
					}
				}
			}
		}

	}

	/**
	 * Used orient status based on the data type (hub or depot).
	 * @return enumSet of orient status
	 */
	public EnumSet<OrientStatus> getOrientKeys() {
		EnumSet<OrientStatus> result = (this.type.compareTo(TypeStatus.HUB) == 0)
				? EnumSet.of(OrientStatus.SINGLE)
						: EnumSet.of(OrientStatus.ORIGIN, OrientStatus.DESTIN);

				return result;
	}

	/**
	 * For depot: find the maximum value/normalized value for the "left_hub" status of items.
	 * @param isValue find it for value or normalized value
	 * @return the maximum value
	 */
	public GraphDecimal maxLeftHub(boolean isValue)	{
		GraphDecimal leftLength = new GraphDecimal(0);

		for (OrientStatus oKey : this.getOrientKeys()) {
			BarData bd = this.items.get(ServiceLevel.ALL).get(oKey).get(ItemStatus.LEFT_HUB_TODAY);
			if (bd != null) {
				GraphDecimal v = (isValue) ? bd.value : bd.normalValue;
	
				if (v.compareTo(leftLength) > 0) {
					leftLength = v;
				}
			}
		}

		return leftLength;
	} 


	/**
	 * Checking the business-as-usual-scale to be bigger than the sum items data.
	 */
	public void checkBaus() {
		GraphDecimal value = new GraphDecimal(0);

		for (OrientStatus oKey : this.getOrientKeys()) {
			GraphDecimal inner = new GraphDecimal(0);
			for (ItemStatus pKey : ItemStatus.values()) {
				BarData bd = this.items.get(ServiceLevel.ALL).get(oKey).get(pKey);
				if (bd != null) {
					inner = inner.add(new GraphDecimal(bd.value));
				}
			}
			if (inner.compareTo(value) > 0) {
				value = new GraphDecimal(inner);
			}
		}

		if (value.intValue() > this.busAsUsSc) {
			this.busAsUsSc = value.intValue();
		}
	}
}
