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

import eu.advance.logistics.live.reporter.model.ChartView;

/**
 * Enum for different type of orientation.
 * @author csirobi
 */
public enum OrientStatus {
	/** Used at "from depots". */
	ORIGIN("origin", "Inbound: "),
	/** Used at "to depots". */
	DESTIN("destin", "Outbound: "),
	/** Used at hub. */
	SINGLE("single", "");

	/** The display key. */
	private String info;
	/** The display predfix of title. */
	private String message;
	/**
	 * Constructor, initializes the fields.
	 * @param info the display key
	 * @param msg the message
	 */
	private OrientStatus(String info, String msg) {
		this.info = info;
		this.message = msg;
	}

	/**
	 * Returns the display key.
	 * @return the display key
	 */
	public String getInfo()	{
		return this.info;
	}

	/**
	 * Returns the display prefix.
	 * @return the display prefix
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * Returns the display prefix based on the chart view.
	 * @param chartView chart view
	 * @return the display prefix
	 */
	public String getMessage(ChartView chartView) {
		String s = "";
		switch (chartView) {
		case HUB_USER: {
			s = this.message;
			break;
		}
		case DEPOT_USER: {
			s = this.message;
			//s = this.getOppositeMessage();
			break;
		}
		default:
		}
		return s;
	}

	/**
	 * Helper function for getting the opposite display prefix.
	 * @return the opposite display prefix
	 */
	protected String getOppositeMessage() {
		String s = "";
		switch (this) {
		case ORIGIN: {
			s = OrientStatus.DESTIN.getMessage();
			break;
		}
		case DESTIN: {
			s = OrientStatus.ORIGIN.getMessage();
			break;
		}
		case SINGLE: {
			s = OrientStatus.SINGLE.getMessage();
			break;
		}
		default:
		}
		return s;
	}

}
