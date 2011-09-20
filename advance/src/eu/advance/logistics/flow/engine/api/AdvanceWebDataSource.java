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

import java.net.URL;

/**
 * The web data source configuration record.
 * @author karnokd, 2011.09.20.
 */
public class AdvanceWebDataSource extends AdvanceCreateModifyInfo {
	/** The unique identifier of the data source. */
	public int id;
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
}
