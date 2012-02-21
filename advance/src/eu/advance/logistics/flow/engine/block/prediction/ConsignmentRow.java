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

package eu.advance.logistics.flow.engine.block.prediction;


import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * A data row from the CSV.
 * @author karnokd, 2012.01.20.
 */
public class ConsignmentRow {
	/** Server-generated date when the consignment was initially entered. */
	public int row;
	/** Server-generated time of day to minute precision the consignment was entered (e.g. 12:01, 14:32). */
	public DateTime enteredDateTime;
	/** Date the consignment was manifested. Time-of-day to minute precision the consignment was manifested. */
	public DateTime manifestedDateTime;
	/** This is only Fradley in the current extract. */
	public String hubFacility;
	/** The encoded hub facility. */
	public int hubFacilityId;
	/** Depot number. */
	public int collectingDepot;
	/** Some of the postcodes are incomplete - i.e. they are missing the last few letters and therefore refer to a larger area. */
	public String collectionPostCode;
	/** The translated postcode id. */
	public int collectionPostCodeId;
	/** Collection of latitude and longitude only started within the last year or so. A missing value is denoted by a ‘?’. */
	public Point2D.Double collectionGPS;
	/** Depot number. */
	public int deliveryDepot;
	/** 
	 * Postcodes are validated down to the sector level, 
	 * which means they do include the number at the start of the second block but
     * not the last two letters (e.g. B47 not B4 7ET). 
     */
	public String deliveryPostCode;
	/** The translated postcode id. */
	public int deliveryPostCodeId;
	/** Some records do not have values. A missing value is denoted by a ‘?’. */
	public Point2D.Double deliveryGPS;
	/** 
	 * Whether the consignment needs to be delivered using a van/truck
     * with a tail lift. This is relevant for depots planning lorry resources
     * but is irrelevant to the hub. 
     */
	public boolean tailLift;
	/** Delivery service level (free, economy or premium). */
	public String service;
	/** The service level id. */
	public int serviceId;
	/** Saturday delivery flag. */
	public boolean saturdayDelivery;
	/** AM delivery flag. */
	public boolean amDelivery;
	/** Timed delivery flag. */
	public boolean timedDelivery;
	/** Book in requested flag. */
	public boolean bookInRequested;
	/** The number of forklift trips to move the consignment (i.e. the number of physical pallets). */
	public int lifts;
	/** Depot number. */
	public int payingDepot;
	/** The total consignment weight in kilogrammes. */
	public int consignmentWeight;
	/** 
	 * An alpha-numeric identifier for the consignment assigned by the
     * depot and meant to be unique for that day (the gui enforces it but
     * can be bypassed by a depot). 
     */
	public String consignmentNumber;
	/** 
	 * An individual alpha-numeric identifier for each pallet in the
     * consignment assigned by Palletways and unique throughout the
     * database. The time of barcoding is not captured but is usually
     * when the pallet is entered. 
     */
	public String barcodes;
	/** The number of quarter pallets. */
	public int q;
	/** The number of half pallets. */
	public int h;
	/** The number of full pallets. */
	public int f;
	/** Free-text notes (see later section). */
	public String notes;
	/**
	 * Convert the given date-time to a minutes since epoch representation.
	 * @param dt the date and time
	 * @return the minutes since epoch
	 */
	static int fromTimestamp(DateTime dt) {
		return (int)(dt.getMillis() / 60000);
	}
	/**
	 * Convert a minute-since-epoch timestamp to datetime.
	 * @param value minutes-since-epoch
	 * @return the datetime
	 */
	static DateTime toTimestamp(int value) {
		return new DateTime(60000L * value);
	}
	/**
	 * Convert a upscaled GPS data into regular double.
	 * @param value the upscaled data
	 * @return the regular double
	 */
	static double toGPSDouble(int value) {
		return value / 10000000d;
	}
	/**
	 * Scale up the double value and return it as integer.
	 * @param value the double value
	 * @return the integer value
	 */
	static int fromGPSDouble(double value) {
		return (int)(value * 10000000);
	}
	/**
	 * Parse a specific row.
	 * @param index the row index.
	 * @param cells the csv cells
	 * @param row the row record
	 * @param strings the string mapping
	 */
	public static void parseRow(int index, List<String> cells, ConsignmentRow row, 
			Map<String, Integer> strings) {
		row.row = index;
		
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yy HH:mm").withPivotYear(2000).withZoneUTC();
		
		row.enteredDateTime = fmt.parseDateTime(cells.get(0) + " " + cells.get(1));
		row.manifestedDateTime = fmt.parseDateTime(cells.get(2) + " " + cells.get(3));
		row.hubFacility = cells.get(4);
		row.collectingDepot = Integer.parseInt(cells.get(5));
		row.collectionPostCode = cells.get(6);
		if (!"?".equals(cells.get(7))) {
			row.collectionGPS = new Point2D.Double(Double.parseDouble(cells.get(7))
					, Double.parseDouble(cells.get(8)));
		} else {
			row.collectionGPS = null;
		}
		row.deliveryDepot = Integer.parseInt(cells.get(9));
		row.deliveryPostCode = cells.get(10);
		if (!"?".equals(cells.get(11))) {
			row.deliveryGPS = new Point2D.Double(Double.parseDouble(cells.get(11)), 
					Double.parseDouble(cells.get(12)));
		} else {
			row.deliveryGPS = null;
		}
		row.tailLift = "yes".equals(cells.get(13));
		row.service = cells.get(14);
		row.saturdayDelivery = "yes".equals(cells.get(15));
		row.amDelivery = "yes".equals(cells.get(16));
		row.timedDelivery = "yes".equals(cells.get(17));
		row.bookInRequested = "yes".equals(cells.get(18));
		row.lifts = Integer.parseInt(cells.get(19));
		row.payingDepot = Integer.parseInt(cells.get(20));
		row.consignmentWeight = Integer.parseInt(cells.get(21));
		row.consignmentNumber = cells.get(22);
		row.barcodes = cells.get(23);
		row.q = Integer.parseInt(cells.get(24));
		row.h = Integer.parseInt(cells.get(25));
		row.f = Integer.parseInt(cells.get(26));
		row.notes = cells.get(27);
		
		row.hubFacilityId = stringId(row.hubFacility, strings);
		row.collectionPostCodeId = stringId(row.collectionPostCode, strings);
		row.deliveryPostCodeId = stringId(row.deliveryPostCode, strings);
		
		row.serviceId = 0;
		if ("PREMIUM".equalsIgnoreCase(row.service)) {
			row.serviceId = 1;
		} else
		if ("FREE".equalsIgnoreCase(row.service)) {
			row.serviceId = 2;
		}
	}
	/**
	 * @return Create the flags field.
	 */
	public int getFlags() {
		int flag = 0;
		if (tailLift) {
			flag += 1;
		}
		if (saturdayDelivery) {
			flag += 2;
		}
		if (amDelivery) {
			flag += 4;
		}
		if (timedDelivery) {
			flag +=  8;
		}
		if (bookInRequested) {
			flag += 16;
		}
		flag += serviceId * 32;
		
		return flag;
	}
	/** @return the service type string from serviceId. */
	public String serviceType() {
		switch (serviceId) {
		case 0:
			return "ECONOMIC";
		case 1:
			return "PREMIUM";
		case 2:
			return "FREE";
		default:
			return "UNKNOWN";
		}
	}
	/**
	 * Returns the saved string id for the string.
	 * @param s the string id
	 * @param strings the 
	 * @return the saved string
	 */
	public static int stringId(String s, Map<String, Integer> strings) {
		Integer i = strings.get(s.toUpperCase());
		if (i == null) {
			i = strings.size();
			strings.put(s.toUpperCase(), i);
		}
		return i;
	}
}
