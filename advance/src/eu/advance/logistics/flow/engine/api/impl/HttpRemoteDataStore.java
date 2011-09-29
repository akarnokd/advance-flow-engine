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

package eu.advance.logistics.flow.engine.api.impl;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceControlToken;
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceHttpAuthentication;
import eu.advance.logistics.flow.engine.api.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceJMSEndpoint;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.AdvanceLocalFileDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceNotificationGroupType;
import eu.advance.logistics.flow.engine.api.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.AdvanceUser;
import eu.advance.logistics.flow.engine.api.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.api.AdvanceUserRights;
import eu.advance.logistics.flow.engine.api.AdvanceWebDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceWebLoginType;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * A datastore which connects to a remote datastore via HTTP(s) and
 * exchanges messages via XML.
 * @author karnokd, 2011.09.27.
 */
public class HttpRemoteDataStore implements AdvanceDataStore {
	/** The communicator used to send and receive requests. */
	protected HttpCommunicator comm;
	/**
	 * Initialize the datastore with the given remote address and authentication
	 * record.
	 * @param remote the remote datastore URL
	 * @param auth the authentication record
	 */
	public HttpRemoteDataStore(@NonNull URL remote, @NonNull AdvanceHttpAuthentication auth) {
		init(remote, auth);
	}
	/**
	 * Convenience method to initialize the datastore with the given remote address
	 * and BASIC authentication mode.
	 * @param remote the remote address
	 * @param username the username
	 * @param password the password
	 */
	public HttpRemoteDataStore(@NonNull URL remote, @NonNull String username, @NonNull char[] password) {
		AdvanceHttpAuthentication auth = new AdvanceHttpAuthentication();
		auth.loginType = AdvanceWebLoginType.BASIC;
		auth.name = username;
		auth.password = password;
		
		init(remote, auth);
	}
	/**
	 * Initialize the internal communicator with the given address and authentication.
	 * @param remote the remote address
	 * @param auth the authentication record
	 */
	private void init(@NonNull URL remote, @NonNull AdvanceHttpAuthentication auth) {
		comm = new HttpCommunicator();
		comm.url = remote;
		comm.authentication = auth;
	}
	@Override
	public List<AdvanceRealm> queryRealms(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-realms"));
		return HttpRemoteUtils.parseList(response, "realm", AdvanceRealm.CREATOR);
	}
	

