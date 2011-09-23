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
import eu.advance.logistics.flow.engine.api.AdvanceControlToken;
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceJMSEndpoint;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.AdvanceLocalFileDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceNotificationGroupType;
import eu.advance.logistics.flow.engine.api.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.AdvanceUser;
import eu.advance.logistics.flow.engine.api.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.api.AdvanceUserRights;
import eu.advance.logistics.flow.engine.api.AdvanceWebDataSource;

/**
 * A JDBC based remote datastore.
 * @author karnokd, 2011.09.23.
 */
public class JDBCDataStore implements AdvanceDataStore {

	@Override
	public List<AdvanceRealm> queryRealms(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createRealm(AdvanceControlToken token, String name)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteRealm(AdvanceControlToken token, String name)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void renameRealm(AdvanceControlToken token, String name,
			String newName) throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopRealm(AdvanceControlToken token, String name)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startRealm(AdvanceControlToken token, String name)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<AdvanceUser> queryUsers(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
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
	public boolean hasUserRight(AdvanceControlToken token,
			AdvanceUserRights expected) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasUserRight(AdvanceControlToken token, String realm,
			AdvanceUserRealmRights expected) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AdvanceKeyStore queryKeyStore(AdvanceControlToken token, String name)
			throws IOException, AdvanceControlException {
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

}
