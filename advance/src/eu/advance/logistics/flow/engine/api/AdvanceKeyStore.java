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

import eu.advance.logistics.flow.model.XSerializable;
import eu.advance.logistics.xml.typesystem.XElement;

/**
 * A record representing information about local or remote key stores.
 * @author karnokd, 2011.09.20.
 */
public class AdvanceKeyStore extends AdvanceCreateModifyInfo implements XSerializable {
	/** The key store name. */
	public String name;
	/** The key store location on disk. */
	public String location;
	/** 
	 * The password.
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 * <p>Note that passwords are never returned from the 
	 * control API calls and are always {@code null}.</p> 
	 * @return the password. 
	 */
	public char[] password;
	@Override
	public void load(XElement source) {
		name = source.get("name");
		location = source.get("location");
		password = getPassword(source, "password");
		super.load(source);
	}
	@Override
	public void save(XElement destination) {
		destination.set("name", name);
		destination.set("location", location);
		setPassword(destination, "password", password);
		super.save(destination);
	}
}
