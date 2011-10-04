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
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
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
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * A JDBC based remote datastore.
 * @author karnokd, 2011.09.23.
 */
public class JDBCDataStore implements AdvanceDataStore {

	@Override
	public List<AdvanceRealm> queryRealms() throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdvanceRealm queryRealm(String realm) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createRealm(String realm, String byUser) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteRealm(String realm) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateRealm(AdvanceRealm realm) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AdvanceUser> queryUsers() throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdvanceUser queryUser(String userName) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void enableUser(String userName, boolean enabled, String byUser)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteUser(String userName, String byUser) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateUser(AdvanceUser user) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<AdvanceNotificationGroupType, Map<String, Set<String>>> queryNotificationGroups()
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateNotificationGroups(
			Map<AdvanceNotificationGroupType, Map<String, Set<String>>> groups)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AdvanceJDBCDataSource> queryJDBCDataSources()
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateJDBCDataSource(AdvanceJDBCDataSource dataSource)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteJDBCDataSource(String dataSourceName) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AdvanceJMSEndpoint> queryJMSEndpoints() throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateJMSEndpoint(AdvanceJMSEndpoint endpoint)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteJMSEndpoint(String jmsName) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AdvanceWebDataSource> queryWebDataSources() throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateWebDataSource(AdvanceWebDataSource endpoint)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteWebDataSource(String webName) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AdvanceFTPDataSource> queryFTPDataSources() throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateFTPDataSource(AdvanceFTPDataSource dataSource)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteFTPDataSource(String ftpName) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AdvanceLocalFileDataSource> queryLocalFileDataSources()
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateLocalFileDataSource(AdvanceLocalFileDataSource dataSource)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteLocalFileDataSource(String fileName) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AdvanceKeyStore> queryKeyStores() throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdvanceKeyStore queryKeyStore(String name) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasUserRight(String userName, AdvanceUserRights expected)
			throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasUserRight(String userName, String realm,
			AdvanceUserRealmRights expected) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void updateKeyStore(AdvanceKeyStore keyStore) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteKeyStore(String keyStore) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AdvanceJDBCDataSource queryJDBCDataSource(String name)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdvanceJMSEndpoint queryJMSEndpoint(String name) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdvanceSOAPChannel querySOAPChannel(String name) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdvanceFTPDataSource queryFTPDataSource(String name)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdvanceWebDataSource queryWebDataSource(String name)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdvanceLocalFileDataSource queryLocalFileDataSource(String name)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> queryNotificationGroup(
			AdvanceNotificationGroupType type, String name) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XElement queryBlockState(String realm, String blockId)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateBlockState(String realm, String blockId, XElement state)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public XElement queryFlow(String realm) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AdvanceSOAPChannel> querySOAPChannels() throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}
}
