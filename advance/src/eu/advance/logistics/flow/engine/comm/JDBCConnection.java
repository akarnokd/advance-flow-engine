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

package eu.advance.logistics.flow.engine.comm;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.LoggerFactory;

/**
 * Wraps a java.sql.Connection and provides some useful methods.
 * @author akarnokd, 2011.11.09.
 */
public class JDBCConnection {
	/** The underlying connection. */
	protected final Connection conn;
	/**
	 * Constructor. Initializes the connection.
	 * @param conn the connection
	 */
	public JDBCConnection(Connection conn) {
		this.conn = conn;
	}
	/**
	 * Returns the underlying connection.
	 * @return the connection
	 */
	public Connection getConnection() {
		return conn;
	}
	/**
	 * Commit the current transaction.
	 * @throws SQLException on error
	 */
	public void commit() throws SQLException {
		conn.commit();
	}
	/**
	 * Roll back the current transaction without throwing exception.
	 */
	public void rollbackSilently() {
		try {
			conn.rollback();
		} catch (SQLException ex) {
			LoggerFactory.getLogger(JDBCConnection.class).error(ex.toString(), ex);
		}
	}
}
