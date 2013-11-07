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

package eu.advance.logistics.flow.engine.test;

import hu.akarnokd.utils.pool.BoundedPool;
import hu.akarnokd.utils.xml.XNElement;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import eu.advance.logistics.flow.engine.JDBCDataStore;
import eu.advance.logistics.flow.engine.api.core.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceNotificationGroupType;
import eu.advance.logistics.flow.engine.comm.JDBCConnection;
import eu.advance.logistics.flow.engine.comm.JDBCPoolManager;

/**
 * Test the JDBC datastore.
 * @author csirobi, 2012.12.07.
 */
public final class JDBCDataStoreTest {
	/** Test class. */
	private JDBCDataStoreTest() { }
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception	{
		AdvanceJDBCDataSource ds = new AdvanceJDBCDataSource();
		ds.driver = "com.mysql.jdbc.Driver";
		ds.url = "jdbc:mysql://localhost/advance_datastore";
		ds.user = "root";
		ds.password("admin".toCharArray());
		ds.schema = "advance_datastore";
		ds.poolSize = 5;

		JDBCPoolManager mgr = new JDBCPoolManager(ds);
		BoundedPool<JDBCConnection> pool = new BoundedPool<JDBCConnection>(ds.poolSize, mgr);
		JDBCDataStore jds = new JDBCDataStore(null, pool);

		//JDBCDataStoreTest.queryTestCases(jds);
		JDBCDataStoreTest.deleteTestCases(jds);

		pool.close();
	}

	/**
	 * Run query test cases.
	 * @param jds the data source
	 * @throws AdvanceControlException on error
	 * @throws IOException on error
	 */
	public static void queryTestCases(JDBCDataStore jds) throws AdvanceControlException, IOException {
		QueryAdvanceDataStore dataStoreMode = QueryAdvanceDataStore.QUERY_FLOW;

		switch (dataStoreMode) {
		case QUERY_BLOCK_STATE :
		{
			XNElement x = jds.queryBlockState("default", "block_02");
			System.out.println((x != null) ? x.toString() : "Nincs ertek");
			break;
		}
		case QUERY_FLOW :
		{
			XNElement x = jds.queryFlow("default");
			System.out.println((x != null) ? x.toString() : "Nincs ertek");
			break;
		}
		case QUERY_NOTIFICATION_GROUPS :
		{
			Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> result = jds.queryNotificationGroups();
			Map<String, Collection<String>> st = result.get(AdvanceNotificationGroupType.PAGER);

			for (String key : st.keySet()) {
				System.out.println("Nevek: " + key);
				for (String notification : st.get(key)) {
					System.out.println("Notification: " + notification);
				}
			}
			break;
		}
		default:
		}

	}
	/**
	 * Run delete test cases.
	 * @param jds the data store
	 * @throws AdvanceControlException on error
	 * @throws IOException on error
	 */
	public static void deleteTestCases(JDBCDataStore jds) throws AdvanceControlException, IOException {
		DeleteAdvanceDataStore dataStoreMode = DeleteAdvanceDataStore.DELETE_REALM;

		switch(dataStoreMode) {
		case DELETE_BLOCK_STATE: {
			jds.deleteBlockStates("DEFAULT2");
			break;
		}
		case DELETE_EMAIL_BOX: {
			jds.deleteEmailBox("teszt");
			break;
		}
		case DELETE_FTP_DATA_SOURCE: {
			jds.deleteFTPDataSource("xx");
			break;
		}
		case DELETE_JDBC_DATA_SOURCE: {
			jds.deleteJMSEndpoint("xx");
			break;
		}
		case DELETE_REALM: {
			jds.deleteRealm("DEFAULT2");
			break;
		}
		default:
		}
	}

}
