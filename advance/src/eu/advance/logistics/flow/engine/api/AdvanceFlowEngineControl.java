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
 * The API for interacting with the ADVANCE Flow Engine remotely.
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
	List<AdvanceRealmInfo> queryRealms(AdvanceControlToken token)
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
}
