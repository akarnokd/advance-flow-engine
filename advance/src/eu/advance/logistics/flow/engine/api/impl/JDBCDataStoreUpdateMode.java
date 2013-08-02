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

package eu.advance.logistics.flow.engine.api.impl;

/**
 * Specifies how an insert-or-update like operation should work in
 * the JDBCDataStore.
 * @author akarnokd, 2013.08.01.
 */
public enum JDBCDataStoreUpdateMode {
	/** 
	 * Execute a select to see if the object exists, then do
	 * an INSERT or UPDATE.
	 */
	SELECT_DECIDES,
	/**
	 * Execute an INSERT and if there was a key violation, execute
	 * the UPDATE.
	 */
	INSERT_BEFORE_UPDATE,
	/**
	 * Execute an update and if the record modification count is zero,
	 * execute an INSERT.
	 */
	UPDATE_BEFORE_INSERT,
	/**
	 * Use the MySQL's REPLACE syntax.
	 */
	MYSQL_REPLACE,
	/**
	 * Use Oracle's MERGE syntax.
	 */
	ORACLE_MERGE,
	/**
	 * Use the MSSQL's MERGE syntax.
	 */
	MSSQL_MERGE
}
