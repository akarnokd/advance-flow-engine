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

import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.advance.logistics.flow.model.AdvanceBlockRegistryEntry;

/**
 * <p>The API for interacting with the ADVANCE Flow Engine remotely.</p>
 * <p>The API does not have separate {@code insertXYZ} type methods to create
 * new instances of various objects. 
 * The {@code Integer.MIN_VALUE} used as the unique identifier (see {@code id fields} will represent
 * the request to create a new object. Once they were created, the proper non-negative
 * unique identifier replaces this value.</p>
 * @author karnokd, 2011.09.19.
 */
public interface AdvanceFlowEngineControl {
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
	 * Retrieve a list of realm information.
	 * @param token the connection token
	 * @return the list of realms
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the realms
	 */
	List<AdvanceRealm> queryRealms(AdvanceControlToken token)
	throws IOException, AdvanceControlException;
	/**
	 * Create a new realm.
	 * @param token the connection token
	 * @param name the realm name.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to create a realm or the realm exists.
	 */
	void createRealm(AdvanceControlToken token, String name) throws IOException, AdvanceControlException;
	/**
	 * Delete a realm.
	 * @param token the connection token
	 * @param name the realm name
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete the realm
	 */
	void deleteRealm(AdvanceControlToken token, String name) throws IOException, AdvanceControlException;
	/**
	 * Rename the given realm.
	 * @param token the connection token
	 * @param name the original realm name
	 * @param newName the new realm name
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to rename the realm
	 */
	void renameRealm(AdvanceControlToken token, String name, String newName) throws IOException, AdvanceControlException;
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
	 * Query the list of users.
	 * @param token the connection token
	 * @return the list of users
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to see the list of users
	 */
	List<AdvanceUser> queryUsers(AdvanceControlToken token) throws IOException, AdvanceControlException;
	/**
	 * Query an individual user.
	 * @param token the connection token
	 * @param userId the user identifier
	 * @return the list of users
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to see the list of users
	 */
	AdvanceUser queryUser(AdvanceControlToken token, int userId) throws IOException, AdvanceControlException;
	/**
	 * Enable/disable a user.
	 * @param token the connection token
	 * @param userId the user's identifier
	 * @param enabled should be enabled or disabled?
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify the target's properties
	 */
	void enableUser(AdvanceControlToken token, int userId, boolean enabled) throws IOException, AdvanceControlException;
	/**
	 * Delete the given user.
	 * @param token the connection token
	 * @param userId the user's identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete the target
	 */
	void deleteUser(AdvanceControlToken token, int userId) throws IOException, AdvanceControlException;
	/**
	 * Update the user's settings.
	 * @param token the connection token
	 * @param user the target user object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify the target user
	 */
	void updateUser(AdvanceControlToken token, AdvanceUser user) throws IOException, AdvanceControlException;
	/**
	 * Retrieve the notification group settings.
	 * @param token the connection token
	 * @return the map from notification group type to notification group name to set of notification address.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify the target user
	 */
	Map<AdvanceNotificationGroupType, Map<String, Set<String>>> queryNotificationGroups(AdvanceControlToken token) throws IOException, AdvanceControlException;
	/**
	 * Update the notification groups. Note that this update is considered complete, e.g., the existing group
	 * settings will be deleted and replaced by the contents of the map.
	 * @param token the connection token
	 * @param groups the map from notification group type to notification group name to set of notification address.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify the target user
	 */
	void updateNotificationGroups(AdvanceControlToken token, Map<AdvanceNotificationGroupType, Map<String, Set<String>>> groups) throws IOException, AdvanceControlException;
	/**
	 * List the available JDBC data sources.
	 * @param token the connection token
	 * @return the list of JDBC data sources
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to query the information
	 */
	List<AdvanceJDBCDataSource> queryJDBCDataSources(AdvanceControlToken token) throws IOException, AdvanceControlException;
	/**
	 * Update a JDBC data source.
	 * @param token the connection token
	 * @param dataSource the data source object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to update the information
	 */
	void updateJDBCDataSource(AdvanceControlToken token, AdvanceJDBCDataSource dataSource) throws IOException, AdvanceControlException;
	/**
	 * Test the JDBC data source connection.
	 * @param token the connection token
	 * @param dataSourceId the data source identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to test the connection or the test failed
	 */
	void testJDBCDataSource(AdvanceControlToken token, int dataSourceId) throws IOException, AdvanceControlException;
	/**
	 * Delete a specific JDBC data source.
	 * @param token the connection token
	 * @param dataSourceId the data source identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete the data source
	 */
	void deleteJDBCDataSource(AdvanceControlToken token, int dataSourceId) throws IOException, AdvanceControlException;
	/**
	 * Retrieve a list of JMS endpoints.
	 * @param token the connection token
	 * @return the list of jms endpoints
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the JMS endpoints
	 */
	List<AdvanceJMSEndpoint> queryJMSEndpoints(AdvanceControlToken token) throws IOException, AdvanceControlException;
	/**
	 * Update a JMS endpoint settings.
	 * @param token the connection token
	 * @param endpoint the entpoint object.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify the JMS endpoint
	 */
	void updateJMSEndpoint(AdvanceControlToken token, AdvanceJMSEndpoint endpoint) throws IOException, AdvanceControlException;
	/**
	 * Test a JMS endpoint configuration.
	 * @param token the connection token
	 * @param jmsId the identifier of the JMS enpoint to test.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to test the connection or the test failed
	 */
	void testJMSEndpoint(AdvanceControlToken token, int jmsId) throws IOException, AdvanceControlException;
	/**
	 * Delete a JMS endpoint configuration.
	 * @param token the connection token
	 * @param jmsId the identifier of the JMS enpoint to test.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete
	 */
	void deleteJMSEndpoint(AdvanceControlToken token, int jmsId) throws IOException, AdvanceControlException;
	/**
	 * Retrieve a list of web data sources.
	 * @param token the connection token
	 * @return the list of web data sources
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the web data sources
	 */
	List<AdvanceWebDataSource> queryWebDataSources(AdvanceControlToken token) throws IOException, AdvanceControlException;
	/**
	 * Update a web data source.
	 * @param token the connection token
	 * @param endpoint the endpoint record
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to update the web data sources
	 */
	void updateWebDataSource(AdvanceControlToken token, AdvanceWebDataSource endpoint) throws IOException, AdvanceControlException;
	/**
	 * Delete a web data source.
	 * @param token the connection token
	 * @param webId the web data source identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete the web data sources
	 */
	void deleteWebDataSource(AdvanceControlToken token, int webId) throws IOException, AdvanceControlException;
	/**
	 * Retrieve the list of FTP data sources.
	 * @param token the connection token
	 * @return the list of FTP data sources
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the data sources
	 */
	List<AdvanceFTPDataSource> queryFTPDataSources(AdvanceControlToken token) throws IOException, AdvanceControlException;
	/**
	 * Update the FTP data source.
	 * @param token the connection token
	 * @param dataSource the data source object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify FTP data sources
	 */
	void updateFTPDataSource(AdvanceControlToken token, AdvanceFTPDataSource dataSource) throws IOException, AdvanceControlException;
	/**
	 * Test the FTP data source.
	 * @param token the connection token
	 * @param ftpId the data source identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to test FTP data sources
	 */
	void testFTPDataSource(AdvanceControlToken token, int ftpId) throws IOException, AdvanceControlException;
	/**
	 * Delete an FTP data source object.
	 * @param token the connection token
	 * @param ftpId the data source identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete FTP data sources
	 */
	void deleteFTPDataSource(AdvanceControlToken token, int ftpId) throws IOException, AdvanceControlException;
	/**
	 * Retrieve the list of local file data sources.
	 * @param token the connection token
	 * @return the list of local file data source
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the local file data sources
	 */
	List<AdvanceLocalFileDataSource> queryLocalFileDataSources(AdvanceControlToken token) throws IOException, AdvanceControlException;
	/**
	 * Update a local file data source object.
	 * @param token the connection token
	 * @param dataSource the data source object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify local file data sources
	 */
	void updateLocalFileDataSource(AdvanceControlToken token, AdvanceLocalFileDataSource dataSource) throws IOException, AdvanceControlException;
	/**
	 * Delete a local file data source record.
	 * @param token the connection token
	 * @param fileId the local file data source identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete local file data sources
	 */
	void deleteLocalFileDataSource(AdvanceControlToken token, int fileId) throws IOException, AdvanceControlException;
	/**
	 * Query the list of available key stores.
	 * @param token the connection token
	 * @return the list of key stores.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to see the key stores.
	 */
	List<AdvanceKeyStore> queryKeyStores(AdvanceControlToken token) throws IOException, AdvanceControlException;
	/**
	 * Query the list of the contents of a particular key store.
	 * @param token the connection token
	 * @param keyStore the key store name
	 * @param password the key store master password
	 * @return the list of key entries
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the key store contents.
	 */
	List<AdvanceKeyEntry> queryKeyStore(AdvanceControlToken token, String keyStore, char[] password) throws IOException, AdvanceControlException;
	/**
	 * Update key store properties.
	 * @param token the connection token
	 * @param keyStore the key store properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify a key store.
	 */
	void updateKeyStore(AdvanceControlToken token, AdvanceKeyStore keyStore) throws IOException, AdvanceControlException;
	/**
	 * Delete a key store.
	 * @param token the connection token
	 * @param keyStore the key store to delete
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete a key store
	 */
	void deleteKeyStore(AdvanceControlToken token, String keyStore) throws IOException, AdvanceControlException;;
	/**
	 * Delete a key entry from a keystore.
	 * @param token the connection token
	 * @param keyStore the key store name
	 * @param password the key store master password
	 * @param keyAlias the key alias
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete a key
	 */
	void deleteKeyEntry(AdvanceControlToken token, String keyStore, char[] password, String keyAlias) throws IOException, AdvanceControlException;;
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
	 * @param data the certificate in textual PEM format.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to export
	 */
	void importPrivateKey(AdvanceControlToken token, AdvanceKeyStoreExport request, String data) throws IOException, AdvanceControlException;
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
}
