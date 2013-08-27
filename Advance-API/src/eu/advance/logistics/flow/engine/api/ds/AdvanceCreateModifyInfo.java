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

package eu.advance.logistics.flow.engine.api.ds;

import hu.akarnokd.utils.Base64;
import hu.akarnokd.utils.xml.XNElement;
import hu.akarnokd.utils.xml.XNSerializable;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

import org.slf4j.LoggerFactory;

/**
 * The creation/modification time and user information.
 * @author akarnokd, 2011.09.19.
 */
public class AdvanceCreateModifyInfo implements XNSerializable {
	/** The creation timestamp of the object. */
	public Date createdAt;
	/** The last modification timestamp of the object. */
	public Date modifiedAt;
	/** The user who created the object. */
	public String createdBy;
	/** The user who modified the object the last time. */
	public String modifiedBy;
	/**
	 * Returns the password characters from the encoded attribute in the given source element.
	 * @param source the source element
	 * @param name the attribute name
	 * @return the password or null if no password
	 */
	public static char[] getPassword(XNElement source, String name) {
		String pwd = source.get(name);
		return decodePassword(pwd);
	}
	/**
	 * Decodes a Base64 encoded password.
	 * @param pwd the raw password
	 * @return the password
	 */
	protected static char[] decodePassword(String pwd) {
		if (pwd != null) {
			try {
				return new String(Base64.decode(pwd), Charset.forName("UTF-8")).toCharArray();
			} catch (IOException ex) {
				LoggerFactory.getLogger(AdvanceCreateModifyInfo.class).error(ex.toString(), ex);
			}
		}
		return null;
	}
	/**
	 * Retrieves a Base64 encoded password from the resultset.
	 * @param rs the result set
	 * @param name the column name
	 * @return the password characters or null
	 * @throws SQLException on error
	 */
	public static char[] getPassword(ResultSet rs, String name) throws SQLException {
		String pwd = rs.getString(name);
		return decodePassword(pwd);
	}
	/**
	 * Retrieves a Base64 encoded password from the resultset.
	 * @param rs the result set
	 * @param index the column index
	 * @return the password characters or null
	 * @throws SQLException on error
	 */
	public static char[] getPassword(ResultSet rs, int index) throws SQLException {
		String pwd = rs.getString(index);
		return decodePassword(pwd);
	}
	/**
	 * Encodes the password into the given {@code source} element under the given {@code name}
	 * attribute.
	 * @param destination the source element
	 * @param name the attribute name
	 * @param password the password characters
	 */
	public static void setPassword(XNElement destination, String name, char[] password) {
		if (password != null) {
			destination.set(name, Base64.encodeBytes(new String(password).getBytes(Charset.forName("UTF-8"))));
		} else {
			destination.set(name, null);
		}
		
	}
	/**
	 * Load the create-modify information from the resultset.
	 * @param rs the resultset
	 * @param out the target object
	 * @throws SQLException on error
	 */
	public static void load(ResultSet rs, AdvanceCreateModifyInfo out) throws SQLException {
		out.createdAt = rs.getTimestamp("created_at");
		out.modifiedAt = rs.getTimestamp("modified_at");
		out.createdBy = rs.getString("created_by");
		out.modifiedBy = rs.getString("modified_by");
	}
	@Override
	public void load(XNElement source) {
		String s = source.get("created-at");
		if (s != null && !s.isEmpty()) {
			try {
				createdAt = XNElement.parseDateTime(s);
			} catch (ParseException ex) {
				LoggerFactory.getLogger(AdvanceCreateModifyInfo.class).error(ex.toString(), ex);
			}
		}
		createdBy = source.get("created-by");
		s = source.get("modified-at");
		if (s != null && !s.isEmpty()) {
			try {
				modifiedAt = XNElement.parseDateTime(s);
			} catch (ParseException ex) {
				LoggerFactory.getLogger(AdvanceCreateModifyInfo.class).error(ex.toString(), ex);
			}
		}
		modifiedBy = source.get("modified-by");
	}
	@Override
	public void save(XNElement destination) {
		destination.set("created-at", createdAt);
		destination.set("created-by", createdBy);
		destination.set("modified-at", modifiedAt);
		destination.set("modified-by", modifiedBy);
	}
	/**
	 * Assign the administrative values to the other record.
	 * @param other the other record
	 */
	public void assignTo(AdvanceCreateModifyInfo other) {
		other.createdAt = createdAt != null ? new Date(createdAt.getTime()) : null;
		other.createdBy = createdBy;
		other.modifiedAt = modifiedAt != null ? new Date(modifiedAt.getTime()) : null;
		other.modifiedBy = modifiedBy;
	}
	@Override
	public String toString() {
		XNElement root = new XNElement(getClass().getSimpleName());
		save(root);
		return root.toString();
	}
}
