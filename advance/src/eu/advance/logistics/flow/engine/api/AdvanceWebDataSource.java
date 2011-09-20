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
public interface AdvanceWebDataSource extends AdvanceCreateModifyInfo {
	/** @return the unique identifier of the data source. */
	int id();
	/** @return the data source name as used by the blocks. */
	String name();
	/** @return the URL. */
	URL url();
	/** @return the login type enumeration. */
	AdvanceWebLoginType loginType();
	/** @return the keystore name if the loginType is CERTIFICATE. */
	String keyStore();
	/** @return the user or key alias name. */
	String userOrKeyAlias();
	/**
	 * <p>Note that passwords are never returned from the 
	 * control API calls and are always {@code null}.</p> 
	 * @return the password for BASIC login or the key password for the CERTIFICATE. 
	 */
	char[] password();
	/** 
	 * @param newName the new name
	 */
	void name(String newName);
	/**
	 * @param newUrl the new URL
	 */
	void url(URL newUrl);
	/**
	 * @param newLoginType the login type
	 */
	void loginType(AdvanceWebLoginType newLoginType);
	/**
	 * @param newValue the user or key alias for the login
	 */
	void userOrKeyAlias(String newValue);
	/**
	 * <p>Set a new password.</p>
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 * @param newPassword the new password
	 */
	void password(char[] newPassword);
}
