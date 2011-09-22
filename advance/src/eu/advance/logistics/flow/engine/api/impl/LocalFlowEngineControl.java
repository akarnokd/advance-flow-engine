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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.advance.logistics.flow.engine.api.AdvanceAccessDenied;
import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceControlToken;
import eu.advance.logistics.flow.engine.api.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceFlowEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceGenerateKey;
import eu.advance.logistics.flow.engine.api.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceJMSEndpoint;
import eu.advance.logistics.flow.engine.api.AdvanceKeyEntry;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStoreExport;
import eu.advance.logistics.flow.engine.api.AdvanceKeyType;
import eu.advance.logistics.flow.engine.api.AdvanceLocalFileDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceNotificationGroupType;
import eu.advance.logistics.flow.engine.api.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.AdvanceRealmStatus;
import eu.advance.logistics.flow.engine.api.AdvanceUser;
import eu.advance.logistics.flow.engine.api.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.api.AdvanceUserRights;
import eu.advance.logistics.flow.engine.api.AdvanceWebDataSource;
import eu.advance.logistics.flow.engine.model.AdvanceBlockRegistryEntry;
import eu.advance.logistics.flow.engine.util.KeystoreFault;
import eu.advance.logistics.flow.engine.util.KeystoreManager;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * A synchronized local flow engine control object storing data in local XML file.
 * <p>May be used to test GUI without the need to connect to real remote data source.
 * @author karnokd, 2011.09.20.
 */
public class LocalFlowEngineControl implements AdvanceFlowEngineControl {
	/** The logger object. */
	private static final Logger LOG = LoggerFactory.getLogger(LocalFlowEngineControl.class);
	/** The local data store. */
	protected final LocalDataStore datastore = new LocalDataStore();
	/** The cryptographic salt used to encrypt the datastore. */
	private static final byte[] CRYPTO_SALT = {
		35, -10, 99, -127, 59, -71, 42, -68
	};
	/** The cycle count for the encryption of the datastore. */
	private static final int CRYPTO_COUNT = 21;
	/**
	 * Check if the user of the given token has the expected rights.
	 * @param token the token to test
	 * @param expected the expected rights
	 * @return true if the user has the expected right
	 */
	protected boolean hasUserRight(AdvanceControlToken token, AdvanceUserRights expected) {
		synchronized (datastore.users) {
			AdvanceUser u = datastore.users.get(token.user.id);
			return u.rights.contains(expected);
		}
	}
	/**
	 * Check if the user of the given token has the expected rights.
	 * @param token the token to test
	 * @param realm the target realm
	 * @param expected the expected rights
	 * @return true if the user has the expected right
	 */
	protected boolean hasUserRight(AdvanceControlToken token, String realm, AdvanceUserRealmRights expected) {
		synchronized (datastore.users) {
			AdvanceUser u = datastore.users.get(token.user.id);
			return u.realmRights.containsEntry(realm, expected);
		}
	}
	@Override
	public AdvanceControlToken login(URI target, String userName,
			char[] password) throws IOException, AdvanceControlException {
		synchronized (datastore.users) {
			for (AdvanceUser u : datastore.users.values()) {
				if (u.enabled && u.name.equals(userName) && Arrays.equals(password, u.password)) {
					AdvanceControlToken token = new AdvanceControlToken();
					token.target = target;
					token.user = u.copy();
					return token;
				}
			}
		}
		throw new AdvanceAccessDenied("Wrong user name or password");
	}

	@Override
	public AdvanceControlToken login(URI target, KeyStore keyStore,
			String keyAlias, char[] keyPassword) throws IOException,
			AdvanceControlException, KeyStoreException {
		// FIXME implement
		throw new UnsupportedOperationException();
	}

