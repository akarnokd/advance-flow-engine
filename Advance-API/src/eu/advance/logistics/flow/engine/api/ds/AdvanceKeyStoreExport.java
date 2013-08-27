/*
 * Copyright 2010-2013 The Advance EU 7th Framework project consortium
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

package eu.advance.logistics.flow.engine.api.ds;

import hu.akarnokd.reactive4java.base.Func0;
import hu.akarnokd.utils.xml.XNElement;
import hu.akarnokd.utils.xml.XNSerializable;
import eu.advance.logistics.flow.engine.api.core.HasPassword;

/**
 * Request for exporting a certificate from a keystore.
 * @author akarnokd, 2011.09.20.
 */
public class AdvanceKeyStoreExport
implements XNSerializable, HasPassword {
	/** The key store name. */
	public String keyStore;
	/** The key alias. */
	public String keyAlias;
	/** 
	 * The key password.
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 */
	private char[] keyPassword;
	/** Creates a new instance of this class. */
	public static final Func0<AdvanceKeyStoreExport> CREATOR = new Func0<AdvanceKeyStoreExport>() {
		@Override
		public AdvanceKeyStoreExport invoke() {
			return new AdvanceKeyStoreExport();
		}
	};
	@Override
	public void load(XNElement source) {
		keyStore = source.get("keystore");
		keyAlias = source.get("keyalias");
		keyPassword = AdvanceCreateModifyInfo.getPassword(source, "password");
	}
	@Override
	public void save(XNElement destination) {
		destination.set("keystore", keyStore);
		destination.set("keyalias", keyAlias);
		AdvanceCreateModifyInfo.setPassword(destination, "password", keyPassword);
	}
	@Override
	public char[] password() {
		return keyPassword != null ? keyPassword.clone() : null;
	}
	@Override
	public void password(char[] newPassword) {
		keyPassword = newPassword != null ? newPassword.clone() : null;
	}
}
