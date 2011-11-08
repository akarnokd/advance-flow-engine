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

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.LoggerFactory;

import eu.advance.logistics.flow.engine.api.core.Copyable;
import eu.advance.logistics.flow.engine.api.core.HasPassword;
import eu.advance.logistics.flow.engine.api.core.Identifiable;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializable;

/**
 * The web data source configuration record.
 * @author akarnokd, 2011.09.20.
 */
public class AdvanceWebDataSource extends AdvanceCreateModifyInfo 
implements XSerializable, HasPassword, Copyable<AdvanceWebDataSource>, Identifiable<String> {
	/** The data source name as used by the blocks. */
	public String name;
	/** The URL. */
	public URL url;
	/** The login type enumeration. */
	public AdvanceLoginType loginType;
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
	private char[] password;
	/** The function to create a new instance of this class. */
	public static final Func0<AdvanceWebDataSource> CREATOR = new Func0<AdvanceWebDataSource>() {
		@Override
		public AdvanceWebDataSource invoke() {
			return new AdvanceWebDataSource();
		}
	};
	@Override
	public void load(XElement source) {
		name = source.get("name");
		try {
			url = new URL(source.get("url"));
		} catch (MalformedURLException ex) {
			LoggerFactory.getLogger(AdvanceWebDataSource.class).error(ex.toString(), ex);
		}
		loginType = AdvanceLoginType.valueOf(source.get("login-type"));
		keyStore = source.get("keystore");
		userOrKeyAlias = source.get("user-or-key");
		password = getPassword(source, "password");
		
		super.load(source);
	}
	@Override
	public void save(XElement destination) {
		
		destination.set("name", name);
		destination.set("url", url);
		destination.set("login-type", loginType);
		destination.set("keystore", keyStore);
		destination.set("user-or-key", userOrKeyAlias);
		setPassword(destination, "password", password);
		
		
		super.save(destination);
	}
	@Override
	public AdvanceWebDataSource copy() {
		AdvanceWebDataSource result = new AdvanceWebDataSource();
		
		result.name = name;
		result.url = url;
		result.loginType = loginType;
		result.keyStore = keyStore;
		result.userOrKeyAlias = userOrKeyAlias;
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
	@Override
	public String id() {
		return name;
	}
}
