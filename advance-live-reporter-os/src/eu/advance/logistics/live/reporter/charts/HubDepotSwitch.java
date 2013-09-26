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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.advance.logistics.live.reporter.db.MasterDB;
import eu.advance.logistics.live.reporter.model.ChartView;
import eu.advance.logistics.live.reporter.model.HubDepotInfo;
import eu.advance.logistics.live.reporter.model.UOM;
import eu.advance.logistics.live.reporter.model.User;

/**
 * The extended HubDepotSwitch is for storing the actual user's settings. 
 * @author csirobi, 2013.02.04.
 */

public class HubDepotSwitch {

	/** Type status: hub or depot. */
	private ChartView chartView;
	/** Type status: hub or depot. */
	private TypeStatus typeStatus;
	/** Unit status. */
	private UOM unit;
	/** DateTime of the request. */
	private Date dateTime;
	/** Puffered DateTime of the request when the real time is checked. */
	private String puffDateTime;
	/** Date format for the DateTime. */
	public DateFormat df;
	/**
	 * The hub identifier.
	 */
	private final long hubId;
	/** The current depot. */
	private long depotId;
	/** During day orientation. */
	private enum DuringOrient {
		/** Inbound. */
		INBD, 
		/** Outbound. */
		OUTBD;
	}
	/** During day current orientation. */
	private DuringOrient ddOrient;
	/** During day horizont chart. */
	private String ddHorizont;
	/** During day unit. */
	private String ddSnips;
	/** Current cursor position. */
	private int ddCursorAt;
	/** The current user id. */
	private final String userId;
	/**
	 * Constructor, initializes the fields to default values.
	 * @param hubId the hub identifier
	 * @param userId the user identifier
	 */
	public HubDepotSwitch(long hubId, String userId) {
		this.hubId = hubId;
		this.userId = userId;
		this.chartView = ChartView.HUB_USER;
		this.typeStatus = TypeStatus.HUB;
		this.unit = UOM.PRICEUNIT;
		this.df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		try {
			this.dateTime = df.parse("2013-09-30 20:00");
		} catch (ParseException ex) {
			this.dateTime = new Date();
		}

		this.ddOrient = DuringOrient.INBD;
		this.ddHorizont = "";
		this.ddSnips = "standard_priority_special";
		this.ddCursorAt = -1;
	}
	/**
	 * Returns the hub id.
	 * @return the hub id
	 */
	public long hubId() {
		return hubId;
	}
	/**
	 * Returns the depot id.
	 * @return the depot id
	 */
	public long depotId() {
		return depotId;
	}
	/**
	 * Sets the chart view based on user.
	 * @param user the user object
	 */
	public void setChartView(User user) {
		this.chartView = (user.depot == null) ? ChartView.HUB_USER : ChartView.DEPOT_USER;
	}
	/**
	 * Set the hub-depot info.
	 * @param typeStatus the type status
	 * @param id the depot id
	 * @throws NumberFormatException
	 * @throws IllegalAccessException
	 */
	public void setHubDepotInfo(String typeStatus, String id) {
		this.typeStatus = TypeStatus.valueOf(typeStatus.toUpperCase());
		if (this.typeStatus == TypeStatus.DEPOT) {
			this.depotId = Long.parseLong(id);
		}
	}
	/**
	 * Set the unit of measure.
	 * @param unit the new unit of measure
	 */
	public void setUnit(String unit) {
		this.unit = UOM.valueOf(unit.toUpperCase());
	}
	/**
	 * Returns the chart view.
	 * @return the chart view
	 */
	public ChartView getChartView() {
		return this.chartView;
	}
	/**
	 * Returns the type status.
	 * @return the type status
	 */
	public TypeStatus getTypeStatus() {
		return this.typeStatus;
	}
	/**
	 * Sets a new date time.
	 * @param dateTime the date time
	 */
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	/**
	 * Sets a new date time value from string.
	 * @param dateTime the date time
	 * @throws ParseException on parsing error
	 */
	public void setDateTime(String dateTime) throws ParseException {
		this.dateTime = df.parse(dateTime);
	}
	/**
	 * Sets a new buffered date time value from string.
	 * @param dateTime the puffered date time
	 */
	public void setBuffDateTime(String dateTime) {
		this.puffDateTime = dateTime;
	}

