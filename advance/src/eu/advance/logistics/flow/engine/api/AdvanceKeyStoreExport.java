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
 * Request for exporting a certificate from a keystore.
 * @author karnokd, 2011.09.20.
 */
public interface AdvanceKeyStoreExport {
	/** @return the key store name. */
	String keyStore();
	/** @return the key store master password. */
	char[] keyStorePassword();
	/** @return the key alias. */
	String keyAlias();
	/** @return the key password. */
	char[] keyPassword();
	/**
	 * @param newKeyStore the new key store name
	 */
	void keyStore(String newKeyStore);
	/**
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 * @param newPassword the key store password
	 */
	void keyStorePassword(char[] newPassword);
	/**
	 * @param newKeyAlias the new key alias
	 */
	void keyAlias(String newKeyAlias);
	/**
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 * @param newPassword the new key password
	 */
	void keyPassword(char[] newPassword);
}
