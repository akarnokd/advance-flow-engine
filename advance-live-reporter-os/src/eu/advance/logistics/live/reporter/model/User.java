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
package eu.advance.logistics.live.reporter.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import hu.akarnokd.utils.crypto.BCrypt;
import hu.akarnokd.utils.database.DB;
import hu.akarnokd.utils.database.SQLResult;

/**
 * The user object.
 * @author karnokd, 2013.09.23.
 */
public class User {
	/** The user identifier. */
	public long id;
	/** The user name. */
	public String name;
	/** The hub. */
	public long hub;
	/** The depot if not null. */
	public Long depot;
	/** Admin user? */
	public boolean admin;
	/** Startup view. */
	public UserView view;
	/** The hashed password. */
	public String password;
	/**
	 * Sets a new password by hashing the given plaintext password.
	 * @param newPlaintextPassword the plaintext password
	 */
	public void setPassword(String newPlaintextPassword) {
		password = BCrypt.hashpw(newPlaintextPassword, BCrypt.gensalt());
	}
	/**
	 * Verifies if the given plaintext password matches the preset hash password.
	 * @param plaintextPassword the plaintext password to check
	 * @return true if the passwords match
	 */
	public boolean verify(String plaintextPassword) {
		return password != null && BCrypt.checkpw(plaintextPassword, password);
	}
	/** Default select. */
	public static final SQLResult<User> SELECT = new SQLResult<User>() {
		@Override
		public User invoke(ResultSet t) throws SQLException {
			User u = new User();
			u.id = t.getLong(1);
			u.name = t.getString(2);
			u.hub = t.getLong(3);
			u.depot = DB.getLong(t, 4);
			u.admin = t.getBoolean(5);
			u.view = UserView.values()[t.getInt(6)];
			u.password = t.getString(7);
			return u;
		}
	};
}
