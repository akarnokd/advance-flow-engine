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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.api.AdvanceAccessDenied;
import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceCreateModifyInfo;
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.AdvanceEmailBox;
import eu.advance.logistics.flow.engine.api.AdvanceFTPDataSource;
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
import eu.advance.logistics.flow.engine.api.Copyable;
import eu.advance.logistics.flow.engine.api.HasPassword;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * A datastore wrapper which checks at each query whether the user
 * is allowed to perform that operation.
 * <p>Whenever an updateXYZ() method is called, the class replaces the {@code modifiedBy} field
 * with the user name.</p>
 * @author karnokd, 2011.09.29.
 */
public class CheckedDataStore implements AdvanceDataStore {
	/** The logger object. */
	protected static final Logger LOG = LoggerFactory.getLogger(CheckedDataStore.class);
	/** The wrapped datastore. */
	protected final AdvanceDataStore datastore;
	/** The target user name. */
	protected final String userName;
	/**
	 * Check if the expected right is present for the user.
	 * @param expected the expected right
	 * @throws AdvanceAccessDenied thrown if the user has no right
	 * @throws IOException if a network error occurs
	 */
	protected void check(AdvanceUserRights expected) throws IOException, AdvanceAccessDenied {
		if (!datastore.hasUserRight(userName, expected)) {
			AdvanceAccessDenied e = new AdvanceAccessDenied(expected.toString());
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	/**
	 * Check if the expected right is present for the user.
	 * @param expected the expected rights
	 * @throws AdvanceAccessDenied thrown if the user has no right
	 * @throws IOException if a network error occurs
	 */
	protected void check(AdvanceUserRights... expected) throws IOException, AdvanceAccessDenied {
		boolean allow = false;
		for (AdvanceUserRights r : expected) {
			allow |= datastore.hasUserRight(userName, r);
		}
		if (!allow) {
			AdvanceAccessDenied e = new AdvanceAccessDenied(Arrays.toString(expected));
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	/**
	 * Check if the expected right is present for the user.
	 * @param realm the realm name
	 * @param expected the expected right
	 * @throws AdvanceAccessDenied thrown if the user has no right
	 * @throws IOException if a network error occurs
	 */
	protected void check(String realm, AdvanceUserRealmRights expected) throws IOException, AdvanceAccessDenied {
		if (!datastore.hasUserRight(userName, realm, expected)) {
			AdvanceAccessDenied e = new AdvanceAccessDenied(expected.toString());
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	/**
	 * Check if the expected right is present for the user.
	 * @param realm the realm name
	 * @param expected the expected rights
	 * @throws AdvanceAccessDenied thrown if the user has no right
	 * @throws IOException if a network error occurs
	 */
	protected void check(String realm, AdvanceUserRealmRights... expected) throws IOException, AdvanceAccessDenied {
		boolean allow = false;
		for (AdvanceUserRealmRights r : expected) {
			allow |= datastore.hasUserRight(userName, realm, r);
		}
		if (!allow) {
			AdvanceAccessDenied e = new AdvanceAccessDenied(expected.toString());
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	/**
	 * Clear the password from the given sequence of objects.
	 * @param <K> an iterable sequence were the elements implement HasPassword
	 * @param source the source sequence
	 * @return the source
	 */
	protected <K extends Iterable<? extends HasPassword>> K clearPassword(K source) {
		for (HasPassword pw : source) {
			pw.password(null);
		}
		return source;
	}
	/**
	 * Clear the password field of the given source object.
	 * @param <K> a type which implements {@code HasPassword}
	 * @param source the source object with password field
	 * @return the {@code source} object itself
	 */
	protected <K extends HasPassword> K clearPassword(K source) {
		source.password(null);
		return source;
	}
	/**
	 * Overwrite the {@code modifiedBy} field of the object with the current user.
	 * @param <T> a type with AdvanceCreateModifyInfo records
	 * @param obj the object to change
	 * @return the {@code obj} itself
	 */
	protected <T extends AdvanceCreateModifyInfo & Copyable<T>> T changeModifiedBy(T obj) {
		obj = obj.copy();
		obj.modifiedBy = userName;
		return obj;
	}
	/**
	 * Construct the wrapper for the given datastore and user name.
	 * @param datastore the datastore to wrap
	 * @param userName the user name to check the access rights
	 */
	public CheckedDataStore(AdvanceDataStore datastore, String userName) {
		this.datastore = datastore;
		this.userName = userName;
	}

	@Override
	public List<AdvanceRealm> queryRealms() throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_REALMS, AdvanceUserRights.MODIFY_USER);
		List<AdvanceRealm> result = Lists.newArrayList();
		AdvanceUser user = datastore.queryUser(userName);
		for (AdvanceRealm r : datastore.queryRealms()) {
			if (user.rights.contains(AdvanceUserRights.MODIFY_USER)
					|| user.realmRights.containsEntry(r.name, AdvanceUserRealmRights.LIST)) {
				result.add(r);
			}
		}
		return result;
	}

	@Override
	public AdvanceRealm queryRealm(String realm) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_REALMS);
		check(realm, AdvanceUserRealmRights.LIST);
		return datastore.queryRealm(realm);
	}

	@Override
	public void createRealm(String realm, String byUser) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.CREATE_REALM);
		datastore.createRealm(realm, userName);
	}

	@Override
	public void deleteRealm(String realm) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.DELETE_REALM);
		datastore.deleteRealm(realm);
	}

