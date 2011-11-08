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

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;

import org.slf4j.LoggerFactory;

import eu.advance.logistics.flow.engine.util.Base64;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializable;

/**
 * The creation/modification time and user information.
 * @author akarnokd, 2011.09.19.
 */
public class AdvanceCreateModifyInfo implements XSerializable {
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
	public static char[] getPassword(XElement source, String name) {
		String pwd = source.get(name);
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
	 * Encodes the password into the given {@code source} element under the given {@code name}
	 * attribute.
	 * @param destination the source element
	 * @param name the attribute name
	 * @param password the password characters
	 */
	public static void setPassword(XElement destination, String name, char[] password) {
		if (password != null) {
			destination.set(name, Base64.encodeBytes(new String(password).getBytes(Charset.forName("UTF-8"))));
		} else {
			destination.set(name, null);
		}
		
	}
	@Override
	public void load(XElement source) {
		String s = source.get("created-at");
		if (s != null && !s.isEmpty()) {
			try {
				createdAt = XElement.parseDateTime(s);
			} catch (ParseException ex) {
				LoggerFactory.getLogger(AdvanceCreateModifyInfo.class).error(ex.toString(), ex);
			}
		}
		createdBy = source.get("created-by");
		s = source.get("modified-at");
		if (s != null && !s.isEmpty()) {
			try {
				modifiedAt = XElement.parseDateTime(s);
			} catch (ParseException ex) {
				LoggerFactory.getLogger(AdvanceCreateModifyInfo.class).error(ex.toString(), ex);
			}
		}
		modifiedBy = source.get("modified-by");
	}
	@Override
	public void save(XElement destination) {
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
		XElement root = new XElement(getClass().getSimpleName());
		save(root);
		return root.toString();
	}
}
