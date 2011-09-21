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

import java.net.MalformedURLException;
import java.net.URL;

import eu.advance.logistics.flow.model.XSerializable;
import eu.advance.logistics.xml.typesystem.XElement;

/**
 * The web data source configuration record.
 * @author karnokd, 2011.09.20.
 */
public class AdvanceWebDataSource extends AdvanceCreateModifyInfo implements XSerializable {
	/** The unique identifier of the data source. */
	public int id = Integer.MIN_VALUE;
	/** The data source name as used by the blocks. */
	public String name;
	/** The URL. */
	public URL url;
	/** The login type enumeration. */
	public AdvanceWebLoginType loginType;
	/** The keystore name if the loginType is CERTIFICATE. */
	public String keyStore;
	/** The user or key alias name. */
	public String userOrKeyAlias;
	/**
	 * The password for BASIC login or the key password for the CERTIFICATE.
	 * <p>Note that passwords are never returned from the 
	 * control API calls and are always {@code null}.</p> 
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 * @return  
	 */
	public char[] password;
	@Override
	public void load(XElement source) {
		id = source.getInt("id");
		name = source.get("name");
		try {
			url = new URL(source.get("url"));
		} catch (MalformedURLException ex) {
			// FIXME ignored
		}
		loginType = AdvanceWebLoginType.valueOf(source.get("login-type"));
		keyStore = source.get("keystore");
		userOrKeyAlias = source.get("user-or-key");
		password = getPassword(source, "password");
		
		super.load(source);
	}
	@Override
	public void save(XElement destination) {
		
		destination.set("id", id);
		destination.set("name", name);
		destination.set("url", url);
		destination.set("login-type", loginType);
		destination.set("keystore", keyStore);
		destination.set("user-or-key", userOrKeyAlias);
		setPassword(destination, "password", password);
		
		
		super.save(destination);
	}
}
