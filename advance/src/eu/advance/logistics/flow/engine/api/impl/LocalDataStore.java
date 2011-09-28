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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceJMSEndpoint;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.AdvanceLocalFileDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceNotificationGroupType;
import eu.advance.logistics.flow.engine.api.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.AdvanceRealmStatus;
import eu.advance.logistics.flow.engine.api.AdvanceSOAPChannel;
import eu.advance.logistics.flow.engine.api.AdvanceUser;
import eu.advance.logistics.flow.engine.api.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.api.AdvanceUserRights;
import eu.advance.logistics.flow.engine.api.AdvanceWebDataSource;
import eu.advance.logistics.flow.engine.model.XSerializable;
import eu.advance.logistics.flow.engine.util.KeystoreFault;
import eu.advance.logistics.flow.engine.util.KeystoreManager;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * The local realm object containing various tables.
 * @author karnokd, 2011.09.21.
 */
public class LocalDataStore implements XSerializable, AdvanceDataStore {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(LocalDataStore.class);
	/** The cryptographic salt used to encrypt the datastore. */
	private static final byte[] CRYPTO_SALT = {
		35, -10, 99, -127, 59, -71, 42, -68
	};
	/** The cycle count for the encryption of the datastore. */
	private static final int CRYPTO_COUNT = 21;
	/** The global sequence used to generate new unique identifiers. */
	public final AtomicInteger sequence = new AtomicInteger();
	/** The users table with general and realm rights. */
	public final Map<Integer, AdvanceUser> users = Maps.newHashMap();
	/** The realms table. */
	public final Map<String, AdvanceRealm> realms = Maps.newHashMap();
	/** The key stores table. */
	public final Map<String, AdvanceKeyStore> keystores = Maps.newHashMap();
	/** The notification groups table. */
	public final Map<AdvanceNotificationGroupType, Map<String, Set<String>>> notificationGroups = Maps.newHashMap();
	/** The JDBC data sources table. */
	public final Map<Integer, AdvanceJDBCDataSource> jdbcDataSources = Maps.newHashMap();
	/** The SOAP channels table. */
	public final Map<Integer, AdvanceSOAPChannel> soapChannels = Maps.newHashMap();
	/** The JMS endpoints table. */
	public final Map<Integer, AdvanceJMSEndpoint> jmsEndpoints = Maps.newHashMap();
	/** The Web data sources table. */
	public final Map<Integer, AdvanceWebDataSource> webDataSources = Maps.newHashMap();
	/** The FTP data sources table. */
	public final Map<Integer, AdvanceFTPDataSource> ftpDataSources = Maps.newHashMap();
	/** The Local file data sources table. */
	public final Map<Integer, AdvanceLocalFileDataSource> localDataSources = Maps.newHashMap();
	/** The dataflow storage per realm. */
	public final Map<String, XElement> dataflows = Maps.newHashMap();
	/** The map from realm to block-id to an arbitrary XML used to persist block states between restarts. */
	public final Map<String, Map<String, XElement>> blockStates = Maps.newHashMap();
	/** Clear all records from the maps. */
	protected void clear() {
		users.clear();
		realms.clear();
		keystores.clear();
		notificationGroups.clear();
		jdbcDataSources.clear();
		soapChannels.clear();
		jmsEndpoints.clear();
		webDataSources.clear();
		ftpDataSources.clear();
		localDataSources.clear();
		dataflows.clear();
		blockStates.clear();
	}
	/**
	 * Add a contact to the given notification type and group.
	 * @param type the notification group type
	 * @param group the group name
	 * @param contact the new contact
	 */
	protected void addNotificationContact(AdvanceNotificationGroupType type, String group, String contact) {
		Map<String, Set<String>> groups = notificationGroups.get(type);
		if (groups == null) {
			groups = Maps.newHashMap();
			notificationGroups.put(type, groups);
		}
		Set<String> contacts = groups.get(group);
		if (contacts == null) {
			contacts = Sets.newHashSet();
			groups.put(group, contacts);
		}
		contacts.add(contact);
	}
	@Override
	public void load(XElement source) {
		clear();
		sequence.set(source.getInt("sequence"));
		for (XElement xe : source.childElement("users").childrenWithName("user")) {
			AdvanceUser e = new AdvanceUser();
			e.load(xe);
			users.put(e.id, e);
		}
		for (XElement xe : source.childElement("realms").childrenWithName("realm")) {
			AdvanceRealm e = new AdvanceRealm();
			e.load(xe);
			realms.put(e.name, e);
		}
		for (XElement xe : source.childElement("keystores").childrenWithName("keystore")) {
			AdvanceKeyStore e = new AdvanceKeyStore();
			e.load(xe);
			keystores.put(e.name, e);
		}
		for (XElement xe : source.childElement("notification-groups").childrenWithName("group")) {
			String name = xe.get("name");
			AdvanceNotificationGroupType type = AdvanceNotificationGroupType.valueOf(xe.get("type"));
			for (XElement xi : xe.childrenWithName("contact")) {
				addNotificationContact(type, name, xi.get("value"));
			}
		}
		for (XElement xe : source.childElement("jdbc-data-sources").childrenWithName("jdbc-source")) {
			AdvanceJDBCDataSource e = new AdvanceJDBCDataSource();
			e.load(xe);
			jdbcDataSources.put(e.id, e);
		}
		for (XElement xe : source.childElement("soap-channels").childrenWithName("channel")) {
			AdvanceSOAPChannel e = new AdvanceSOAPChannel();
			e.load(xe);
			soapChannels.put(e.id, e);
		}
		for (XElement xe : source.childElement("jms-endpoints").childrenWithName("endpoint")) {
			AdvanceJMSEndpoint e = new AdvanceJMSEndpoint();
			e.load(xe);
			jmsEndpoints.put(e.id, e);
		}
		for (XElement xe : source.childElement("web-data-sources").childrenWithName("web-source")) {
			AdvanceWebDataSource e = new AdvanceWebDataSource();
			e.load(xe);
			webDataSources.put(e.id, e);
		}
		for (XElement xe : source.childElement("ftp-data-sources").childrenWithName("ftp-source")) {
			AdvanceFTPDataSource e = new AdvanceFTPDataSource();
			e.load(xe);
			ftpDataSources.put(e.id, e);
		}
		for (XElement xe : source.childElement("local-data-sources").childrenWithName("local-source")) {
			AdvanceLocalFileDataSource e = new AdvanceLocalFileDataSource();
			e.load(xe);
			localDataSources.put(e.id, e);
		}
		for (XElement xe : source.childElement("dataflows").childrenWithName("flow")) {
			dataflows.put(xe.get("realm"), xe.childElement("flow-description").copy());
		}
		for (XElement xe : source.childElement("block-states").childrenWithName("realm")) {
			String realm = xe.get("name");
			Map<String, XElement> r = blockStates.get(realm);
			if (r == null) {
				r = Maps.newHashMap();
				blockStates.put(realm, r);
			}
			for (XElement be : xe.childrenWithName("block")) {
				String block = be.get("id");
				if (be.children().size() == 1) {
					r.put(block, be.children().get(0).copy());
				}
			}
		}
	}
	/**
	 * Create an XElement from the given complex map of notification groups.
	 * @param name the name of the element
	 * @param groups the map from group type to group to set of contact information
	 * @return the XElement created
	 */
	public static XElement createGroups(String name, Map<AdvanceNotificationGroupType, Map<String, Set<String>>> groups) {
		XElement result = new XElement(name);
		
		for (Map.Entry<AdvanceNotificationGroupType, Map<String, Set<String>>> e : groups.entrySet()) {
			for (Map.Entry<String, Set<String>> e2 : e.getValue().entrySet()) {
				XElement xgroup = result.add("group");
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
	 * @param source the source XElement
	 * @return the parsed map from group type to group name to contacts
	 */
	public static Map<AdvanceNotificationGroupType, Map<String, Set<String>>> parseGroups(XElement source) {
		Map<AdvanceNotificationGroupType, Map<String, Set<String>>> result = Maps.newHashMap();
		
		for (XElement xe : source.childrenWithName("group")) {
			String name = xe.get("name");
			AdvanceNotificationGroupType type = AdvanceNotificationGroupType.valueOf(xe.get("type"));
			for (XElement xi : xe.childrenWithName("contact")) {
				Map<String, Set<String>> groups = result.get(type);
				if (groups == null) {
					groups = Maps.newHashMap();
					result.put(type, groups);
				}
				Set<String> contacts = groups.get(name);
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
	public void save(XElement destination) {
		saveInto(destination, "users", "user", users);
		saveInto(destination, "realms", "realm", realms);
		saveInto(destination, "keystores", "keystore", keystores);
		
		synchronized (notificationGroups) {
			destination.add(createGroups("notification-groups", notificationGroups));
		}
		
		saveInto(destination, "jdbc-data-sources", "jdbc-source", jdbcDataSources);
		saveInto(destination, "soap-channels", "channel", soapChannels);
		saveInto(destination, "jms-endpoints", "endpoint", jmsEndpoints);
		saveInto(destination, "web-data-sources", "web-source", webDataSources);
		saveInto(destination, "ftp-data-sources", "ftp-source", ftpDataSources);
		saveInto(destination, "local-data-sources", "local-source", localDataSources);
		
		XElement xflows = destination.add("dataflows");
		for (Map.Entry<String, XElement> fe : dataflows.entrySet()) {
			XElement xf = xflows.add("flow");
			xf.set("realm", fe.getKey());
			xf.add(fe.getValue());
		}
		XElement xstate = destination.add("block-states");
		for (Map.Entry<String, Map<String, XElement>> bs : blockStates.entrySet()) {
			XElement xr = xstate.add("realm");
			xr.set("name", bs.getKey());
			for (Map.Entry<String, XElement> bse : bs.getValue().entrySet()) {
				XElement xb = xr.add("block");
				xb.set("id", bse.getKey());
				xb.add(bse.getValue());
			}
		}
		
		destination.set("sequence", sequence.get());
	}
	/**
	 * Save the XSerializable elements with the given names into the destination.
	 * @param destination the destination XElement
	 * @param collectionName the collection name to use
	 * @param itemName the item name to use
	 * @param elements the sequence of elements
	 */
	protected void saveInto(XElement destination, String collectionName, 
			String itemName, Map<?, ? extends XSerializable> elements) {
		XElement xe = destination.add(collectionName);
		synchronized (elements) {
			for (XSerializable e : elements.values()) {
				e.save(xe.add(itemName));
			}
		}
	}
	/**
	 * Load database from disk.
	 * @param fileName the datastore file name
	 */
	public void load(String fileName) {
		File dsFile = new File(fileName);
		if (dsFile.canRead()) {
			try {
				load(XElement.parseXML(dsFile));
			} catch (IOException ex) {
				LOG.error(ex.toString(), ex);
			} catch (XMLStreamException ex) {
				LOG.error(ex.toString(), ex);
			}
		}
	}
	/**
	 * Load a password encrypted data store.
	 * @param fileName the file name
	 * @param password the password
	 */
	public void loadEncrypted(String fileName, char[] password) {
		File dsFile = new File(fileName);
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
					load(xdatastore);
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
	 * @param fileName the file name
	 * @param password the password
	 */
	public void saveEncrypted(String fileName, char[] password) {
		File dsFile = new File(fileName);
		try {
			XElement xdatastore = new XElement("datastore");
			save(xdatastore);
			
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
	 * @param fileName the file name
	 */
	public void save(String fileName) {
		File dsFile = new File(fileName);
		try {
			XElement xdatastore = new XElement("datastore");
			save(xdatastore);
			xdatastore.save(dsFile);
		} catch (IOException ex) {
			LOG.error(ex.toString(), ex);
		}
	}
	@Override
	public List<AdvanceRealm> queryRealms(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_REALMS)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (realms) {
			List<AdvanceRealm> result = Lists.newArrayList();
			for (AdvanceRealm e : realms.values()) {
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
		synchronized (realms) {
			if (!realms.containsKey(name)) {
				AdvanceRealm r = new AdvanceRealm();
				r.name = name;
				r.status = AdvanceRealmStatus.STOPPED;
				r.createdAt = new Date();
				r.createdBy = token.user.name;
				r.modifiedAt = new Date();
				r.modifiedBy = token.user.name;
				realms.put(r.name, r);
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
		synchronized (realms) {
			realms.remove(name);
		}
	}

	@Override
	public void renameRealm(AdvanceControlToken token, String name,
			String newName) throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.MODIFY_REALM)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (realms) {
			AdvanceRealm r = realms.get(name);
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
	public AdvanceRealm queryRealm(AdvanceControlToken token, String realm)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.MODIFY_REALM)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (realms) {
			AdvanceRealm r = realms.get(realm);
			if (r != null) {
				return r.copy();
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
		synchronized (users) {
			List<AdvanceUser> result = Lists.newArrayList();
			for (AdvanceUser u : users.values()) {
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
		synchronized (users) {
			AdvanceUser u = users.get(userId);
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
		synchronized (users) {
			AdvanceUser u = users.get(userId);
			if (u == null) {
				throw new AdvanceControlException("User not found");
			}
			
			int maybeAdmin = 0;
			for (AdvanceUser u2 : users.values()) {
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
		synchronized (users) {
			int maybeAdmin = 0;
			for (AdvanceUser u2 : users.values()) {
				if (u2.mayModifyUser()) {
					maybeAdmin++;
				}
			}
			AdvanceUser u = users.get(userId);
			if (u.id != token.user.id) {
				if (u.mayModifyUser() && maybeAdmin <= 1) {
					throw new AdvanceControlException("No user admins would remain");
				}
				users.remove(userId);
			} else {
				throw new AdvanceControlException("Can't delete self");
			}
		}
	}

	@Override
	public void updateUser(AdvanceControlToken token, AdvanceUser user)
			throws IOException, AdvanceControlException {
		synchronized (users) {
			boolean mustExist = true;
			if (user.id == Integer.MIN_VALUE) {
				if (!hasUserRight(token, AdvanceUserRights.CREATE_USER)) {
					throw new AdvanceAccessDenied();
				}
				user.id = sequence.incrementAndGet();
				mustExist = false;
			} else {
				if (!hasUserRight(token, AdvanceUserRights.MODIFY_USER)) {
					throw new AdvanceAccessDenied();
				}
			}
			if (mustExist && !users.containsKey(user.id)) {
				throw new AdvanceControlException("User not found");
			}

			AdvanceUser prev = users.get(user.id);
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
			users.put(u.id, u);
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
		synchronized (notificationGroups) {
			Map<AdvanceNotificationGroupType, Map<String, Set<String>>> result = Maps.newHashMap();
			for (AdvanceNotificationGroupType t : notificationGroups.keySet()) {
				Map<String, Set<String>> type = Maps.newHashMap();
				result.put(t, type);
				for (String group : notificationGroups.get(t).keySet()) {
					Set<String> set = Sets.newHashSet();
					type.put(group, set);
					for (String s : notificationGroups.get(t).get(group)) {
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
		synchronized (notificationGroups) {
			notificationGroups.clear();
			for (AdvanceNotificationGroupType t : groups.keySet()) {
				Map<String, Set<String>> type = Maps.newHashMap();
				notificationGroups.put(t, type);
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
		synchronized (jdbcDataSources) {
			List<AdvanceJDBCDataSource> result = Lists.newArrayList();
			for (AdvanceJDBCDataSource e : jdbcDataSources.values()) {
				result.add(e.copy());
			}
			
			return result;
		}
	}

	@Override
	public void updateJDBCDataSource(AdvanceControlToken token,
			AdvanceJDBCDataSource dataSource) throws IOException,
			AdvanceControlException {
		synchronized (jdbcDataSources) {
			boolean mustExist = true;
			if (dataSource.id == Integer.MIN_VALUE) {
				if (!hasUserRight(token, AdvanceUserRights.CREATE_JDBC_DATA_SOURCE)) {
					throw new AdvanceAccessDenied();
				}
				dataSource.id = sequence.incrementAndGet();
				mustExist = false;
			} else {
				if (!hasUserRight(token, AdvanceUserRights.MODIFY_JDBC_DATA_SOURCE)) {
					throw new AdvanceAccessDenied();
				}
			}
			if (mustExist && !jdbcDataSources.containsKey(dataSource.id)) {
				throw new AdvanceControlException("User not found");
			}
			AdvanceJDBCDataSource u = dataSource.copy();
			AdvanceJDBCDataSource prev = jdbcDataSources.get(dataSource.id);
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
			jdbcDataSources.put(dataSource.id, u);
		}
	}

	@Override
	public void deleteJDBCDataSource(AdvanceControlToken token, int dataSourceId)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.DELETE_JDBC_DATA_SOURCE)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (jdbcDataSources) {
			jdbcDataSources.remove(dataSourceId);
		}
	}

	@Override
	public List<AdvanceJMSEndpoint> queryJMSEndpoints(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_JMS_ENDPOINTS)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (jmsEndpoints) {
			List<AdvanceJMSEndpoint> result = Lists.newArrayList();
			for (AdvanceJMSEndpoint e : jmsEndpoints.values()) {
				result.add(e.copy());
			}
			return result;
		}
	}

	@Override
	public void updateJMSEndpoint(AdvanceControlToken token,
			AdvanceJMSEndpoint endpoint) throws IOException,
			AdvanceControlException {
		
		synchronized  (jmsEndpoints) {
			boolean mustExist = true;
			if (endpoint.id == Integer.MIN_VALUE) {
				if (!hasUserRight(token, AdvanceUserRights.CREATE_JMS_ENDPOINT)) {
					throw new AdvanceAccessDenied();
				}
				endpoint.id = sequence.incrementAndGet();
				mustExist = false;
			} else {
				if (!hasUserRight(token, AdvanceUserRights.MODIFY_JMS_ENDPOINT)) {
					throw new AdvanceAccessDenied();
				}
			}
			
			if (mustExist && !jmsEndpoints.containsKey(endpoint.id)) {
				throw new AdvanceControlException("User not found");
			}
			AdvanceJMSEndpoint u = endpoint.copy();
			AdvanceJMSEndpoint prev = jmsEndpoints.get(endpoint.id);
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
			jmsEndpoints.put(endpoint.id, u);
		}
		

	}

	@Override
	public void deleteJMSEndpoint(AdvanceControlToken token, int jmsId)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.DELETE_JMS_ENDPOINT)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (jmsEndpoints) {
			jmsEndpoints.remove(jmsId);
		}
	}

	@Override
	public List<AdvanceWebDataSource> queryWebDataSources(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_WEB_DATA_SOURCES)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (webDataSources) {
			List<AdvanceWebDataSource> result = Lists.newArrayList();
			for (AdvanceWebDataSource e : webDataSources.values()) {
				result.add(e.copy());
			}
			return result;
		}
	}

	@Override
	public void updateWebDataSource(AdvanceControlToken token,
			AdvanceWebDataSource endpoint) throws IOException,
			AdvanceControlException {
		synchronized  (webDataSources) {
			boolean mustExist = true;
			if (endpoint.id == Integer.MIN_VALUE) {
				if (!hasUserRight(token, AdvanceUserRights.CREATE_WEB_DATA_SOURCE)) {
					throw new AdvanceAccessDenied();
				}
				endpoint.id = sequence.incrementAndGet();
				mustExist = false;
			} else {
				if (!hasUserRight(token, AdvanceUserRights.MODIFY_WEB_DATA_SOURCE)) {
					throw new AdvanceAccessDenied();
				}
			}
			
			if (mustExist && !webDataSources.containsKey(endpoint.id)) {
				throw new AdvanceControlException("User not found");
			}
			AdvanceWebDataSource u = endpoint.copy();
			AdvanceWebDataSource prev = webDataSources.get(endpoint.id);
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
			webDataSources.put(endpoint.id, u);
		}

	}

	@Override
	public void deleteWebDataSource(AdvanceControlToken token, int webId)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.DELETE_WEB_DATA_SOURCE)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (webDataSources) {
			webDataSources.remove(webId);
		}
	}

	@Override
	public List<AdvanceFTPDataSource> queryFTPDataSources(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_FTP_DATA_SOURCES)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (ftpDataSources) {
			List<AdvanceFTPDataSource> result = Lists.newArrayList();
			for (AdvanceFTPDataSource e : ftpDataSources.values()) {
				result.add(e.copy());
			}
			return result;
		}
	}

	@Override
	public void updateFTPDataSource(AdvanceControlToken token,
			AdvanceFTPDataSource dataSource) throws IOException,
			AdvanceControlException {
		synchronized  (ftpDataSources) {
			boolean mustExist = true;
			if (dataSource.id == Integer.MIN_VALUE) {
				if (!hasUserRight(token, AdvanceUserRights.CREATE_FTP_DATA_SOURCE)) {
					throw new AdvanceAccessDenied();
				}
				dataSource.id = sequence.incrementAndGet();
				mustExist = false;
			} else {
				if (!hasUserRight(token, AdvanceUserRights.MODIFY_FTP_DATA_SOURCE)) {
					throw new AdvanceAccessDenied();
				}
			}
			
			if (mustExist && !ftpDataSources.containsKey(dataSource.id)) {
				throw new AdvanceControlException("User not found");
			}
			AdvanceFTPDataSource u = dataSource.copy();
			AdvanceFTPDataSource prev = ftpDataSources.get(dataSource.id);
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
			ftpDataSources.put(dataSource.id, u);
		}
	}

	@Override
	public void deleteFTPDataSource(AdvanceControlToken token, int ftpId)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.DELETE_FTP_DATA_SOURCE)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (ftpDataSources) {
			ftpDataSources.remove(ftpId);
		}
	}

	@Override
	public List<AdvanceLocalFileDataSource> queryLocalFileDataSources(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_LOCAL_FILE_DATA_SOURCES)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (localDataSources) {
			List<AdvanceLocalFileDataSource> result = Lists.newArrayList();
			for (AdvanceLocalFileDataSource e : localDataSources.values()) {
				result.add(e.copy());
			}
			return result;
		}
	}

	@Override
	public void updateLocalFileDataSource(AdvanceControlToken token,
			AdvanceLocalFileDataSource dataSource) throws IOException,
			AdvanceControlException {
		synchronized  (localDataSources) {
			boolean mustExist = true;
			if (dataSource.id == Integer.MIN_VALUE) {
				if (!hasUserRight(token, AdvanceUserRights.CREATE_LOCAL_FILE_DATA_SOURCE)) {
					throw new AdvanceAccessDenied();
				}
				dataSource.id = sequence.incrementAndGet();
				mustExist = false;
			} else {
				if (!hasUserRight(token, AdvanceUserRights.MODIFY_LOCAL_FILE_DATA_SOURCE)) {
					throw new AdvanceAccessDenied();
				}
			}
			
			if (mustExist && !localDataSources.containsKey(dataSource.id)) {
				throw new AdvanceControlException("User not found");
			}
			AdvanceLocalFileDataSource u = dataSource.copy();
			AdvanceLocalFileDataSource prev = localDataSources.get(dataSource.id);
			
			if (prev != null) {
				u.createdAt = prev.createdAt;
				u.createdBy = prev.createdBy;
			} else {
				u.createdAt = new Date();
				u.createdBy = token.user.name;
			}
			u.modifiedAt = new Date();
			u.modifiedBy = token.user.name;
			localDataSources.put(dataSource.id, u);
		}
	}

	@Override
	public void deleteLocalFileDataSource(AdvanceControlToken token, int fileId)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.DELETE_LOCAL_FILE_DATA_SOURCE)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (localDataSources) {
			localDataSources.remove(fileId);
		}
	}

	@Override
	public List<AdvanceKeyStore> queryKeyStores(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_KEYSTORES)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (keystores) {
			List<AdvanceKeyStore> result = Lists.newArrayList();
			
			for (AdvanceKeyStore e : keystores.values()) {
				result.add(e.copy());
			}
			
			return result;
		}
	}
	/**
	 * Check if the user of the given token has the expected rights.
	 * @param token the token to test
	 * @param expected the expected rights
	 * @return true if the user has the expected right
	 */
	@Override
	public boolean hasUserRight(AdvanceControlToken token, AdvanceUserRights expected) {
		synchronized (users) {
			AdvanceUser u = users.get(token.user.id);
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
	@Override
	public boolean hasUserRight(AdvanceControlToken token, String realm, AdvanceUserRealmRights expected) {
		synchronized (users) {
			AdvanceUser u = users.get(token.user.id);
			return u.realmRights.containsEntry(realm, expected);
		}
	}
	@Override
	public void updateKeyStore(AdvanceControlToken token,
			AdvanceKeyStore keyStore) throws IOException,
			AdvanceControlException {
		KeystoreManager mgr = new KeystoreManager();
		synchronized (keystores) {
			AdvanceKeyStore e = keystores.get(keyStore.name);
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
					
					keystores.put(e.name, e);
				} else {
					if (!hasUserRight(token, AdvanceUserRights.MODIFY_KEYSTORE)) {
						throw new AdvanceAccessDenied();
					}
					
					File f = new File(e.location);
					if (f.exists()) {
						mgr.load(e.location, e.password);
						if (!f.delete()) {
							LOG.warn("Could not delete keystore " + e.location);
						}
					} else {
						mgr.create();
					}
					
					e.location = keyStore.location;
					if (keyStore.password != null) {
						e.password = keyStore.password;
					}
					
					e.modifiedAt = new Date();
					e.modifiedBy = token.user.name;

					mgr.save(e.location, e.password);
					
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
		synchronized (keystores) {
			AdvanceKeyStore e = keystores.get(keyStore);
			if (e != null) {
				File f = new File(e.location);
				if (!f.delete()) {
					LOG.warn("Could not delete keystore " + e.location);
				}
				keystores.remove(keyStore);
			} else {
				throw new AdvanceControlException("Keystore not found");
			}
		}
	}
	@Override
	public AdvanceKeyStore queryKeyStore(AdvanceControlToken token, String name)
			throws IOException, AdvanceControlException {
		if (!hasUserRight(token, AdvanceUserRights.LIST_KEYSTORES)) {
			throw new AdvanceAccessDenied();
		}
		synchronized (keystores) {
			
			AdvanceKeyStore e = keystores.get(name);
			if (e != null) {
				return e.copy();
			} else {
				throw new AdvanceControlException("Keystore not found");
			}
		}
	}
}
