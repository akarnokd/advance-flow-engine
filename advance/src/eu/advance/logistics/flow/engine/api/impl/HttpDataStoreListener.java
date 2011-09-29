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
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceJMSEndpoint;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.AdvanceLocalFileDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceNotificationGroupType;
import eu.advance.logistics.flow.engine.api.AdvanceUser;
import eu.advance.logistics.flow.engine.api.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.api.AdvanceUserRights;
import eu.advance.logistics.flow.engine.api.AdvanceWebDataSource;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Utility class to parse and compose DataStore request coming as XML format
 * and delegate it properly to an AdvanceDataStore object.
 * @author karnokd, 2011.09.27.
 */
public class HttpDataStoreListener {
	/** The wrapped datastore. */
	protected final AdvanceDataStore datastore;
	/**
	 * Constructor to wrap a datastore.
	 * @param datastore the datastore to wrap
	 */
	public HttpDataStoreListener(AdvanceDataStore datastore) {
		this.datastore = datastore;
	}
	/**
	 * Dispatch the request to the proper datastore function.
	 * @param userName the requestor
	 * @param request the request XML
	 * @return the response XElement or null if no response
	 * @throws IOException when the underlying datastore throws this exception
	 * @throws AdvanceControlException if the user has no right to execute the request or it is malformed
	 */
	@Nullable
	public XElement dispatch(@NonNull String userName, @NonNull XElement request) throws IOException, AdvanceControlException {
		AdvanceDataStore ds = new CheckedDataStore(datastore, userName);
		String function = request.name;
		if ("query-realms".equals(function)) {
			return HttpRemoteUtils.storeList("realms", "realm", ds.queryRealms());
		} else
		if ("query-realm".equals(function)) {
			return HttpRemoteUtils.storeItem("realm", ds.queryRealm(request.get("realm")));
		} else
		if ("create-realm".equals(function)) {
			ds.createRealm(request.get("realm"), userName);
			return null;
		} else
		if ("delete-realm".equals(function)) {
			ds.deleteRealm(request.get("realm"));
			return null;
		} else
		if ("rename-realm".equals(function)) {
			ds.renameRealm(request.get("realm"), request.get("new-realm"), userName);
			return null;
		} else
		if ("query-users".equals(function)) {
			return HttpRemoteUtils.storeList("users", "user", ds.queryUsers());
		} else
		if ("query-user".equals(function)) {
			return HttpRemoteUtils.storeItem("user", ds.queryUser(request.get("user-name")));
		} else
		if ("enable-user".equals(function)) {
			ds.enableUser(request.get("user-name"), request.getBoolean("enabled"), userName);
			return null;
		} else
		if ("delete-user".equals(function)) {
			ds.deleteUser(request.get("user-name"), userName);
			return null;
		} else
		if ("update-user".equals(function)) {
			ds.updateUser(HttpRemoteUtils.parseItem(request, AdvanceUser.CREATOR));
			return null;
		} else
		if ("query-notification-groups".equals(function)) {
			return LocalDataStore.createGroups("notification-groups", ds.queryNotificationGroups());
		} else
		if ("update-notification-groups".equals(function)) {
			ds.updateNotificationGroups(LocalDataStore.parseGroups(request));
			return null;
		} else
		if ("query-jdbc-data-sources".equals(function)) {
			return HttpRemoteUtils.storeList("jdbc-data-sources", "jdbc-source", ds.queryJDBCDataSources());
		} else
		if ("update-jdbc-data-source".equals(function)) {
			ds.updateJDBCDataSource(HttpRemoteUtils.parseItem(request, AdvanceJDBCDataSource.CREATOR));
			return null;
		} else
		if ("delete-jdbc-data-source".equals(function)) {
			ds.deleteJDBCDataSource(request.get("data-source-name"));
			return null;
		} else
		if ("query-jms-endpoints".equals(function)) {
			return HttpRemoteUtils.storeList("jms-endpoints", "endpoint", ds.queryJMSEndpoints());
		} else
		if ("update-jms-endpoint".equals(function)) {
			ds.updateJMSEndpoint(HttpRemoteUtils.parseItem(request, AdvanceJMSEndpoint.CREATOR));
			return null;
		} else
		if ("delete-jms-endpoint".equals(function)) {
			ds.deleteJMSEndpoint(request.get("jms-name"));
			return null;
		} else
		if ("query-web-data-sources".equals(function)) {
			return HttpRemoteUtils.storeList("web-data-sources", "web-source", ds.queryWebDataSources());
		} else
		if ("update-web-data-source".equals(function)) {
			ds.updateWebDataSource(HttpRemoteUtils.parseItem(request, AdvanceWebDataSource.CREATOR));
			return null;
		} else
		if ("delete-web-data-source".equals(function)) {
			ds.deleteWebDataSource(request.get("web-name"));
			return null;
		} else
		if ("query-ftp-data-sources".equals(function)) {
			return HttpRemoteUtils.storeList("ftp-data-sources", "ftp-source", ds.queryFTPDataSources());
		} else
		if ("update-ftp-data-source".equals(function)) {
			ds.updateFTPDataSource(HttpRemoteUtils.parseItem(request, AdvanceFTPDataSource.CREATOR));
			return null;
		} else
		if ("delete-ftp-data-source".equals(function)) {
			ds.deleteFTPDataSource(request.get("ftp-name"));
			return null;
		} else
		if ("query-local-file-data-sources".equals(function)) {
			return HttpRemoteUtils.storeList("local-file-data-sources", "local-source", ds.queryLocalFileDataSources());
		} else
		if ("update-local-file-data-source".equals(function)) {
			ds.updateLocalFileDataSource(HttpRemoteUtils.parseItem(request, AdvanceLocalFileDataSource.CREATOR));
			return null;
		} else
		if ("delete-local-file-data-source".equals(function)) {
			ds.deleteLocalFileDataSource(request.get("file-name"));
			return null;
		} else
		if ("query-keystores".equals(function)) {
			return HttpRemoteUtils.storeList("keystores", "keystore", ds.queryKeyStores());
		} else
		if ("query-keystore".equals(function)) {
			return HttpRemoteUtils.storeItem("keystore", ds.queryKeyStore(request.get("name")));
		} else
		if ("has-user-right".equals(function)) {
			XElement e = new XElement("boolean");
			e.content = String.valueOf(ds.hasUserRight(userName, AdvanceUserRights.valueOf(request.get("expected"))));
			return e;
		} else
		if ("has-user-realm-right".equals(function)) {
			XElement e = new XElement("boolean");
			e.content = String.valueOf(ds.hasUserRight(userName, request.get("realm"), 
					AdvanceUserRealmRights.valueOf(request.get("expected"))));
			return e;
		} else
		if ("update-keystore".equals(function)) {
			ds.updateKeyStore(HttpRemoteUtils.parseItem(request, AdvanceKeyStore.CREATOR));
			return null;
		} else
		if ("delete-keystore".equals(function)) {
			ds.deleteKeyStore(request.get("keystore"));
			return null;
		} else
		if ("query-notification-group".equals(function)) {
			Set<String> contacts = ds.queryNotificationGroup(AdvanceNotificationGroupType.valueOf(request.get("type")), request.get("name"));
			XElement response = new XElement("group");
			for (String s : contacts) {
				response.add("contact").set("value", s);
			}
			return response;
		} else
		if ("query-jdbc-data-source".equals(function)) {
			return HttpRemoteUtils.storeItem("jdbc-source", ds.queryJDBCDataSource(request.get("name")));
		} else
		if ("query-jms-endpoint".equals(function)) {
			return HttpRemoteUtils.storeItem("endpoint", ds.queryJMSEndpoint(request.get("name")));
		} else
		if ("query-soap-channel".equals(function)) {
			return HttpRemoteUtils.storeItem("channel", ds.querySOAPChannel(request.get("name")));
		} else
		if ("query-web-data-source".equals(function)) {
			return HttpRemoteUtils.storeItem("web-source", ds.queryWebDataSource(request.get("name")));
		} else
		if ("query-local-file-data-source".equals(function)) {
			return HttpRemoteUtils.storeItem("local-source", ds.queryLocalFileDataSource(request.get("name")));
		} else
		if ("query-block-state".equals(function)) {
			return ds.queryBlockState(request.get("realm"), request.get("block-id"));
		} else
		if ("update-block-state".equals(function)) {
			ds.updateBlockState(request.get("realm"), request.get("block-id"), request.children().get(0));
			return null;
		} else
		if ("query-flow".equals(function)) {
			return ds.queryFlow(request.get("realm"));
		} else
		if ("query-soap-channels".equals(function)) {
			return HttpRemoteUtils.storeList("soap-channels", "channel", ds.querySOAPChannels());
		}
		
		throw new AdvanceControlException("Unknown request " + request);
	}
}