	@Override
	public AdvanceRealm queryRealm(AdvanceControlToken token, String realm)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-realm", "realm", realm));
		return HttpRemoteUtils.parseItem(response, AdvanceRealm.CREATOR);
	}

	@Override
	public void createRealm(AdvanceControlToken token, String realm)
			throws IOException, AdvanceControlException {
		comm.send(HttpRemoteUtils.createRequest("create-realm", "realm", realm));
	}

	@Override
	public void deleteRealm(AdvanceControlToken token, String realm)
			throws IOException, AdvanceControlException {
		comm.send(HttpRemoteUtils.createRequest("delete-realm", "realm", realm));
	}

	@Override
	public void renameRealm(AdvanceControlToken token, String realm,
			String newName) throws IOException, AdvanceControlException {
		comm.send(HttpRemoteUtils.createRequest("rename-realm", "realm", realm, "new-realm", newName));
	}

	@Override
	public List<AdvanceUser> queryUsers(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-users"));
		return HttpRemoteUtils.parseList(response, "user", AdvanceUser.CREATOR);
	}

	@Override
	public AdvanceUser queryUser(AdvanceControlToken token, String userName)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-user", "user-name", userName));
		return HttpRemoteUtils.parseItem(response, AdvanceUser.CREATOR);
	}

	@Override
	public void enableUser(AdvanceControlToken token, String userName,
			boolean enabled) throws IOException, AdvanceControlException {
		comm.send(HttpRemoteUtils.createRequest("enable-user", "user-name", userName, "enabled", enabled));
	}

	@Override
	public void deleteUser(AdvanceControlToken token, String userName)
			throws IOException, AdvanceControlException {
		comm.send(HttpRemoteUtils.createRequest("delete-user", "user-name", userName));
	}

	@Override
	public void updateUser(AdvanceControlToken token, AdvanceUser user)
			throws IOException, AdvanceControlException {
		comm.send(HttpRemoteUtils.createUpdate("update-user", user));
	}

	@Override
	public Map<AdvanceNotificationGroupType, Map<String, Set<String>>> queryNotificationGroups(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-notification-groups"));
		return LocalDataStore.parseGroups(response);
	}

	@Override
	public void updateNotificationGroups(AdvanceControlToken token,
			Map<AdvanceNotificationGroupType, Map<String, Set<String>>> groups)
			throws IOException, AdvanceControlException {
		XElement request = LocalDataStore.createGroups("update-notification-groups", groups);
		comm.send(request);
	}
	
	@Override
	public List<AdvanceJDBCDataSource> queryJDBCDataSources(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-jdbc-data-sources"));
		return HttpRemoteUtils.parseList(response, "jdbc-source", AdvanceJDBCDataSource.CREATOR);
	}

	@Override
	public void updateJDBCDataSource(AdvanceControlToken token,
			AdvanceJDBCDataSource dataSource) throws IOException,
			AdvanceControlException {
		comm.send(HttpRemoteUtils.createUpdate("update-jdbc-data-source", dataSource));
	}

	@Override
	public void deleteJDBCDataSource(AdvanceControlToken token, String dataSourceName)
			throws IOException, AdvanceControlException {
		comm.send(HttpRemoteUtils.createRequest("delete-jdbc-data-source", "data-source-name", dataSourceName));
	}

	@Override
	public List<AdvanceJMSEndpoint> queryJMSEndpoints(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-jms-endpoints"));
		return HttpRemoteUtils.parseList(response, "endpoint", AdvanceJMSEndpoint.CREATOR);
	}

	@Override
	public void updateJMSEndpoint(AdvanceControlToken token,
			AdvanceJMSEndpoint endpoint) throws IOException,
			AdvanceControlException {
		comm.send(HttpRemoteUtils.createUpdate("update-jms-endpoint", endpoint));
	}

	@Override
	public void deleteJMSEndpoint(AdvanceControlToken token, String jmsName)
			throws IOException, AdvanceControlException {
		comm.send(HttpRemoteUtils.createRequest("delete-jms-endpoint", "jms-name", jmsName));
	}

	@Override
	public List<AdvanceWebDataSource> queryWebDataSources(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-web-data-sources"));
		return HttpRemoteUtils.parseList(response, "web-source", AdvanceWebDataSource.CREATOR);
	}

	@Override
	public void updateWebDataSource(AdvanceControlToken token,
			AdvanceWebDataSource endpoint) throws IOException,
			AdvanceControlException {
		comm.send(HttpRemoteUtils.createUpdate("update-web-data-source", endpoint));
	}

	@Override
	public void deleteWebDataSource(AdvanceControlToken token, String webName)
			throws IOException, AdvanceControlException {
		comm.send(HttpRemoteUtils.createRequest("delete-web-data-source", "web-name", webName));
	}

	@Override
	public List<AdvanceFTPDataSource> queryFTPDataSources(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-ftp-data-sources"));
		return HttpRemoteUtils.parseList(response, "ftp-source", AdvanceFTPDataSource.CREATOR);
	}

	@Override
	public void updateFTPDataSource(AdvanceControlToken token,
			AdvanceFTPDataSource dataSource) throws IOException,
			AdvanceControlException {
		comm.send(HttpRemoteUtils.createUpdate("update-ftp-data-source", dataSource));
	}

	@Override
	public void deleteFTPDataSource(AdvanceControlToken token, String ftpName)
			throws IOException, AdvanceControlException {
		comm.send(HttpRemoteUtils.createRequest("delete-ftp-data-source", "ftp-name", ftpName));
	}

	@Override
	public List<AdvanceLocalFileDataSource> queryLocalFileDataSources(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-local-file-data-sources"));
		return HttpRemoteUtils.parseList(response, "local-source", AdvanceLocalFileDataSource.CREATOR);
	}

	@Override
	public void updateLocalFileDataSource(AdvanceControlToken token,
			AdvanceLocalFileDataSource dataSource) throws IOException,
			AdvanceControlException {
		comm.send(HttpRemoteUtils.createUpdate("update-local-file-data-source", dataSource));
	}

	@Override
	public void deleteLocalFileDataSource(AdvanceControlToken token, String fileName)
			throws IOException, AdvanceControlException {
		comm.send(HttpRemoteUtils.createRequest("delete-local-file-data-source", "file-name", fileName));
	}

	@Override
	public List<AdvanceKeyStore> queryKeyStores(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-keystores"));
		return HttpRemoteUtils.parseList(response, "keystore", AdvanceKeyStore.CREATOR);
	}

	@Override
	public AdvanceKeyStore queryKeyStore(AdvanceControlToken token, String name)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-keystore", "name", name));
		return HttpRemoteUtils.parseItem(response, AdvanceKeyStore.CREATOR);
	}

	@Override
	public boolean hasUserRight(AdvanceControlToken token,
			AdvanceUserRights expected) throws IOException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("has-user-right", "expected", expected));
		return "true".equals(response.content);
	}

	@Override
	public boolean hasUserRight(AdvanceControlToken token, String realm,
			AdvanceUserRealmRights expected) throws IOException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("has-user-realm-right", "realm", realm, "expected", expected));
		return "true".equals(response.content);
	}

	@Override
	public void updateKeyStore(AdvanceControlToken token,
			AdvanceKeyStore keyStore) throws IOException,
			AdvanceControlException {
		comm.send(HttpRemoteUtils.createUpdate("update-keystore", keyStore));
	}

	@Override
	public void deleteKeyStore(AdvanceControlToken token, String keyStore)
			throws IOException, AdvanceControlException {
		comm.send(HttpRemoteUtils.createRequest("delete-keystore", "keystore", keyStore));
	}

}
