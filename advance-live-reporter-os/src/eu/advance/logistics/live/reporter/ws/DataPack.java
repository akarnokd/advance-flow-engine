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

package eu.advance.logistics.live.reporter.ws;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The outer record of the import data.
 * @author Jani, 2013.05.27.
 */
@XmlRootElement
public class DataPack {
	/** The user name. */
	public String userName;
	/** The password. */
	public String password;
	/** The array of consignments. */
	@XmlElementWrapper(name = "consignments")
	@XmlElement(name = "consignment")	
	public ImportConsignment[] consignments;
	/** The array of items. */
	@XmlElementWrapper(name = "items")
	@XmlElement(name = "item")
	public ImportItem[] items;
	/** The array of events. */
	@XmlElementWrapper(name = "events")
	@XmlElement(name = "event")	
	public ImportEvent[] events;
	/** The array of territories. */
	@XmlElementWrapper(name = "territories")
	@XmlElement(name = "territory")	
	public ImportTerritory[] territories;
	/** The array of vehicle scans. */
	@XmlElementWrapper(name = "vehicleHeaders")
	@XmlElement(name = "vehicleHeader")	
	public ImportVehicleHeader[] vehicleHeaders;
	/** The array of vehicle scans. */
	@XmlElementWrapper(name = "vehicleScans")
	@XmlElement(name = "vehicleScan")	
	public ImportVehicleScan[] vehicleScans;
	/** The array of vehicle item. */
	@XmlElementWrapper(name = "vehicleItems")
	@XmlElement(name = "vehicleItem")	
	public ImportVehicleItem[] vehicleItems;
	/** The array of vehicle declared items. */
	@XmlElementWrapper(name = "vehicleDeclareds")
	@XmlElement(name = "vehicleDeclared")	
	public ImportVehicleDeclared[] vehicleDeclared;
}
