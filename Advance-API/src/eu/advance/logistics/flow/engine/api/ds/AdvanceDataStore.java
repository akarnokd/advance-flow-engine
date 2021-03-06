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

import hu.akarnokd.utils.xml.XNElement;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.api.core.AdvanceControlException;

/**
 * Interface for performing datastore related operations.
 * @author akarnokd, 2011.09.23.
 */
public interface AdvanceDataStore extends AdvanceDataStoreUpdate {
	/**
	 * Create a new realm.
	 * @param realm the realm name.
	 * @param byUser the user who changed the object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to create a realm or the realm exists.
	 */
	void createRealm(@NonNull String realm, @NonNull String byUser) throws IOException, AdvanceControlException;
	/**
	 * Delete the block states of all blocks in the specified realm.
	 * @param realm the target realm
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	void deleteBlockStates(@NonNull String realm) throws IOException, AdvanceControlException;
	/**
	 * Delete an email box record.
	 * @param name the box name
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right to delete email boxes.
	 */
	void deleteEmailBox(@NonNull String name) throws IOException, AdvanceControlException;
	/**
	 * Delete an FTP data source object.
	 * @param ftpName the data source identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete FTP data sources
	 */
	void deleteFTPDataSource(@NonNull String ftpName) throws IOException, AdvanceControlException;
	/**
	 * Delete a specific JDBC data source.
	 * @param dataSourceName the data source identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete the data source
	 */
	void deleteJDBCDataSource(@NonNull String dataSourceName) throws IOException, AdvanceControlException;
	/**
	 * Delete a JMS endpoint configuration.
	 * @param jmsName the identifier of the JMS endpoint to delete.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete
	 */
	void deleteJMSEndpoint(@NonNull String jmsName) throws IOException, AdvanceControlException;
	/**
	 * Delete a key store.
	 * @param keyStore the key store to delete
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete a key store
	 */
	void deleteKeyStore(@NonNull String keyStore) throws IOException, AdvanceControlException;
	/**
	 * Delete a local file data source record.
	 * @param fileName the local file data source identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete local file data sources
	 */
	void deleteLocalFileDataSource(@NonNull String fileName) throws IOException, AdvanceControlException;
	/**
	 * Delete a realm.
	 * @param realm the realm name
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete the realm
	 */
	void deleteRealm(@NonNull String realm) throws IOException, AdvanceControlException;
	/**
	 * Delete the SOAP channel.
	 * @param name the channel name
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right to delete a channel
	 */
	void deleteSOAPEndpoint(@NonNull String name) throws IOException, AdvanceControlException;
	/**
	 * Delete the given user.
	 * @param userName the user's identifier
	 * @param byUser the user who is changes the object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete the target
	 */
	void deleteUser(@NonNull String userName, @NonNull String byUser) throws IOException, AdvanceControlException;
	/**
	 * Delete a web data source.
	 * @param webName the web data source identifier
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to delete the web data sources
	 */
	void deleteWebDataSource(@NonNull String webName) throws IOException, AdvanceControlException;
	/**
	 * Enable/disable a user.
	 * @param userName the user's identifier
	 * @param enabled should be enabled or disabled?
	 * @param byUser the user who changed the object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify the target's properties
	 */
	void enableUser(@NonNull String userName, boolean enabled, @NonNull String byUser) throws IOException, AdvanceControlException;
	/**
	 * Check if the user has the expected rights.
	 * @param userName the user's name
	 * @param expected the expected rights
	 * @return true if the user has the expected right
	 * @throws IOException if a network error occurs
	 */
	boolean hasUserRight(@NonNull String userName, @NonNull AdvanceUserRights expected) throws IOException;
	/**
	 * Check if the user has the expected realm rights.
	 * @param userName the user's name
	 * @param realm the target realm
	 * @param expected the expected rights
	 * @return true if the user has the expected right
	 * @throws IOException if a network error occurs
	 */
	boolean hasUserRight(@NonNull String userName, @NonNull String realm, @NonNull AdvanceUserRealmRights expected) throws IOException;
	/**
	 * Retrieve the block state.
	 * @param realm the target realm
	 * @param blockId the block identifier
	 * @return the state XNElement
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	@Nullable 
	XNElement queryBlockState(@NonNull String realm, @NonNull String blockId) throws IOException, AdvanceControlException;
	/**
	 * Retrieve a particular email box record.
	 * @param name the box name
	 * @return the box record
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right to list the email boxes.
	 */
	@NonNull 
	AdvanceEmailBox queryEmailBox(@NonNull String name) throws IOException, AdvanceControlException;
	/**
	 * Retrieve a list of email boxes.
	 * @return the list of email boxes
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right to list the email boxes.
	 */
	@NonNull 
	List<AdvanceEmailBox> queryEmailBoxes() throws IOException, AdvanceControlException;
	/**
	 * Retrieve the flow descriptor of the given realm.
	 * @param realm the target realm
	 * @return the flow description XNElement
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	@Nullable
	XNElement queryFlow(@NonNull String realm) throws IOException, AdvanceControlException;
	/**
	 * Return the properties of a a FTP data source.
	 * @param name the name of the data source
	 * @return the data source properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	@NonNull 
	AdvanceFTPDataSource queryFTPDataSource(@NonNull String name) throws IOException, AdvanceControlException;
	/**
	 * Retrieve the list of FTP data sources.
	 * @return the list of FTP data sources
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the data sources
	 */
	@NonNull 
	List<AdvanceFTPDataSource> queryFTPDataSources() throws IOException, AdvanceControlException;
	/**
	 * Return the properties of a a JDBC data source.
	 * @param name the name of the data source
	 * @return the data source properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	@NonNull 
	AdvanceJDBCDataSource queryJDBCDataSource(@NonNull String name) throws IOException, AdvanceControlException;
	/**
	 * List the available JDBC data sources.
	 * @return the list of JDBC data sources
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to query the information
	 */
	@NonNull 
	List<AdvanceJDBCDataSource> queryJDBCDataSources() throws IOException, AdvanceControlException;
	/**
	 * Return the properties of a a JMS endpoint.
	 * @param name the name of the endpoint
	 * @return the endpoint properties, or null if not present
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	@NonNull 
	AdvanceJMSEndpoint queryJMSEndpoint(@NonNull String name) throws IOException, AdvanceControlException;
	/**
	 * Retrieve a list of JMS endpoints.
	 * @return the list of jms endpoints
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the JMS endpoints
	 */
	@NonNull 
	List<AdvanceJMSEndpoint> queryJMSEndpoints() throws IOException, AdvanceControlException;
	/**
	 * Query an individual keystore item.
	 * @param name the keystore name
	 * @return the list of key stores.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to see the key stores.
	 */
	@NonNull 
	AdvanceKeyStore queryKeyStore(@NonNull String name) throws IOException, AdvanceControlException;
	/**
	 * Query the list of available key stores.
	 * @return the list of key stores.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to see the key stores.
	 */
	@NonNull 
	List<AdvanceKeyStore> queryKeyStores() throws IOException, AdvanceControlException;
	/**
	 * Return the properties of a a local file data source.
	 * @param name the name of the data source
	 * @return the data source properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	@NonNull 
	AdvanceLocalFileDataSource queryLocalFileDataSource(@NonNull String name) throws IOException, AdvanceControlException;
	/**
	 * Retrieve the list of local file data sources.
	 * @return the list of local file data source
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the local file data sources
	 */
	@NonNull 
	List<AdvanceLocalFileDataSource> queryLocalFileDataSources() throws IOException, AdvanceControlException;
	/**
	 * Retrieve the contact information of a notification group type and name.
	 * @param type the group type
	 * @param name the group name
	 * @return the set of contact information
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	@NonNull 
	Collection<String> queryNotificationGroup(@NonNull AdvanceNotificationGroupType type, @NonNull String name) throws IOException, AdvanceControlException;
	/**
	 * Retrieve the notification group settings.
	 * @return the map from notification group type to notification group name to set of notification address.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify the target user
	 */
	@NonNull 
	Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> queryNotificationGroups() throws IOException, AdvanceControlException;
	/**
	 * Retrieve a concrete realm.
	 * @param realm the the target realm
	 * @return the realm record
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the realms
	 */
	@NonNull 
	AdvanceRealm queryRealm(@NonNull String realm)
	throws IOException, AdvanceControlException;
	/**
	 * Retrieve a list of realm information.
	 * @return the list of realms
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the realms
	 */
	@NonNull 
	List<AdvanceRealm> queryRealms()
	throws IOException, AdvanceControlException;
	/**
	 * Return the properties of a a SOAP endpoint.
	 * @param name the name of the endpoint
	 * @return the endpoint properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	@NonNull 
	AdvanceSOAPEndpoint querySOAPEndpoint(@NonNull String name) throws IOException, AdvanceControlException;
	/**
	 * Return the list of SOAP endpoints.
	 * @return the endpoint properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	@NonNull 
	List<AdvanceSOAPEndpoint> querySOAPEndpoints() throws IOException, AdvanceControlException;
	/**
	 * Query an individual user.
	 * @param userName the user identifier
	 * @return the list of users
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to see the list of users
	 */
	AdvanceUser queryUser(@NonNull String userName) throws IOException, AdvanceControlException;
	/**
	 * Query the list of users.
	 * @return the list of users
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to see the list of users
	 */
	@NonNull 
	List<AdvanceUser> queryUsers() throws IOException, AdvanceControlException;
	/**
	 * Return the properties of a a Web data source.
	 * @param name the name of the data source
	 * @return the data source properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	@NonNull 
	AdvanceWebDataSource queryWebDataSource(@NonNull String name) throws IOException, AdvanceControlException;
	/**
	 * Retrieve a list of web data sources.
	 * @return the list of web data sources
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to list the web data sources
	 */
	@NonNull 
	List<AdvanceWebDataSource> queryWebDataSources() throws IOException, AdvanceControlException;
}
