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

package eu.advance.logistics.flow.engine;

import eu.advance.logistics.flow.engine.api.AdvanceCreateModifyInfo;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializable;

/**
 * The listener configuration record for the engine.
 * @author karnokd, 2011.09.29.
 */
public class AdvanceListenerConfig implements XSerializable {
	/** The port number where the basic HTTPS listener should be. */
	public int basicPort;
	/** The port number where the certificate HTTPS listener should be. */
	public int certificatePort;
	/** The keystore name. */
	public String serverKeyStore;
	/** The server certificate key alias. */
	public String serverKeyAlias;
	/** The server certificate key password. */
	public char[] serverKeyPassword;
	/** The keystore where the client certificates are located. */
	public String clientKeyStore;
	@Override
	public void load(XElement source) {
		basicPort = source.getInt("basic-auth-port");
		certificatePort = source.getInt("cert-auth-port");
		serverKeyStore = source.get("server-keystore");
		serverKeyAlias = source.get("server-keyalias");
		serverKeyPassword = AdvanceCreateModifyInfo.getPassword(source, "server-password");
		clientKeyStore = source.get("client-keystore");
	}
	@Override
	public void save(XElement destination) {
		destination.set("basic-auth-port", basicPort);
		destination.set("cert-auth-port", certificatePort);
		destination.set("server-keystore", serverKeyStore);
		destination.set("server-keyalias", serverKeyAlias);
		AdvanceCreateModifyInfo.setPassword(destination, "server-password", serverKeyPassword);
		destination.set("client-keystore", clientKeyStore);
	}
}
