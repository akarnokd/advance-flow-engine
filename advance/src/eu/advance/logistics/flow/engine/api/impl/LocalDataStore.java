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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.advance.logistics.flow.engine.api.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceJMSEndpoint;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.AdvanceLocalFileDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceNotificationGroupType;
import eu.advance.logistics.flow.engine.api.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.AdvanceSOAPChannel;
import eu.advance.logistics.flow.engine.api.AdvanceUser;
import eu.advance.logistics.flow.engine.api.AdvanceWebDataSource;
import eu.advance.logistics.flow.model.XSerializable;
import eu.advance.logistics.xml.typesystem.XElement;

/**
 * The local realm object containing various tables.
 * @author karnokd, 2011.09.21.
 */
public class LocalDataStore implements XSerializable {
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
		for (XElement xe : source.childElement("jdbc-data-sources").childrenWithName("source")) {
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
		for (XElement xe : source.childElement("web-data-sources").childrenWithName("source")) {
			AdvanceWebDataSource e = new AdvanceWebDataSource();
			e.load(xe);
			webDataSources.put(e.id, e);
		}
		for (XElement xe : source.childElement("ftp-data-sources").childrenWithName("source")) {
			AdvanceFTPDataSource e = new AdvanceFTPDataSource();
			e.load(xe);
			ftpDataSources.put(e.id, e);
		}
		for (XElement xe : source.childElement("local-data-sources").childrenWithName("source")) {
			AdvanceLocalFileDataSource e = new AdvanceLocalFileDataSource();
			e.load(xe);
			localDataSources.put(e.id, e);
		}
	}
	@Override
	public void save(XElement destination) {
		destination.set("sequence", sequence.get());

		saveInto(destination, "users", "user", users.values());
		saveInto(destination, "realms", "realm", realms.values());
		saveInto(destination, "keystores", "keystore", keystores.values());
		
		XElement xnot = destination.add("notification-groups");
		for (Map.Entry<AdvanceNotificationGroupType, Map<String, Set<String>>> e : notificationGroups.entrySet()) {
			for (Map.Entry<String, Set<String>> e2 : e.getValue().entrySet()) {
				XElement xgroup = xnot.add("group");
				xgroup.set("name", e2.getKey());
				xgroup.set("type", e.getKey());
				for (String e3 : e2.getValue()) {
					xgroup.add("contact").set("value", e3);
				}
			}
		}
		
		saveInto(destination, "jdbc-data-sources", "source", jdbcDataSources.values());
		saveInto(destination, "soap-channels", "channel", soapChannels.values());
		saveInto(destination, "jms-endpoints", "endpoint", jmsEndpoints.values());
		saveInto(destination, "web-data-sources", "source", webDataSources.values());
		saveInto(destination, "ftp-data-sources", "source", ftpDataSources.values());
		saveInto(destination, "local-data-sources", "source", localDataSources.values());
	}
	/**
	 * Save the XSerializable elements with the given names into the destination.
	 * @param destination the destination XElement
	 * @param collectionName the collection name to use
	 * @param itemName the item name to use
	 * @param elements the sequence of elements
	 */
	protected void saveInto(XElement destination, String collectionName, 
			String itemName, Iterable<? extends XSerializable> elements) {
		XElement xe = destination.add(collectionName);
		for (XSerializable e : elements) {
			e.save(xe.add(itemName));
		}
	}
}
