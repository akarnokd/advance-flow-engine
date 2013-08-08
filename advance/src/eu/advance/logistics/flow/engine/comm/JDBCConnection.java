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

import hu.akarnokd.reactive4java.base.Action1E;
import hu.akarnokd.reactive4java.base.Func1E;
import hu.akarnokd.utils.database.DB;
import hu.akarnokd.utils.database.SQLAction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Wraps a java.sql.Connection and provides some useful methods.
 * @author akarnokd, 2011.11.09.
 */
public class JDBCConnection {
	/** The underlying connection. */
	protected final DB db;
	/**
	 * Constructor. Initializes the connection.
	 * @param db the database
	 */
	public JDBCConnection(DB db) {
		this.db = db;
	}
	/**
	 * Commit the current transaction.
	 * @throws SQLException on error
	 */
	public void commit() throws SQLException {
		db.commit();
	}
	/**
	 * Roll back the current transaction without throwing exception.
	 */
	public void rollbackSilently() {
		db.rollback();
	}
	/**
	 * Sets a single parameter as string.
	 */
	public static final SQLAction<String> DELETE_BY_NAME = new SQLAction<String>() {
		@Override
		public void invoke(PreparedStatement pstmt, String s) throws SQLException {
			pstmt.setString(1, s);
		}
	};
	/**
	 * Execute a simple, parametric update-like SQL.
	 * @param sql the SQL query
	 * @param params the parameters
	 * @throws SQLException on error
	 */
	public void update(CharSequence sql, Object... params) throws SQLException {
		db.update(sql, params);
	}
	/**
	 * Executes a parametric query and uses the unmarshaller function to
	 * extract objects from the resultset.
	 * @param <T> the result element type
	 * @param sql the SQL query
	 * @param unmarshaller the unmarshaller
	 * @param params the parameters
	 * @return the list of objects from the query
	 * @throws SQLException on error
	 */
	public <T> List<T> query(CharSequence sql, 
			Func1E<? super ResultSet, ? extends T, ? extends SQLException> unmarshaller, 
			Object... params) throws SQLException {
		return db.query(sql, unmarshaller, params);
	}
	/**
	 * Executes a parametric query and uses the unmarshaller function to
	 * extract objects from the resultset.
	 * @param sql the SQL query
	 * @param unmarshaller the unmarshaller
	 * @param params the parameters
	 * @throws SQLException on error
	 */
	public void query(CharSequence sql, 
			Action1E<? super ResultSet, ? extends SQLException> unmarshaller, 
			Object... params) throws SQLException {
		db.query(sql, unmarshaller, params);
	}
	/** @return the underlying database handler. */
	public DB db() {
		return db;
	}
}