	/**
	 * Sets a new orient value from string.
	 * @param ddOrient the orient string
	 */
	public void setDuringOrient(String ddOrient) {
		this.ddOrient = DuringOrient.valueOf(ddOrient.toUpperCase());	  
	}

	/**
	 * Sets a new horizont value from string.
	 * @param ddHorizont the horizont string
	 */
	public void setDuringHorizont(String ddHorizont) {
		this.ddHorizont = ddHorizont;
	}

	/**
	 * Sets a new snis string.
	 * @param ddSnips the snips string
	 */
	public void setDuringSnips(String ddSnips) {
		this.ddSnips = ddSnips;
	}

	/**
	 * Sets a new cursor value from string.
	 * @param cursor the cursor value
	 */
	public void setDuringCursor(String cursor) {
		this.ddCursorAt = Integer.parseInt(cursor);
	}

	/**
	 * Returns the hub-depot info.
	 * @return the hub-depot info
	 */
	public HubDepotInfo getHubDepotInfo() {
		switch (typeStatus) {
		case HUB:
			return MasterDB.getHubSummaryValueMaxes(hubId, userId);
		case DEPOT:
			return MasterDB.getDepotSummaryValueMaxes(hubId, depotId, userId);
		default:
			throw new IllegalStateException();
		}
	}
	/**
	 * Returns the unit of measure.
	 * @return the unit of measure
	 */
	public UOM getUnit() {
		return this.unit;
	}
	/**
	 * Returns the datetime.
	 * @return the datetime
	 */
	public Date getDateTime() {
		return this.dateTime;
	}
	/**
	 * Returns the puffered datetime.
	 * @return the puffered datetime
	 */
	public String getPuffDateTime() {
		return this.puffDateTime;
	}

	/**
	 * Returns the selector value.
	 * @return the selector value
	 */
	public String getSelectorValue() {
		return this.typeStatus.getInfo() + "_" + (typeStatus == TypeStatus.HUB ? hubId : depotId);
	}

	/**
	 * Returns the current unit value.
	 * @return the unit value
	 */
	public String getUnitValue() {
		switch (unit) {
		case FLOORSPACE:
			return "floorspace";
		case ITEMCOUNT:
			return "itemcount";
		case PRICEUNIT:
			return "priceunit";
		default:
			return "";
		}
	}
	/**
	 * Returns the current date and time as string.
	 * @return the date time string
	 */
	public String getDateTimeString() {
		return this.df.format(this.dateTime);
	}
	/**
	 * Returns the maximum value for the current unit status and
	 * referenced object.
	 * @return the maximum value
	 */
	public int getMax() {
		return getHubDepotInfo().busAsUsualScale.get(getUnit()).intValue();
	}

	/**
	 * Returns the current orient value as string.
	 * @return the current orient value as string
	 */
	public String getDuringOrient() {
		return this.ddOrient.name().toLowerCase(); 
	}

	/**
	 * Converts the during-day orient type to the orient status type.
	 * @return the converted rient status type
	 */
	public OrientStatus convertDuringOrient() {
		if (this.typeStatus == TypeStatus.HUB) {
			return OrientStatus.SINGLE;
		} else {
			if (this.ddOrient == DuringOrient.INBD) {
				return OrientStatus.ORIGIN;
			} else {
				return OrientStatus.DESTIN;
			}
		}
	}

	/**
	 * Returns the current horizont as a string.
	 * @return the current horizont as a string
	 */
	public String getDuringHorizont() {
		return this.ddHorizont;
	}

	/**
	 * Returns the current snips as a string.
	 * @return the current snips as a string
	 */
	public String getDuringSnips() {
		return this.ddSnips;
	}

	/**
	 * Returns the current cursor position as a string.
	 * @return the current cursor position as a string
	 */
	public String getDuringCursor() {
		return "" + this.ddCursorAt;
	}

	/**
	 * Returns the redirect parameters for the link.
	 * @return redirect parameters for the link
	 */
	public String getParams() {
		return "store_type=" + TypeStatus.HUB.name().toLowerCase() 
				+ "&store_id=" + this.hubId 
				+ "&unit=" + UOM.ITEMCOUNT.name().toLowerCase() 
				+ "&datetime=" + this.getDateTimeString();
	}
}
