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

package eu.advance.logistics.flow.engine.block.file;


/**
 * The column enumerations.
 * @author karnokd, 2012.01.24.
 */
public enum ConsignmentColumn {
	/** Column. */
	ENTERED(ColumnTypes.MINUTES),
	/** Column. */
	MANIFESTED(ColumnTypes.MINUTES),
	/** Column. */
	HUB(ColumnTypes.INT),
	/** Column. */
	COLLECTING_DEPOT(ColumnTypes.INT),
	/** Column. */
	COLLECTION_POSTCODE(ColumnTypes.INT),
	/** Column. */
	COLLECTION_GPS(ColumnTypes.GPS),
	/** Column. */
	DELIVERY_DEPOT(ColumnTypes.INT),
	/** Column. */
	DELIVERY_POSTCODE(ColumnTypes.INT),
	/** Column. */
	DELIVERY_GPS(ColumnTypes.GPS),
	/** The flags: 
	 * 0:tail lift, 1:saturday delivery, 
	 * 2:am delivery, 3:timed delivery, 
	 * 4: book_in, 5-6: servicetype. */
	FLAGS(ColumnTypes.BYTE),
	/** Column. */
	LIFTS(ColumnTypes.INT),
	/** Column. */
	PAYING_DEPOT(ColumnTypes.INT),
	/** Column. */
	CONSIGNMENT_WEIGHT(ColumnTypes.INT),
//	/** Column. */
//	CONSIGNMENT_NUMBER,
//	/** Column. */
//	BARCODES,
	/** Column. */
	Q(ColumnTypes.INT),
	/** Column. */
	H(ColumnTypes.INT),
	/** Column. */
	F(ColumnTypes.INT),
//	/** Column. */
//	NOTES
	;
	/** The column type. */
	public final ColumnTypes type;
	/**
	 * Constructor.
	 * @param type the column type
	 */
	ConsignmentColumn(ColumnTypes type) {
		this.type = type;
	}
}