	@Override
	public List<AdvanceBlockRegistryEntry> queryBlocks(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_BLOCKS)) {
			throw new AdvanceAccessDenied();
		}
		return AdvanceBlockRegistryEntry.parseDefaultRegistry();
	}

	@Override
	public List<AdvanceRealm> queryRealms(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_REALMS)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.realms) {
			List<AdvanceRealm> result = Lists.newArrayList();
			for (AdvanceRealm e : datastore.realms.values()) {
				result.add(e.copy());
			}
			return result;
		}
	}
	@Override
	public void createRealm(AdvanceControlToken token, String name)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.CREATE_REALM)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.realms) {
			if (!datastore.realms.containsKey(name)) {
				AdvanceRealm r = new AdvanceRealm();
				r.name = name;
				r.status = AdvanceRealmStatus.STOPPED;
				r.createdAt = new Date();
				r.createdBy = token.user.name;
				r.modifiedAt = new Date();
				r.modifiedBy = token.user.name;
				datastore.realms.put(r.name, r);
			} else {
				throw new AdvanceControlException("Realm exists");
			}
		}
	}

	@Override
	public void deleteRealm(AdvanceControlToken token, String name)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.DELETE_REALM)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.realms) {
			datastore.realms.remove(name);
		}
	}

	@Override
	public void renameRealm(AdvanceControlToken token, String name,
			String newName) throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.MODIFY_REALM)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.realms) {
			AdvanceRealm r = datastore.realms.get(name);
			if (r != null) {
				r.name = newName;
				r.modifiedAt = new Date();
				r.modifiedBy = token.user.name;
			} else {
				throw new AdvanceControlException("Realm not found");
			}
		}		
	}

	@Override
	public void stopRealm(AdvanceControlToken token, String name)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, name, AdvanceUserRealmRights.STOP)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.realms) {
			AdvanceRealm r = datastore.realms.get(name);
			if (r != null) {
				if (r.status == AdvanceRealmStatus.RUNNING) {
					r.status = AdvanceRealmStatus.STOPPED;
					r.modifiedAt = new Date();
					r.modifiedBy = token.user.name;
				} else {
					throw new AdvanceControlException("Realm not running");
				}
			} else {
				throw new AdvanceControlException("Realm not found");
			}
		}
	}

	@Override
	public void startRealm(AdvanceControlToken token, String name)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, name, AdvanceUserRealmRights.START)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.realms) {
			AdvanceRealm r = datastore.realms.get(name);
			if (r != null) {
				if (r.status == AdvanceRealmStatus.STOPPED) {
					r.status = AdvanceRealmStatus.RUNNING;
					r.modifiedAt = new Date();
					r.modifiedBy = token.user.name;
				} else {
					throw new AdvanceControlException("Realm not stopped");
				}
			} else {
				throw new AdvanceControlException("Realm not found");
			}
		}
	}

	@Override
	public List<AdvanceUser> queryUsers(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_USERS)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.users) {
			List<AdvanceUser> result = Lists.newArrayList();
			for (AdvanceUser u : datastore.users.values()) {
				result.add(u.copy());
			}
			return result;
		}
	}

	@Override
	public AdvanceUser queryUser(AdvanceControlToken token, int userId)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_USERS)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.users) {
			AdvanceUser u = datastore.users.get(userId);
			if (u != null) {
				return u.copy();
			}
			throw new AdvanceControlException("User not found");
		}
	}

	@Override
	public void enableUser(AdvanceControlToken token, int userId,
			boolean enabled) throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.MODIFY_USER)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.users) {
			AdvanceUser u = datastore.users.get(userId);
			if (u == null) {
				throw new AdvanceControlException("User not found");
			}
			
			int maybeAdmin = 0;
			for (AdvanceUser u2 : datastore.users.values()) {
				if (u2.mayModifyUser()) {
					maybeAdmin++;
				}
			}
			// do not allow disabling self
			if (u.id != token.user.id) {
				if (u.mayModifyUser() && maybeAdmin <= 1) {
					throw new AdvanceControlException("No user admins would remain");
				}
				u.enabled = enabled;
				u.modifiedAt = new Date();
				u.modifiedBy = token.user.name;
			} else {
				throw new AdvanceControlException("Can't disable self");
			}
		}
	}

	@Override
	public void deleteUser(AdvanceControlToken token, int userId)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.DELETE_USER)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.users) {
			int maybeAdmin = 0;
			for (AdvanceUser u2 : datastore.users.values()) {
				if (u2.mayModifyUser()) {
					maybeAdmin++;
				}
			}
			AdvanceUser u = datastore.users.get(userId);
			if (u.id != token.user.id) {
				if (u.mayModifyUser() && maybeAdmin <= 1) {
					throw new AdvanceControlException("No user admins would remain");
				}
				datastore.users.remove(userId);
			} else {
				throw new AdvanceControlException("Can't delete self");
			}
		}
	}

	@Override
	public void updateUser(AdvanceControlToken token, AdvanceUser user)
			throws IOException, AdvanceControlException {
		synchronized (datastore.users) {
			boolean mustExist = true;
			if (user.id == Integer.MIN_VALUE) {
				if (!hasUserRight(token, AdvanceUserRights.CREATE_USER)) {
					throw new AdvanceAccessDenied();
				}
				user.id = datastore.sequence.incrementAndGet();
				mustExist = false;
			} else {
				if (!hasUserRight(token, AdvanceUserRights.MODIFY_USER)) {
					throw new AdvanceAccessDenied();
				}
			}
			if (mustExist && !datastore.users.containsKey(user.id)) {
				throw new AdvanceControlException("User not found");
			}

			AdvanceUser prev = datastore.users.get(user.id);
			AdvanceUser u = user.copy();
			u.password = user.password != null ? user.password.clone() : (prev != null ? prev.password : null);
			if (prev != null) {
				u.createdAt = prev.createdAt;
				u.createdBy = prev.createdBy;
			} else {
				u.createdAt = new Date();
				u.createdBy = token.user.name;
			}
			u.modifiedAt = new Date();
			u.modifiedBy = token.user.name;
			datastore.users.put(u.id, u);
			// ensure that self is nut turned off or loses admin rights
			if (u.id == token.user.id && prev != null) {
				u.enabled = prev.enabled;
				if (prev.mayModifyUser()) {
					u.rights.add(AdvanceUserRights.LIST_USERS);
					u.rights.add(AdvanceUserRights.MODIFY_USER);
				}
			}
		}
	}

	@Override
	public Map<AdvanceNotificationGroupType, Map<String, Set<String>>> queryNotificationGroups(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_NOTIFICATION_GROUPS)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.notificationGroups) {
			Map<AdvanceNotificationGroupType, Map<String, Set<String>>> result = Maps.newHashMap();
			for (AdvanceNotificationGroupType t : datastore.notificationGroups.keySet()) {
				Map<String, Set<String>> type = Maps.newHashMap();
				result.put(t, type);
				for (String group : datastore.notificationGroups.get(t).keySet()) {
					Set<String> set = Sets.newHashSet();
					type.put(group, set);
					for (String s : datastore.notificationGroups.get(t).get(group)) {
						set.add(s);
					}
				}
			}
			return result;
		}
	}

	@Override
	public void updateNotificationGroups(AdvanceControlToken token,
			Map<AdvanceNotificationGroupType, Map<String, Set<String>>> groups)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.MODIFY_NOTIFICATION_GROUP)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.notificationGroups) {
			datastore.notificationGroups.clear();
			for (AdvanceNotificationGroupType t : groups.keySet()) {
				Map<String, Set<String>> type = Maps.newHashMap();
				datastore.notificationGroups.put(t, type);
				for (Map.Entry<String, Set<String>> group : groups.get(t).entrySet()) {
					Set<String> set = Sets.newHashSet();
					type.put(group.getKey(), set);
					for (String s : group.getValue()) {
						set.add(s);
					}
				}
			}
		}

	}

	@Override
	public List<AdvanceJDBCDataSource> queryJDBCDataSources(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_JDBC_DATA_SOURCES)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.jdbcDataSources) {
			List<AdvanceJDBCDataSource> result = Lists.newArrayList();
			for (AdvanceJDBCDataSource e : datastore.jdbcDataSources.values()) {
				result.add(e.copy());
			}
			
			return result;
		}
	}

	@Override
	public void updateJDBCDataSource(AdvanceControlToken token,
			AdvanceJDBCDataSource dataSource) throws IOException,
			AdvanceControlException {
		synchronized (datastore.jdbcDataSources) {
			boolean mustExist = true;
			if (dataSource.id == Integer.MIN_VALUE) {
				if (!hasUserRight(token, AdvanceUserRights.CREATE_JDBC_DATA_SOURCE)) {
					throw new AdvanceAccessDenied();
				}
				dataSource.id = datastore.sequence.incrementAndGet();
				mustExist = false;
			} else {
				if (!hasUserRight(token, AdvanceUserRights.MODIFY_JDBC_DATA_SOURCE)) {
					throw new AdvanceAccessDenied();
				}
			}
			if (mustExist && !datastore.jdbcDataSources.containsKey(dataSource.id)) {
				throw new AdvanceControlException("User not found");
			}
			AdvanceJDBCDataSource u = dataSource.copy();
			AdvanceJDBCDataSource prev = datastore.jdbcDataSources.get(dataSource.id);
			u.password = dataSource.password != null ? dataSource.password.clone() : (prev != null ? prev.password : null);
			
			if (prev != null) {
				u.createdAt = prev.createdAt;
				u.createdBy = prev.createdBy;
			} else {
				u.createdAt = new Date();
				u.createdBy = token.user.name;
			}
			u.modifiedAt = new Date();
			u.modifiedBy = token.user.name;
			datastore.jdbcDataSources.put(dataSource.id, u);
		}
	}

	@Override
	public void testJDBCDataSource(AdvanceControlToken token, int dataSourceId)
			throws IOException, AdvanceControlException {
		// FIXME implement
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteJDBCDataSource(AdvanceControlToken token, int dataSourceId)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.DELETE_JDBC_DATA_SOURCE)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.jdbcDataSources) {
			datastore.jdbcDataSources.remove(dataSourceId);
		}
	}

	@Override
	public List<AdvanceJMSEndpoint> queryJMSEndpoints(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_JMS_ENDPOINTS)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.jmsEndpoints) {
			List<AdvanceJMSEndpoint> result = Lists.newArrayList();
			for (AdvanceJMSEndpoint e : datastore.jmsEndpoints.values()) {
				result.add(e.copy());
			}
			return result;
		}
	}

	@Override
	public void updateJMSEndpoint(AdvanceControlToken token,
			AdvanceJMSEndpoint endpoint) throws IOException,
			AdvanceControlException {
		
		synchronized  (datastore.jmsEndpoints) {
			boolean mustExist = true;
			if (endpoint.id == Integer.MIN_VALUE) {
				if (!hasUserRight(token, AdvanceUserRights.CREATE_JMS_ENDPOINT)) {
					throw new AdvanceAccessDenied();
				}
				endpoint.id = datastore.sequence.incrementAndGet();
				mustExist = false;
			} else {
				if (!hasUserRight(token, AdvanceUserRights.MODIFY_JMS_ENDPOINT)) {
					throw new AdvanceAccessDenied();
				}
			}
			
			if (mustExist && !datastore.jmsEndpoints.containsKey(endpoint.id)) {
				throw new AdvanceControlException("User not found");
			}
			AdvanceJMSEndpoint u = endpoint.copy();
			AdvanceJMSEndpoint prev = datastore.jmsEndpoints.get(endpoint.id);
			u.password = endpoint.password != null ? endpoint.password.clone() : (prev != null ? prev.password : null);
			
			if (prev != null) {
				u.createdAt = prev.createdAt;
				u.createdBy = prev.createdBy;
			} else {
				u.createdAt = new Date();
				u.createdBy = token.user.name;
			}
			u.modifiedAt = new Date();
			u.modifiedBy = token.user.name;
			datastore.jmsEndpoints.put(endpoint.id, u);
		}
		

	}

	@Override
	public void testJMSEndpoint(AdvanceControlToken token, int jmsId)
			throws IOException, AdvanceControlException {
		// FIXME implement
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteJMSEndpoint(AdvanceControlToken token, int jmsId)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.DELETE_JMS_ENDPOINT)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.jmsEndpoints) {
			datastore.jmsEndpoints.remove(jmsId);
		}
	}

	@Override
	public List<AdvanceWebDataSource> queryWebDataSources(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_WEB_DATA_SOURCES)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.webDataSources) {
			List<AdvanceWebDataSource> result = Lists.newArrayList();
			for (AdvanceWebDataSource e : datastore.webDataSources.values()) {
				result.add(e.copy());
			}
			return result;
		}
	}

	@Override
	public void updateWebDataSource(AdvanceControlToken token,
			AdvanceWebDataSource endpoint) throws IOException,
			AdvanceControlException {
		synchronized  (datastore.webDataSources) {
			boolean mustExist = true;
			if (endpoint.id == Integer.MIN_VALUE) {
				if (!hasUserRight(token, AdvanceUserRights.CREATE_WEB_DATA_SOURCE)) {
					throw new AdvanceAccessDenied();
				}
				endpoint.id = datastore.sequence.incrementAndGet();
				mustExist = false;
			} else {
				if (!hasUserRight(token, AdvanceUserRights.MODIFY_WEB_DATA_SOURCE)) {
					throw new AdvanceAccessDenied();
				}
			}
			
			if (mustExist && !datastore.webDataSources.containsKey(endpoint.id)) {
				throw new AdvanceControlException("User not found");
			}
			AdvanceWebDataSource u = endpoint.copy();
			AdvanceWebDataSource prev = datastore.webDataSources.get(endpoint.id);
			u.password = endpoint.password != null ? endpoint.password.clone() : (prev != null ? prev.password : null);
			
			if (prev != null) {
				u.createdAt = prev.createdAt;
				u.createdBy = prev.createdBy;
			} else {
				u.createdAt = new Date();
				u.createdBy = token.user.name;
			}
			u.modifiedAt = new Date();
			u.modifiedBy = token.user.name;
			datastore.webDataSources.put(endpoint.id, u);
		}

	}

	@Override
	public void deleteWebDataSource(AdvanceControlToken token, int webId)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.DELETE_WEB_DATA_SOURCE)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.webDataSources) {
			datastore.webDataSources.remove(webId);
		}
	}

	@Override
	public List<AdvanceFTPDataSource> queryFTPDataSources(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_FTP_DATA_SOURCES)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.ftpDataSources) {
			List<AdvanceFTPDataSource> result = Lists.newArrayList();
			for (AdvanceFTPDataSource e : datastore.ftpDataSources.values()) {
				result.add(e.copy());
			}
			return result;
		}
	}

	@Override
	public void updateFTPDataSource(AdvanceControlToken token,
			AdvanceFTPDataSource dataSource) throws IOException,
			AdvanceControlException {
		synchronized  (datastore.ftpDataSources) {
			boolean mustExist = true;
			if (dataSource.id == Integer.MIN_VALUE) {
				if (!hasUserRight(token, AdvanceUserRights.CREATE_FTP_DATA_SOURCE)) {
					throw new AdvanceAccessDenied();
				}
				dataSource.id = datastore.sequence.incrementAndGet();
				mustExist = false;
			} else {
				if (!hasUserRight(token, AdvanceUserRights.MODIFY_FTP_DATA_SOURCE)) {
					throw new AdvanceAccessDenied();
				}
			}
			
			if (mustExist && !datastore.ftpDataSources.containsKey(dataSource.id)) {
				throw new AdvanceControlException("User not found");
			}
			AdvanceFTPDataSource u = dataSource.copy();
			AdvanceFTPDataSource prev = datastore.ftpDataSources.get(dataSource.id);
			u.password = dataSource.password != null ? dataSource.password.clone() : (prev != null ? prev.password : null);
			
			if (prev != null) {
				u.createdAt = prev.createdAt;
				u.createdBy = prev.createdBy;
			} else {
				u.createdAt = new Date();
				u.createdBy = token.user.name;
			}
			u.modifiedAt = new Date();
			u.modifiedBy = token.user.name;
			datastore.ftpDataSources.put(dataSource.id, u);
		}
	}

	@Override
	public void testFTPDataSource(AdvanceControlToken token, int ftpId)
			throws IOException, AdvanceControlException {
		// FIXME implement
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteFTPDataSource(AdvanceControlToken token, int ftpId)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.DELETE_FTP_DATA_SOURCE)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.ftpDataSources) {
			datastore.ftpDataSources.remove(ftpId);
		}
	}

	@Override
	public List<AdvanceLocalFileDataSource> queryLocalFileDataSources(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_LOCAL_FILE_DATA_SOURCES)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.localDataSources) {
			List<AdvanceLocalFileDataSource> result = Lists.newArrayList();
			for (AdvanceLocalFileDataSource e : datastore.localDataSources.values()) {
				result.add(e.copy());
			}
			return result;
		}
	}

	@Override
	public void updateLocalFileDataSource(AdvanceControlToken token,
			AdvanceLocalFileDataSource dataSource) throws IOException,
			AdvanceControlException {
		synchronized  (datastore.localDataSources) {
			boolean mustExist = true;
			if (dataSource.id == Integer.MIN_VALUE) {
				if (!hasUserRight(token, AdvanceUserRights.CREATE_LOCAL_FILE_DATA_SOURCE)) {
					throw new AdvanceAccessDenied();
				}
				dataSource.id = datastore.sequence.incrementAndGet();
				mustExist = false;
			} else {
				if (!hasUserRight(token, AdvanceUserRights.MODIFY_LOCAL_FILE_DATA_SOURCE)) {
					throw new AdvanceAccessDenied();
				}
			}
			
			if (mustExist && !datastore.localDataSources.containsKey(dataSource.id)) {
				throw new AdvanceControlException("User not found");
			}
			AdvanceLocalFileDataSource u = dataSource.copy();
			AdvanceLocalFileDataSource prev = datastore.localDataSources.get(dataSource.id);
			
			if (prev != null) {
				u.createdAt = prev.createdAt;
				u.createdBy = prev.createdBy;
			} else {
				u.createdAt = new Date();
				u.createdBy = token.user.name;
			}
			u.modifiedAt = new Date();
			u.modifiedBy = token.user.name;
			datastore.localDataSources.put(dataSource.id, u);
		}
	}

	@Override
	public void deleteLocalFileDataSource(AdvanceControlToken token, int fileId)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.DELETE_LOCAL_FILE_DATA_SOURCE)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.localDataSources) {
			datastore.localDataSources.remove(fileId);
		}
	}

	@Override
	public List<AdvanceKeyStore> queryKeyStores(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_KEYSTORES)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.keystores) {
			List<AdvanceKeyStore> result = Lists.newArrayList();
			
			for (AdvanceKeyStore e : datastore.keystores.values()) {
				result.add(e.copy());
			}
			
			return result;
		}
	}

	@Override
	public List<AdvanceKeyEntry> queryKeyStore(AdvanceControlToken token,
			String keyStore) throws IOException,
			AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_KEYS)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.keystores) {
			AdvanceKeyStore e = datastore.keystores.get(keyStore);
			KeystoreManager mgr = new KeystoreManager();
			try {
				List<AdvanceKeyEntry> result = Lists.newArrayList();
				mgr.load(e.location, e.password);
				KeyStore ks = mgr.getKeyStore();
				Enumeration<String> aliases = ks.aliases();
				while (aliases.hasMoreElements()) {
					String alias = aliases.nextElement();
					
					AdvanceKeyEntry k = new AdvanceKeyEntry();
					if (ks.isKeyEntry(alias)) {
						k.type = AdvanceKeyType.PRIVATE_KEY;
					} else
					if (ks.isCertificateEntry(alias)) {
						k.type = AdvanceKeyType.CERTIFICATE;
					}
					k.name = alias;
					k.createdAt = ks.getCreationDate(alias);
					
					result.add(k);
				}
				return result;
			} catch (KeystoreFault ex) {
				throw new AdvanceControlException(ex);
			} catch (KeyStoreException ex) {
				throw new AdvanceControlException(ex);
			}
		}
	}

	@Override
	public void updateKeyStore(AdvanceControlToken token,
			AdvanceKeyStore keyStore) throws IOException,
			AdvanceControlException {
		KeystoreManager mgr = new KeystoreManager();
		synchronized (datastore.keystores) {
			AdvanceKeyStore e = datastore.keystores.get(keyStore.name);
			try {
				if (e == null) {
					if (!hasUserRight(token, AdvanceUserRights.CREATE_KEYSTORE)) {
						throw new AdvanceAccessDenied();
					}
					e = new AdvanceKeyStore();
					e.name = keyStore.name;
					e.password = keyStore.password;
					e.location = keyStore.location;
					e.createdAt = new Date();
					e.createdBy = token.user.name;
					e.modifiedAt = new Date();
					e.modifiedBy = token.user.name;
					
					mgr.create();
					mgr.save(e.location, e.password);
					
					datastore.keystores.put(e.name, e);
				} else {
					if (!hasUserRight(token, AdvanceUserRights.MODIFY_KEYSTORE)) {
						throw new AdvanceAccessDenied();
					}
					
					mgr.load(e.location, e.password);

					e.location = keyStore.location;
					if (keyStore.password != null) {
						e.password = keyStore.password;
					}
					
					mgr.save(e.location, e.password);

					e.modifiedAt = new Date();
					e.modifiedBy = token.user.name;

					File f = new File(e.location);
					if (!f.delete()) {
						LOG.warn("Could not delete keystore " + e.location);
					}
				}
			} catch (KeystoreFault ex) {
				throw new AdvanceControlException(ex);
			}
		}
	}

	@Override
	public void deleteKeyStore(AdvanceControlToken token, String keyStore)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.DELETE_KEYSTORE)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.keystores) {
			AdvanceKeyStore e = datastore.keystores.get(keyStore);
			if (e != null) {
				File f = new File(e.location);
				if (!f.delete()) {
					LOG.warn("Could not delete keystore " + e.location);
				} else {
					datastore.keystores.remove(keyStore);
				}
			} else {
				throw new AdvanceControlException("Keystore not found");
			}
		}

	}

	@Override
	public void deleteKeyEntry(AdvanceControlToken token, String keyStore,
			String keyAlias) throws IOException,
			AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.DELETE_KEY)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.keystores) {
			AdvanceKeyStore e = datastore.keystores.get(keyStore);
			if (e != null) {
				KeystoreManager mgr = new KeystoreManager();
				try {
					mgr.load(e.location, e.password);
					mgr.getKeyStore().deleteEntry(keyAlias);
				} catch (KeyStoreException ex) {
					throw new AdvanceControlException(ex);
				} catch (KeystoreFault ex) {
					throw new AdvanceControlException(ex);
				}
			} else {
				throw new AdvanceControlException("Keystore not found");
			}
		}
	}

	@Override
	public void generateKey(AdvanceControlToken token, AdvanceGenerateKey key)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.GENERATE_KEY)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.keystores) {
			AdvanceKeyStore e = datastore.keystores.get(key.keyStore);
			if (e != null) {
				KeystoreManager mgr = new KeystoreManager();
				try {
					mgr.load(e.location, e.password);
					
					KeyPair kp = mgr.generateKeyPair(key.algorithm, key.keySize);
					Certificate cert = mgr.createX509Certificate(kp, 12, 
							key.issuerDn.toString(), key.subjectDn.toString(), 
							"http://www.advance-logistics.eu", // FIXME maybe parametrize 
							"MD5withRSA"); // FIXME maybe parametrize
					
					mgr.getKeyStore().setKeyEntry(key.keyAlias, kp.getPrivate(), key.keyPassword, new Certificate[] { cert });
					
					mgr.save(e.location, e.password);
					e.modifiedAt = new Date();
					e.modifiedBy = token.user.name;
					
				} catch (KeyStoreException ex) {
					throw new AdvanceControlException(ex);
				} catch (KeystoreFault ex) {
					throw new AdvanceControlException(ex);
				}
			} else {
				throw new AdvanceControlException("Keystore not found");
			}
		}
	}

	@Override
	public String exportCertificate(AdvanceControlToken token,
			AdvanceKeyStoreExport request) throws IOException,
			AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.EXPORT_CERTIFICATE)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.keystores) {
			AdvanceKeyStore e = datastore.keystores.get(request.keyStore);
			if (e != null) {
				KeystoreManager mgr = new KeystoreManager();
				try {
					mgr.load(e.location, e.password);
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					mgr.exportCertificate(request.keyAlias, out, false);
					return out.toString("UTF-8");
				} catch (KeystoreFault ex) {
					throw new AdvanceControlException(ex);
				}
			} else {
				throw new AdvanceControlException("Keystore not found");
			}
		}
	}

	@Override
	public String exportPrivateKey(AdvanceControlToken token,
			AdvanceKeyStoreExport request) throws IOException,
			AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.EXPORT_CERTIFICATE)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.keystores) {
			AdvanceKeyStore e = datastore.keystores.get(request.keyStore);
			if (e != null) {
				KeystoreManager mgr = new KeystoreManager();
				try {
					mgr.load(e.location, e.password);
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					mgr.exportPrivateKey(request.keyAlias, request.keyPassword, out, false);
					return out.toString("UTF-8");
				} catch (KeystoreFault ex) {
					throw new AdvanceControlException(ex);
				}
			} else {
				throw new AdvanceControlException("Keystore not found");
			}
		}
	}

	@Override
	public void importCertificate(AdvanceControlToken token,
			AdvanceKeyStoreExport request, String data) throws IOException,
			AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.IMPORT_CERTIFICATE)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.keystores) {
			AdvanceKeyStore e = datastore.keystores.get(request.keyStore);
			if (e != null) {
				KeystoreManager mgr = new KeystoreManager();
				try {
					mgr.load(e.location, e.password);
					mgr.importCertificate(request.keyAlias, new ByteArrayInputStream(data.getBytes("UTF-8")));
					mgr.save(e.location, e.password);
				} catch (KeystoreFault ex) {
					throw new AdvanceControlException(ex);
				}
			} else {
				throw new AdvanceControlException("Keystore not found");
			}
		}
	}

	@Override
	public void importPrivateKey(AdvanceControlToken token,
			AdvanceKeyStoreExport request, String keyData, String certData) throws IOException,
			AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.IMPORT_PRIVATE_KEY)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.keystores) {
			AdvanceKeyStore e = datastore.keystores.get(request.keyStore);
			if (e != null) {
				KeystoreManager mgr = new KeystoreManager();
				try {
					mgr.load(e.location, e.password);
					mgr.importPrivateKey(request.keyAlias, request.keyPassword, 
							new ByteArrayInputStream(keyData.getBytes("UTF-8")),
							new ByteArrayInputStream(certData.getBytes("UTF-8"))
					);
					mgr.save(e.location, e.password);
				} catch (KeystoreFault ex) {
					throw new AdvanceControlException(ex);
				}
			} else {
				throw new AdvanceControlException("Keystore not found");
			}
		}
	}

	@Override
	public String exportSigningRequest(AdvanceControlToken token,
			AdvanceKeyStoreExport request) throws IOException,
			AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.EXPORT_CERTIFICATE)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.keystores) {
			AdvanceKeyStore e = datastore.keystores.get(request.keyStore);
			if (e != null) {
				KeystoreManager mgr = new KeystoreManager();
				try {
					mgr.load(e.location, e.password);
					return mgr.createRSASigningRequest(request.keyAlias, request.keyPassword);
				} catch (KeystoreFault ex) {
					throw new AdvanceControlException(ex);
				}
			} else {
				throw new AdvanceControlException("Keystore not found");
			}
		}
	}

	@Override
	public void importSigningResponse(AdvanceControlToken token,
			AdvanceKeyStoreExport request, String data) throws IOException,
			AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.IMPORT_CERTIFICATE)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.keystores) {
			AdvanceKeyStore e = datastore.keystores.get(request.keyStore);
			if (e != null) {
				KeystoreManager mgr = new KeystoreManager();
				try {
					mgr.load(e.location, e.password);
					mgr.installReply(request.keyAlias, request.keyPassword, new ByteArrayInputStream(data.getBytes("UTF-8")), 
							true); // FIXME not sure
					mgr.save(e.location, e.password);
				} catch (KeystoreFault ex) {
					throw new AdvanceControlException(ex);
				}
			} else {
				throw new AdvanceControlException("Keystore not found");
			}
		}
	}
	/**
	 * Load database from disk.
	 */
	public void load() {
		File dsFile = new File("datastore.xml");
		if (dsFile.canRead()) {
			try {
				datastore.load(XElement.parseXML(dsFile));
			} catch (IOException ex) {
				LOG.error(ex.toString(), ex);
			} catch (XMLStreamException ex) {
				LOG.error(ex.toString(), ex);
			}
		}
	}
	/**
	 * Load a password encrypted data store.
	 * @param password the password
	 */
	public void loadEncrypted(char[] password) {
		File dsFile = new File("datastore.xml");
		if (dsFile.canRead()) {
			try {
				PBEParameterSpec pbeParamSpec = new PBEParameterSpec(CRYPTO_SALT, CRYPTO_COUNT);
				PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
				SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES", KeystoreManager.BC_PROVIDER);
				SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
				
				Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES", KeystoreManager.BC_PROVIDER);
				
				pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
				
				CipherInputStream in = new CipherInputStream(new BufferedInputStream(new FileInputStream(dsFile)), pbeCipher);
				try {
					XElement xdatastore = XElement.parseXML(in);
					datastore.load(xdatastore);
				} finally {
					in.close();
				}
			} catch (XMLStreamException ex) {
				LOG.error(ex.toString(), ex);
			} catch (IOException ex) {
				LOG.error(ex.toString(), ex);
			} catch (InvalidAlgorithmParameterException ex) {
				LOG.error(ex.toString(), ex);
			} catch (InvalidKeyException ex) {
				LOG.error(ex.toString(), ex);
			} catch (NoSuchAlgorithmException ex) {
				LOG.error(ex.toString(), ex);
			} catch (InvalidKeySpecException ex) {
				LOG.error(ex.toString(), ex);
			} catch (NoSuchPaddingException ex) {
				LOG.error(ex.toString(), ex);
			}
		}		
	}
	
	/**
	 * Save a password encrypted data store.
	 * @param password the password
	 */
	public void saveEncrypted(char[] password) {
		File dsFile = new File("datastore.xml");
		backupDataStore(dsFile);
		try {
			XElement xdatastore = new XElement("datastore");
			datastore.save(xdatastore);
			
			PBEParameterSpec pbeParamSpec = new PBEParameterSpec(CRYPTO_SALT, CRYPTO_COUNT);
			PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
			SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES", KeystoreManager.BC_PROVIDER);
			SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
			
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES", KeystoreManager.BC_PROVIDER);
			
			pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
			
			CipherOutputStream out = new CipherOutputStream(new BufferedOutputStream(new FileOutputStream(dsFile)), pbeCipher);
			try {
				xdatastore.save(out);
			} finally {
				out.close();
			}
		} catch (IOException ex) {
			LOG.error(ex.toString(), ex);
		} catch (InvalidAlgorithmParameterException ex) {
			LOG.error(ex.toString(), ex);
		} catch (InvalidKeyException ex) {
			LOG.error(ex.toString(), ex);
		} catch (NoSuchAlgorithmException ex) {
			LOG.error(ex.toString(), ex);
		} catch (InvalidKeySpecException ex) {
			LOG.error(ex.toString(), ex);
		} catch (NoSuchPaddingException ex) {
			LOG.error(ex.toString(), ex);
		}
	}
	/**
	 * Save database to disk.
	 */
	public void save() {
		File dsFile = new File("datastore.xml");
		backupDataStore(dsFile);
		try {
			XElement xdatastore = new XElement("datastore");
			datastore.save(xdatastore);
			xdatastore.save(dsFile);
		} catch (IOException ex) {
			LOG.error(ex.toString(), ex);
		}
	}

	/**
	 * Backup the current data store.
	 * @param dsFile the datastore file name
	 */
	protected void backupDataStore(File dsFile) {
		File dsFileBackup1 = new File("datastore.xml.bak");
		File dsFileBackup2 = new File("datastore.xml.ba1");
		if (dsFileBackup2.exists()) {
			if (!dsFileBackup2.delete()) {
				LOG.warn("Could not delete file " + dsFileBackup2);
			}
		}
		if (dsFileBackup1.exists()) {
			if (!dsFileBackup1.renameTo(dsFileBackup2)) {
				LOG.warn("Could not rename file " + dsFileBackup1 + " into " + dsFileBackup2);
			}
			if (!dsFileBackup1.delete()) {
				LOG.warn("Could not delete file " + dsFileBackup1);
			}
		}
		if (dsFile.exists()) {
			if (!dsFile.renameTo(dsFileBackup1)) {
				LOG.warn("Could not rename file " + dsFile + " into " + dsFileBackup1);
			}
		}
	}
	/**
	 * Initialize the datastore with the first admin record.
	 */
	public void initialize() {
		AdvanceUser u = new AdvanceUser();
		u.id = 0;
		u.name = "admin";
		u.password = "admin".toCharArray();
		u.thousandSeparator = ',';
		u.decimalSeparator = '.';
		u.dateFormat = "yyyy-MM-dd";
		u.dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
		u.numberFormat = "#,###";
		u.enabled = true;
		u.passwordLogin = true;
		u.rights.addAll(Arrays.asList(AdvanceUserRights.values()));
		
		synchronized (datastore.users) {
			datastore.users.put(u.id, u);
		}
	}
}
