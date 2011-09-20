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
public interface AdvanceJDBCDataSource extends AdvanceCreateModifyInfo {
	/** @return the unique identifier of the record. */
	int id();
	/** @return the name used by blocks to reference this data source. */
	String name();
	/** @return the JDBC driver. */
	AdvanceJDBCDrivers driver();
	/** @return the connection url. */
	String url();
	/** @return the user who connects. */
	String user();
	/** 
	 * <p>Note that passwords are never returned from the 
	 * control API calls and are always {@code null}.</p> 
	 * @return the password for connection. 
	 */
	char[] password();
	/** @return the connection pool size. */
	int poolSize();
	/**
	 * @param newName the new name
	 */
	void name(String newName);
	/**
	 * @param newDriver the new driver
	 */
	void driver(AdvanceJDBCDrivers newDriver);
	/**
	 * @param newUrl the new url
	 */
	void url(String newUrl);
	/**
	 * @param newUser the new user
	 */
	void user(String newUser);
	/**
	 * <p>Set a new password.</p>
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 * @param newPassword the new password
	 */
	void password(char[] newPassword);
	/**
	 * @param newPoolSize the new pool size
	 */
	void poolSize(int newPoolSize);
}
