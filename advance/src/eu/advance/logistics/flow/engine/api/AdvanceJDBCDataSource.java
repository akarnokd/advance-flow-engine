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
 * Description of JDBC data store records.
 * @author karnokd, 2011.09.20.
 */
public class AdvanceJDBCDataSource extends AdvanceCreateModifyInfo {
	/** The unique identifier of the record. */
	public int id;
	/** The name used by blocks to reference this data source. */
	public String name;
	/** The JDBC driver. */
	public AdvanceJDBCDrivers driver;
	/** The connection url. */
	public String url;
	/** The user who connects. */
	public String user;
	/** 
	 * The password for connection.
	 * <p>Note that passwords are never returned from the 
	 * control API calls and are always {@code null}.</p> 
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 */
	public char[] password;
	/** The connection pool size. */
	public int poolSize;
}
