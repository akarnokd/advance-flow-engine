/*
 * Copyright 2010-2012 The Advance EU 7th Framework project consortium
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

package eu.advance.logistics.applet;

import java.io.Serializable;

/**
 * Callback interface to provide table cell data to
 * a Table drawer component.
 * @author karnokd, 2008.02.06.
 * @version $Revision 1.0$
 */
public interface TableModel extends Serializable {
	/**
	 * @return the number of rows to expect
	 */
	int getRowCount();
	/**
	 * @return the number of columns to expect
	 */
	int getColumnCount();
	/**
	 * Returns an object to a given row/column coordinate.
	 * @param row the row index, zero based
	 * @param column the column index, zero based
	 * @return the object value, can be null
	 */
	Object getValueAt(int row, int column);
	/**
	 * Returns a tooltip for a given row/column coordinate.
	 * @param row the row index, zero based
	 * @param column the column index, zero based
	 * @return the tooltip string, can be null
	 */
	String getTooltipAt(int row, int column);
}
