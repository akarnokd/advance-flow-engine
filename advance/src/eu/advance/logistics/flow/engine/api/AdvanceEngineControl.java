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
import java.util.List;

import eu.advance.logistics.flow.engine.AdvanceBlockDiagnostic;
import eu.advance.logistics.flow.engine.AdvanceCompilationResult;
import eu.advance.logistics.flow.engine.AdvanceParameterDiagnostic;
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
	 * Retrieve the user settings.
	 * @return the token representing the connection
	 * @throws IOException if the network connection fails
	 * @throws AdvanceControlException if the username/password is incorrect
	 */
	AdvanceUser getUser()
	throws IOException, AdvanceControlException;
	/**
	 * Retrieve a list of supported block types.
	 * @return the list of supported block types
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the blocks.
	 */
	List<AdvanceBlockRegistryEntry> queryBlocks() 
	throws IOException, AdvanceControlException;
	/**
	 * Return the engine version infromation.
	 * @return the engine version information
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not authenticated
	 */
	AdvanceEngineVersion queryVersion() throws IOException, AdvanceControlException;
	/**
	 * Query the list of schemas known by the engine.
	 * @return the list and value of the known schemas
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the schemas.
	 */
	List<AdvanceSchemaRegistryEntry> querySchemas() throws IOException, AdvanceControlException;
	/**
	 * Query a concrete schema.
	 * @param name the schema name
	 * @return the schema as XElement
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the schemas.
	 */
	AdvanceSchemaRegistryEntry querySchema(String name) throws IOException, AdvanceControlException; 
	/**
	 * Add or modify a new schema in the flow engine.
	 * @param name the schema's filename (without path)
	 * @param schema the schema content
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to add new schemas
	 */
	void updateSchema(String name, XElement schema) throws IOException, AdvanceControlException;
	/**
	 * Delete the specified schema.
	 * @param name the schema name
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete schemas
	 */
	void deleteSchema(String name) throws IOException, AdvanceControlException;
	/**
	 * Delete a key entry from a keystore.
	 * @param keyStore the key store name
	 * @param keyAlias the key alias
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete a key
	 */
	void deleteKeyEntry(String keyStore, String keyAlias) throws IOException, AdvanceControlException;;
	/**
	 * Generate a new key with the given properties.
	 * @param key the key generation properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to generate a key
	 */
	void generateKey(AdvanceGenerateKey key) throws IOException, AdvanceControlException;
	/**
	 * Export a certificate from a designated key store.
	 * @param request represents the key store and key alias to export
	 * @return the certificate in textual CER format.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to export
	 */
	String exportCertificate(AdvanceKeyStoreExport request) throws IOException, AdvanceControlException;
	/**
	 * Export a private key from a designated key store.
	 * @param request represents the key store and key alias to export
	 * @return the private key in textual PEM format.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to export
	 */
	String exportPrivateKey(AdvanceKeyStoreExport request) throws IOException, AdvanceControlException;
	/**
	 * Import a certificate into a designated key store.
	 * @param request represents the key store and key alias to import
	 * @param data the certificate in textual CER format.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to export
	 */
	void importCertificate(AdvanceKeyStoreExport request, String data) throws IOException, AdvanceControlException;
	/**
	 * Import a private key into a designated key store.
	 * @param request represents the key store and key alias to import
	 * @param keyData the key in textual PEM format.
	 * @param certData the certificate int textual CER format
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to export
	 */
	void importPrivateKey(
			AdvanceKeyStoreExport request, String keyData, String certData) throws IOException, AdvanceControlException;
	/**
	 * Export a signing request of the given private key.
	 * @param request represents the key store and key alias to export
	 * @return the signing request in textual format.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to export
	 */
	String exportSigningRequest(AdvanceKeyStoreExport request) throws IOException, AdvanceControlException;
	/**
	 * Import a signing response into the designated key store.
	 * @param request represents the key store and key alias to export
	 * @param data the signing response in textual format.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to export
	 */
	void importSigningResponse(AdvanceKeyStoreExport request, String data) throws IOException, AdvanceControlException;
	/**
	 * Test the JDBC data source connection.
	 * @param dataSourceName the data source identifier
	 * @return the test result
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to test the connection
	 */
	DataStoreTestResult testJDBCDataSource(String dataSourceName) throws IOException, AdvanceControlException;
	/**
	 * Test a JMS endpoint configuration.
	 * @param jmsName the identifier of the JMS enpoint to test.
	 * @return the test result
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to test the connection
	 */
	DataStoreTestResult testJMSEndpoint(String jmsName) throws IOException, AdvanceControlException;
	/**
	 * Test the FTP data source.
	 * @param ftpName the data source identifier
	 * @return the test result
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to test FTP data sources
	 */
	DataStoreTestResult testFTPDataSource(String ftpName) throws IOException, AdvanceControlException;
	/**
	 * List the keys of the given keystore.
	 * @param keyStore the keystore name
	 * @return the list of key entries
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException i fthe user is not allowed to list the keys
	 */
	List<AdvanceKeyEntry> queryKeys(String keyStore) throws IOException, AdvanceControlException;
	/** @return the datastore interface. */
	AdvanceDataStore datastore();
	/**
	 * Stop a realm's execution.
	 * @param name the realm's name
	 * @param byUser the user who is stopping the realm
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to stop the realm or other problems arise
	 */
	void stopRealm(String name, String byUser) throws IOException, AdvanceControlException;
	/**
	 * Start a realm's execution.
	 * @param name the realm's name
	 * @param byUser the user who is starting the realm
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to start the realm or other problems arise
	 */
	void startRealm(String name, String byUser) throws IOException, AdvanceControlException;
	/**
	 * Retrieve the current flow, if any, from the given realm.
	 * @param realm the target realm
	 * @return the composite block representing the flow, or a completely empty composite block
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to query the flow
	 */
	AdvanceCompositeBlock queryFlow(String realm) throws IOException, AdvanceControlException;
	/**
	 * Update a flow in a the given realm.
	 * @param realm the target realm
	 * @param flow the new flow to upload
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to update a flow
	 */
	void updateFlow(String realm, AdvanceCompositeBlock flow) throws IOException, AdvanceControlException;
	/**
	 * Verify the given flow.
	 * @param flow the flow to verify
	 * @return the results of the compilation in terms of errors and computed types of wires
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to update a flow
	 */
	AdvanceCompilationResult verifyFlow(AdvanceCompositeBlock flow) throws IOException, AdvanceControlException;
	/**
	 * Ask for the observable sequence of block diagnostic messages for the given block within the given realm.
	 * @param realm the realm
	 * @param blockId the block unique identifier (as in the flow)
	 * @return the observable for the diagnostic messages
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to debug in the realm or the block is missing
	 */
	Observable<AdvanceBlockDiagnostic> debugBlock(String realm, String blockId) throws IOException, AdvanceControlException;
	/**
	 * Ask for the observable sequence of port messages of the given port/block/realm.
	 * @param realm the realm
	 * @param blockId the block unique identifier (as in the flow)
	 * @param port the port name
	 * @return the observable of parameter messages
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to debug in the realm or the referenced parameter is missing
	 */
	Observable<AdvanceParameterDiagnostic> debugParameter(String realm, String blockId, String port) throws IOException, AdvanceControlException;
	/**
	 * Inject a value into the given  realm/block/input port.
	 * @param realm the realm
	 * @param blockId the block unique identifier (as in the flow)
	 * @param port the port name
	 * @param value the value as XElement to inject
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to debug a realm or the referenced object is missing
	 */
	void injectValue(String realm, String blockId, String port, XElement value) throws IOException, AdvanceControlException;
	/** 
	 * Shut down the flow engine.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to shut down the engine
	 */
	void shutdown() throws IOException, AdvanceControlException;
}
