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
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Interface for performing datastore related operations.
 * <p>The users of the updateXYZ methods must set the {@code modifiedBy} field.</p>
 * @author karnokd, 2011.09.23.
 */
public interface AdvanceDataStore {
	/**
	 * Retrieve a list of realm information.
	 * @return the list of realms
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the realms
	 */
	List<AdvanceRealm> queryRealms()
	throws IOException, AdvanceControlException;
	/**
	 * Retrieve a concrete realm.
	 * @param realm the the target realm
	 * @return the list of realms
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the realms
	 */
	AdvanceRealm queryRealm(String realm)
	throws IOException, AdvanceControlException;
	/**
	 * Create a new realm.
	 * @param realm the realm name.
	 * @param byUser the user who changed the object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to create a realm or the realm exists.
	 */
	void createRealm(String realm, String byUser) throws IOException, AdvanceControlException;
	/**
	 * Delete a realm.
	 * @param realm the realm name
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete the realm
	 */
	void deleteRealm(String realm) throws IOException, AdvanceControlException;
	/**
	 * Rename the given realm.
	 * @param realm the original realm name
	 * @param newName the new realm name
	 * @param byUser the user who changed the object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to rename the realm
	 */
	void renameRealm(String realm, String newName, String byUser) throws IOException, AdvanceControlException;
	/**
	 * Query the list of users.
	 * @return the list of users
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to see the list of users
	 */
	List<AdvanceUser> queryUsers() throws IOException, AdvanceControlException;
	/**
	 * Query an individual user.
	 * @param userName the user identifier
	 * @return the list of users
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to see the list of users
	 */
	AdvanceUser queryUser(String userName) throws IOException, AdvanceControlException;
	/**
	 * Enable/disable a user.
	 * @param userName the user's identifier
	 * @param enabled should be enabled or disabled?
	 * @param byUser the user who changed the object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify the target's properties
	 */
	void enableUser(String userName, boolean enabled, String byUser) throws IOException, AdvanceControlException;
	/**
	 * Delete the given user.
	 * @param userName the user's identifier
	 * @param byUser the user who is changes the object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete the target
	 */
	void deleteUser(String userName, String byUser) throws IOException, AdvanceControlException;
	/**
	 * Update the user's settings.
	 * @param user the target user object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify the target user
	 */
	void updateUser(AdvanceUser user) throws IOException, AdvanceControlException;
	/**
	 * Retrieve the notification group settings.
	 * @return the map from notification group type to notification group name to set of notification address.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify the target user
	 */
	Map<AdvanceNotificationGroupType, Map<String, Set<String>>> queryNotificationGroups() throws IOException, AdvanceControlException;
	/**
	 * Update the notification groups. Note that this update is considered complete, e.g., the existing group
	 * settings will be deleted and replaced by the contents of the map.
	 * @param groups the map from notification group type to notification group name to set of notification address.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify the target user
	 */
	void updateNotificationGroups(Map<AdvanceNotificationGroupType, Map<String, Set<String>>> groups) throws IOException, AdvanceControlException;
	/**
	 * List the available JDBC data sources.
	 * @return the list of JDBC data sources
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to query the information
	 */
	List<AdvanceJDBCDataSource> queryJDBCDataSources() throws IOException, AdvanceControlException;
	/**
	 * Update a JDBC data source.
	 * @param dataSource the data source object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to update the information
	 */
	void updateJDBCDataSource(AdvanceJDBCDataSource dataSource) throws IOException, AdvanceControlException;
	/**
	 * Delete a specific JDBC data source.
	 * @param dataSourceName the data source identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete the data source
	 */
	void deleteJDBCDataSource(String dataSourceName) throws IOException, AdvanceControlException;
	/**
	 * Retrieve a list of JMS endpoints.
	 * @return the list of jms endpoints
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the JMS endpoints
	 */
	List<AdvanceJMSEndpoint> queryJMSEndpoints() throws IOException, AdvanceControlException;
	/**
	 * Update a JMS endpoint settings.
	 * @param endpoint the entpoint object.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify the JMS endpoint
	 */
	void updateJMSEndpoint(AdvanceJMSEndpoint endpoint) throws IOException, AdvanceControlException;
	/**
	 * Delete a JMS endpoint configuration.
	 * @param jmsName the identifier of the JMS enpoint to delete.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete
	 */
	void deleteJMSEndpoint(String jmsName) throws IOException, AdvanceControlException;
	/**
	 * Retrieve a list of web data sources.
	 * @return the list of web data sources
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the web data sources
	 */
	List<AdvanceWebDataSource> queryWebDataSources() throws IOException, AdvanceControlException;
	/**
	 * Update a web data source.
	 * @param endpoint the endpoint record
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to update the web data sources
	 */
	void updateWebDataSource(AdvanceWebDataSource endpoint) throws IOException, AdvanceControlException;
	/**
	 * Delete a web data source.
	 * @param webName the web data source identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete the web data sources
	 */
	void deleteWebDataSource(String webName) throws IOException, AdvanceControlException;
	/**
	 * Retrieve the list of FTP data sources.
	 * @return the list of FTP data sources
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the data sources
	 */
	List<AdvanceFTPDataSource> queryFTPDataSources() throws IOException, AdvanceControlException;
	/**
	 * Update the FTP data source.
	 * @param dataSource the data source object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify FTP data sources
	 */
	void updateFTPDataSource(AdvanceFTPDataSource dataSource) throws IOException, AdvanceControlException;
	/**
	 * Delete an FTP data source object.
	 * @param ftpName the data source identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete FTP data sources
	 */
	void deleteFTPDataSource(String ftpName) throws IOException, AdvanceControlException;
	/**
	 * Retrieve the list of local file data sources.
	 * @return the list of local file data source
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the local file data sources
	 */
	List<AdvanceLocalFileDataSource> queryLocalFileDataSources() throws IOException, AdvanceControlException;
	/**
	 * Update a local file data source object.
	 * @param dataSource the data source object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify local file data sources
	 */
	void updateLocalFileDataSource(AdvanceLocalFileDataSource dataSource) throws IOException, AdvanceControlException;
	/**
	 * Delete a local file data source record.
	 * @param fileName the local file data source identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete local file data sources
	 */
	void deleteLocalFileDataSource(String fileName) throws IOException, AdvanceControlException;
	/**
	 * Query the list of available key stores.
	 * @return the list of key stores.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to see the key stores.
	 */
	List<AdvanceKeyStore> queryKeyStores() throws IOException, AdvanceControlException;
	/**
	 * Query an individual keystore item.
	 * @param name the keystore name
	 * @return the list of key stores.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to see the key stores.
	 */
	AdvanceKeyStore queryKeyStore(String name) throws IOException, AdvanceControlException;
	/**
	 * Check if the user has the expected rights.
	 * @param userName the user's name
	 * @param expected the expected rights
	 * @return true if the user has the expected right
	 * @throws IOException if a network error occurs
	 */
	boolean hasUserRight(String userName, AdvanceUserRights expected) throws IOException;
	/**
	 * Check if the user has the expected realm rights.
	 * @param userName the user's name
	 * @param realm the target realm
	 * @param expected the expected rights
	 * @return true if the user has the expected right
	 * @throws IOException if a network error occurs
	 */
	boolean hasUserRight(String userName, String realm, AdvanceUserRealmRights expected) throws IOException;
	/**
	 * Update key store properties.
	 * @param keyStore the key store properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify a key store.
	 */
	void updateKeyStore(AdvanceKeyStore keyStore) throws IOException, AdvanceControlException;
	/**
	 * Delete a key store.
	 * @param keyStore the key store to delete
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete a key store
	 */
	void deleteKeyStore(String keyStore) throws IOException, AdvanceControlException;
	/**
	 * Return the properties of a a JDBC data source.
	 * @param name the name of the data source
	 * @return the data source properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	AdvanceJDBCDataSource queryJDBCDataSource(String name) throws IOException, AdvanceControlException;
	/**
	 * Return the properties of a a JMS endpoint.
	 * @param name the name of the endpoint
	 * @return the endpoint properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	AdvanceJMSEndpoint queryJMSEndpoint(String name) throws IOException, AdvanceControlException;
	/**
	 * Return the properties of a a SOAP channel.
	 * @param name the name of the channel
	 * @return the channel properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	AdvanceSOAPChannel querySOAPChannel(String name) throws IOException, AdvanceControlException;
	/**
	 * Return the properties of a a FTP data source.
	 * @param name the name of the data source
	 * @return the data source properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	AdvanceFTPDataSource queryFTPDataSource(String name) throws IOException, AdvanceControlException;
	/**
	 * Return the properties of a a Web data source.
	 * @param name the name of the data source
	 * @return the data source properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	AdvanceWebDataSource queryWebDataSource(String name) throws IOException, AdvanceControlException;
	/**
	 * Return the properties of a a local file data source.
	 * @param name the name of the data source
	 * @return the data source properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	AdvanceLocalFileDataSource queryLocalFileDataSource(String name) throws IOException, AdvanceControlException;
	/**
	 * Retrieve the contact information of a notification group type and name.
	 * @param type the group type
	 * @param name the group name
	 * @return the set of contact information
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	Set<String> queryNotificationGroup(AdvanceNotificationGroupType type, String name) throws IOException, AdvanceControlException;
	/**
	 * Retrieve the block state.
	 * @param realm the target realm
	 * @param blockId the block identifier
	 * @return the state XElement
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	XElement queryBlockState(String realm, String blockId) throws IOException, AdvanceControlException;
	/**
	 * Save the block state.
	 * @param realm the target realm
	 * @param blockId the target block identifier
	 * @param state the state XElement
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	void updateBlockState(String realm, String blockId, XElement state) throws IOException, AdvanceControlException;
	/**
	 * Retrieve the flow descriptor of the given realm.
	 * @param realm the target realm
	 * @return the flow description XElement
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	XElement queryFlow(String realm) throws IOException, AdvanceControlException;
	/**
	 * Return the list of SOAP channels.
	 * @return the channel properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	List<AdvanceSOAPChannel> querySOAPChannels() throws IOException, AdvanceControlException;
}
