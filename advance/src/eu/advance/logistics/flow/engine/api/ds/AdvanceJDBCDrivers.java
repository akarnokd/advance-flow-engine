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

package eu.advance.logistics.flow.engine.api.ds;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Enumeration for JDBC drivers.
 * @author akarnokd, 2011.09.19.
 */
public enum AdvanceJDBCDrivers {
	/** Generic driver with custom settings. */
	GENERIC(null, "jdbc:"),
	/** Oracle driver. */
	ORACLE("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin://{host}:{port 1521}:{sid}"),
	/** MySQL driver. */
	MYSQL("com.mysql.jdbc.Driver", "jdbc:mysql://{host}:{port 3306}/{database}"),
	/** MS SQL driver. */
	MSSQL("net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://{host}:{port 1433}/{database}")
	;
	/** The driver class. */
	@Nullable
	public final String driverClass;
	/** The URL prefix. */
	@NonNull
	public final String urlTemplate;
	/**
	 * Initializes the enum with the driver class and URL prefix for the connection.
	 * @param driverClass the driver class
	 * @param urlTemplate the URL prefix to help the GUI
	 */
	AdvanceJDBCDrivers(@Nullable String driverClass, @NonNull String urlTemplate) {
		this.driverClass = driverClass;
		this.urlTemplate = urlTemplate;
	}
}
