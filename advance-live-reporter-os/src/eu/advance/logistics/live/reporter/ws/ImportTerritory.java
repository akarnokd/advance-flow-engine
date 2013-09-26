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

/***********************************************************************
 * Module:  ImportTerritories.java
 * Author:  Jani
 * Purpose: Defines the Class ImportTerritories
 ***********************************************************************/


/**
 * A territory record. 
 */
public class ImportTerritory {
	/** The depot. */
	public long depot;
	/** The start of the territory. */
	@XmlElement(required = true)
	public java.util.Date startDate;
	/** The postcode area. */
	@XmlElement(required = true)
	public java.lang.String postcodeArea;
}