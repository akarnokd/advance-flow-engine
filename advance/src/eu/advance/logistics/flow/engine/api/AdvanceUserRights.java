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
 * Enumeration for defining user rights.
 * @author karnokd, 2011.09.19.
 */
public enum AdvanceUserRights {
	/** List users. */
	LIST_USERS,
	/** Create new user. */
	CREATE_USER,
	/** Modify user. */
	MODIFY_USER,
	/** Delete user. */
	DELETE_USER,
	/** List realms. */
	LIST_REALMS,
	/** Create new realm. */
	CREATE_REALM,
	/** Modify properties of a new realm. */
	MODIFY_REALM,
	/** Delete a realm. */
	DELETE_REALM,
	/** Manage keystore. */
	MANAGE_KEYSTORE
	// TODO more rights
}
