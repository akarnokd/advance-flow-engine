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

import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.AdvanceEmailBox;
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
import eu.advance.logistics.flow.engine.util.KeystoreFault;
import eu.advance.logistics.flow.engine.util.KeystoreManager;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializable;

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
	/** The users table with general and realm rights. */
	public final Map<String, AdvanceUser> users = Maps.newHashMap();
	/** The realms table. */
	public final Map<String, AdvanceRealm> realms = Maps.newHashMap();
	/** The key stores table. */
	public final Map<String, AdvanceKeyStore> keystores = Maps.newHashMap();
	/** The notification groups table. */
	public final Map<AdvanceNotificationGroupType, Map<String, Set<String>>> notificationGroups = Maps.newHashMap();
	/** The JDBC data sources table. */
	public final Map<String, AdvanceJDBCDataSource> jdbcDataSources = Maps.newHashMap();
	/** The SOAP channels table. */
	public final Map<String, AdvanceSOAPChannel> soapChannels = Maps.newHashMap();
	/** The JMS endpoints table. */
	public final Map<String, AdvanceJMSEndpoint> jmsEndpoints = Maps.newHashMap();
	/** The Web data sources table. */
	public final Map<String, AdvanceWebDataSource> webDataSources = Maps.newHashMap();
	/** The FTP data sources table. */
	public final Map<String, AdvanceFTPDataSource> ftpDataSources = Maps.newHashMap();
	/** The Local file data sources table. */
	public final Map<String, AdvanceLocalFileDataSource> localDataSources = Maps.newHashMap();
	/** The dataflow storage per realm. */
	public final Map<String, XElement> dataflows = Maps.newHashMap();
	/** The map from realm to block-id to an arbitrary XML used to persist block states between restarts. */
	public final Map<String, Map<String, XElement>> blockStates = Maps.newHashMap();
	/** The email boxes. */
	public final Map<String, AdvanceEmailBox> emailBoxes = Maps.newHashMap();
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
		emailBoxes.clear();	}
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
		for (XElement xe : source.childElement("users").childrenWithName("user")) {
			AdvanceUser e = new AdvanceUser();
			e.load(xe);
			users.put(e.name, e);
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
			jdbcDataSources.put(e.name, e);
		}
		for (XElement xe : source.childElement("soap-channels").childrenWithName("channel")) {
			AdvanceSOAPChannel e = new AdvanceSOAPChannel();
			e.load(xe);
			soapChannels.put(e.name, e);
		}
		for (XElement xe : source.childElement("jms-endpoints").childrenWithName("endpoint")) {
			AdvanceJMSEndpoint e = new AdvanceJMSEndpoint();
			e.load(xe);
			jmsEndpoints.put(e.name, e);
		}
		for (XElement xe : source.childElement("web-data-sources").childrenWithName("web-source")) {
			AdvanceWebDataSource e = new AdvanceWebDataSource();
			e.load(xe);
			webDataSources.put(e.name, e);
		}
		for (XElement xe : source.childElement("ftp-data-sources").childrenWithName("ftp-source")) {
			AdvanceFTPDataSource e = new AdvanceFTPDataSource();
			e.load(xe);
			ftpDataSources.put(e.name, e);
		}
		for (XElement xe : source.childElement("local-data-sources").childrenWithName("local-source")) {
			AdvanceLocalFileDataSource e = new AdvanceLocalFileDataSource();
			e.load(xe);
			localDataSources.put(e.name, e);
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
	public List<AdvanceRealm> queryRealms()
			throws IOException, AdvanceControlException {
		synchronized (realms) {
			List<AdvanceRealm> result = Lists.newArrayList();
			for (AdvanceRealm e : realms.values()) {
				result.add(e.copy());
			}
			return result;
		}
	}
	@Override
	public void createRealm(String name, String byUser)
			throws IOException, AdvanceControlException {
		synchronized (realms) {
			if (!realms.containsKey(name)) {
				AdvanceRealm r = new AdvanceRealm();
				r.name = name;
				r.status = AdvanceRealmStatus.STOPPED;
				r.createdAt = new Date();
				r.createdBy = byUser;
				r.modifiedAt = new Date();
				r.modifiedBy = byUser;
				realms.put(r.name, r);
			} else {
				throw new AdvanceControlException("Realm exists");
			}
		}
	}

	@Override
	public void deleteRealm(String name)
			throws IOException, AdvanceControlException {
		synchronized (realms) {
			realms.remove(name);
		}
	}

	@Override
	public void updateRealm(AdvanceRealm realm) throws IOException, AdvanceControlException {
		synchronized (realms) {
			AdvanceRealm prev = realms.get(realm.name);
			AdvanceRealm next = realm.copy();
			if (prev != null) {
				next.createdAt = prev.createdAt;
				next.createdBy = prev.createdBy;
				next.modifiedAt = new Date();
			} else {
				next.createdAt = new Date();
				next.createdBy = next.modifiedBy;
				next.modifiedAt = new Date();
			}
			realms.put(next.name, next);
		}		
	}
	@Override
	public AdvanceRealm queryRealm(String realm)
			throws IOException, AdvanceControlException {
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
	public List<AdvanceUser> queryUsers()
			throws IOException, AdvanceControlException {
		synchronized (users) {
			List<AdvanceUser> result = Lists.newArrayList();
			for (AdvanceUser u : users.values()) {
				result.add(u.copy());
			}
			return result;
		}
	}

	@Override
	public AdvanceUser queryUser(String userName)
			throws IOException, AdvanceControlException {
		synchronized (users) {
			AdvanceUser u = users.get(userName);
			if (u != null) {
				return u.copy();
			}
			throw new AdvanceControlException("User not found");
		}
	}

	@Override
	public void enableUser(String userName,
			boolean enabled, String byUser) throws IOException, AdvanceControlException {
		synchronized (users) {
			AdvanceUser u = users.get(userName);
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
			if (!u.name.equals(byUser)) {
				if (u.mayModifyUser() && maybeAdmin <= 1) {
					throw new AdvanceControlException("No user admins would remain");
				}
				u.enabled = enabled;
				u.modifiedAt = new Date();
				u.modifiedBy = byUser;
			} else {
				throw new AdvanceControlException("Can't disable self");
			}
		}
	}

	@Override
	public void deleteUser(String userName, String byUser)
			throws IOException, AdvanceControlException {
		synchronized (users) {
			int maybeAdmin = 0;
			for (AdvanceUser u2 : users.values()) {
				if (u2.mayModifyUser()) {
					maybeAdmin++;
				}
			}
			AdvanceUser u = users.get(userName);
			if (!u.name.equals(byUser)) {
				if (u.mayModifyUser() && maybeAdmin <= 1) {
					throw new AdvanceControlException("No user admins would remain");
				}
				users.remove(userName);
			} else {
				throw new AdvanceControlException("Can't delete self");
			}
		}
	}

	@Override
	public void updateUser(AdvanceUser user)
			throws IOException, AdvanceControlException {
		synchronized (users) {
			AdvanceUser prev = users.get(user.name);
			AdvanceUser u = user.copy();
			u.password(user.password() != null ? user.password().clone() : (prev != null ? prev.password() : null));
			if (prev != null) {
				u.createdAt = prev.createdAt;
				u.createdBy = prev.createdBy;
			} else {
				u.createdAt = new Date();
				u.createdBy = u.modifiedBy;
			}
			u.modifiedAt = new Date();
			users.put(u.name, u);
			// ensure that self is nut turned off or loses admin rights
			if (u.name.equals(u.modifiedBy) && prev != null) {
				u.enabled = prev.enabled;
				if (prev.mayModifyUser()) {
					u.rights.add(AdvanceUserRights.LIST_USERS);
					u.rights.add(AdvanceUserRights.MODIFY_USER);
				}
			}
		}
	}

	@Override
	public Map<AdvanceNotificationGroupType, Map<String, Set<String>>> queryNotificationGroups()
			throws IOException,
			AdvanceControlException {
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
	public void updateNotificationGroups(
			Map<AdvanceNotificationGroupType, Map<String, Set<String>>> groups)
			throws IOException, AdvanceControlException {
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
	public List<AdvanceJDBCDataSource> queryJDBCDataSources() throws IOException,
			AdvanceControlException {
		synchronized (jdbcDataSources) {
			List<AdvanceJDBCDataSource> result = Lists.newArrayList();
			for (AdvanceJDBCDataSource e : jdbcDataSources.values()) {
				result.add(e.copy());
			}
			
			return result;
		}
	}

	@Override
	public void updateJDBCDataSource(
			AdvanceJDBCDataSource dataSource) throws IOException,
			AdvanceControlException {
		synchronized (jdbcDataSources) {
			AdvanceJDBCDataSource u = dataSource.copy();
			AdvanceJDBCDataSource prev = jdbcDataSources.get(dataSource.name);
			u.password(dataSource.password() != null ? dataSource.password().clone() : (prev != null ? prev.password() : null));
			
			if (prev != null) {
				u.createdAt = prev.createdAt;
				u.createdBy = prev.createdBy;
			} else {
				u.createdAt = new Date();
				u.createdBy = u.modifiedBy;
			}
			u.modifiedAt = new Date();
			jdbcDataSources.put(u.name, u);
		}
	}

	@Override
	public void deleteJDBCDataSource(String dataSourceName)
			throws IOException, AdvanceControlException {
		synchronized (jdbcDataSources) {
			jdbcDataSources.remove(dataSourceName);
		}
	}

	@Override
	public List<AdvanceJMSEndpoint> queryJMSEndpoints()
			throws IOException, AdvanceControlException {
		synchronized (jmsEndpoints) {
			List<AdvanceJMSEndpoint> result = Lists.newArrayList();
			for (AdvanceJMSEndpoint e : jmsEndpoints.values()) {
				result.add(e.copy());
			}
			return result;
		}
	}

	@Override
	public void updateJMSEndpoint(
			AdvanceJMSEndpoint endpoint) throws IOException,
			AdvanceControlException {
		
		synchronized  (jmsEndpoints) {
			AdvanceJMSEndpoint prev = jmsEndpoints.get(endpoint.name);
			
			AdvanceJMSEndpoint u = endpoint.copy();
			u.password(endpoint.password() != null ? endpoint.password().clone() : (prev != null ? prev.password() : null));
			
			if (prev != null) {
				u.createdAt = prev.createdAt;
				u.createdBy = prev.createdBy;
			} else {
				u.createdAt = new Date();
				u.createdBy = endpoint.modifiedBy;
			}
			u.modifiedAt = new Date();
			jmsEndpoints.put(endpoint.name, u);
		}
		

	}

	@Override
	public void deleteJMSEndpoint(String jmsName)
			throws IOException, AdvanceControlException {
		synchronized (jmsEndpoints) {
			jmsEndpoints.remove(jmsName);
		}
	}

	@Override
	public List<AdvanceWebDataSource> queryWebDataSources() throws IOException,
			AdvanceControlException {
		synchronized (webDataSources) {
			List<AdvanceWebDataSource> result = Lists.newArrayList();
			for (AdvanceWebDataSource e : webDataSources.values()) {
				result.add(e.copy());
			}
			return result;
		}
	}

	@Override
	public void updateWebDataSource(AdvanceWebDataSource endpoint) throws IOException,
			AdvanceControlException {
		synchronized  (webDataSources) {
			AdvanceWebDataSource prev = webDataSources.get(endpoint.name);
			
			AdvanceWebDataSource u = endpoint.copy();
			u.password(endpoint.password() != null ? endpoint.password().clone() : (prev != null ? prev.password() : null));
			
			if (prev != null) {
				u.createdAt = prev.createdAt;
				u.createdBy = prev.createdBy;
			} else {
				u.createdAt = new Date();
				u.createdBy = endpoint.modifiedBy;
			}
			u.modifiedAt = new Date();
			webDataSources.put(u.name, u);
		}

	}

	@Override
	public void deleteWebDataSource(String webName)
			throws IOException, AdvanceControlException {
		synchronized (webDataSources) {
			webDataSources.remove(webName);
		}
	}

	@Override
	public List<AdvanceFTPDataSource> queryFTPDataSources() throws IOException,
			AdvanceControlException {
		synchronized (ftpDataSources) {
			List<AdvanceFTPDataSource> result = Lists.newArrayList();
			for (AdvanceFTPDataSource e : ftpDataSources.values()) {
				result.add(e.copy());
			}
			return result;
		}
	}

	@Override
	public void updateFTPDataSource(
			AdvanceFTPDataSource dataSource) throws IOException,
			AdvanceControlException {
		synchronized  (ftpDataSources) {
			AdvanceFTPDataSource prev = ftpDataSources.get(dataSource.name);
			
			AdvanceFTPDataSource u = dataSource.copy();
			u.password(dataSource.password() != null ? dataSource.password() : (prev != null ? prev.password() : null));
			
			if (prev != null) {
				u.createdAt = prev.createdAt;
				u.createdBy = prev.createdBy;
			} else {
				u.createdAt = new Date();
				u.createdBy = dataSource.modifiedBy;
			}
			u.modifiedAt = new Date();
			ftpDataSources.put(u.name, u);
		}
	}

	@Override
	public void deleteFTPDataSource(String ftpName)
			throws IOException, AdvanceControlException {
		synchronized (ftpDataSources) {
			ftpDataSources.remove(ftpName);
		}
	}

	@Override
	public List<AdvanceLocalFileDataSource> queryLocalFileDataSources() throws IOException,
			AdvanceControlException {
		synchronized (localDataSources) {
			List<AdvanceLocalFileDataSource> result = Lists.newArrayList();
			for (AdvanceLocalFileDataSource e : localDataSources.values()) {
				result.add(e.copy());
			}
			return result;
		}
	}

	@Override
	public void updateLocalFileDataSource(
			AdvanceLocalFileDataSource dataSource) throws IOException,
			AdvanceControlException {
		synchronized  (localDataSources) {
			AdvanceLocalFileDataSource prev = localDataSources.get(dataSource.name);
			
			AdvanceLocalFileDataSource u = dataSource.copy();
			
			if (prev != null) {
				u.createdAt = prev.createdAt;
				u.createdBy = prev.createdBy;
			} else {
				u.createdAt = new Date();
				u.createdBy = dataSource.modifiedBy;
			}
			u.modifiedAt = new Date();
			localDataSources.put(u.name, u);
		}
	}

	@Override
	public void deleteLocalFileDataSource(String fileName)
			throws IOException, AdvanceControlException {
		synchronized (localDataSources) {
			localDataSources.remove(fileName);
		}
	}

	@Override
	public List<AdvanceKeyStore> queryKeyStores()
			throws IOException, AdvanceControlException {
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
	 * @param userName the user name to test
	 * @param expected the expected rights
	 * @return true if the user has the expected right
	 */
	@Override
	public boolean hasUserRight(String userName, AdvanceUserRights expected) {
		synchronized (users) {
			AdvanceUser u = users.get(userName);
			return u.rights.contains(expected);
		}
	}
	/**
	 * Check if the user of the given token has the expected rights.
	 * @param userName the user name
	 * @param realm the target realm
	 * @param expected the expected rights
	 * @return true if the user has the expected right
	 */
	@Override
	public boolean hasUserRight(String userName, String realm, AdvanceUserRealmRights expected) {
		synchronized (users) {
			AdvanceUser u = users.get(userName);
			return u.realmRights.containsEntry(realm, expected);
		}
	}
	@Override
	public void updateKeyStore(AdvanceKeyStore keyStore) throws IOException,
			AdvanceControlException {
		KeystoreManager mgr = new KeystoreManager();
		synchronized (keystores) {
			AdvanceKeyStore e = keystores.get(keyStore.name);
			try {
				if (e == null) {
					e = new AdvanceKeyStore();
					e.name = keyStore.name;
					e.password(keyStore.password());
					e.location = keyStore.location;
					e.createdAt = new Date();
					e.createdBy = keyStore.modifiedBy;
					e.modifiedAt = new Date();
					e.modifiedBy = keyStore.modifiedBy;
					
					mgr.create();
					mgr.save(e.location, e.password());
					
					keystores.put(e.name, e);
				} else {
					
					File f = new File(e.location);
					if (f.exists()) {
						mgr.load(e.location, e.password());
						if (!f.delete()) {
							LOG.warn("Could not delete keystore " + e.location);
						}
					} else {
						mgr.create();
					}
					
					e.location = keyStore.location;
					if (keyStore.password() != null) {
						e.password(keyStore.password());
					}
					
					e.modifiedAt = new Date();
					e.modifiedBy = keyStore.modifiedBy;

					mgr.save(e.location, e.password());
					
				}
			} catch (KeystoreFault ex) {
				throw new AdvanceControlException(ex);
			}
		}
	}

	@Override
	public void deleteKeyStore(String keyStore)
			throws IOException, AdvanceControlException {
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
	public AdvanceKeyStore queryKeyStore(String name)
			throws IOException, AdvanceControlException {
		synchronized (keystores) {
			AdvanceKeyStore e = keystores.get(name);
			if (e != null) {
				return e.copy();
			} else {
				throw new AdvanceControlException("Keystore not found");
			}
		}
	}
	@Override
	public AdvanceJDBCDataSource queryJDBCDataSource(String name)
			throws IOException {
		return jdbcDataSources.get(name);
	}
	@Override
	public AdvanceJMSEndpoint queryJMSEndpoint(String name) throws IOException {
		return jmsEndpoints.get(name);
	}
	@Override
	public AdvanceSOAPChannel querySOAPChannel(String name) throws IOException {
		return soapChannels.get(name);
	}
	@Override
	public AdvanceFTPDataSource queryFTPDataSource(String name)
			throws IOException {
		return ftpDataSources.get(name);
	}
	@Override
	public AdvanceWebDataSource queryWebDataSource(String name)
			throws IOException {
		return webDataSources.get(name);
	}
	@Override
	public AdvanceLocalFileDataSource queryLocalFileDataSource(String name)
			throws IOException {
		return localDataSources.get(name);
	}
	@Override
	public Set<String> queryNotificationGroup(
			AdvanceNotificationGroupType type, String name) throws IOException {
		Map<String, Set<String>> map = notificationGroups.get(type);
		if (map != null) {
			Set<String> result = map.get(name);
			if (result == null) {
				LOG.error("Missing group " + name + " in type " + type);
			}
			return result;
		}
		LOG.error("Missing group type: " + type);
		return null;
	}
	@Override
	public XElement queryBlockState(String realm, String blockId)
			throws IOException {
		Map<String, XElement> map = blockStates.get(realm);
		if (map != null) {
			XElement result = map.get(blockId);
			return result;
		}
		LOG.error("Missing realm: " + realm);
		return null;
	}
	@Override
	public void updateBlockState(String realm, String blockId, XElement state)
			throws IOException {
		Map<String, XElement> map = blockStates.get(realm);
		if (map == null) {
			map = Maps.newHashMap();
			blockStates.put(realm, map);
		}
		if (state == null) {
			map.remove(blockId);
		} else {
			map.put(blockId, state);
		}
	}
	@Override
	public XElement queryFlow(String realm) throws IOException {
		return dataflows.get(realm);
	}
	@Override
	public List<AdvanceSOAPChannel> querySOAPChannels() throws IOException,
			AdvanceControlException {
		List<AdvanceSOAPChannel> result = Lists.newArrayList();
		for (AdvanceSOAPChannel e : soapChannels.values()) {
			result.add(e.copy());
		}
		return result;
	}
	@Override
	public void deleteBlockStates(String realm) throws IOException,
			AdvanceControlException {
		synchronized (blockStates) {
			blockStates.remove(realm);
		}
	}
	@Override
	public void updateFlow(String realm, XElement flow) throws IOException,
			AdvanceControlException {
		synchronized (dataflows) {
			dataflows.put(realm, flow);
		}
	}
	@Override
	public void deleteEmailBox(String name) throws IOException,
			AdvanceControlException {
		synchronized (emailBoxes) {
			emailBoxes.remove(name);
		}
	}
	@Override
	public AdvanceEmailBox queryEmailBox(String name) throws IOException,
			AdvanceControlException {
		synchronized (emailBoxes) {
			return emailBoxes.get(name);
		}
	}
	@Override
	public List<AdvanceEmailBox> queryEmailBoxes() throws IOException,
			AdvanceControlException {
		synchronized (emailBoxes) {
			List<AdvanceEmailBox> result = Lists.newArrayList();
			for (AdvanceEmailBox b : emailBoxes.values()) {
				result.add(b.copy());
			}
			return result;
		}
	}
	@Override
	public void updateEmailBox(AdvanceEmailBox box) throws IOException,
			AdvanceControlException {
		synchronized (emailBoxes) {
			emailBoxes.put(box.name, box.copy());
		}
	}
}
