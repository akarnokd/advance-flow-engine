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

import hu.akarnokd.reactive4java.base.Action1E;
import hu.akarnokd.reactive4java.base.Func1E;
import hu.akarnokd.utils.database.DB;
import hu.akarnokd.utils.database.SQLInvoke;
import hu.akarnokd.utils.database.SQLResult;
import hu.akarnokd.utils.mix.XNUtils;
import hu.akarnokd.utils.sequence.SequenceUtils;
import hu.akarnokd.utils.xml.XNElement;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.core.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.core.Pool;
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
import eu.advance.logistics.flow.engine.api.ds.AdvanceSOAPEndpoint;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUser;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRights;
import eu.advance.logistics.flow.engine.api.ds.AdvanceWebDataSource;
import eu.advance.logistics.flow.engine.comm.JDBCConnection;

/**
 * A JDBC based remote datastore.
 * 
 * @author akarnokd, 2011.09.23.
 */
public class JDBCDataStore implements AdvanceDataStore {
	/** The update methods. */
	protected final AdvanceDataStoreUpdate update;
	/** The connection pool. */
	protected final Pool<JDBCConnection>   pool;
	/**
	 * Constructs a JDBC datastore with the given update implementation and the
	 * given connection pool.
	 * 
	 * @param updateImpl
	 *          the update implementation
	 * @param pool
	 *          the connection pool
	 */
	public JDBCDataStore(@NonNull AdvanceDataStoreUpdate updateImpl,
			@NonNull Pool<JDBCConnection> pool)	{
		this.update = updateImpl;
		this.pool = pool;
	}
	@Override
	public void createRealm(String realm, String byUser) throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteBlockStates(String realm) throws IOException, AdvanceControlException {
		String sql = "DELETE FROM block_state WHERE fi_realm_name = ?";
		this.delete(sql, realm);
	}

	@Override
	public void deleteEmailBox(String name) throws IOException, AdvanceControlException {
		String sql = "DELETE FROM email_box WHERE name = ?";
		this.delete(sql, name);
	}

	@Override
	public void deleteFTPDataSource(String ftpName) throws IOException, AdvanceControlException {
		String sql = "DELETE FROM ftp_data_source WHERE name = ?";
		this.delete(sql, ftpName);
	}

	@Override
	public void deleteJDBCDataSource(String dataSourceName) throws IOException, AdvanceControlException {
		String sql = "DELETE FROM jdbc_data_source WHERE name = ?";
		this.delete(sql, dataSourceName);
	}

	@Override
	public void deleteJMSEndpoint(String jmsName) throws IOException, AdvanceControlException {
		String sql = "DELETE FROM jms_endpoint WHERE name = ?";
		this.delete(sql, jmsName);
	}

	@Override
	public void deleteKeyStore(String keyStore) throws IOException, AdvanceControlException {
		String sql = "DELETE FROM keystore WHERE name = ?";
		this.delete(sql, keyStore);
	}

	@Override
	public void deleteLocalFileDataSource(String fileName) throws IOException, AdvanceControlException {
		String sql = "DELETE FROM local_file_data_source WHERE name = ?";
		this.delete(sql, fileName);
	}

	@Override
	public void deleteRealm(String realm) throws IOException, AdvanceControlException {
		String sql = "DELETE FROM realm WHERE name = ?";
		this.delete(sql, realm);
	}

	@Override
	public void deleteSOAPEndpoint(String name) throws IOException, AdvanceControlException	{
		String sql = "DELETE FROM soap_channel WHERE name = ?";
		this.delete(sql, name);
	}

	@Override
	public void deleteUser(String userName, String byUser) throws IOException, AdvanceControlException {
		String sql = "DELETE FROM user WHERE name = ?";
		this.delete(sql, userName);
	}

	@Override
	public void deleteWebDataSource(String webName) throws IOException, AdvanceControlException {
		String sql = "DELETE FROM web_data_source WHERE name = ?";
		this.delete(sql, webName);
	}

