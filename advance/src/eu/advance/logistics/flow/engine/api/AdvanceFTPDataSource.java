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

/**
 * The FTP data source record.
 * @author karnokd, 2011.09.20.
 */
public class AdvanceFTPDataSource extends AdvanceCreateModifyInfo {
	/** @return the unique identifier. */
	public int id;
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
}
