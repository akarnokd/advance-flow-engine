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
import java.util.Collection;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.api.AdvanceHttpListener;
import eu.advance.logistics.flow.engine.api.AdvanceXMLExchange;
import eu.advance.logistics.flow.engine.api.core.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceEmailBox;
import eu.advance.logistics.flow.engine.api.ds.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJMSEndpoint;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceLocalFileDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceNotificationGroupType;
import eu.advance.logistics.flow.engine.api.ds.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.ds.AdvanceSOAPEndpoint;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUser;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRights;
import eu.advance.logistics.flow.engine.api.ds.AdvanceWebDataSource;

/**
 * Utility class to parse and compose DataStore request coming as XML format
 * and delegate it properly to an AdvanceDataStore object.
 * @author akarnokd, 2011.09.27.
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
	public AdvanceXMLExchange dispatch(@NonNull final XNElement request, @NonNull final String userName) throws IOException, AdvanceControlException {
		AdvanceDataStore ds = new CheckedDataStore(datastore, userName);
		String function = request.name;
		if ("query-realms".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeList("realms", "realm", ds.queryRealms()));
		} else
		if ("query-realm".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeItem("realm", ds.queryRealm(request.get("realm"))));
		} else
		if ("create-realm".equals(function)) {
			ds.createRealm(request.get("realm"), userName);
			return AdvanceXMLExchange.none();
		} else
		if ("delete-realm".equals(function)) {
			ds.deleteRealm(request.get("realm"));
			return AdvanceXMLExchange.none();
		} else
		if ("update-realm".equals(function)) {
			ds.updateRealm(XNSerializables.parseItem(request, AdvanceRealm.CREATOR));
			return AdvanceXMLExchange.none();
		} else
		if ("query-users".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeList("users", "user", ds.queryUsers()));
		} else
		if ("query-user".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeItem("user", ds.queryUser(request.get("user-name"))));
		} else
		if ("enable-user".equals(function)) {
			ds.enableUser(request.get("user-name"), request.getBoolean("enabled"), userName);
			return AdvanceXMLExchange.none();
		} else
		if ("delete-user".equals(function)) {
			ds.deleteUser(request.get("user-name"), userName);
			return AdvanceXMLExchange.none();
		} else
		if ("update-user".equals(function)) {
			ds.updateUser(XNSerializables.parseItem(request, AdvanceUser.CREATOR));
			return AdvanceXMLExchange.none();
		} else
		if ("query-notification-groups".equals(function)) {
			return AdvanceXMLExchange.single(LocalDataStore.createGroups("notification-groups", ds.queryNotificationGroups()));
		} else
		if ("update-notification-groups".equals(function)) {
			ds.updateNotificationGroups(LocalDataStore.parseGroups(request));
			return AdvanceXMLExchange.none();
		} else
		if ("query-jdbc-data-sources".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeList("jdbc-data-sources", "jdbc-source", ds.queryJDBCDataSources()));
		} else
		if ("update-jdbc-data-source".equals(function)) {
			ds.updateJDBCDataSource(XNSerializables.parseItem(request, AdvanceJDBCDataSource.CREATOR));
			return AdvanceXMLExchange.none();
		} else
		if ("delete-jdbc-data-source".equals(function)) {
			ds.deleteJDBCDataSource(request.get("data-source-name"));
			return AdvanceXMLExchange.none();
		} else
		if ("query-jms-endpoints".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeList("jms-endpoints", "endpoint", ds.queryJMSEndpoints()));
		} else
		if ("update-jms-endpoint".equals(function)) {
			ds.updateJMSEndpoint(XNSerializables.parseItem(request, AdvanceJMSEndpoint.CREATOR));
			return AdvanceXMLExchange.none();
		} else
		if ("delete-jms-endpoint".equals(function)) {
			ds.deleteJMSEndpoint(request.get("jms-name"));
			return AdvanceXMLExchange.none();
		} else
		if ("query-web-data-sources".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeList("web-data-sources", "web-source", ds.queryWebDataSources()));
		} else
		if ("update-web-data-source".equals(function)) {
			ds.updateWebDataSource(XNSerializables.parseItem(request, AdvanceWebDataSource.CREATOR));
			return AdvanceXMLExchange.none();
		} else
		if ("delete-web-data-source".equals(function)) {
			ds.deleteWebDataSource(request.get("web-name"));
			return AdvanceXMLExchange.none();
		} else
		if ("query-ftp-data-sources".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeList("ftp-data-sources", "ftp-source", ds.queryFTPDataSources()));
		} else
		if ("update-ftp-data-source".equals(function)) {
			ds.updateFTPDataSource(XNSerializables.parseItem(request, AdvanceFTPDataSource.CREATOR));
			return AdvanceXMLExchange.none();
		} else
		if ("delete-ftp-data-source".equals(function)) {
			ds.deleteFTPDataSource(request.get("ftp-name"));
			return AdvanceXMLExchange.none();
		} else
		if ("query-local-file-data-sources".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeList("local-file-data-sources", "local-source", ds.queryLocalFileDataSources()));
		} else
		if ("update-local-file-data-source".equals(function)) {
			ds.updateLocalFileDataSource(XNSerializables.parseItem(request, AdvanceLocalFileDataSource.CREATOR));
			return AdvanceXMLExchange.none();
		} else
		if ("delete-local-file-data-source".equals(function)) {
			ds.deleteLocalFileDataSource(request.get("file-name"));
			return AdvanceXMLExchange.none();
		} else
		if ("query-keystores".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeList("keystores", "keystore", ds.queryKeyStores()));
		} else
		if ("query-keystore".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeItem("keystore", ds.queryKeyStore(request.get("name"))));
		} else
		if ("has-user-right".equals(function)) {
			XNElement e = new XNElement("boolean");
			e.content = String.valueOf(ds.hasUserRight(userName, AdvanceUserRights.valueOf(request.get("expected"))));
			return AdvanceXMLExchange.single(e);
		} else
		if ("has-user-realm-right".equals(function)) {
			XNElement e = new XNElement("boolean");
			e.content = String.valueOf(ds.hasUserRight(userName, request.get("realm"), 
					AdvanceUserRealmRights.valueOf(request.get("expected"))));
			return AdvanceXMLExchange.single(e);
		} else
		if ("update-keystore".equals(function)) {
			ds.updateKeyStore(XNSerializables.parseItem(request, AdvanceKeyStore.CREATOR));
			return AdvanceXMLExchange.none();
		} else
		if ("delete-keystore".equals(function)) {
			ds.deleteKeyStore(request.get("keystore"));
			return AdvanceXMLExchange.none();
		} else
		if ("query-notification-group".equals(function)) {
			Collection<String> contacts = ds.queryNotificationGroup(AdvanceNotificationGroupType.valueOf(request.get("type")), request.get("name"));
			XNElement response = new XNElement("group");
			for (String s : contacts) {
				response.add("contact").set("value", s);
			}
			return AdvanceXMLExchange.single(response);
		} else
		if ("query-jdbc-data-source".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeItem("jdbc-source", ds.queryJDBCDataSource(request.get("name"))));
		} else
		if ("query-jms-endpoint".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeItem("endpoint", ds.queryJMSEndpoint(request.get("name"))));
		} else
		if ("query-soap-channel".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeItem("channel", ds.querySOAPEndpoint(request.get("name"))));
		} else
		if ("query-web-data-source".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeItem("web-source", ds.queryWebDataSource(request.get("name"))));
		} else
		if ("query-local-file-data-source".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeItem("local-source", ds.queryLocalFileDataSource(request.get("name"))));
		} else
		if ("query-block-state".equals(function)) {
			return AdvanceXMLExchange.single(ds.queryBlockState(request.get("realm"), request.get("block-id")));
		} else
		if ("update-block-state".equals(function)) {
			ds.updateBlockState(request.get("realm"), request.get("block-id"), request.children().get(0));
			return AdvanceXMLExchange.none();
		} else
		if ("query-flow".equals(function)) {
			return AdvanceXMLExchange.single(ds.queryFlow(request.get("realm")));
		} else
		if ("query-soap-channels".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeList("soap-channels", "channel", ds.querySOAPEndpoints()));
		} else
		if ("update-flow".equals(function)) {
			ds.updateFlow(request.get("realm"), request.children().get(0).copy());
			return AdvanceXMLExchange.none();
		} else
		if ("delete-block-states".equals(function)) {
			ds.deleteBlockStates(request.get("realm"));
			return AdvanceXMLExchange.none();
		} else
		if ("query-email-boxes".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeList("email-boxes", "email-box", ds.queryEmailBoxes()));
		} else
		if ("query-email-box".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeItem("email-box", ds.queryEmailBox(request.get("name"))));
		} else
		if ("update-email-box".equals(function)) {
			ds.updateEmailBox(XNSerializables.parseItem(request, AdvanceEmailBox.CREATOR));
			return AdvanceXMLExchange.none();
		} else
		if ("delete-email-box".equals(function)) {
			ds.deleteEmailBox(request.get("name"));
			return AdvanceXMLExchange.none();
		} else
		if ("update-soap-channel".equals(function)) {
			ds.updateSOAPEndpoint(XNSerializables.parseItem(request, AdvanceSOAPEndpoint.CREATOR));
			return AdvanceXMLExchange.none();
		} else
		if ("delete-soap-channel".equals(function)) {
			ds.deleteSOAPEndpoint(request.get("name"));
			return AdvanceXMLExchange.none();
		} else {
			throw new AdvanceControlException("Unknown request " + request);
		}
	}
}
