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
package eu.advance.logistics.live.reporter.db;

import hu.akarnokd.utils.database.DB;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.live.reporter.model.User;

/**
 * Database methods for managing users.
 * @author karnokd, 2013.09.23.
 */
public final class UserDB {
	/** Database class. */
	private UserDB() { }
	/** The logger. */
	private static final Logger LOG = LoggerFactory.getLogger(UserDB.class);
	/**
	 * Returns an user with the given namee.
	 * @param name the user name
	 * @return the user object or null if no such user
	 */
	@Nullable
	public static User getUser(String name) {
		try (DB db = DB.connect()) {
			return db.querySingle(
					"SELECT id, name, hub, depot, admin, default_view, password FROM users WHERE name = ? ",
					User.SELECT, name);
		} catch (IOException | SQLException ex) {
			LOG.error(ex.toString(), ex);
			return null;
		}
	}
	/**
	 * Returns an user with the given namee.
	 * @param id the user's identifier
	 * @return the user object or null if no such user
	 */
	@Nullable
	public static User getUser(long id) {
		try (DB db = DB.connect()) {
			return db.querySingle(
					"SELECT id, name, hub, depot, admin, default_view, password FROM users WHERE id = ? ",
					User.SELECT, id);
		} catch (IOException | SQLException ex) {
			LOG.error(ex.toString(), ex);
			return null;
		}
	}
	/**
	 * Returns all users.
	 * @return the list of users
	 */
	public static List<User> getAll() {
		try (DB db = DB.connect()) {
			return db.query("SELECT id, name, hub, depot, admin, default_view, password "
					+ "FROM users", User.SELECT);
		} catch (IOException | SQLException ex) {
			LOG.error(ex.toString(), ex);
			return new ArrayList<>();
		}
	}
	/**
	 * Save the given user object.
	 * @param u the user object
	 */
	public static void saveUser(@NonNull User u) {
		try (DB db = DB.connect()) {
			if (u.id == 0L) {
				u.id = db.insertAuto("INSERT INTO users "
						+ "(name, hub, depot, admin, default_view, password) "
						+ "VALUES (?, ?, ?, ?, ?, ?)",
						u.name, DB.maybeNull(u.hub), DB.maybeNull(u.depot), 
						u.admin, u.view.ordinal(), u.password);
			} else {
				db.update("UPDATE users SET "
						+ "name = ?, hub = ?, depot = ?, admin = ?, default_view = ?, password = ? "
						+ "WHERE id = ? ", 
						u.name, DB.maybeNull(u.hub), DB.maybeNull(u.depot), 
						u.admin, u.view.ordinal(), u.password, u.id);
			}
			db.commit();
		} catch (IOException | SQLException ex) {
			LOG.error(ex.toString(), ex);
		}
	}

	/**
	 * Delete the given user object.
	 * @param userId the user identifier
	 */
	public static void deleteUser(long userId) {
		try (DB db = DB.connect()) {
			db.update("DELETE FROM users WHERE id = ?", userId);
			db.commit();
		} catch (IOException | SQLException ex) {
			LOG.error(ex.toString(), ex);
		}
	}
}