	@Override
	public void updateRealm(AdvanceRealm realm) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.MODIFY_REALM);
		datastore.updateRealm(changeModifiedBy(realm));
	}

	@Override
	public List<AdvanceUser> queryUsers() throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_USERS);
		return clearPassword(datastore.queryUsers());
	}

	@Override
	public AdvanceUser queryUser(String userName) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_USERS);
		return clearPassword(datastore.queryUser(userName));
	}

	@Override
	public void enableUser(String userName, boolean enabled, String byUser)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.MODIFY_USER);
		datastore.enableUser(userName, enabled, this.userName);
	}

	@Override
	public void deleteUser(String userName, String byUser) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.DELETE_USER);
		datastore.deleteUser(userName, this.userName);
	}

	@Override
	public void updateUser(AdvanceUser user) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.CREATE_USER, AdvanceUserRights.MODIFY_USER);
		datastore.updateUser(changeModifiedBy(user.copy()));
	}

	@Override
	public Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> queryNotificationGroups()
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.LIST_NOTIFICATION_GROUPS);
		return datastore.queryNotificationGroups();
	}

	@Override
	public void updateNotificationGroups(
			Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> groups)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.MODIFY_NOTIFICATION_GROUPS);
		datastore.updateNotificationGroups(groups);
	}

	@Override
	public List<AdvanceJDBCDataSource> queryJDBCDataSources()
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.LIST_JDBC_DATA_SOURCES);
		return clearPassword(datastore.queryJDBCDataSources());
	}

	@Override
	public void updateJDBCDataSource(AdvanceJDBCDataSource dataSource)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.CREATE_JDBC_DATA_SOURCE, AdvanceUserRights.MODIFY_JDBC_DATA_SOURCE);
		datastore.updateJDBCDataSource(changeModifiedBy(dataSource));
	}

	@Override
	public void deleteJDBCDataSource(String dataSourceName) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.DELETE_JDBC_DATA_SOURCE);
		datastore.deleteJDBCDataSource(dataSourceName);
	}

	@Override
	public List<AdvanceJMSEndpoint> queryJMSEndpoints() throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_JMS_ENDPOINTS);
		return clearPassword(datastore.queryJMSEndpoints());
	}

	@Override
	public void updateJMSEndpoint(AdvanceJMSEndpoint endpoint)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.CREATE_JMS_ENDPOINT, AdvanceUserRights.MODIFY_JMS_ENDPOINT);
		datastore.updateJMSEndpoint(changeModifiedBy(endpoint));
	}

	@Override
	public void deleteJMSEndpoint(String jmsName) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.DELETE_JMS_ENDPOINT);
		datastore.deleteJMSEndpoint(jmsName);
	}

	@Override
	public List<AdvanceWebDataSource> queryWebDataSources() throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_WEB_DATA_SOURCES);
		return clearPassword(datastore.queryWebDataSources());
	}

	@Override
	public void updateWebDataSource(AdvanceWebDataSource endpoint)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.CREATE_WEB_DATA_SOURCE, AdvanceUserRights.MODIFY_WEB_DATA_SOURCE);
		datastore.updateWebDataSource(changeModifiedBy(endpoint));
	}

	@Override
	public void deleteWebDataSource(String webName) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.DELETE_WEB_DATA_SOURCE);
		datastore.deleteWebDataSource(webName);
	}

	@Override
	public List<AdvanceFTPDataSource> queryFTPDataSources() throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_FTP_DATA_SOURCES);
		return clearPassword(datastore.queryFTPDataSources());
	}

	@Override
	public void updateFTPDataSource(AdvanceFTPDataSource dataSource)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.CREATE_FTP_DATA_SOURCE, AdvanceUserRights.MODIFY_FTP_DATA_SOURCE);
		datastore.updateFTPDataSource(changeModifiedBy(dataSource));

	}

	@Override
	public void deleteFTPDataSource(String ftpName) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.DELETE_FTP_DATA_SOURCE);
		datastore.deleteFTPDataSource(ftpName);
	}

	@Override
	public List<AdvanceLocalFileDataSource> queryLocalFileDataSources()
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.LIST_LOCAL_FILE_DATA_SOURCES);
		return datastore.queryLocalFileDataSources();
	}

	@Override
	public void updateLocalFileDataSource(AdvanceLocalFileDataSource dataSource)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.CREATE_LOCAL_FILE_DATA_SOURCE, AdvanceUserRights.MODIFY_LOCAL_FILE_DATA_SOURCE);
		datastore.updateLocalFileDataSource(changeModifiedBy(dataSource));
	}

	@Override
	public void deleteLocalFileDataSource(String fileName) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.DELETE_LOCAL_FILE_DATA_SOURCE);
		datastore.deleteLocalFileDataSource(fileName);
	}

	@Override
	public List<AdvanceKeyStore> queryKeyStores() throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_KEYSTORES);
		return clearPassword(datastore.queryKeyStores());
	}

	@Override
	public AdvanceKeyStore queryKeyStore(String name) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_KEYSTORES);
		return clearPassword(datastore.queryKeyStore(name));
	}

	@Override
	public boolean hasUserRight(String userName, AdvanceUserRights expected)
			throws IOException {
		return datastore.hasUserRight(this.userName, expected);
	}

	@Override
	public boolean hasUserRight(String userName, String realm,
			AdvanceUserRealmRights expected) throws IOException {
		return datastore.hasUserRight(this.userName, realm, expected);
	}

	@Override
	public void updateKeyStore(AdvanceKeyStore keyStore) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.CREATE_KEYSTORE, AdvanceUserRights.MODIFY_KEYSTORE);
		datastore.updateKeyStore(changeModifiedBy(keyStore));
	}

	@Override
	public void deleteKeyStore(String keyStore) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.DELETE_KEYSTORE);
		datastore.deleteKeyStore(keyStore);
	}

	@Override
	public AdvanceJDBCDataSource queryJDBCDataSource(String name)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.LIST_JDBC_DATA_SOURCES);
		return clearPassword(datastore.queryJDBCDataSource(name));
	}

	@Override
	public AdvanceJMSEndpoint queryJMSEndpoint(String name) throws IOException, AdvanceControlException {
		check(AdvanceUserRights.LIST_JMS_ENDPOINTS);
		return clearPassword(datastore.queryJMSEndpoint(name));
	}

	@Override
	public AdvanceSOAPChannel querySOAPChannel(String name) throws IOException, AdvanceControlException {
		check(AdvanceUserRights.LIST_SOAP_CHANNELS);
		return clearPassword(datastore.querySOAPChannel(name));
	}
	@Override
	public List<AdvanceSOAPChannel> querySOAPChannels() throws IOException, AdvanceControlException {
		check(AdvanceUserRights.LIST_SOAP_CHANNELS);
		return clearPassword(datastore.querySOAPChannels());
	}
	@Override
	public AdvanceFTPDataSource queryFTPDataSource(String name)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.LIST_FTP_DATA_SOURCES);
		return clearPassword(datastore.queryFTPDataSource(name));
	}

	@Override
	public AdvanceWebDataSource queryWebDataSource(String name)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.LIST_WEB_DATA_SOURCES);
		return clearPassword(datastore.queryWebDataSource(name));
	}

	@Override
	public AdvanceLocalFileDataSource queryLocalFileDataSource(String name)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.LIST_LOCAL_FILE_DATA_SOURCES);
		return datastore.queryLocalFileDataSource(name);
	}

	@Override
	public Collection<String> queryNotificationGroup(
			AdvanceNotificationGroupType type, String name) throws IOException, AdvanceControlException {
		check(AdvanceUserRights.LIST_NOTIFICATION_GROUPS);
		return datastore.queryNotificationGroup(type, name);
	}

	@Override
	public XElement queryBlockState(String realm, String blockId)
			throws IOException, AdvanceControlException {
		check(realm, AdvanceUserRealmRights.DEBUG);
		return datastore.queryBlockState(realm, blockId);
	}

	@Override
	public void updateBlockState(String realm, String blockId, XElement state)
			throws IOException, AdvanceControlException {
		check(realm, AdvanceUserRealmRights.DEBUG);
		datastore.updateBlockState(realm, blockId, state);
	}

	@Override
	public XElement queryFlow(String realm) throws IOException, AdvanceControlException {
		check(realm, AdvanceUserRealmRights.READ);
		return datastore.queryFlow(realm);
	}
	@Override
	public void deleteBlockStates(String realm) throws IOException,
			AdvanceControlException {
		check(realm, AdvanceUserRealmRights.DEBUG);
		datastore.deleteBlockStates(realm);
	}
	@Override
	public void updateFlow(String realm, XElement flow) throws IOException,
			AdvanceControlException {
		check(realm, AdvanceUserRealmRights.WRITE);
		datastore.updateFlow(realm, flow);
	}
	@Override
	public void deleteEmailBox(String name) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.DELETE_EMAIL);
		datastore.deleteEmailBox(name);
	}
	@Override
	public AdvanceEmailBox queryEmailBox(String name) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_EMAIL);
		return clearPassword(datastore.queryEmailBox(name));
	}
	@Override
	public List<AdvanceEmailBox> queryEmailBoxes() throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_EMAIL);
		return clearPassword(datastore.queryEmailBoxes());
	}
	@Override
	public void updateEmailBox(AdvanceEmailBox box) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.CREATE_EMAIL, AdvanceUserRights.MODIFY_EMAIL);
		datastore.updateEmailBox(changeModifiedBy(box));
	}
	@Override
	public void deleteSOAPChannel(String name) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.DELETE_SOAP_CHANNEL);
		datastore.deleteSOAPChannel(name);
	}
	@Override
	public void updateSOAPChannel(AdvanceSOAPChannel channel)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.MODIFY_SOAP_CHANNEL);
		datastore.updateSOAPChannel(changeModifiedBy(channel));
	}
}
