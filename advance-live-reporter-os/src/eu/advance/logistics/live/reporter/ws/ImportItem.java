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

/**
 * The import items record.
 * @author karnokd, 2013.05.28.
 */
public class ImportItem {
	/** The consignment id. */
	public long consignmentId;
	/** The item id. */
	public long itemId;
	/** The external id. */
	@XmlElement(required = true)
	public java.lang.String externalId;
	/** Field. */
	public double width;
	/** Field. */
	public double height;
	/** Field. */
	public double length;
	/** Indicate a deleted state. */
	public Boolean isDeleted;
}