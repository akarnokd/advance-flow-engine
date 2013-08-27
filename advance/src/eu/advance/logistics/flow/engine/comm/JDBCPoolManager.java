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

import hu.akarnokd.utils.database.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.PoolManager;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJDBCDataSource;

/**
 * @author akarnokd, 2011.10.05.
 *
 */
public class JDBCPoolManager implements PoolManager<JDBCConnection> {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(JDBCPoolManager.class);
	/** The data source for the connection configuration. */
	private final AdvanceJDBCDataSource ds;
	/**
	 * Constructor with the JDBC connection settings.
	 * @param ds the connection settings
	 */
	public JDBCPoolManager(AdvanceJDBCDataSource ds) {
		this.ds = ds.copy();
	}
	@Override
	public JDBCConnection create() throws Exception {
		Class.forName(ds.driver);
		char[] pw = ds.password();
		Connection conn = DriverManager.getConnection(ds.url, ds.user, new String(pw != null ? pw : new char[0]));
		conn.setAutoCommit(false);
		return new JDBCConnection(DB.connect(conn));
	}
	@Override
	public boolean verify(JDBCConnection obj) throws Exception {
		try {
			try {
				return obj.db.isValid(0);
			} catch (NoSuchMethodError err) {
				LOG.warn("Driver " + ds.driver + " does not support JDBC 4.0");
				if (!obj.db.isClosed()) {
					ResultSet rs = obj.db.getMetaData().getTables(null, null, null, null);
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
	public void close(JDBCConnection obj) throws Exception {
		obj.db.close();
	}
	/**
	 * Test if the supplied data source can be accessed.
	 * @param ds the datasource
	 * @return the test results
	 */
	public static String test(@NonNull AdvanceJDBCDataSource ds) {
		JDBCPoolManager mgr = new JDBCPoolManager(ds);
		try {
			JDBCConnection conn = mgr.create();
			try {
				if (mgr.verify(conn)) {
					return "";
				}
				return "Verification failed on JDBC connection due unknown reasons";
			} finally {
				mgr.close(conn);
			}
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
			return ex.toString();
		}
	}
}
