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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Date;
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
import eu.advance.logistics.flow.engine.api.AdvanceLocalFileDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceNotificationGroupType;
import eu.advance.logistics.flow.engine.api.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.AdvanceRealmStatus;
import eu.advance.logistics.flow.engine.api.AdvanceUser;
import eu.advance.logistics.flow.engine.api.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.api.AdvanceUserRights;
import eu.advance.logistics.flow.engine.api.AdvanceWebDataSource;
import eu.advance.logistics.flow.model.AdvanceBlockRegistryEntry;
import eu.advance.logistics.util.KeystoreManager;
import eu.advance.logistics.xml.typesystem.XElement;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void enableUser(AdvanceControlToken token, int userId,
			boolean enabled) throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteUser(AdvanceControlToken token, int userId)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateUser(AdvanceControlToken token, AdvanceUser user)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<AdvanceNotificationGroupType, Map<String, Set<String>>> queryNotificationGroups(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateNotificationGroups(AdvanceControlToken token,
			Map<AdvanceNotificationGroupType, Map<String, Set<String>>> groups)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<AdvanceJDBCDataSource> queryJDBCDataSources(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateJDBCDataSource(AdvanceControlToken token,
			AdvanceJDBCDataSource dataSource) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void testJDBCDataSource(AdvanceControlToken token, int dataSourceId)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteJDBCDataSource(AdvanceControlToken token, int dataSourceId)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<AdvanceJMSEndpoint> queryJMSEndpoints(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateJMSEndpoint(AdvanceControlToken token,
			AdvanceJMSEndpoint endpoint) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void testJMSEndpoint(AdvanceControlToken token, int jmsId)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteJMSEndpoint(AdvanceControlToken token, int jmsId)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<AdvanceWebDataSource> queryWebDataSources(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateWebDataSource(AdvanceControlToken token,
			AdvanceWebDataSource endpoint) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteWebDataSource(AdvanceControlToken token, int webId)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<AdvanceFTPDataSource> queryFTPDataSources(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateFTPDataSource(AdvanceControlToken token,
			AdvanceFTPDataSource dataSource) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void testFTPDataSource(AdvanceControlToken token, int ftpId)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteFTPDataSource(AdvanceControlToken token, int ftpId)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<AdvanceLocalFileDataSource> queryLocalFileDataSources(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateLocalFileDataSource(AdvanceControlToken token,
			AdvanceLocalFileDataSource dataSource) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteLocalFileDataSource(AdvanceControlToken token, int fileId)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<AdvanceKeyStore> queryKeyStores(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AdvanceKeyEntry> queryKeyStore(AdvanceControlToken token,
			String keyStore, char[] password) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateKeyStore(AdvanceControlToken token,
			AdvanceKeyStore keyStore) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteKeyStore(AdvanceControlToken token, String keyStore)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteKeyEntry(AdvanceControlToken token, String keyStore,
			char[] password, String keyAlias) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void generateKey(AdvanceControlToken token, AdvanceGenerateKey key)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public String exportCertificate(AdvanceControlToken token,
			AdvanceKeyStoreExport request) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String exportPrivateKey(AdvanceControlToken token,
			AdvanceKeyStoreExport request) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void importCertificate(AdvanceControlToken token,
			AdvanceKeyStoreExport request, String data) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void importPrivateKey(AdvanceControlToken token,
			AdvanceKeyStoreExport request, String data) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public String exportSigningRequest(AdvanceControlToken token,
			AdvanceKeyStoreExport request) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void importSigningResponse(AdvanceControlToken token,
			AdvanceKeyStoreExport request, String data) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

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
}
