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

package eu.advance.logistics.flow.engine.api.impl;

import hu.akarnokd.utils.xml.XNElement;
import hu.akarnokd.utils.xml.XNSerializables;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceHttpAuthentication;
import eu.advance.logistics.flow.engine.api.AdvanceXMLCommunicator;
import eu.advance.logistics.flow.engine.api.core.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceEmailBox;
import eu.advance.logistics.flow.engine.api.ds.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJMSEndpoint;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceLocalFileDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceLoginType;
import eu.advance.logistics.flow.engine.api.ds.AdvanceNotificationGroupType;
import eu.advance.logistics.flow.engine.api.ds.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.ds.AdvanceSOAPEndpoint;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUser;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRights;
import eu.advance.logistics.flow.engine.api.ds.AdvanceWebDataSource;

/**
 * A datastore which connects to a remote datastore via HTTP(s) and
 * exchanges messages via XML.
 * @author akarnokd, 2011.09.27.
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
		auth.password(password);
		
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
		XNElement response = comm.query(XNSerializables.createRequest("query-realms"));
		return XNSerializables.parseList(response, "realm", AdvanceRealm.CREATOR);
	}
	

	@Override
	public AdvanceRealm queryRealm(String realm)
			throws IOException, AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-realm", "realm", realm));
		return XNSerializables.parseItem(response, AdvanceRealm.CREATOR);
	}

	@Override
	public void createRealm(String realm, String byUser)
			throws IOException, AdvanceControlException {
		comm.send(XNSerializables.createRequest("create-realm", "realm", realm, "by-user", byUser));
	}

	@Override
	public void deleteRealm(String realm)
			throws IOException, AdvanceControlException {
		comm.send(XNSerializables.createRequest("delete-realm", "realm", realm));
	}

	@Override
	public void updateRealm(AdvanceRealm realm) throws IOException, AdvanceControlException {
		comm.send(XNSerializables.createUpdate("update-realm", realm));
	}

	@Override
	public List<AdvanceUser> queryUsers()
			throws IOException, AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-users"));
		return XNSerializables.parseList(response, "user", AdvanceUser.CREATOR);
	}

	@Override
	public AdvanceUser queryUser(String userName)
			throws IOException, AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-user", "user-name", userName));
		return XNSerializables.parseItem(response, AdvanceUser.CREATOR);
	}

	@Override
	public void enableUser(String userName,
			boolean enabled, String byUser) throws IOException, AdvanceControlException {
		comm.send(XNSerializables.createRequest("enable-user", "user-name", userName, "enabled", enabled, "by-user", byUser));
	}

	@Override
	public void deleteUser(String userName, String byUser)
			throws IOException, AdvanceControlException {
		comm.send(XNSerializables.createRequest("delete-user", "user-name", userName, "by-user", byUser));
	}

	@Override
	public void updateUser(AdvanceUser user)
			throws IOException, AdvanceControlException {
		comm.send(XNSerializables.createUpdate("update-user", user));
	}

	@Override
	public Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> queryNotificationGroups()
			throws IOException,
			AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-notification-groups"));
		return parseGroups(response);
	}

	@Override
	public void updateNotificationGroups(
			Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> groups)
			throws IOException, AdvanceControlException {
		XNElement request = createGroups("update-notification-groups", groups);
		comm.send(request);
	}
	/**
	 * Create an XNElement from the given complex map of notification groups.
	 * @param name the name of the element
	 * @param groups the map from group type to group to set of contact information
	 * @return the XNElement created
	 */
	private static XNElement createGroups(String name, Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> groups) {
		XNElement result = new XNElement(name);
		
		for (Map.Entry<AdvanceNotificationGroupType, Map<String, Collection<String>>> e : groups.entrySet()) {
			for (Map.Entry<String, Collection<String>> e2 : e.getValue().entrySet()) {
				XNElement xgroup = result.add("group");
				xgroup.set("name", e2.getKey());
				xgroup.set("type", e.getKey());
				for (String e3 : e2.getValue()) {
					xgroup.add("contact").set("value", e3);
				}
			}
		}
		return result;
	}
	/**
	 * Parse the given source into the complex map of notification groups and contacts.
	 * @param source the source XNElement
	 * @return the parsed map from group type to group name to contacts
	 */
	private static Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> parseGroups(XNElement source) {
		Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> result = Maps.newHashMap();
		
		for (XNElement xe : source.childrenWithName("group")) {
			String name = xe.get("name");
			AdvanceNotificationGroupType type = AdvanceNotificationGroupType.valueOf(xe.get("type"));
			for (XNElement xi : xe.childrenWithName("contact")) {
				Map<String, Collection<String>> groups = result.get(type);
				if (groups == null) {
					groups = Maps.newHashMap();
					result.put(type, groups);
				}
				Collection<String> contacts = groups.get(name);
				if (contacts == null) {
					contacts = Sets.newHashSet();
					groups.put(name, contacts);
				}
				contacts.add(xi.get("value"));
			}
		}
		return result;
	}
	
	@Override
	public List<AdvanceJDBCDataSource> queryJDBCDataSources() throws IOException,
			AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-jdbc-data-sources"));
		return XNSerializables.parseList(response, "jdbc-source", AdvanceJDBCDataSource.CREATOR);
	}

	@Override
	public void updateJDBCDataSource(
			AdvanceJDBCDataSource dataSource) throws IOException,
			AdvanceControlException {
		comm.send(XNSerializables.createUpdate("update-jdbc-data-source", dataSource));
	}

	@Override
	public void deleteJDBCDataSource(String dataSourceName)
			throws IOException, AdvanceControlException {
		comm.send(XNSerializables.createRequest("delete-jdbc-data-source", "data-source-name", dataSourceName));
	}

	@Override
	public List<AdvanceJMSEndpoint> queryJMSEndpoints()
			throws IOException, AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-jms-endpoints"));
		return XNSerializables.parseList(response, "endpoint", AdvanceJMSEndpoint.CREATOR);
	}

	@Override
	public void updateJMSEndpoint(
			AdvanceJMSEndpoint endpoint) throws IOException,
			AdvanceControlException {
		comm.send(XNSerializables.createUpdate("update-jms-endpoint", endpoint));
	}

	@Override
	public void deleteJMSEndpoint(String jmsName)
			throws IOException, AdvanceControlException {
		comm.send(XNSerializables.createRequest("delete-jms-endpoint", "jms-name", jmsName));
	}

	@Override
	public List<AdvanceWebDataSource> queryWebDataSources() throws IOException,
			AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-web-data-sources"));
		return XNSerializables.parseList(response, "web-source", AdvanceWebDataSource.CREATOR);
	}

	@Override
	public void updateWebDataSource(
			AdvanceWebDataSource endpoint) throws IOException,
			AdvanceControlException {
		comm.send(XNSerializables.createUpdate("update-web-data-source", endpoint));
	}

	@Override
	public void deleteWebDataSource(String webName)
			throws IOException, AdvanceControlException {
		comm.send(XNSerializables.createRequest("delete-web-data-source", "web-name", webName));
	}

	@Override
	public List<AdvanceFTPDataSource> queryFTPDataSources() throws IOException,
			AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-ftp-data-sources"));
		return XNSerializables.parseList(response, "ftp-source", AdvanceFTPDataSource.CREATOR);
	}

	@Override
	public void updateFTPDataSource(
			AdvanceFTPDataSource dataSource) throws IOException,
			AdvanceControlException {
		comm.send(XNSerializables.createUpdate("update-ftp-data-source", dataSource));
	}

	@Override
	public void deleteFTPDataSource(String ftpName)
			throws IOException, AdvanceControlException {
		comm.send(XNSerializables.createRequest("delete-ftp-data-source", "ftp-name", ftpName));
	}

	@Override
	public List<AdvanceLocalFileDataSource> queryLocalFileDataSources(
			) throws IOException,
			AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-local-file-data-sources"));
		return XNSerializables.parseList(response, "local-source", AdvanceLocalFileDataSource.CREATOR);
	}

	@Override
	public void updateLocalFileDataSource(
			AdvanceLocalFileDataSource dataSource) throws IOException,
			AdvanceControlException {
		comm.send(XNSerializables.createUpdate("update-local-file-data-source", dataSource));
	}

	@Override
	public void deleteLocalFileDataSource(String fileName)
			throws IOException, AdvanceControlException {
		comm.send(XNSerializables.createRequest("delete-local-file-data-source", "file-name", fileName));
	}

	@Override
	public List<AdvanceKeyStore> queryKeyStores()
			throws IOException, AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-keystores"));
		return XNSerializables.parseList(response, "keystore", AdvanceKeyStore.CREATOR);
	}

	@Override
	public AdvanceKeyStore queryKeyStore(String name)
			throws IOException, AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-keystore", "name", name));
		return XNSerializables.parseItem(response, AdvanceKeyStore.CREATOR);
	}

	@Override
	public boolean hasUserRight(String userName,
			AdvanceUserRights expected) throws IOException {
		XNElement response = comm.query(XNSerializables.createRequest("has-user-right", "expected", expected));
		return "true".equals(response.content);
	}

	@Override
	public boolean hasUserRight(String userName, String realm,
			AdvanceUserRealmRights expected) throws IOException {
		XNElement response = comm.query(XNSerializables.createRequest("has-user-realm-right", "realm", realm, "expected", expected));
		return "true".equals(response.content);
	}

	@Override
	public void updateKeyStore(
			AdvanceKeyStore keyStore) throws IOException,
			AdvanceControlException {
		comm.send(XNSerializables.createUpdate("update-keystore", keyStore));
	}

	@Override
	public void deleteKeyStore(String keyStore)
			throws IOException, AdvanceControlException {
		comm.send(XNSerializables.createRequest("delete-keystore", "keystore", keyStore));
	}
	@Override
	public Set<String> queryNotificationGroup(
			AdvanceNotificationGroupType type, String name) throws IOException {
		XNElement response = comm.query(XNSerializables.createRequest("query-notification-group", "type", type, "name", name));
		Set<String> result = Sets.newHashSet();
		for (XNElement x : response.childrenWithName("contact")) {
			result.add(x.get("value"));
		}
		return result;
	}
	@Override
	public AdvanceJDBCDataSource queryJDBCDataSource(String name)
			throws IOException {
		return XNSerializables.parseItem(comm.query(
				XNSerializables.createRequest("query-jdbc-data-source", "name", name)), 
				AdvanceJDBCDataSource.CREATOR);
	}
	@Override
	public AdvanceJMSEndpoint queryJMSEndpoint(String name) throws IOException {
		return XNSerializables.parseItem(comm.query(
				XNSerializables.createRequest("query-jms-endpoint", "name", name)), 
				AdvanceJMSEndpoint.CREATOR);
	}
	@Override
	public AdvanceSOAPEndpoint querySOAPEndpoint(String name) throws IOException {
		return XNSerializables.parseItem(comm.query(
				XNSerializables.createRequest("query-soap-channel", "name", name)), 
				AdvanceSOAPEndpoint.CREATOR);
	}
	@Override
	public AdvanceFTPDataSource queryFTPDataSource(String name)
			throws IOException {
		return XNSerializables.parseItem(comm.query(
				XNSerializables.createRequest("query-ftp-data-source", "name", name)), 
				AdvanceFTPDataSource.CREATOR);
	}
	@Override
	public AdvanceWebDataSource queryWebDataSource(String name)
			throws IOException {
		return XNSerializables.parseItem(comm.query(
				XNSerializables.createRequest("query-web-data-source", "name", name)), 
				AdvanceWebDataSource.CREATOR);
	}
	@Override
	public AdvanceLocalFileDataSource queryLocalFileDataSource(String name)
			throws IOException {
		return XNSerializables.parseItem(comm.query(
				XNSerializables.createRequest("query-local-file-data-source", "name", name)), 
				AdvanceLocalFileDataSource.CREATOR);
	}
	@Override
	public XNElement queryBlockState(String realm, String blockId)
			throws IOException {
		return comm.query(
				XNSerializables.createRequest("query-block-state", "realm", realm, "block-id", blockId));
	}
	@Override
	public void updateBlockState(String realm, String blockId, XNElement state)
			throws IOException {
		XNElement request = XNSerializables.createRequest("update-block-state", "realm", realm, "block-id", blockId);
		if (state != null) {
			request.add(state.copy());
		}
		comm.send(request);
	}
	@Override
	public XNElement queryFlow(String realm) throws IOException {
		return comm.query(
				XNSerializables.createRequest("query-flow", "realm", realm));
	}
	@Override
	public List<AdvanceSOAPEndpoint> querySOAPEndpoints() throws IOException,
			AdvanceControlException {
		return XNSerializables.parseList(comm.query(
				XNSerializables.createRequest("query-soap-channels")), 
				"channel", AdvanceSOAPEndpoint.CREATOR);
	}
	@Override
	public void deleteBlockStates(String realm) throws IOException,
			AdvanceControlException {
		comm.send(XNSerializables.createRequest("delete-block-states", "realm", realm));
	}
	@Override
	public void updateFlow(String realm, XNElement flow) throws IOException,
			AdvanceControlException {
		XNElement request = XNSerializables.createRequest("update-flow", "realm", realm);
		request.add(flow.copy());
		comm.send(request);
	}
	@Override
	public void deleteEmailBox(String name) throws IOException,
			AdvanceControlException {
		comm.send(XNSerializables.createRequest("delete-email-box", "name", name));
	}
	@Override
	public AdvanceEmailBox queryEmailBox(String name) throws IOException,
			AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-email-box", "name", name));
		return XNSerializables.parseItem(response, AdvanceEmailBox.CREATOR);
	}
	@Override
	public List<AdvanceEmailBox> queryEmailBoxes() throws IOException,
			AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-email-boxes"));
		return XNSerializables.parseList(response, "email-box", AdvanceEmailBox.CREATOR);
	}
	@Override
	public void updateEmailBox(AdvanceEmailBox box) throws IOException,
			AdvanceControlException {
		comm.send(XNSerializables.createUpdate("update-email-box", box));
	}
	@Override
	public void deleteSOAPEndpoint(String name) throws IOException,
			AdvanceControlException {
		comm.send(XNSerializables.createRequest("delete-soap-channel", "name", name));		
	}
	@Override
	public void updateSOAPEndpoint(AdvanceSOAPEndpoint channel)
			throws IOException, AdvanceControlException {
		comm.send(XNSerializables.createUpdate("update-soap-channel", channel));		
	}
}
