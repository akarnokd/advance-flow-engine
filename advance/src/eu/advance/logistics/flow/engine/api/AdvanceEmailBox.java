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
 * Represents the connection information to a POP3(s)/IMAP(s) based email-box to 
 * retrieve messages.
 * @author karnokd, 2011.10.10.
 */
public class AdvanceEmailBox extends AdvanceCreateModifyInfo implements 
XSerializable, HasPassword, Copyable<AdvanceEmailBox> {
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
	@Override
	public char[] password() {
		return password != null ? password.clone() : null;
	}

	@Override
	public void password(char[] newPassword) {
		password = newPassword != null ? newPassword.clone() : null;
	}

	@Override
	public void load(XElement source) {
		name = source.get("name");
		receive = AdvanceEmailReceiveProtocols.valueOf(source.get("receive"));
		send = AdvanceEmailSendProtocols.valueOf(source.get("send"));
		sendAddress = source.get("send-address");
		receiveAddress = source.get("receive-address");
		folder = source.get("folder");
		keyStore = source.get("keystore");
		user = source.get("user");
		password = getPassword(source, "password");
		super.load(source);
	}

	@Override
	public void save(XElement destination) {
		destination.set("name", name, "receive", receive, "send", send, "send-address", sendAddress,
				"receive-address", receiveAddress,
				"email", email, "folder", folder, "keystore", keyStore, "user", user);
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
		
		return result;
	}
}
