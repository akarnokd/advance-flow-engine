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

import hu.akarnokd.reactive4java.base.Func0;

import java.text.ParseException;
import java.util.Date;

import org.slf4j.LoggerFactory;

import eu.advance.logistics.flow.engine.api.core.Copyable;
import eu.advance.logistics.flow.engine.api.core.Identifiable;
import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.flow.engine.xml.XSerializable;


/**
 * Properties of a concrete key in a key store.
 * @author akarnokd, 2011.09.20.
 */
public class AdvanceKeyEntry implements XSerializable, Copyable<AdvanceKeyEntry>, Identifiable<String> {
	/** The key type. */
	public AdvanceKeyType type;
	/** The key name. */
	public String name;
	/** The creation date. */
	public Date createdAt;
	/** Creates a new instance of this class. */
	public static final Func0<AdvanceKeyEntry> CREATOR = new Func0<AdvanceKeyEntry>() {
		@Override
		public AdvanceKeyEntry invoke() {
			return new AdvanceKeyEntry();
		}
	};
	@Override
	public AdvanceKeyEntry copy() {
		AdvanceKeyEntry result = new AdvanceKeyEntry();
		result.type = type;
		result.name = name;
		result.createdAt = new Date(createdAt.getTime());
		return result;
	}
	@Override
	public void load(XElement source) {
		type = AdvanceKeyType.valueOf(source.get("type"));
		name = source.get("name");
		try {
			createdAt = XElement.parseDateTime(source.get("created-at"));
		} catch (ParseException ex) {
			LoggerFactory.getLogger(AdvanceKeyEntry.class).error(ex.toString(), ex);
		}
	}
	@Override
	public void save(XElement destination) {
		destination.set("type", type);
		destination.set("name", name);
		destination.set("created-at", XElement.formatDateTime(createdAt));
	}
	@Override
	public String id() {
		return name;
	}
}
