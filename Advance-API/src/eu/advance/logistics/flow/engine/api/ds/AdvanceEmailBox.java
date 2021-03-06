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
 * Represents the connection information to a POP3(s)/IMAP(s) based email-box to 
 * retrieve messages.
 * @author akarnokd, 2011.10.10.
 */
public class AdvanceEmailBox extends AdvanceCreateModifyInfo implements 
XNSerializable, HasPassword, Copyable<AdvanceEmailBox>, Identifiable<String> {
	/** The identifier for referencing this box. */
	public String name;
	/** The receive protocol. */
	public AdvanceEmailReceiveProtocols receive;
	/** The send protocol. */
	public AdvanceEmailSendProtocols send;
	/** The login type. */
	public AdvanceLoginType login;
	/** The send address[:port]. */
	public String sendAddress;
	/** The send address[:port]. */
	public String receiveAddress;
	/** The folder name on the server. */
	public String folder;
	/** The sender's email address. */
	public String email;
	/** The keystore for certificate authentication. */
	public String keyStore;
	/** The user name or key alias for authentication. */
	public String user;
	/** The user password or key password for authentication. */
	private char[] password;
	/**
	 * The function to create a new instance of this class.
	 */
	public static final Func0<AdvanceEmailBox> CREATOR = new Func0<AdvanceEmailBox>() {
		@Override
		public AdvanceEmailBox invoke() {
			return new AdvanceEmailBox();
		}
	};
	/** The function to select a new instance of this class. */
	public static final SQLResult<AdvanceEmailBox> SELECT = new SQLResult<AdvanceEmailBox>() {
		@Override
		public AdvanceEmailBox invoke(ResultSet rs) throws SQLException	{
			AdvanceEmailBox emailBox = new AdvanceEmailBox();
			emailBox.name = rs.getString("name");
			emailBox.receive = AdvanceEmailReceiveProtocols.valueOf(rs.getString("receive"));
			emailBox.send = AdvanceEmailSendProtocols.valueOf(rs.getString("send"));
			emailBox.login = AdvanceLoginType.valueOf(rs.getString("login"));
			emailBox.sendAddress = rs.getString("send_address");
			emailBox.receiveAddress = rs.getString("receive_address");
			emailBox.folder = rs.getString("folder");
			emailBox.email = rs.getString("email");
			emailBox.keyStore = rs.getString("keystore");
			emailBox.user = rs.getString("user_or_key");
			emailBox.password = AdvanceCreateModifyInfo.getPassword(rs, "password");

			AdvanceCreateModifyInfo.load(rs, emailBox);
			return emailBox;
		}

	};
	@Override
	public char[] password() {
		return password != null ? password.clone() : null;
	}

	@Override
	public void password(char[] newPassword) {
		password = newPassword != null ? newPassword.clone() : null;
	}

	@Override
	public void load(XNElement source) {
		name = source.get("name");
		receive = AdvanceEmailReceiveProtocols.valueOf(source.get("receive"));
		send = AdvanceEmailSendProtocols.valueOf(source.get("send"));
		sendAddress = source.get("send-address");
		receiveAddress = source.get("receive-address");
		folder = source.get("folder");
		keyStore = source.get("keystore");
		user = source.get("user");
		password = getPassword(source, "password");
		login = AdvanceLoginType.valueOf(source.get("login-type"));
		email = source.get("email");
		super.load(source);
	}

	@Override
	public void save(XNElement destination) {
		destination.set("name", name, "receive", receive, "send", send, "send-address", sendAddress,
				"receive-address", receiveAddress,
				"email", email, "folder", folder, "keystore", keyStore, "user", user, "login-type", login);
		setPassword(destination, "password", password);
		super.save(destination);
	}
	@Override
	public AdvanceEmailBox copy() {
		AdvanceEmailBox result = new AdvanceEmailBox();
		
		result.name = name;
		result.receive = receive;
		result.send = send;
		result.sendAddress = sendAddress;
		result.receiveAddress = receiveAddress;
		result.folder = folder;
		result.email = email;
		result.keyStore = keyStore;
		result.user = user;
		result.password = password();
		result.login = login;
		assignTo(result);
		
		return result;
	}
	@Override
	public String id() {
		return name;
	}
}
