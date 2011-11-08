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

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Interface having only the {@code updateXYZ} methods of the datastore.
 * <p>This will help implement the SQL dialect specific insert or update operations on a JDBC datastore.
 * <p>The users of the updateXYZ methods must set the {@code modifiedBy} field.</p>
 * @author akarnokd, 2011.10.17.
 */
public interface AdvanceDataStoreUpdate {
	/**
	 * Save the block state.
	 * @param realm the target realm
	 * @param blockId the target block identifier
	 * @param state the state XElement or null
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	void updateBlockState(@NonNull String realm, @NonNull String blockId, @Nullable XElement state) throws IOException, AdvanceControlException;
	/**
	 * Update an email box record.
	 * @param box the new box record
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right to update email boxes.
	 */
	void updateEmailBox(@NonNull AdvanceEmailBox box) throws IOException, AdvanceControlException;
	/**
	 * Update the flow descriptor in the given realm.
	 * @param realm the target realm
	 * @param flow the flow descriptor XML
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right
	 */
	void updateFlow(@NonNull String realm, @Nullable XElement flow) throws IOException, AdvanceControlException;
	/**
	 * Update the FTP data source.
	 * @param dataSource the data source object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify FTP data sources
	 */
	void updateFTPDataSource(@NonNull AdvanceFTPDataSource dataSource) throws IOException, AdvanceControlException;
	/**
	 * Update a JDBC data source.
	 * @param dataSource the data source object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to update the information
	 */
	void updateJDBCDataSource(@NonNull AdvanceJDBCDataSource dataSource) throws IOException, AdvanceControlException;
	/**
	 * Update a JMS endpoint settings.
	 * @param endpoint the endpoint object.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify the JMS endpoint
	 */
	void updateJMSEndpoint(@NonNull AdvanceJMSEndpoint endpoint) throws IOException, AdvanceControlException;
	/**
	 * Update key store properties.
	 * @param keyStore the key store properties
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify a key store.
	 */
	void updateKeyStore(@NonNull AdvanceKeyStore keyStore) throws IOException, AdvanceControlException;
	/**
	 * Update a local file data source object.
	 * @param dataSource the data source object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify local file data sources
	 */
	void updateLocalFileDataSource(@NonNull AdvanceLocalFileDataSource dataSource) throws IOException, AdvanceControlException;
	/**
	 * Update the notification groups. Note that this update is considered complete, e.g., the existing group
	 * settings will be deleted and replaced by the contents of the map.
	 * @param groups the map from notification group type to notification group name to set of notification address.
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify the target user
	 */
	void updateNotificationGroups(@NonNull Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> groups) throws IOException, AdvanceControlException;
	/**
	 * Update the properties of the given realm.
	 * @param realm the realm record
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to rename the realm
	 */
	void updateRealm(@NonNull AdvanceRealm realm) throws IOException, AdvanceControlException;
	/**
	 * Update the SOAP channel.
	 * @param channel the new channel settings
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user has no right to update a channel
	 */
	void updateSOAPChannel(@NonNull AdvanceSOAPChannel channel) throws IOException, AdvanceControlException;
	/**
	 * Update the user's settings.
	 * @param user the target user object
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to modify the target user
	 */
	void updateUser(@NonNull AdvanceUser user) throws IOException, AdvanceControlException;
	/**
	 * Update a web data source.
	 * @param endpoint the endpoint record
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user is not allowed to update the web data sources
	 */
	void updateWebDataSource(@NonNull AdvanceWebDataSource endpoint) throws IOException, AdvanceControlException;
}
