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
 * The FTP data source record.
 * @author karnokd, 2011.09.20.
 */
public class AdvanceFTPDataSource extends AdvanceCreateModifyInfo implements XSerializable {
	/** @return the unique identifier. */
	public int id = Integer.MIN_VALUE;
	/** @return the name used by blocks to reference this data source. */
	public String name;
	/** @return the protocol enumeration. */
	public AdvanceFTPProtocols protocol;
	/** @return the FTP address. */
	public String address;
	/** @return the remote base directory. */
	public String remoteDirectory;
	/** @return the user name used to login. */
	public String user;
	/** 
	 * <p>Note that passwords are never returned from the 
	 * control API calls and are always {@code null}.</p> 
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 * @return the password used to login. 
	 */
	public char[] password;
	/** @return the connection should be passive? */
	public boolean passive;
	@Override
	public void load(XElement source) {
		id = source.getInt("id");
		name = source.get("name");
		protocol = AdvanceFTPProtocols.valueOf(source.get("protocol"));
		address = source.get("address");
		remoteDirectory = source.get("remoted-directory");
		user = source.get("user");
		password = getPassword(source, "password");
		passive = "true".equals(source.get("passive"));
		
		super.load(source);
	}
	@Override
	public void save(XElement destination) {
		
		destination.set("id", id);
		destination.set("name", name);
		destination.set("protocol", protocol);
		destination.set("address", address);
		destination.set("remote-directory", remoteDirectory);
		destination.set("user", user);
		setPassword(destination, "password", password);
		destination.set("passive", passive);
		
		super.save(destination);
	}
}
