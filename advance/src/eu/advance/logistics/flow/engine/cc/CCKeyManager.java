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

package eu.advance.logistics.flow.engine.cc;

import java.awt.Component;
import java.io.File;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.ds.AdvanceGenerateKey;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyEntry;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStoreExport;

/**
 * Base interface for managing key related operations.
 * @author akarnokd, 2011.10.18.
 */
public interface CCKeyManager {
	/** 
	 * Set the manager's parent component.
	 * @param c the component
	 */
	void setParent(Component c);
	/**
	 * Query the list of keys within the named keystore.
	 * @param keyStore the keystore
	 * @return the list of keys
	 * @throws Exception on error
	 */
	List<AdvanceKeyEntry> queryKeys(String keyStore) throws Exception;
	/**
	 * Query the list of keystores.
	 * @return the list of keystore
	 * @throws Exception on error
	 */
	List<AdvanceKeyStore> queryKeyStores() throws Exception;
	/**
	 * Query a particular keystore.
	 * @param name the name
	 * @return the keystore
	 * @throws Exception on error
	 */
	AdvanceKeyStore queryKeyStore(String name) throws Exception;
	/**
	 * Update a keystore specification.
	 * @param keyStore the keystore
	 * @throws Exception on error
	 */
	void updateKeyStore(AdvanceKeyStore keyStore) throws Exception;
	/**
	 * Delete a keystore.
	 * @param name the name
	 * @throws Exception on error
	 */
	void deleteKeyStore(String name) throws Exception;
	/**
	 * Delete the given keys from the specified keystore.
	 * @param keyStore the target keystore
	 * @param keys the sequence of keys
	 * @throws Exception on error
	 */
	void deleteKeys(String keyStore, Iterable<String> keys) throws Exception;
	/**
	 * Generate key.
	 * @param key the key generation settings
	 * @throws Exception on error
	 */
	void generateKey(AdvanceGenerateKey key) throws Exception;
	/**
	 * Export the given certificate.
	 * @param request the export request
	 * @return the certificate data in textual format
	 * @throws Exception on error
	 */
	String exportCertificate(AdvanceKeyStoreExport request) throws Exception;
	/**
	 * Export the private key.
	 * @param request the export request
	 * @return the private key in textual format
	 * @throws Exception on error
	 */
	String exportKey(AdvanceKeyStoreExport request) throws Exception;
	/**
	 * Import a certificate.
	 * @param request the request
	 * @param data the certificate data
	 * @throws Exception on error
	 */
	void importCertificate(AdvanceKeyStoreExport request, String data) throws Exception;
	/**
	 * Import a private key.
	 * @param request the import settings
	 * @param keyData the key data
	 * @param certData the certificate data
	 * @throws Exception on error
	 */
	void importKey(AdvanceKeyStoreExport request, String keyData, String certData) throws Exception;
	/**
	 * Export an RSA signing request.
	 * @param request the export request
	 * @return the signing request text
	 * @throws Exception on error
	 */
	String exportSigningRequest(AdvanceKeyStoreExport request) throws Exception;
	/**
	 * Import an RSA signing response. 
	 * @param request the import settings
	 * @param data the response text
	 * @throws Exception on error
	 */
	void importSigningResponse(AdvanceKeyStoreExport request, String data) throws Exception;
	/** @return the current directory. */
	@NonNull
	File getCurrentDir();
	/** 
	 * Sets the current directory.
	 * @param dir the new directory 
	 */
	void setCurrentDir(File dir);
}
