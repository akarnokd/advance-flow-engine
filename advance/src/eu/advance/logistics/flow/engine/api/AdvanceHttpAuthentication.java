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

import java.security.KeyStore;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.api.core.HasPassword;
import eu.advance.logistics.flow.engine.api.ds.AdvanceLoginType;

/**
 * The record holding authentication information for basic or certificate based HTTP(s) connection.
 * @author  akarnokd, 2011.09.27.
 */
public class AdvanceHttpAuthentication implements HasPassword {
	/** The login type. */
	@NonNull
	public AdvanceLoginType loginType;
	/** The certificate store to verify the HTTPS server. */
	@Nullable
	public KeyStore certStore;
	/** The authentication store to send certificate credentials to the HTTPS server. */
	@Nullable
	public KeyStore authStore;
	/** The user name for basic authentication or the key alias for certificate credentials. */
	@NonNull
	public String name;
	/** The password for the basic authentication or the key password for certificate credentials. */
	@NonNull
	private char[] password;
	@Override
	public char[] password() {
		return password != null ? password.clone() : null;
	}
	@Override
	public void password(char[] newPassword) {
		this.password = newPassword != null ? newPassword.clone() : null;
	}
}