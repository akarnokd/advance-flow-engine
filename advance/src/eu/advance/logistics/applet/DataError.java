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

/**
 * Common errors when accessing WindMT.
 * @author karnokd
 */
public enum DataError {
	/** Database driver error. */
	DRIVER_ERROR,
	/** Connection error. */
	CONNECTION_ERROR,
	/** Query execution error. */
	QUERY_ERROR,
	/** User access error. */
	ACCESS_ERROR,
	/** An application resource problem. */
	RESOURCE_ERROR,
	/** The data source is missing. */
	MISSING_DATASOURCE
}
