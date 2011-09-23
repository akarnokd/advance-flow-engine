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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.api.AdvanceAccessDenied;
import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceControlToken;
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.AdvanceFlowEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceGenerateKey;
import eu.advance.logistics.flow.engine.api.AdvanceKeyEntry;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStoreExport;
import eu.advance.logistics.flow.engine.api.AdvanceKeyType;
import eu.advance.logistics.flow.engine.api.AdvanceUser;
import eu.advance.logistics.flow.engine.api.AdvanceUserRights;
import eu.advance.logistics.flow.engine.model.AdvanceBlockRegistryEntry;
import eu.advance.logistics.flow.engine.util.KeystoreFault;
import eu.advance.logistics.flow.engine.util.KeystoreManager;

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
		if (!datastore.hasUserRight(token, AdvanceUserRights.LIST_BLOCKS)) {
			throw new AdvanceAccessDenied();
		}
		return AdvanceBlockRegistryEntry.parseDefaultRegistry();
	}
	/** @return the datastore instance */
	public AdvanceDataStore datastore() {
		return datastore;
	}

	@Override
	public List<AdvanceKeyEntry> queryKeys(AdvanceControlToken token,
			String keyStore) throws IOException,
			AdvanceControlException {
		if (!datastore.hasUserRight(token, AdvanceUserRights.LIST_KEYS)) {
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
	public void deleteKeyEntry(AdvanceControlToken token, String keyStore,
			String keyAlias) throws IOException,
			AdvanceControlException {
		if (!datastore.hasUserRight(token, AdvanceUserRights.DELETE_KEY)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (datastore.keystores) {
			AdvanceKeyStore e = datastore.keystores.get(keyStore);
			if (e != null) {
				KeystoreManager mgr = new KeystoreManager();
				try {
					mgr.load(e.location, e.password);
					mgr.getKeyStore().deleteEntry(keyAlias);
					mgr.save(e.location, e.password);
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
		if (!datastore.hasUserRight(token, AdvanceUserRights.GENERATE_KEY)) {
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
		if (!datastore.hasUserRight(token, AdvanceUserRights.EXPORT_CERTIFICATE)) {
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
		if (!datastore.hasUserRight(token, AdvanceUserRights.EXPORT_CERTIFICATE)) {
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
		if (!datastore.hasUserRight(token, AdvanceUserRights.IMPORT_CERTIFICATE)) {
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
		if (!datastore.hasUserRight(token, AdvanceUserRights.IMPORT_PRIVATE_KEY)) {
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
		if (!datastore.hasUserRight(token, AdvanceUserRights.EXPORT_CERTIFICATE)) {
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
		if (!datastore.hasUserRight(token, AdvanceUserRights.IMPORT_CERTIFICATE)) {
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
	/**
	 * Load database from disk.
	 */
	public void load() {
		File dsFile = new File("datastore.xml");
		datastore.load(dsFile.toString());
	}
	/**
	 * Load a password encrypted data store.
	 * @param password the password
	 */
	public void loadEncrypted(char[] password) {
		File dsFile = new File("datastore.xml");
		datastore.loadEncrypted(dsFile.toString(), password);
	}
	
	/**
	 * Save a password encrypted data store.
	 * @param password the password
	 */
	public void saveEncrypted(char[] password) {
		File dsFile = new File("datastore.xml");
		backupDataStore(dsFile);
		datastore.saveEncrypted(dsFile.toString(), password);
	}
	/**
	 * Save database to disk.
	 */
	public void save() {
		File dsFile = new File("datastore.xml");
		backupDataStore(dsFile);
		datastore.save(dsFile.toString());
	}
	@Override
	public void testJDBCDataSource(AdvanceControlToken token, int dataSourceId)
			throws IOException, AdvanceControlException {
		// FIXME implement
		throw new UnsupportedOperationException();
	}
	@Override
	public void testJMSEndpoint(AdvanceControlToken token, int jmsId)
			throws IOException, AdvanceControlException {
		// FIXME implement
		throw new UnsupportedOperationException();
	}
	@Override
	public void testFTPDataSource(AdvanceControlToken token, int ftpId)
			throws IOException, AdvanceControlException {
		// FIXME implement
		throw new UnsupportedOperationException();
	}
}
