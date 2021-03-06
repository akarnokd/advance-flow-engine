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

import java.sql.ResultSet;
import java.sql.SQLException;

import eu.advance.logistics.flow.engine.api.core.Copyable;
import eu.advance.logistics.flow.engine.api.core.HasPassword;
import eu.advance.logistics.flow.engine.api.core.Identifiable;

/**
 * The FTP data source record.
 * @author akarnokd, 2011.09.20.
 */
public class AdvanceFTPDataSource extends AdvanceCreateModifyInfo 
implements XNSerializable, HasPassword, Copyable<AdvanceFTPDataSource>, Identifiable<String> {
	/** The name used by blocks to reference this data source. */
	public String name;
	/** The protocol enumeration. */
	public AdvanceFTPProtocols protocol;
	/** The FTP address. */
	public String address;
	/** The remote base directory. */
	public String remoteDirectory;
	/** The user name used for login. */
	public String userOrKey;
	/**
	 * The password used to login. 
	 * <p>Note that passwords are never returned from the 
	 * control API calls and are always {@code null}.</p> 
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 */
	private char[] password;
	/** The connection should be passive? */
	public boolean passive;
	/** The keystore used to trust the server. */
	public String keyStore;
	/** The login type. */
	public AdvanceLoginType loginType;
	/** The function to create a new instance of this class. */
	public static final Func0<AdvanceFTPDataSource> CREATOR = new Func0<AdvanceFTPDataSource>() {
		@Override
		public AdvanceFTPDataSource invoke() {
			return new AdvanceFTPDataSource();
		}
	};
	/** The function to select a new instance of this class. */
	public static final SQLResult<AdvanceFTPDataSource> SELECT = new SQLResult<AdvanceFTPDataSource>() {
		@Override
		public AdvanceFTPDataSource invoke(ResultSet rs) throws SQLException {
			AdvanceFTPDataSource ftpDataSource = new AdvanceFTPDataSource();
			ftpDataSource.name = rs.getString("name");
			ftpDataSource.protocol = AdvanceFTPProtocols.valueOf(rs.getString("protocol"));
			ftpDataSource.address = rs.getString("address");
			ftpDataSource.remoteDirectory = rs.getString("remote_directory");
			ftpDataSource.userOrKey = rs.getString("user_or_key");
			ftpDataSource.password = AdvanceCreateModifyInfo.getPassword(rs, "password");
			ftpDataSource.passive = rs.getBoolean("passive");
			ftpDataSource.keyStore = rs.getString("keystore");
			ftpDataSource.loginType = AdvanceLoginType.valueOf(rs.getString("login_type"));

			AdvanceCreateModifyInfo.load(rs, ftpDataSource);
			return ftpDataSource;
		}
	};
	@Override
	public void load(XNElement source) {
		name = source.get("name");
		protocol = AdvanceFTPProtocols.valueOf(source.get("protocol"));
		address = source.get("address");
		remoteDirectory = source.get("remote-directory");
		userOrKey = source.get("user-or-key");
		password = getPassword(source, "password");
		passive = source.getBoolean("passive");
		keyStore = source.get("keystore");
		loginType = AdvanceLoginType.valueOf(source.get("login-type"));
		super.load(source);
	}
	@Override
	public void save(XNElement destination) {
		
		destination.set("name", name);
		destination.set("protocol", protocol);
		destination.set("address", address);
		destination.set("remote-directory", remoteDirectory);
		destination.set("user-or-key", userOrKey);
		setPassword(destination, "password", password);
		destination.set("passive", passive);
		destination.set("keystore", keyStore);
		destination.set("login-type", loginType);
		
		super.save(destination);
	}
	@Override
	public AdvanceFTPDataSource copy() {
		AdvanceFTPDataSource result = new AdvanceFTPDataSource();
		
		result.name = name;
		result.protocol = protocol;
		result.address = address;
		result.remoteDirectory = remoteDirectory;
		result.userOrKey = userOrKey;
		result.passive = passive;
		result.password = password != null ? password.clone() : null;
		result.keyStore = keyStore;
		
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
