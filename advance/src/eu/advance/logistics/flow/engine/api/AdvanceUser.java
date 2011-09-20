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
public interface AdvanceUser extends AdvanceCreateModifyInfo {
	/** @return The user's unique identifier. */
	int id();
	/** @return is the user enabled? */
	boolean enabled();
	/**
	 * Set the enabled state on the user.
	 * @param newEnabled is it enabled?
	 */
	void enabled(boolean newEnabled);
	/** @return The user's name. */
	String name();
	/**
	 * Set the new name for the user.
	 * @param newName the new name
	 */
	void name(String newName);
	/** @return The email address. */
	String email();
	/**
	 * Set the new email address.
	 * @param newEmail the new email address
	 */
	void email(String newEmail);
	/** @return the pager number. */
	String pager();
	/**
	 * Set the new pager number.
	 * @param newPager the new pager number
	 */
	void pager(String newPager);
	/** @return the sms number. */
	String sms();
	/**
	 * Set the new sms number.
	 * @param newSms the new sms number
	 */
	void sms(String newSms);
	/** @return the date format string. */
	String dateFormat();
	/**
	 * Set the date format string.
	 * @param newDateFormat the new format string
	 */
	void dateFormat(String newDateFormat);
	/** @return the date-time format string. */
	String dateTimeFormat();
	/**
	 * Set the date-time format string.
	 * @param newDateTimeFormat the new format string
	 */
	void dateTimeFormat(String newDateTimeFormat);
	/** @return the number format string. */
	String numberFormat();
	/**
	 * Set the number format string.
	 * @param newNumberFormat the new format string
	 */
	void numberFormat(String newNumberFormat);
	/** @return the thousand separator character. */
	char thousandSeparator();
	/**
	 * Set the new thousand separator character.
	 * @param newSeparator the new separator
	 */
	void thousandSeparator(char newSeparator);
	/** @return the decimal separator character. */
	char decimalSeparator();
	/**
	 * Set the decimal separator character.
	 * @param newSeparator the new separator
	 */
	void decimalSeparator(char newSeparator);
	/** @return is the user logging in via username/password? */
	boolean passwordLogin();
	/**
	 * Set the user login method to password (true) or certificate (false).
	 * @param viaPassword use password?
	 */
	void passwordLogin(boolean viaPassword);
	/** 
	 * <p>Note that passwords are never returned from the 
	 * control API calls and are always {@code null}.</p> 
	 * @return the current password login characters or null if not present. 
	 */
	char[] password();
	/**
	 * <p>Set a new password.</p>
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 * @param newPassword the new password
	 */
	void password(char[] newPassword);
	/** @return the keystore where the user certificate is located. */
	String keyStore();
	/**
	 * Set the keystore where the user certificate is located.
	 * @param newKeystore the new keystore name
	 */
	void keyStore(String newKeystore);
	/** @return the certificate alias. */
	String keyAlias();
	/**
	 * Set the certificate alias.
	 * @param newAlias the new alias
	 */
	void keyAlias(String newAlias);
	/** @return a modifiable set of enabled user rights. */
	Set<AdvanceUserRights> rights();
	/**
	 * @return a modifiable multimap of realms and set of rights.
	 */
	Multimap<String, AdvanceUserRealmRights> realmRights();
}
