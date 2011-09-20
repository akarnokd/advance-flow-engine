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

import java.net.URI;
import java.net.URL;

/**
 * Describes a SOAP communication channel.
 * @author karnokd, 2011.09.20.
 */
public interface AdvanceSOAPChannel extends AdvanceCreateModifyInfo {
	/** @return the unique channel identifier. */
	int id();
	/** @return the name used to reference this channel from blocks. */
	String name();
	/** @return the endpoint URL. */
	URL endpoint();
	/** @return the target object URI. */
	URI targetObject();
	/** @return the remote method. */
	String method();
	/** @return the communication should be encrypted. */
	boolean encrypted();
	/** @return the keystore for the encryption key. */
	String keyStore();
	/** @return the key alias for the encryption. */
	String keyAlias();
	/**
	 * @param newName the new name
	 */
	void name(String newName);
	/**
	 * @param newEndpoint the new endpoint
	 */
	void endpoint(URL newEndpoint);
	/**
	 * @param newTargetObject the new target object
	 */
	void targetObject(URI newTargetObject);
	/**
	 * @param newMethod the new method.
	 */
	void method(String newMethod);
	/**
	 * @param newEncrypted the new encrypted settings
	 */
	void encrypted(boolean newEncrypted);
	/**
	 * @param newKeystore the new keystore
	 */
	void keyStore(String newKeystore);
	/**
	 * @param newKeyAlias the new key alias
	 */
	void keyAlias(String newKeyAlias);
}
