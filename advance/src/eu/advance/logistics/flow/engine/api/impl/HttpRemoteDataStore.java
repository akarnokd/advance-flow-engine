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

import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.AdvanceEmailBox;
import eu.advance.logistics.flow.engine.api.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceHttpAuthentication;
import eu.advance.logistics.flow.engine.api.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceJMSEndpoint;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.AdvanceLocalFileDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceNotificationGroupType;
import eu.advance.logistics.flow.engine.api.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.AdvanceSOAPChannel;
import eu.advance.logistics.flow.engine.api.AdvanceUser;
import eu.advance.logistics.flow.engine.api.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.api.AdvanceUserRights;
import eu.advance.logistics.flow.engine.api.AdvanceWebDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceLoginType;
import eu.advance.logistics.flow.engine.api.AdvanceXMLCommunicator;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializables;

/**
 * A datastore which connects to a remote datastore via HTTP(s) and
 * exchanges messages via XML.
 * @author karnokd, 2011.09.27.
 */
public class HttpRemoteDataStore implements AdvanceDataStore {
	/** The communicator used to send and receive requests. */
	protected AdvanceXMLCommunicator comm;
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
		auth.loginType = AdvanceLoginType.BASIC;
		auth.name = username;
		auth.password = password;
		
		init(remote, auth);
	}
	/**
	 * Initialize the datastore with the supplied communicator.
	 * @param comm the communicator instance
	 */
	public HttpRemoteDataStore(@NonNull AdvanceXMLCommunicator comm) {
		this.comm = comm;
	}
	/**
	 * Initialize the internal communicator with the given address and authentication.
	 * @param remote the remote address
	 * @param auth the authentication record
	 */
	private void init(@NonNull URL remote, @NonNull AdvanceHttpAuthentication auth) {
		HttpCommunicator comm = new HttpCommunicator();
		comm.url = remote;
		comm.authentication = auth;
		this.comm = comm;
	}
	@Override
	public List<AdvanceRealm> queryRealms()
			throws IOException, AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-realms"));
		return XSerializables.parseList(response, "realm", AdvanceRealm.CREATOR);
	}
	

	@Override
	public AdvanceRealm queryRealm(String realm)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-realm", "realm", realm));
		return XSerializables.parseItem(response, AdvanceRealm.CREATOR);
	}

	@Override
	public void createRealm(String realm, String byUser)
			throws IOException, AdvanceControlException {
		comm.send(XSerializables.createRequest("create-realm", "realm", realm));
	}

	@Override
	public void deleteRealm(String realm)
			throws IOException, AdvanceControlException {
		comm.send(XSerializables.createRequest("delete-realm", "realm", realm));
	}

	@Override
	public void updateRealm(AdvanceRealm realm) throws IOException, AdvanceControlException {
		comm.send(XSerializables.createUpdate("update-realm", realm));
	}

	@Override
	public List<AdvanceUser> queryUsers()
			throws IOException, AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-users"));
		return XSerializables.parseList(response, "user", AdvanceUser.CREATOR);
	}

	@Override
	public AdvanceUser queryUser(String userName)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-user", "user-name", userName));
		return XSerializables.parseItem(response, AdvanceUser.CREATOR);
	}

	@Override
	public void enableUser(String userName,
			boolean enabled, String byUser) throws IOException, AdvanceControlException {
		comm.send(XSerializables.createRequest("enable-user", "user-name", userName, "enabled", enabled));
	}

	@Override
	public void deleteUser(String userName, String byUser)
			throws IOException, AdvanceControlException {
		comm.send(XSerializables.createRequest("delete-user", "user-name", userName));
	}

	@Override
	public void updateUser(AdvanceUser user)
			throws IOException, AdvanceControlException {
		comm.send(XSerializables.createUpdate("update-user", user));
	}

	@Override
	public Map<AdvanceNotificationGroupType, Map<String, Set<String>>> queryNotificationGroups()
			throws IOException,
			AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-notification-groups"));
		return LocalDataStore.parseGroups(response);
	}

	@Override
	public void updateNotificationGroups(
			Map<AdvanceNotificationGroupType, Map<String, Set<String>>> groups)
			throws IOException, AdvanceControlException {
		XElement request = LocalDataStore.createGroups("update-notification-groups", groups);
		comm.send(request);
	}
	
	@Override
	public List<AdvanceJDBCDataSource> queryJDBCDataSources() throws IOException,
			AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-jdbc-data-sources"));
		return XSerializables.parseList(response, "jdbc-source", AdvanceJDBCDataSource.CREATOR);
	}

	@Override
	public void updateJDBCDataSource(
			AdvanceJDBCDataSource dataSource) throws IOException,
			AdvanceControlException {
		comm.send(XSerializables.createUpdate("update-jdbc-data-source", dataSource));
	}

	@Override
	public void deleteJDBCDataSource(String dataSourceName)
			throws IOException, AdvanceControlException {
		comm.send(XSerializables.createRequest("delete-jdbc-data-source", "data-source-name", dataSourceName));
	}

	@Override
	public List<AdvanceJMSEndpoint> queryJMSEndpoints()
			throws IOException, AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-jms-endpoints"));
		return XSerializables.parseList(response, "endpoint", AdvanceJMSEndpoint.CREATOR);
	}

	@Override
	public void updateJMSEndpoint(
			AdvanceJMSEndpoint endpoint) throws IOException,
			AdvanceControlException {
		comm.send(XSerializables.createUpdate("update-jms-endpoint", endpoint));
	}

	@Override
	public void deleteJMSEndpoint(String jmsName)
			throws IOException, AdvanceControlException {
		comm.send(XSerializables.createRequest("delete-jms-endpoint", "jms-name", jmsName));
	}

	@Override
	public List<AdvanceWebDataSource> queryWebDataSources() throws IOException,
			AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-web-data-sources"));
		return XSerializables.parseList(response, "web-source", AdvanceWebDataSource.CREATOR);
	}

	@Override
	public void updateWebDataSource(
			AdvanceWebDataSource endpoint) throws IOException,
			AdvanceControlException {
		comm.send(XSerializables.createUpdate("update-web-data-source", endpoint));
	}

	@Override
	public void deleteWebDataSource(String webName)
			throws IOException, AdvanceControlException {
		comm.send(XSerializables.createRequest("delete-web-data-source", "web-name", webName));
	}

	@Override
	public List<AdvanceFTPDataSource> queryFTPDataSources() throws IOException,
			AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-ftp-data-sources"));
		return XSerializables.parseList(response, "ftp-source", AdvanceFTPDataSource.CREATOR);
	}

	@Override
	public void updateFTPDataSource(
			AdvanceFTPDataSource dataSource) throws IOException,
			AdvanceControlException {
		comm.send(XSerializables.createUpdate("update-ftp-data-source", dataSource));
	}

	@Override
	public void deleteFTPDataSource(String ftpName)
			throws IOException, AdvanceControlException {
		comm.send(XSerializables.createRequest("delete-ftp-data-source", "ftp-name", ftpName));
	}

	@Override
	public List<AdvanceLocalFileDataSource> queryLocalFileDataSources(
			) throws IOException,
			AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-local-file-data-sources"));
		return XSerializables.parseList(response, "local-source", AdvanceLocalFileDataSource.CREATOR);
	}

	@Override
	public void updateLocalFileDataSource(
			AdvanceLocalFileDataSource dataSource) throws IOException,
			AdvanceControlException {
		comm.send(XSerializables.createUpdate("update-local-file-data-source", dataSource));
	}

	@Override
	public void deleteLocalFileDataSource(String fileName)
			throws IOException, AdvanceControlException {
		comm.send(XSerializables.createRequest("delete-local-file-data-source", "file-name", fileName));
	}

	@Override
	public List<AdvanceKeyStore> queryKeyStores()
			throws IOException, AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-keystores"));
		return XSerializables.parseList(response, "keystore", AdvanceKeyStore.CREATOR);
	}

	@Override
	public AdvanceKeyStore queryKeyStore(String name)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-keystore", "name", name));
		return XSerializables.parseItem(response, AdvanceKeyStore.CREATOR);
	}

	@Override
	public boolean hasUserRight(String userName,
			AdvanceUserRights expected) throws IOException {
		XElement response = comm.query(XSerializables.createRequest("has-user-right", "expected", expected));
		return "true".equals(response.content);
	}

	@Override
	public boolean hasUserRight(String userName, String realm,
			AdvanceUserRealmRights expected) throws IOException {
		XElement response = comm.query(XSerializables.createRequest("has-user-realm-right", "realm", realm, "expected", expected));
		return "true".equals(response.content);
	}

	@Override
	public void updateKeyStore(
			AdvanceKeyStore keyStore) throws IOException,
			AdvanceControlException {
		comm.send(XSerializables.createUpdate("update-keystore", keyStore));
	}

	@Override
	public void deleteKeyStore(String keyStore)
			throws IOException, AdvanceControlException {
		comm.send(XSerializables.createRequest("delete-keystore", "keystore", keyStore));
	}
	@Override
	public Set<String> queryNotificationGroup(
			AdvanceNotificationGroupType type, String name) throws IOException {
		XElement response = comm.query(XSerializables.createRequest("query-notification-group", "type", type, "name", name));
		Set<String> result = Sets.newHashSet();
		for (XElement x : response.childrenWithName("contact")) {
			result.add(x.get("value"));
		}
		return result;
	}
	@Override
	public AdvanceJDBCDataSource queryJDBCDataSource(String name)
			throws IOException {
		return XSerializables.parseItem(comm.query(
				XSerializables.createRequest("query-jdbc-data-source", "name", name)), 
				AdvanceJDBCDataSource.CREATOR);
	}
	@Override
	public AdvanceJMSEndpoint queryJMSEndpoint(String name) throws IOException {
		return XSerializables.parseItem(comm.query(
				XSerializables.createRequest("query-jms-endpoint", "name", name)), 
				AdvanceJMSEndpoint.CREATOR);
	}
	@Override
	public AdvanceSOAPChannel querySOAPChannel(String name) throws IOException {
		return XSerializables.parseItem(comm.query(
				XSerializables.createRequest("query-soap-channel", "name", name)), 
				AdvanceSOAPChannel.CREATOR);
	}
	@Override
	public AdvanceFTPDataSource queryFTPDataSource(String name)
			throws IOException {
		return XSerializables.parseItem(comm.query(
				XSerializables.createRequest("query-ftp-data-source", "name", name)), 
				AdvanceFTPDataSource.CREATOR);
	}
	@Override
	public AdvanceWebDataSource queryWebDataSource(String name)
			throws IOException {
		return XSerializables.parseItem(comm.query(
				XSerializables.createRequest("query-web-data-source", "name", name)), 
				AdvanceWebDataSource.CREATOR);
	}
	@Override
	public AdvanceLocalFileDataSource queryLocalFileDataSource(String name)
			throws IOException {
		return XSerializables.parseItem(comm.query(
				XSerializables.createRequest("query-local-file-data-source", "name", name)), 
				AdvanceLocalFileDataSource.CREATOR);
	}
	@Override
	public XElement queryBlockState(String realm, String blockId)
			throws IOException {
		return comm.query(
				XSerializables.createRequest("query-block-state", "realm", realm, "block-id", blockId));
	}
	@Override
	public void updateBlockState(String realm, String blockId, XElement state)
			throws IOException {
		XElement request = XSerializables.createRequest("update-block-state", "realm", realm, "block-id", blockId);
		request.add(state);
		comm.send(request);
	}
	@Override
	public XElement queryFlow(String realm) throws IOException {
		return comm.query(
				XSerializables.createRequest("query-flow", "realm", realm));
	}
	@Override
	public List<AdvanceSOAPChannel> querySOAPChannels() throws IOException,
			AdvanceControlException {
		return XSerializables.parseList(comm.query(
				XSerializables.createRequest("query-soap-channels")), 
				"channel", AdvanceSOAPChannel.CREATOR);
	}
	@Override
	public void deleteBlockStates(String realm) throws IOException,
			AdvanceControlException {
		comm.send(XSerializables.createRequest("delete-block-states", "realm", realm));
	}
	@Override
	public void updateFlow(String realm, XElement flow) throws IOException,
			AdvanceControlException {
		XElement request = XSerializables.createRequest("update-flow", "realm", realm);
		request.add(flow.copy());
		comm.send(request);
	}
	@Override
	public void deleteEmailBox(String name) throws IOException,
			AdvanceControlException {
		comm.send(XSerializables.createRequest("delete-email-box", "name", name));
	}
	@Override
	public AdvanceEmailBox queryEmailBox(String name) throws IOException,
			AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-email-box", "name", name));
		return XSerializables.parseItem(response, AdvanceEmailBox.CREATOR);
	}
	@Override
	public List<AdvanceEmailBox> queryEmailBoxes() throws IOException,
			AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-email-boxes"));
		return XSerializables.parseList(response, "email-box", AdvanceEmailBox.CREATOR);
	}
	@Override
	public void updateEmailBox(AdvanceEmailBox box) throws IOException,
			AdvanceControlException {
		comm.send(XSerializables.createUpdate("update-email-box", box));
	}
	@Override
	public void deleteSOAPChannel(String name) throws IOException,
			AdvanceControlException {
		comm.send(XSerializables.createRequest("delete-soap-channel", "name", name));		
	}
	@Override
	public void updateSOAPChannel(AdvanceSOAPChannel channel)
			throws IOException, AdvanceControlException {
		comm.send(XSerializables.createUpdate("update-soap-channel", channel));		
	}
}
