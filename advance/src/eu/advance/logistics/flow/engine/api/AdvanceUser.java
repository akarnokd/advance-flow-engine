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

import java.util.Set;

import com.google.common.collect.Multimap;

/**
 * User settings.
 * @author karnokd, 2011.09.19.
 */
public class AdvanceUser extends AdvanceCreateModifyInfo {
	/** The user's unique identifier. */
	public int id;
	/** Is the user enabled? */
	public boolean enabled;
	/** The user's name. */
	public String name;
	/** The email address. */
	public String email;
	/** The pager number. */
	public String pager;
	/** The sms number. */
	public String sms;
	/** The date format string. */
	public String dateFormat;
	/** The date-time format string. */
	public String dateTimeFormat;
	/** The number format string. */
	public String numberFormat;
	/** The thousand separator character. */
	public char thousandSeparator;
	/** The decimal separator character. */
	public char decimalSeparator;
	/** Is the user logging in via username/password? */
	public boolean passwordLogin;
	/** 
	 * The password.
	 * <p>Note that passwords are never returned from the 
	 * control API calls and are always {@code null}.</p> 
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 */
	public char[] password;
	/** The keystore where the user certificate is located. */
	public String keyStore;
	/** The certificate alias. */
	public String keyAlias;
	/** Set of enabled user rights. */
	public Set<AdvanceUserRights> rights;
	/**
	 * A multimap of realms and set of rights.
	 */
	public Multimap<String, AdvanceUserRealmRights> realmRights;
}
