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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import eu.advance.logistics.live.reporter.model.ChartView;
import eu.advance.logistics.live.reporter.model.ServiceLevel;
import eu.advance.logistics.live.reporter.model.UOM;

/**
 * DayByData record stores data for daybyday chart.
 * @author csirobi, 2013.02.04.
 */

public class DayByData {
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
	public final Map<Date, Map<OrientStatus, Map<ServiceLevel, BarData>>> items;
	/**
	 * Constructor with the switch source.
	 * @param hubDepotSwitch the switch source
	 */
	public DayByData(HubDepotSwitch hubDepotSwitch) {
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
	 * @param coord the coordinate object
	 */
	public void normalizeBy(BarCoordinate coord) {
		GraphDecimal ratio = coord.totalCoord;

		for (Date dateKey : this.items.keySet()) {
			for (OrientStatus oKey : this.getOrientKeys()) {
				for (ServiceLevel sKey : ServiceLevel.values()) {
					this.items.get(dateKey).get(oKey).get(sKey).normalize(ratio);
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
	 * Convert the date into String based on English local.
	 * @param date the date
	 * @return converted date
	 */
	public String getDateString(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, EEE", Locale.ENGLISH);

		return sdf.format(date);
	}

	/**
	 * Checking the business-as-usual-scale to be bigger than the sum items data.
	 */
	public void checkBaus() {
		if (this.items.size() > 0) {
			for (Date dateKey : this.items.keySet()) {
				for (OrientStatus oKey : this.getOrientKeys()) {
					int inner = this.items.get(dateKey).get(oKey).get(ServiceLevel.ALL).value.intValue();
					if (inner > this.busAsUsSc) {
						this.busAsUsSc = inner;
					}
				}
			}

		}
	}

}
