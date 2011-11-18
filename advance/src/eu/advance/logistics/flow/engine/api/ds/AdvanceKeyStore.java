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

package eu.advance.logistics.flow.engine.api.ds;

import hu.akarnokd.reactive4java.base.Func0;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.api.core.Copyable;
import eu.advance.logistics.flow.engine.api.core.HasPassword;
import eu.advance.logistics.flow.engine.api.core.Identifiable;
import eu.advance.logistics.flow.engine.util.KeystoreManager;
import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.flow.engine.xml.XSerializable;

/**
 * A record representing information about local or remote key stores.
 * @author akarnokd, 2011.09.20.
 */
public class AdvanceKeyStore extends AdvanceCreateModifyInfo 
implements XSerializable, HasPassword, Copyable<AdvanceKeyStore>, Identifiable<String> {
	/** The logger object. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceKeyStore.class);
	/** The key store name. */
	public String name;
	/** The location prefix to emulate a working directory. */
	public String locationPrefix = "";
	/** The key store location on disk. */
	public String location;
	/** 
	 * The password.
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 * <p>Note that passwords are never returned from the 
	 * control API calls and are always {@code null}.</p> 
	 * @return the password. 
	 */
	private char[] password;
	/** The function to create a new instance of this class. */
	public static final Func0<AdvanceKeyStore> CREATOR = new Func0<AdvanceKeyStore>() {
		@Override
		public AdvanceKeyStore invoke() {
			return new AdvanceKeyStore();
		}
	};
	@Override
	public void load(XElement source) {
		name = source.get("name");
		location = source.get("location");
		password = getPassword(source, "password");
		super.load(source);
	}
	@Override
	public void save(XElement destination) {
		destination.set("name", name);
		destination.set("location", location);
		setPassword(destination, "password", password);
		super.save(destination);
	}
	@Override
	public AdvanceKeyStore copy() {
		AdvanceKeyStore result = new AdvanceKeyStore();
		
		result.name = name;
		result.locationPrefix = locationPrefix; 
		result.location = location;
		result.password = password != null ? password.clone() : null;
		
		assignTo(result);
		
		return result;
	}
	@Override
	public char[] password() {
		return password != null ? password.clone() : null;
	}
	@Override
	public void password(char[] newPassword) {
		password = newPassword != null ? newPassword.clone() : null;
	}
	@Override
	public String id() {
		return name;
	}
	/**
	 * Export the certificate from the supplied keystore.
	 * @param alias the key alias
	 * @return the certifcate text
	 * @throws IOException on error
	 */
	public String exportCertificate(String alias) throws IOException {
		KeystoreManager mgr = new KeystoreManager();
		mgr.load(locationPrefix + location, password());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		mgr.exportCertificate(alias, out, false);
		return out.toString("UTF-8");
	}
	/**
	 * Export the given private key.
	 * @param alias the key alias
	 * @param password the key password
	 * @return the key text
	 */
	public String exportPrivateKey(String alias, char[] password) {
		KeystoreManager mgr = new KeystoreManager();
		mgr.load(locationPrefix + location, password());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			mgr.exportPrivateKey(alias, password, out, false);
			return out.toString("UTF-8");
		} catch (IOException ex) {
			LOG.warn(ex.toString(), ex);
			return "";
		}
	}
	/**
	 * Delete the specified keys from the keystore.
	 * @param aliases the key aliases
	 * @throws KeyStoreException on error
	 */
	public void deleteKey(Iterable<String> aliases) throws KeyStoreException {
		KeystoreManager mgr = new KeystoreManager();
		mgr.load(locationPrefix + location, password());
		for (String a : aliases) {
			mgr.getKeyStore().deleteEntry(a);
		}
		mgr.save(locationPrefix + location, password());
	}
	/**
	 * Delete the specified keys from the keystore.
	 * @param aliases the key aliases
	 * @throws KeyStoreException on error
	 */
	public void deleteKey(String... aliases) throws KeyStoreException {
		deleteKey(Arrays.asList(aliases));
	}
	/**
	 * Generate a new key with the given properties.
	 * @param key the key properties
	 * @throws KeyStoreException on error
	 */
	public void generateKey(AdvanceGenerateKey key) throws KeyStoreException {
		KeystoreManager mgr = new KeystoreManager();
		mgr.load(locationPrefix + location, password());
		
		KeyPair kp = mgr.generateKeyPair(key.algorithm, key.keySize);
		Certificate cert = mgr.createX509Certificate(kp, 12, 
				key.issuerDn.toString(), key.subjectDn.toString(), 
				key.domain,  
				"MD5with" + key.algorithm); 
		
		mgr.getKeyStore().setKeyEntry(key.keyAlias, kp.getPrivate(), key.password(), 
				new Certificate[] { cert });
		
		mgr.save(locationPrefix + location, password());
			
	}
	/**
	 * List the keys of this keystore.
	 * @return the list of keys
	 * @throws KeyStoreException on error
	 */
	public List<AdvanceKeyEntry> queryKeys() throws KeyStoreException {
		KeystoreManager mgr = new KeystoreManager();
		List<AdvanceKeyEntry> result = Lists.newArrayList();
		mgr.load(locationPrefix + location, password());
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
	}
	/**
	 * Import a certificate under the given alias.
	 * @param alias the certificate alias
	 * @param data the certificate text
	 * @throws KeyStoreException on error
	 */
	public void importCertificate(String alias, String data) throws KeyStoreException {
		KeystoreManager mgr = new KeystoreManager();
		mgr.load(locationPrefix + location, password());
		try {
			mgr.importCertificate(alias, new ByteArrayInputStream(data.getBytes("UTF-8")));
			mgr.save(locationPrefix + location, password());
		} catch (UnsupportedEncodingException ex) {
			LOG.warn(ex.toString(), ex);
		}
	}
	/**
	 * Imports a private key with certificate into this keystore.
	 * @param alias the key alias
	 * @param password the key password
	 * @param keyData the key data
	 * @param certData the certificate data
	 * @throws IOException on error
	 */
	public void importPrivateKey(String alias, char[] password, String keyData, String certData) throws IOException {
		KeystoreManager mgr = new KeystoreManager();
		mgr.load(locationPrefix + location, password());
		mgr.importPrivateKey(alias, password, 
				new ByteArrayInputStream(keyData.getBytes("UTF-8")),
				new ByteArrayInputStream(certData.getBytes("UTF-8"))
		);
		mgr.save(locationPrefix + location, password());
	}
	/**
	 * Export an RSA signing request for the given key.
	 * @param alias the key alias
	 * @param password the key password
	 * @return the request text
	 */
	public String exportSigningRequest(String alias, char[] password) {
		KeystoreManager mgr = new KeystoreManager();
		mgr.load(locationPrefix + location, password());
		return mgr.createRSASigningRequest(alias, password);
	}
	/**
	 * Import an RSA signing response text.
	 * @param alias the key alias
	 * @param password the key password
	 * @param data the data
	 */
	public void importSigningResponse(String alias, char[] password, String data) {
		KeystoreManager mgr = new KeystoreManager();
		mgr.load(locationPrefix + location, password());
		try {
			mgr.installReply(alias, password, 
					new ByteArrayInputStream(data.getBytes("UTF-8")), 
					true); // FIXME not sure
		} catch (UnsupportedEncodingException ex) {
			LOG.error(ex.toString(), ex);
		}
		mgr.save(locationPrefix + location, password());
	}
	/**
	 * Open the given keystore.
	 * @return the keystore
	 */
	public KeyStore open() {
		KeystoreManager mgr = new KeystoreManager();
		mgr.load(locationPrefix + location, password());
		return mgr.getKeyStore();
	}
}
