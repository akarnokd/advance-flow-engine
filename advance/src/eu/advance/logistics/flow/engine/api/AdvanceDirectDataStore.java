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

import java.io.IOException;
import java.util.Set;

import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Contains functions for accessing the datastore without a control token.
 * Used by the engine and its blocks to access the datastore.
 * @author karnokd, 2011.09.28.
 */
public interface AdvanceDirectDataStore {
	/**
	 * Return the properties of a specific keystore.
	 * @param name the name of the keystore
	 * @return the keystore properties
	 * @throws IOException if a network error occurs
	 */
	AdvanceKeyStore queryKeyStore(String name) throws IOException;
	/**
	 * Return the properties of a a JDBC data source.
	 * @param name the name of the data source
	 * @return the data source properties
	 * @throws IOException if a network error occurs
	 */
	AdvanceJDBCDataSource queryJDBCDataSource(String name) throws IOException;
	/**
	 * Return the properties of a a JMS endpoint.
	 * @param name the name of the endpoint
	 * @return the endpoint properties
	 * @throws IOException if a network error occurs
	 */
	AdvanceJMSEndpoint queryJMSEndpoint(String name) throws IOException;
	/**
	 * Return the properties of a a SOAP channel.
	 * @param name the name of the channel
	 * @return the channel properties
	 * @throws IOException if a network error occurs
	 */
	AdvanceSOAPChannel querySOAPChannel(String name) throws IOException;
	/**
	 * Return the properties of a a FTP data source.
	 * @param name the name of the data source
	 * @return the data source properties
	 * @throws IOException if a network error occurs
	 */
	AdvanceFTPDataSource queryFTPDataSource(String name) throws IOException;
	/**
	 * Return the properties of a a Web data source.
	 * @param name the name of the data source
	 * @return the data source properties
	 * @throws IOException if a network error occurs
	 */
	AdvanceWebDataSource queryWebDataSource(String name) throws IOException;
	/**
	 * Return the properties of a a local file data source.
	 * @param name the name of the data source
	 * @return the data source properties
	 * @throws IOException if a network error occurs
	 */
	AdvanceLocalFileDataSource queryLocalFileDataSource(String name) throws IOException;
	/**
	 * Retrieve the contact information of a notification group type and name.
	 * @param type the group type
	 * @param name the group name
	 * @return the set of contact information
	 * @throws IOException if a network error occurs
	 */
	Set<String> queryNotificationGroup(AdvanceNotificationGroupType type, String name) throws IOException;
	/**
	 * Retrieve the block state.
	 * @param realm the target realm
	 * @param blockId the block identifier
	 * @return the state XElement
	 * @throws IOException if a network error occurs
	 */
	XElement queryBlockState(String realm, String blockId) throws IOException;
	/**
	 * Save the block state.
	 * @param realm the target realm
	 * @param blockId the target block identifier
	 * @param state the state XElement
	 * @throws IOException if a network error occurs
	 */
	void updateBlockState(String realm, String blockId, XElement state) throws IOException;
	/**
	 * Retrieve the flow descriptor of the given realm.
	 * @param realm the target realm
	 * @return the flow description XElement
	 * @throws IOException if a network error occurs
	 */
	XElement queryFlow(String realm) throws IOException;
}
