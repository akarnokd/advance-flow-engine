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

package eu.advance.logistics.flow.engine.api.impl;

import java.io.IOException;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.ds.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStoreUpdate;
import eu.advance.logistics.flow.engine.api.ds.AdvanceEmailBox;
import eu.advance.logistics.flow.engine.api.ds.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJMSEndpoint;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceLocalFileDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceNotificationGroupType;
import eu.advance.logistics.flow.engine.api.ds.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.ds.AdvanceSOAPChannel;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUser;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRights;
import eu.advance.logistics.flow.engine.api.ds.AdvanceWebDataSource;
import eu.advance.logistics.flow.engine.comm.Pool;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * A JDBC based remote datastore.
 * @author akarnokd, 2011.09.23.
 */
public class JDBCDataStore implements AdvanceDataStore {
	/** The update methods. */
	protected final AdvanceDataStoreUpdate update;
	/** The connection pool. */
	protected final Pool<Connection> pool;
	/**
	 * Constructs a JDBC datastore with the given update implementation and the given
	 * connection pool.
	 * @param updateImpl the update implementation
	 * @param pool the connection pool
	 */
	public JDBCDataStore(@NonNull AdvanceDataStoreUpdate updateImpl, @NonNull Pool<Connection> pool) {
		this.update = updateImpl;
		this.pool = pool;
	}
	@Override
	public void createRealm(String realm, String byUser) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteBlockStates(String realm) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteEmailBox(String name) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteFTPDataSource(String ftpName) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteJDBCDataSource(String dataSourceName) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteJMSEndpoint(String jmsName) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteKeyStore(String keyStore) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteLocalFileDataSource(String fileName) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteRealm(String realm) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteSOAPChannel(String name) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteUser(String userName, String byUser) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteWebDataSource(String webName) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enableUser(String userName, boolean enabled, String byUser)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		
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
	public XElement queryBlockState(String realm, String blockId)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdvanceEmailBox queryEmailBox(String name) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AdvanceEmailBox> queryEmailBoxes() throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XElement queryFlow(String realm) throws IOException,
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
	public List<AdvanceFTPDataSource> queryFTPDataSources() throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdvanceJDBCDataSource queryJDBCDataSource(String name)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AdvanceJDBCDataSource> queryJDBCDataSources()
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
	public List<AdvanceJMSEndpoint> queryJMSEndpoints() throws IOException,
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
	public List<AdvanceKeyStore> queryKeyStores() throws IOException,
			AdvanceControlException {
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
	public List<AdvanceLocalFileDataSource> queryLocalFileDataSources()
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
	public Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> queryNotificationGroups()
			throws IOException, AdvanceControlException {
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
	public List<AdvanceRealm> queryRealms() throws IOException,
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
	public List<AdvanceSOAPChannel> querySOAPChannels() throws IOException,
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
	public List<AdvanceUser> queryUsers() throws IOException,
			AdvanceControlException {
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
	public List<AdvanceWebDataSource> queryWebDataSources() throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateBlockState(String realm, String blockId, XElement state)
			throws IOException, AdvanceControlException {
		update.updateBlockState(realm, blockId, state);
	}

	@Override
	public void updateEmailBox(AdvanceEmailBox box) throws IOException,
			AdvanceControlException {
		update.updateEmailBox(box);
	}

	@Override
	public void updateFlow(String realm, XElement flow) throws IOException,
			AdvanceControlException {
		update.updateFlow(realm, flow);
	}

	@Override
	public void updateFTPDataSource(AdvanceFTPDataSource dataSource)
			throws IOException, AdvanceControlException {
		update.updateFTPDataSource(dataSource);
	}

	@Override
	public void updateJDBCDataSource(AdvanceJDBCDataSource dataSource)
			throws IOException, AdvanceControlException {
		update.updateJDBCDataSource(dataSource);
	}
	@Override
	public void updateJMSEndpoint(AdvanceJMSEndpoint endpoint)
			throws IOException, AdvanceControlException {
		update.updateJMSEndpoint(endpoint);
	}
	@Override
	public void updateKeyStore(AdvanceKeyStore keyStore) throws IOException,
			AdvanceControlException {
		update.updateKeyStore(keyStore);
	}
	@Override
	public void updateLocalFileDataSource(AdvanceLocalFileDataSource dataSource)
			throws IOException, AdvanceControlException {
		update.updateLocalFileDataSource(dataSource);
	}
	@Override
	public void updateNotificationGroups(
			Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> groups)
			throws IOException, AdvanceControlException {
		update.updateNotificationGroups(groups);
	}
	@Override
	public void updateRealm(AdvanceRealm realm) throws IOException,
			AdvanceControlException {
		update.updateRealm(realm);
	}
	@Override
	public void updateSOAPChannel(AdvanceSOAPChannel channel)
			throws IOException, AdvanceControlException {
		update.updateSOAPChannel(channel);
	}
	@Override
	public void updateUser(AdvanceUser user) throws IOException,
			AdvanceControlException {
		update.updateUser(user);
	}
	@Override
	public void updateWebDataSource(AdvanceWebDataSource endpoint)
			throws IOException, AdvanceControlException {
		update.updateWebDataSource(endpoint);
	}
}
