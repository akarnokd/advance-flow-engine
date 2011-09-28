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

package eu.advance.logistics.flow.engine.api;

/**
 * Enumeration for defining user rights.
 * @author karnokd, 2011.09.19.
 */
public enum AdvanceUserRights {
	/** List users. */
	LIST_USERS,
	/** Create new user. */
	CREATE_USER,
	/** Modify user. */
	MODIFY_USER,
	/** Delete user. */
	DELETE_USER,
	/** List realms. */
	LIST_REALMS,
	/** Create new realm. */
	CREATE_REALM,
	/** Modify properties of a new realm. */
	MODIFY_REALM,
	/** Delete a realm. */
	DELETE_REALM,
	/** List the keystores. */
	LIST_KEYSTORES,
	/** Create keystore. */
	CREATE_KEYSTORE,
	/** Delete keystore. */
	DELETE_KEYSTORE,
	/** Modify keystore. */
	MODIFY_KEYSTORE,
	/** List keystore keys. */
	LIST_KEYS,
	/** Delete a key. */
	DELETE_KEY,
	/** Export key certificate. */
	EXPORT_CERTIFICATE,
	/** Export private key. */
	EXPORT_PRIVATE_KEY,
	/** Import certificate. */
	IMPORT_CERTIFICATE,
	/** Import private key. */
	IMPORT_PRIVATE_KEY,
	/** Generate new key. */
	GENERATE_KEY,
	/** List notification groups. */
	LIST_NOTIFICATION_GROUPS,
	/** Create notification group. */
	CREATE_NOTIFICATION_GROUP,
	/** Modify an existing notification group. */
	MODIFY_NOTIFICATION_GROUP,
	/** Delete a notification group. */
	DELETE_NOTIFICATION_GROUP,
	/** List JDBC data sources. */
	LIST_JDBC_DATA_SOURCES,
	/** Create new JDBC data source. */
	CREATE_JDBC_DATA_SOURCE,
	/** Modify an existing JDBC data source. */
	MODIFY_JDBC_DATA_SOURCE,
	/** Delete JDBC data source. */
	DELETE_JDBC_DATA_SOURCE,
	/** List Java Messaging Endpoints. */
	LIST_JMS_ENDPOINTS,
	/** Create new JMS endpoint. */
	CREATE_JMS_ENDPOINT,
	/** Modify JMS endpoint. */
	MODIFY_JMS_ENDPOINT,
	/** Delete JMS endpoint. */
	DELETE_JMS_ENDPOINT,
	/** List SOAP channels. */
	LIST_SOAP_CHANNELS,
	/** Create a SOAP channel. */
	CREATE_SOAP_CHANNEL,
	/** Modify a SOAP channel. */
	MODIFY_SOAP_CHANNEL,
	/** Delete a SOAP channel. */
	DELETE_SOAP_CHANNEL,
	/** List web data sources. */
	LIST_WEB_DATA_SOURCES,
	/** Create web data source. */
	CREATE_WEB_DATA_SOURCE,
	/** Modify web data source. */
	MODIFY_WEB_DATA_SOURCE,
	/** Delete web data source. */
	DELETE_WEB_DATA_SOURCE,
	/** List FTP data sources. */
	LIST_FTP_DATA_SOURCES,
	/** Create FTP data source. */
	CREATE_FTP_DATA_SOURCE,
	/** Modify FTP data source. */
	MODIFY_FTP_DATA_SOURCE,
	/** Delete FTP data source. */
	DELETE_FTP_DATA_SOURCE,
	/** List local file data sources. */
	LIST_LOCAL_FILE_DATA_SOURCES,
	/** Create local file data source. */
	CREATE_LOCAL_FILE_DATA_SOURCE,
	/** Modify local file data source. */
	MODIFY_LOCAL_FILE_DATA_SOURCE,
	/** Delete local file data source. */
	DELETE_LOCAL_FILE_DATA_SOURCE,
	/** List the available blocks. */
	LIST_BLOCKS,
	/** List the available schemas. */
	LIST_SCHEMAS,
	/** Add a new schema. */
	CREATE_SCHEMA,
	/** Modify an existing schema. */
	MODIFY_SCHEMA
}
