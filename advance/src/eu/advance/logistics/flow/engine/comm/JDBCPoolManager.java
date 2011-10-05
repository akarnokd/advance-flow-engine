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

package eu.advance.logistics.flow.engine.comm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.advance.logistics.flow.engine.api.AdvanceJDBCDataSource;

/**
 * @author karnokd, 2011.10.05.
 *
 */
public class JDBCPoolManager implements PoolManager<Connection> {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(JDBCPoolManager.class);
	/** The data source for the connection configuration. */
	private final AdvanceJDBCDataSource ds;
	/**
	 * Constructor with the JDBC conenction settings.
	 * @param ds the connection settings
	 */
	public JDBCPoolManager(AdvanceJDBCDataSource ds) {
		this.ds = ds;
	}
	@Override
	public Connection create() throws Exception {
		Class.forName(ds.driver);
		Connection conn = DriverManager.getConnection(ds.url, ds.user, new String(ds.password()));
		conn.setAutoCommit(false);
		return conn;
	}
	@Override
	public boolean verify(Connection obj) throws Exception {
		try {
			try {
				return obj.isValid(0);
			} catch (NoSuchMethodError err) {
				LOG.warn("Driver " + ds.driver + " does not support JDBC 4.0");
				if (!obj.isClosed()) {
					ResultSet rs = obj.getMetaData().getTables(null, null, null, null);
					try {
						if (rs.next()) {
							rs.getString(1);
							return true;
						}
					} finally {
						rs.close();
					}
				}
			}
		} catch (SQLException ex) {
			LOG.error(ex.toString(), ex);
		}
		return false;
	}
	@Override
	public void close(Connection obj) throws Exception {
		obj.close();
	}
	
}
