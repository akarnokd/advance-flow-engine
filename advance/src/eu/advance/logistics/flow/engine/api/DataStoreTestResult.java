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

package eu.advance.logistics.flow.engine.api;

/**
 * The result of testing JDBC, JMS, FTP and other resources.
 * @author karnokd, 2011.10.03.
 */
public enum DataStoreTestResult {
	/** The test succeded. */
	SUCCESS,
	/** No driver was found. */
	MISSING_DRIVER,
	/** The connection was refused. */
	CONNECTION_REFUSED,
	/** The supplied credentials are invalid. */
	INVALID_CREDENTIALS,
	/** Other network error. */
	NETWORK_ERROR
}
