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
import eu.advance.logistics.flow.engine.api.AdvanceEmailBox;
import eu.advance.logistics.flow.engine.api.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceSOAPChannel;
import eu.advance.logistics.flow.engine.api.AdvanceXMLExchange;
import eu.advance.logistics.flow.engine.api.AdvanceHttpListener;
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
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializables;

/**
 * Utility class to parse and compose DataStore request coming as XML format
 * and delegate it properly to an AdvanceDataStore object.
 * @author karnokd, 2011.09.27.
 */
public class HttpDataStoreListener implements AdvanceHttpListener  {
	/** The wrapped datastore. */
	protected final AdvanceDataStore datastore;
	/**
	 * Constructor to wrap a datastore.
	 * @param datastore the datastore to wrap
	 */
	public HttpDataStoreListener(AdvanceDataStore datastore) {
		this.datastore = datastore;
	}
	@Nullable
	@Override
	public void dispatch(@NonNull AdvanceXMLExchange exch) throws IOException, AdvanceControlException {
		XElement request = exch.request();
		String userName = exch.userName();
		AdvanceDataStore ds = new CheckedDataStore(datastore, userName);
		String function = request.name;
		if ("query-realms".equals(function)) {
			exch.next(XSerializables.storeList("realms", "realm", ds.queryRealms()));
		} else
		if ("query-realm".equals(function)) {
			exch.next(XSerializables.storeItem("realm", ds.queryRealm(request.get("realm"))));
		} else
		if ("create-realm".equals(function)) {
			ds.createRealm(request.get("realm"), userName);
		} else
		if ("delete-realm".equals(function)) {
			ds.deleteRealm(request.get("realm"));
		} else
		if ("update-realm".equals(function)) {
			ds.updateRealm(XSerializables.parseItem(request, AdvanceRealm.CREATOR));
		} else
		if ("query-users".equals(function)) {
			exch.next(XSerializables.storeList("users", "user", ds.queryUsers()));
		} else
		if ("query-user".equals(function)) {
			exch.next(XSerializables.storeItem("user", ds.queryUser(request.get("user-name"))));
		} else
		if ("enable-user".equals(function)) {
			ds.enableUser(request.get("user-name"), request.getBoolean("enabled"), userName);
		} else
		if ("delete-user".equals(function)) {
			ds.deleteUser(request.get("user-name"), userName);
		} else
		if ("update-user".equals(function)) {
			ds.updateUser(XSerializables.parseItem(request, AdvanceUser.CREATOR));
		} else
		if ("query-notification-groups".equals(function)) {
			exch.next(LocalDataStore.createGroups("notification-groups", ds.queryNotificationGroups()));
		} else
		if ("update-notification-groups".equals(function)) {
			ds.updateNotificationGroups(LocalDataStore.parseGroups(request));
		} else
		if ("query-jdbc-data-sources".equals(function)) {
			exch.next(XSerializables.storeList("jdbc-data-sources", "jdbc-source", ds.queryJDBCDataSources()));
		} else
		if ("update-jdbc-data-source".equals(function)) {
			ds.updateJDBCDataSource(XSerializables.parseItem(request, AdvanceJDBCDataSource.CREATOR));
		} else
		if ("delete-jdbc-data-source".equals(function)) {
			ds.deleteJDBCDataSource(request.get("data-source-name"));
		} else
		if ("query-jms-endpoints".equals(function)) {
			exch.next(XSerializables.storeList("jms-endpoints", "endpoint", ds.queryJMSEndpoints()));
		} else
		if ("update-jms-endpoint".equals(function)) {
			ds.updateJMSEndpoint(XSerializables.parseItem(request, AdvanceJMSEndpoint.CREATOR));
		} else
		if ("delete-jms-endpoint".equals(function)) {
			ds.deleteJMSEndpoint(request.get("jms-name"));
		} else
		if ("query-web-data-sources".equals(function)) {
			exch.next(XSerializables.storeList("web-data-sources", "web-source", ds.queryWebDataSources()));
		} else
		if ("update-web-data-source".equals(function)) {
			ds.updateWebDataSource(XSerializables.parseItem(request, AdvanceWebDataSource.CREATOR));
		} else
		if ("delete-web-data-source".equals(function)) {
			ds.deleteWebDataSource(request.get("web-name"));
		} else
		if ("query-ftp-data-sources".equals(function)) {
			exch.next(XSerializables.storeList("ftp-data-sources", "ftp-source", ds.queryFTPDataSources()));
		} else
		if ("update-ftp-data-source".equals(function)) {
			ds.updateFTPDataSource(XSerializables.parseItem(request, AdvanceFTPDataSource.CREATOR));
		} else
		if ("delete-ftp-data-source".equals(function)) {
			ds.deleteFTPDataSource(request.get("ftp-name"));
		} else
		if ("query-local-file-data-sources".equals(function)) {
			exch.next(XSerializables.storeList("local-file-data-sources", "local-source", ds.queryLocalFileDataSources()));
		} else
		if ("update-local-file-data-source".equals(function)) {
			ds.updateLocalFileDataSource(XSerializables.parseItem(request, AdvanceLocalFileDataSource.CREATOR));
		} else
		if ("delete-local-file-data-source".equals(function)) {
			ds.deleteLocalFileDataSource(request.get("file-name"));
		} else
		if ("query-keystores".equals(function)) {
			exch.next(XSerializables.storeList("keystores", "keystore", ds.queryKeyStores()));
		} else
		if ("query-keystore".equals(function)) {
			exch.next(XSerializables.storeItem("keystore", ds.queryKeyStore(request.get("name"))));
		} else
		if ("has-user-right".equals(function)) {
			XElement e = new XElement("boolean");
			e.content = String.valueOf(ds.hasUserRight(userName, AdvanceUserRights.valueOf(request.get("expected"))));
		} else
		if ("has-user-realm-right".equals(function)) {
			XElement e = new XElement("boolean");
			e.content = String.valueOf(ds.hasUserRight(userName, request.get("realm"), 
					AdvanceUserRealmRights.valueOf(request.get("expected"))));
			exch.next(e);
		} else
		if ("update-keystore".equals(function)) {
			ds.updateKeyStore(XSerializables.parseItem(request, AdvanceKeyStore.CREATOR));
		} else
		if ("delete-keystore".equals(function)) {
			ds.deleteKeyStore(request.get("keystore"));
		} else
		if ("query-notification-group".equals(function)) {
			Set<String> contacts = ds.queryNotificationGroup(AdvanceNotificationGroupType.valueOf(request.get("type")), request.get("name"));
			XElement response = new XElement("group");
			for (String s : contacts) {
				response.add("contact").set("value", s);
			}
			exch.next(response);
		} else
		if ("query-jdbc-data-source".equals(function)) {
			exch.next(XSerializables.storeItem("jdbc-source", ds.queryJDBCDataSource(request.get("name"))));
		} else
		if ("query-jms-endpoint".equals(function)) {
			exch.next(XSerializables.storeItem("endpoint", ds.queryJMSEndpoint(request.get("name"))));
		} else
		if ("query-soap-channel".equals(function)) {
			exch.next(XSerializables.storeItem("channel", ds.querySOAPChannel(request.get("name"))));
		} else
		if ("query-web-data-source".equals(function)) {
			exch.next(XSerializables.storeItem("web-source", ds.queryWebDataSource(request.get("name"))));
		} else
		if ("query-local-file-data-source".equals(function)) {
			exch.next(XSerializables.storeItem("local-source", ds.queryLocalFileDataSource(request.get("name"))));
		} else
		if ("query-block-state".equals(function)) {
			exch.next(ds.queryBlockState(request.get("realm"), request.get("block-id")));
		} else
		if ("update-block-state".equals(function)) {
			ds.updateBlockState(request.get("realm"), request.get("block-id"), request.children().get(0));
		} else
		if ("query-flow".equals(function)) {
			exch.next(ds.queryFlow(request.get("realm")));
		} else
		if ("query-soap-channels".equals(function)) {
			exch.next(XSerializables.storeList("soap-channels", "channel", ds.querySOAPChannels()));
		} else
		if ("update-flow".equals(function)) {
			ds.updateFlow(request.get("realm"), request.children().get(0).copy());
		} else
		if ("delete-block-states".equals(function)) {
			ds.deleteBlockStates(request.get("realm"));
		} else
		if ("query-email-boxes".equals(function)) {
			exch.next(XSerializables.storeList("email-boxes", "email-box", ds.queryEmailBoxes()));
		} else
		if ("query-email-box".equals(function)) {
			exch.next(XSerializables.storeItem("email-box", ds.queryEmailBox(request.get("name"))));
		} else
		if ("update-email-box".equals(function)) {
			ds.updateEmailBox(XSerializables.parseItem(request, AdvanceEmailBox.CREATOR));
		} else
		if ("delete-email-box".equals(function)) {
			ds.deleteEmailBox(request.get("name"));
		} else
		if ("update-soap-channel".equals(function)) {
			ds.updateSOAPChannel(XSerializables.parseItem(request, AdvanceSOAPChannel.CREATOR));
		} else
		if ("delete-soap-channel".equals(function)) {
			ds.deleteSOAPChannel(request.get("name"));
		} else {
			throw new AdvanceControlException("Unknown request " + request);
		}
	}
}
