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

import hu.akarnokd.reactive4java.base.Func0;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceControlToken;
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceJMSEndpoint;
import eu.advance.logistics.flow.engine.api.AdvanceLocalFileDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceUser;
import eu.advance.logistics.flow.engine.api.AdvanceWebDataSource;
import eu.advance.logistics.flow.engine.model.XSerializable;
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
	 * Create an XElement with the given name and items stored from the source sequence.
	 * @param container the container name
	 * @param item the item name
	 * @param source the source of items
	 * @return the list in XElement
	 */
	protected XElement storeList(String container, String item, Iterable<? extends XSerializable> source) {
		XElement result = new XElement(container);
		for (XSerializable e : source) {
			e.save(result.add(item));
		}
		return result;
	}
	/**
	 * Store the value of a single serializable object with the given element name.
	 * @param itemName the item element name
	 * @param source the object to store
	 * @return the created XElement
	 */
	protected XElement storeItem(String itemName, XSerializable source) {
		XElement result = new XElement(itemName);
		source.save(result);
		return result;
	}
	/**
	 * Create an XSerializable object through the {@code creator} function
	 * and load it from the {@code item}.
	 * @param <T> the XSerializable object
	 * @param item the item to load from
	 * @param creator the function to create Ts
	 * @return the created and loaded object
	 */
	protected <T extends XSerializable> T parseItem(XElement item, Func0<T> creator) {
		T result = creator.invoke();
		result.load(item);
		return result;
	}
	/**
	 * Dispatch the request to the proper datastore function.
	 * @param token the control token for the data access checks
	 * @param request the request XML
	 * @return the response XElement or null if no response
	 * @throws IOException when the underlying datastore throws this exception
	 * @throws AdvanceControlException if the user has no right to execute the request or it is malformed
	 */
	@Nullable
	public XElement dispatch(@NonNull AdvanceControlToken token, @NonNull XElement request) throws IOException, AdvanceControlException {
		String function = request.name;
		if ("query-realms".equals(function)) {
			return storeList("realms", "realm", datastore.queryRealms(token));
		} else
		if ("query-realm".equals(function)) {
			return storeItem("realm", datastore.queryRealm(token, request.get("realm")));
		} else
		if ("create-realm".equals(function)) {
			datastore.createRealm(token, request.get("realm"));
			return null;
		} else
		if ("delete-realm".equals(function)) {
			datastore.deleteRealm(token, request.get("realm"));
			return null;
		} else
		if ("rename-realm".equals(function)) {
			datastore.renameRealm(token, request.get("realm"), request.get("new-realm"));
			return null;
		} else
		if ("query-users".equals(function)) {
			return storeList("users", "user", datastore.queryUsers(token));
		} else
		if ("query-user".equals(function)) {
			return storeItem("user", datastore.queryUser(token, request.getInt("user-id")));
		} else
		if ("enable-user".equals(function)) {
			datastore.enableUser(token, request.getInt("user-id"), request.getBoolean("enabled"));
			return null;
		} else
		if ("delete-user".equals(function)) {
			datastore.deleteUser(token, request.getInt("user-id"));
			return null;
		} else
		if ("update-user".equals(function)) {
			datastore.updateUser(token, parseItem(request, AdvanceUser.CREATOR));
			return null;
		} else
		if ("query-notification-groups".equals(function)) {
			return LocalDataStore.createGroups("notification-groups", datastore.queryNotificationGroups(token));
		} else
		if ("update-notification-groups".equals(function)) {
			datastore.updateNotificationGroups(token, LocalDataStore.parseGroups(request));
			return null;
		} else
		if ("query-jdbc-data-sources".equals(function)) {
			return storeList("jdbc-data-sources", "jdbc-source", datastore.queryJDBCDataSources(token));
		} else
		if ("update-jdbc-data-source".equals(function)) {
			datastore.updateJDBCDataSource(token, parseItem(request, AdvanceJDBCDataSource.CREATOR));
			return null;
		} else
		if ("delete-jdbc-data-source".equals(function)) {
			datastore.deleteJDBCDataSource(token, request.getInt("data-source-id"));
			return null;
		} else
		if ("query-jms-endpoints".equals(function)) {
			return storeList("jms-endpoints", "endpoint", datastore.queryJMSEndpoints(token));
		} else
		if ("update-jms-endpoint".equals(function)) {
			datastore.updateJMSEndpoint(token, parseItem(request, AdvanceJMSEndpoint.CREATOR));
			return null;
		} else
		if ("delete-jms-endpoint".equals(function)) {
			datastore.deleteJMSEndpoint(token, request.getInt("jms-id"));
			return null;
		} else
		if ("query-web-data-sources".equals(function)) {
			return storeList("web-data-sources", "web-source", datastore.queryWebDataSources(token));
		} else
		if ("update-web-data-source".equals(function)) {
			datastore.updateWebDataSource(token, parseItem(request, AdvanceWebDataSource.CREATOR));
			return null;
		} else
		if ("delete-web-data-source".equals(function)) {
			datastore.deleteWebDataSource(token, request.getInt("web-id"));
			return null;
		} else
		if ("query-ftp-data-sources".equals(function)) {
			return storeList("ftp-data-sources", "ftp-source", datastore.queryFTPDataSources(token));
		} else
		if ("update-ftp-data-source".equals(function)) {
			datastore.updateFTPDataSource(token, parseItem(request, AdvanceFTPDataSource.CREATOR));
			return null;
		} else
		if ("delete-ftp-data-source".equals(function)) {
			datastore.deleteFTPDataSource(token, request.getInt("ftp-id"));
			return null;
		} else
		if ("query-local-file-data-sources".equals(function)) {
			return storeList("local-file-data-sources", "local-source", datastore.queryLocalFileDataSources(token));
		} else
		if ("update-local-file-data-source".equals(function)) {
			datastore.updateLocalFileDataSource(token, parseItem(request, AdvanceLocalFileDataSource.CREATOR));
			return null;
		} else
		if ("delete-local-file-data-source".equals(function)) {
			datastore.deleteLocalFileDataSource(token, request.getInt("file-id"));
			return null;
		} else
		if ("query-keystores".equals(function)) {
			return storeList("keystores", "keystore", datastore.queryKeyStores(token));
		}
		// TODO implement remaining requests
		
		throw new AdvanceControlException("Unknown function " + function);
	}
}
