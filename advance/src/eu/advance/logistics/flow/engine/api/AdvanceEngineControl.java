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

import hu.akarnokd.reactive4java.reactive.Observable;

import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.List;

import eu.advance.logistics.flow.engine.AdvanceBlockDiagnostic;
import eu.advance.logistics.flow.engine.AdvanceParameterDiagnostic;
import eu.advance.logistics.flow.engine.error.AdvanceCompilationError;
import eu.advance.logistics.flow.engine.model.AdvanceBlockRegistryEntry;
import eu.advance.logistics.flow.engine.model.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * <p>The API for interacting with the ADVANCE Flow Engine remotely.</p>
 * <p>The API does not have separate {@code insertXYZ} type methods to create
 * new instances of various objects. 
 * The {@code Integer.MIN_VALUE} used as the unique identifier (see {@code id fields} will represent
 * the request to create a new object. Once they were created, the proper non-negative
 * unique identifier replaces this value.</p>
 * @author karnokd, 2011.09.19.
 */
public interface AdvanceEngineControl {
	/**
	 * Connect to the target ADVANCE Flow Engine via username/password pair.
	 * @param target the target URI
	 * @param userName the user name
	 * @param password the password characters
	 * @return the token representing the connection
	 * @throws IOException if the network connection fails
	 * @throws AdvanceControlException if the username/password is incorrect
	 */
	AdvanceControlToken login(URI target, String userName, char[] password)
	throws IOException, AdvanceControlException;
	/**
	 * Connect to the target ADVANCE Flow Engine via client certificate.
	 * @param target the target URI
	 * @param keyStore the keystore instance
	 * @param keyAlias the client certificate alias
	 * @param keyPassword the client certificate password
	 * @return the token representing the connection
	 * @throws IOException if the network connection fails
	 * @throws AdvanceControlException if the authentication fails
	 * @throws KeyStoreException if a problem arises when accessing the certificate
	 */
	AdvanceControlToken login(URI target, KeyStore keyStore, String keyAlias, char[] keyPassword)
	throws IOException, AdvanceControlException, KeyStoreException;
	/**
	 * Retrieve a list of supported block types.
	 * @param token the connection token
	 * @return the list of supported block types
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the blocks.
	 */
	List<AdvanceBlockRegistryEntry> queryBlocks(AdvanceControlToken token) 
	throws IOException, AdvanceControlException;
	/**
	 * Delete a key entry from a keystore.
	 * @param token the connection token
	 * @param keyStore the key store name
	 * @param keyAlias the key alias
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete a key
	 */
	void deleteKeyEntry(AdvanceControlToken token, String keyStore, String keyAlias) throws IOException, AdvanceControlException;;
	/**
	 * Generate a new key with the given properties.
	 * @param token the connection token
	 * @param key the key generation properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to generate a key
	 */
	void generateKey(AdvanceControlToken token, AdvanceGenerateKey key) throws IOException, AdvanceControlException;
	/**
	 * Export a certificate from a designated key store.
	 * @param token the connection token
	 * @param request represents the key store and key alias to export
	 * @return the certificate in textual CER format.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to export
	 */
	String exportCertificate(AdvanceControlToken token, AdvanceKeyStoreExport request) throws IOException, AdvanceControlException;
	/**
	 * Export a private key from a designated key store.
	 * @param token the connection token
	 * @param request represents the key store and key alias to export
	 * @return the private key in textual PEM format.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to export
	 */
	String exportPrivateKey(AdvanceControlToken token, AdvanceKeyStoreExport request) throws IOException, AdvanceControlException;
	/**
	 * Import a certificate into a designated key store.
	 * @param token the connection token
	 * @param request represents the key store and key alias to import
	 * @param data the certificate in textual CER format.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to export
	 */
	void importCertificate(AdvanceControlToken token, AdvanceKeyStoreExport request, String data) throws IOException, AdvanceControlException;
	/**
	 * Import a private key into a designated key store.
	 * @param token the connection token
	 * @param request represents the key store and key alias to import
	 * @param keyData the key in textual PEM format.
	 * @param certData the certificate int textual CER format
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to export
	 */
	void importPrivateKey(AdvanceControlToken token, 
			AdvanceKeyStoreExport request, String keyData, String certData) throws IOException, AdvanceControlException;
	/**
	 * Export a signing request of the given private key.
	 * @param token the connection token
	 * @param request represents the key store and key alias to export
	 * @return the signing request in textual format.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to export
	 */
	String exportSigningRequest(AdvanceControlToken token, AdvanceKeyStoreExport request) throws IOException, AdvanceControlException;
	/**
	 * Import a signing response into the designated key store.
	 * @param token the connection token
	 * @param request represents the key store and key alias to export
	 * @param data the signing response in textual format.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to export
	 */
	void importSigningResponse(AdvanceControlToken token, AdvanceKeyStoreExport request, String data) throws IOException, AdvanceControlException;
	/**
	 * Test the JDBC data source connection.
	 * @param token the connection token
	 * @param dataSourceId the data source identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to test the connection or the test failed
	 */
	void testJDBCDataSource(AdvanceControlToken token, int dataSourceId) throws IOException, AdvanceControlException;
	/**
	 * Test a JMS endpoint configuration.
	 * @param token the connection token
	 * @param jmsId the identifier of the JMS enpoint to test.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to test the connection or the test failed
	 */
	void testJMSEndpoint(AdvanceControlToken token, int jmsId) throws IOException, AdvanceControlException;
	/**
	 * Test the FTP data source.
	 * @param token the connection token
	 * @param ftpId the data source identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to test FTP data sources
	 */
	void testFTPDataSource(AdvanceControlToken token, int ftpId) throws IOException, AdvanceControlException;
	/**
	 * List the keys of the given keystore.
	 * @param token the connection token.
	 * @param keyStore the keystore name
	 * @return the list of key entries
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException i fthe user is not allowed to list the keys
	 */
	List<AdvanceKeyEntry> queryKeys(AdvanceControlToken token, String keyStore) throws IOException, AdvanceControlException;
	/** @return the datastore interface. */
	AdvanceDataStore datastore();
	/**
	 * Stop a realm's execution.
	 * @param token the connection token
	 * @param name the realm's name
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to stop the realm or other problems arise
	 */
	void stopRealm(AdvanceControlToken token, String name) throws IOException, AdvanceControlException;
	/**
	 * Start a realm's execution.
	 * @param token the connection token
	 * @param name the realm's name
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to start the realm or other problems arise
	 */
	void startRealm(AdvanceControlToken token, String name) throws IOException, AdvanceControlException;
	/**
	 * Retrieve the current flow, if any, from the given realm.
	 * @param token the connection token
	 * @param realm the target realm
	 * @return the composite block representing the flow, or a completely empty composite block
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to query the flow
	 */
	AdvanceCompositeBlock queryFlow(AdvanceControlToken token, String realm) throws IOException, AdvanceControlException;
	/**
	 * Update a flow in a the given realm.
	 * @param token the connection token
	 * @param realm the target realm
	 * @param flow the new flow to upload
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to update a flow
	 */
	void updateFlow(AdvanceControlToken token, String realm, AdvanceCompositeBlock flow) throws IOException, AdvanceControlException;
	/**
	 * Verify the given flow.
	 * @param token the connection token
	 * @param flow the flow to verify
	 * @return the list of compilation errors.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to update a flow
	 */
	List<AdvanceCompilationError> verifyFlow(AdvanceControlToken token, AdvanceCompositeBlock flow) throws IOException, AdvanceControlException;
	/**
	 * Ask for the observable sequence of block diagnostic messages for the given block within the given realm.
	 * @param token the connection token
	 * @param realm the realm
	 * @param blockId the block unique identifier (as in the flow)
	 * @return the observable for the diagnostic messages
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to debug in the realm or the block is missing
	 */
	Observable<AdvanceBlockDiagnostic> debugBlock(AdvanceControlToken token, String realm, String blockId) throws IOException, AdvanceControlException;
	/**
	 * Ask for the observable sequence of port messages of the given port/block/realm.
	 * @param token the connection token
	 * @param realm the realm
	 * @param blockId the block unique identifier (as in the flow)
	 * @param port the port name
	 * @param isImput is this port an input port?
	 * @return the observable of parameter messages
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to debug in the realm or the referenced parameter is missing
	 */
	Observable<AdvanceParameterDiagnostic> debugParameter(AdvanceControlToken token, String realm, String blockId, String port, boolean isImput) throws IOException, AdvanceControlException;
	/**
	 * Inject a value into the given  realm/block/input port.
	 * @param token the connection token
	 * @param realm the realm
	 * @param blockId the block unique identifier (as in the flow)
	 * @param port the port name
	 * @param value the value as XElement to inject
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to debug a realm or the referenced object is missing
	 */
	void injectValue(AdvanceControlToken token, String realm, String blockId, String port, XElement value) throws IOException, AdvanceControlException;
}
