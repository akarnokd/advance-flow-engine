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

import hu.akarnokd.reactive4java.base.Func0;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializable;

/**
 * A record representing information about local or remote key stores.
 * @author karnokd, 2011.09.20.
 */
public class AdvanceKeyStore extends AdvanceCreateModifyInfo 
implements XSerializable, HasPassword, Copyable<AdvanceKeyStore> {
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
	private char[] password;
	/** The function to create a new instance of this class. */
	public static final Func0<AdvanceKeyStore> CREATOR = new Func0<AdvanceKeyStore>() {
		@Override
		public AdvanceKeyStore invoke() {
			return new AdvanceKeyStore();
		}
	};
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
	@Override
	public AdvanceKeyStore copy() {
		AdvanceKeyStore result = new AdvanceKeyStore();
		
		result.name = name;
		result.location = location;
		result.password = password != null ? password.clone() : null;
		
		assignTo(result);
		
		return result;
	}
	@Override
	public char[] password() {
		return password != null ? password.clone() : null;
	}
	@Override
	public void password(char[] newPassword) {
		password = newPassword != null ? newPassword.clone() : null;
	}
}