	/**
	 * The general environment for delete sql-scripts.
	 * 
	 * @param sql
	 *          the delete script
	 * @param id
	 *          the parameter of WHERE condition of delete script
	 * @throws IOException on error
	 */
	protected void delete(String sql, String id) throws IOException {
		try {
			JDBCConnection jconn = pool.get();
			try {
				jconn.update(sql, id);
				jconn.commit();
			} catch (SQLException ex) {
				jconn.rollbackSilently();
				throw new IOException(ex);
			} finally {
				pool.put(jconn);
			}
		} catch (IOException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}
	/**
	 * Query a list of objects.
	 * @param <T> the result element type
	 * @param sql the SQL query
	 * @param unmarshaller the record unmarshaller
	 * @param params the parameters
	 * @return the list of the result objects
	 * @throws IOException on error
	 */
	protected <T> List<T> query(CharSequence sql, 
			Func1E<? super ResultSet, ? extends T, ? extends SQLException> unmarshaller,
			Object... params)
			throws IOException {
		try {
			JDBCConnection jconn = pool.get();
			try {
				return jconn.query(sql, unmarshaller, params);
			} catch (SQLException ex) {
				jconn.rollbackSilently();
				throw new IOException(ex);
			} finally {
				pool.put(jconn);
			}
		} catch (IOException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}
	/**
	 * Query a list of objects.
	 * @param sql the SQL query
	 * @param unmarshaller the record unmarshaller
	 * @param params the parameters
	 * @throws IOException on error
	 */
	protected void query(CharSequence sql, 
			Action1E<? super ResultSet, ? extends SQLException> unmarshaller,
			Object... params)
			throws IOException {
		try {
			JDBCConnection jconn = pool.get();
			try {
				jconn.query(sql, unmarshaller, params);
			} catch (SQLException ex) {
				jconn.rollbackSilently();
				throw new IOException(ex);
			} finally {
				pool.put(jconn);
			}
		} catch (IOException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}
	/**
	 * Query a list of objects.
	 * @param <T> the result element type
	 * @param sql the SQL query
	 * @param unmarshaller the record unmarshaller
	 * @param params the parameters
	 * @return the result objects or null if not present
	 * @throws IOException on error
	 */
	protected <T> T querySingle(CharSequence sql, 
			Func1E<? super ResultSet, ? extends T, ? extends SQLException> unmarshaller,
			Object... params)
			throws IOException {
		try {
			JDBCConnection jconn = pool.get();
			try {
				return Iterables.getFirst(jconn.query(sql, unmarshaller, params), null);
			} catch (SQLException ex) {
				throw new IOException(ex);
			} finally {
				pool.put(jconn);
			}
		} catch (IOException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void enableUser(String userName, boolean enabled, String byUser) throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasUserRight(String userName, AdvanceUserRights expected) throws IOException {
		String sql = "SELECT 1 FROM user_rights WHERE name = ? AND right = ? ";
		return Integer.valueOf(1).equals(querySingle(sql, DB.SELECT_INT, userName, expected.name()));
	}

	@Override
	public boolean hasUserRight(String userName, String realm, AdvanceUserRealmRights expected) throws IOException {
		String sql = "SELECT 1 FROM user_realm_rights WHERE name = ? AND realm = ? AND right = ? ";
		return Integer.valueOf(1).equals(querySingle(sql, DB.SELECT_INT, userName, realm, expected.name()));
	}

	@Override
	public XNElement queryBlockState(String realm, String blockId) throws IOException, AdvanceControlException {
		StringBuffer sql;
		SQLResult<XNElement> sqlResult;

		try {
			JDBCConnection jconn = pool.get();
			try {
				sql = new StringBuffer();
				sql.append("SELECT description ");
				sql.append("FROM block_state ");
				sql.append("WHERE fi_realm_name = ? ");
				sql.append("AND id_block = ?");

				sqlResult = new SQLResult<XNElement>() {
					@Override
					public XNElement invoke(ResultSet rs) throws SQLException {
						XNElement xe = null;
						try {
							xe = XNElement.parseXML(rs.getBinaryStream("description"));
						} catch (XMLStreamException e) {
							new IOException("Parse error.. " + e);
						}
						return xe;
					}
				};

				List<XNElement> r = jconn.query(sql.toString(), sqlResult, realm, blockId);

				return Iterables.getFirst(r, null);
			} finally {
				pool.put(jconn);
			}
		} catch (SQLException ex) {
			throw new IOException(ex);
		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public AdvanceEmailBox queryEmailBox(String name) 
			throws IOException, AdvanceControlException {
		String sql = "SELECT * FROM email_box WHERE name = ? ";
		return querySingle(sql, AdvanceEmailBox.SELECT, name);
	}

	@Override
	public List<AdvanceEmailBox> queryEmailBoxes() throws IOException, AdvanceControlException {
		String sql = "SELECT * FROM email_box WHERE name = ? ";
		return query(sql, AdvanceEmailBox.SELECT);
	}

	@Override
	public XNElement queryFlow(String realm) throws IOException, AdvanceControlException {
		String sql = "SELECT description FROM dataflow WHERE realm = ? ";
		return querySingle(sql, XNUtils.fromResultSet(1), realm);
	}

	@Override
	public AdvanceFTPDataSource queryFTPDataSource(String name) throws IOException, AdvanceControlException {
		String sql = "SELECT * FROM ftp_data_source WHERE name = ?";
		return querySingle(sql, AdvanceFTPDataSource.SELECT, name);
	}

	@Override
	public List<AdvanceFTPDataSource> queryFTPDataSources() throws IOException, AdvanceControlException {
		String sql = "SELECT * FROM ftp_data_source";
		return query(sql, AdvanceFTPDataSource.SELECT);
	}

	@Override
	public AdvanceJDBCDataSource queryJDBCDataSource(String name) throws IOException, AdvanceControlException {
		String sql = "SELECT * FROM jdbc_data_source WHERE name = ? ";
		return querySingle(sql, AdvanceJDBCDataSource.SELECT, name);
	}

	@Override
	public List<AdvanceJDBCDataSource> queryJDBCDataSources() throws IOException, AdvanceControlException {
		String sql = "SELECT * FROM jdbc_data_source";
		return query(sql, AdvanceJDBCDataSource.SELECT);
	}

	@Override
	public AdvanceJMSEndpoint queryJMSEndpoint(String name) throws IOException, AdvanceControlException	{
		String sql = "SELECT * FROM jms_endpoint WHERE name = ? ";
		return querySingle(sql, AdvanceJMSEndpoint.SELECT, name);
	}

	@Override
	public List<AdvanceJMSEndpoint> queryJMSEndpoints() throws IOException, AdvanceControlException	{
		String sql = "SELECT * FROM jms_endpoint";
		return query(sql, AdvanceJMSEndpoint.SELECT);
	}

	@Override
	public AdvanceKeyStore queryKeyStore(String name) throws IOException, AdvanceControlException {
		String sql = "SELECT * FROM keystore WHERE name = ? ";
		return querySingle(sql, AdvanceKeyStore.SELECT, name);
	}

	@Override
	public List<AdvanceKeyStore> queryKeyStores() throws IOException, AdvanceControlException {
		String sql = "SELECT * FROM keystore ";
		return query(sql, AdvanceKeyStore.SELECT);
	}

	@Override
	public AdvanceLocalFileDataSource queryLocalFileDataSource(String name) throws IOException, AdvanceControlException {
		String sql = "SELECT * FROM local_file_data_source WHERE name = ? ";
		return querySingle(sql, AdvanceLocalFileDataSource.SELECT, name);
	}

	@Override
	public List<AdvanceLocalFileDataSource> queryLocalFileDataSources() throws IOException, AdvanceControlException {
		String sql = "SELECT * FROM local_file_data_source ";
		return query(sql, AdvanceLocalFileDataSource.SELECT);
	}

	@Override
	public Set<String> queryNotificationGroup(AdvanceNotificationGroupType type, String name) throws IOException, AdvanceControlException {
		String sql = "SELECT contact_info FROM notification_group WHERE type = ? AND name = ? ";
		
		Set<String> result = Sets.newHashSet();
		
		query(sql, SequenceUtils.into(result, DB.SELECT_STRING), type.name(), name);
		
		
		return result;
	}

	@Override
	public Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> queryNotificationGroups() throws IOException, AdvanceControlException	{
		final Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> result = Maps.newHashMap();

		String sql = "SELECT * FROM notification_group";
		
		query(sql, new SQLInvoke() {
			@Override
			public void invoke(ResultSet t) throws SQLException {
				AdvanceNotificationGroupType type = AdvanceNotificationGroupType.valueOf(t.getString("type"));
				String name = t.getString("name");
				String value = t.getString("value");
				
				Map<String, Collection<String>> typeMap = result.get(type);
				if (typeMap == null) {
					typeMap = new HashMap<>();
					result.put(type, typeMap);
				}
				Collection<String> coll = typeMap.get(name);
				if (coll == null) {
					coll = new HashSet<>();
					typeMap.put(name, coll);
				}
				coll.add(value);
			}
		});
		
		return result;
	}

	@Override
	public AdvanceRealm queryRealm(String realm) throws IOException, AdvanceControlException {
		String sql = "SELECT * FROM realm WHERE name = ? ";
		return querySingle(sql, AdvanceRealm.SELECT, realm);
	}

	@Override
	public List<AdvanceRealm> queryRealms() throws IOException, AdvanceControlException {
		String sql = "SELECT * FROM realm ";
		return query(sql, AdvanceRealm.SELECT);
	}

	@Override
	public AdvanceUser queryUser(String userName) throws IOException, AdvanceControlException {
		String sql = "SELECT * FROM user WHERE name = ? ";
		
		final AdvanceUser result = querySingle(sql, AdvanceUser.SELECT, userName);
		
		sql = "SELECT * FROM user_rights WHERE name = ? ";
		
		for (String s : query(sql, DB.SELECT_STRING, userName)) {
			result.rights.add(AdvanceUserRights.valueOf(s));
		}
		
		sql = "SELECT * FROM user_realm_rights WHERE name = ? ";
		
		query(sql, new SQLInvoke() {
			@Override
			public void invoke(ResultSet t) throws SQLException {
				String realm = t.getString("realm");
				AdvanceUserRealmRights rights = AdvanceUserRealmRights.valueOf(t.getString("right"));
				result.realmRights.put(realm, rights);
			}
		}, userName);
		
		return result;
	}

	@Override
	public List<AdvanceUser> queryUsers() throws IOException, AdvanceControlException {
		String sql = "SELECT * FROM user ";
		
		List<AdvanceUser> result = query(sql, AdvanceUser.SELECT);
		
		final Map<String, AdvanceUser> map = new HashMap<>();
		for (AdvanceUser u : result) {
			map.put(u.id(), u);
		}
		
		sql = "SELECT * FROM user_rights ";

		query(sql, new SQLInvoke() {
			@Override
			public void invoke(ResultSet t) throws SQLException {
				AdvanceUserRights rights = AdvanceUserRights.valueOf(t.getString("right"));
				AdvanceUser u = map.get(t.getString("name"));
				if (u != null) {
					u.rights.add(rights);
				}
			}
		});
		
		sql = "SELECT * FROM user_realm_rights ";
		
		query(sql, new SQLInvoke() {
			@Override
			public void invoke(ResultSet t) throws SQLException {
				String realm = t.getString("realm");
				AdvanceUserRealmRights rights = AdvanceUserRealmRights.valueOf(t.getString("right"));
				AdvanceUser u = map.get(t.getString("name"));
				if (u != null) {
					u.realmRights.put(realm, rights);
				}
			}
		});
		
		return result;
	}

	@Override
	public AdvanceWebDataSource queryWebDataSource(String name) throws IOException, AdvanceControlException {
		String sql = "SELECT * FROM web_data_source WHERE name = ? ";
		return querySingle(sql, AdvanceWebDataSource.SELECT, name);
	}

	@Override
	public List<AdvanceWebDataSource> queryWebDataSources() throws IOException, AdvanceControlException	{
		String sql = "SELECT * FROM web_data_source ";
		return query(sql, AdvanceWebDataSource.SELECT);
	}

	@Override
	public void updateEmailBox(AdvanceEmailBox box) throws IOException, AdvanceControlException {
		update.updateEmailBox(box);
	}

	@Override
	public void updateFTPDataSource(AdvanceFTPDataSource dataSource) throws IOException, AdvanceControlException {
		update.updateFTPDataSource(dataSource);
	}

	@Override
	public void updateJDBCDataSource(AdvanceJDBCDataSource dataSource) throws IOException, AdvanceControlException {
		update.updateJDBCDataSource(dataSource);
	}
	@Override
	public void updateJMSEndpoint(AdvanceJMSEndpoint endpoint) throws IOException, AdvanceControlException {
		update.updateJMSEndpoint(endpoint);
	}
	@Override
	public void updateKeyStore(AdvanceKeyStore keyStore) throws IOException, AdvanceControlException {
		update.updateKeyStore(keyStore);
	}
	@Override
	public void updateLocalFileDataSource(AdvanceLocalFileDataSource dataSource) throws IOException, AdvanceControlException {
		update.updateLocalFileDataSource(dataSource);
	}
	@Override
	public void updateNotificationGroups(Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> groups) throws IOException, AdvanceControlException {
		update.updateNotificationGroups(groups);
	}
	@Override
	public void updateRealm(AdvanceRealm realm) throws IOException, AdvanceControlException {
		update.updateRealm(realm);
	}
	@Override
	public void updateUser(AdvanceUser user) throws IOException, AdvanceControlException {
		update.updateUser(user);
	}
	@Override
	public void updateWebDataSource(AdvanceWebDataSource endpoint) throws IOException, AdvanceControlException {
		update.updateWebDataSource(endpoint);
	}
	@Override
	public void updateBlockState(String realm, String blockId, XNElement state)
			throws IOException, AdvanceControlException {
		update.updateBlockState(realm, blockId, state);
	}
	@Override
	public void updateFlow(String realm, XNElement flow) throws IOException,
			AdvanceControlException {
		update.updateFlow(realm, flow);
	}
	@Override
	public void updateSOAPEndpoint(AdvanceSOAPEndpoint endpoint)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public AdvanceSOAPEndpoint querySOAPEndpoint(String name)
			throws IOException, AdvanceControlException {
		String sql = "SELECT * FROM soap_endpoint WHERE name = ? ";
		return querySingle(sql, AdvanceSOAPEndpoint.SELECT, name);
	}
	@Override
	public List<AdvanceSOAPEndpoint> querySOAPEndpoints() throws IOException,
			AdvanceControlException {
		String sql = "SELECT * FROM soap_endpoint ";
		return query(sql, AdvanceSOAPEndpoint.SELECT);
	}
	
}
