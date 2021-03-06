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
import hu.akarnokd.utils.database.SQLResult;
import hu.akarnokd.utils.xml.XNElement;
import hu.akarnokd.utils.xml.XNSerializable;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.LoggerFactory;

import eu.advance.logistics.flow.engine.api.core.Copyable;
import eu.advance.logistics.flow.engine.api.core.HasPassword;
import eu.advance.logistics.flow.engine.api.core.Identifiable;

/**
 * The web data source configuration record.
 * @author akarnokd, 2011.09.20.
 */
public class AdvanceWebDataSource extends AdvanceCreateModifyInfo 
implements XNSerializable, HasPassword, Copyable<AdvanceWebDataSource>, Identifiable<String> {
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
	/** The function to select a new instance of this class. */
	public static final SQLResult<AdvanceWebDataSource> SELECT = new SQLResult<AdvanceWebDataSource>() {
		@Override
		public AdvanceWebDataSource invoke(ResultSet rs) throws SQLException {
			AdvanceWebDataSource wds = new AdvanceWebDataSource();
			wds.name = rs.getString("name");
			try	{
				wds.url = new URL(rs.getString("url"));
			} catch (MalformedURLException e) {
				throw new SQLException("Malformed URL.. ", e);
			}
			wds.loginType = AdvanceLoginType.valueOf(rs.getString("login_type"));
			wds.keyStore = rs.getString("keystore");
			wds.userOrKeyAlias = rs.getString("user_or_key");
			wds.password = AdvanceCreateModifyInfo.getPassword(rs, "password");

			AdvanceCreateModifyInfo.load(rs, wds);            
			return wds;
		}
	};
	@Override
	public void load(XNElement source) {
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
	public void save(XNElement destination) {
		
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
