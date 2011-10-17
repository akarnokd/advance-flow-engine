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

package eu.advance.logistics.flow.engine.test;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.HasPassword;
import eu.advance.logistics.flow.engine.api.impl.HttpDataStoreListener;
import eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializable;

/**
 * Test methods of HttpRemoteDataStore and HttpDataStoreListener.
 * @author akarnokd, 2011.10.03.
 */
public class TestHttpDataStore {
	/** The datastore object. */
	protected HttpRemoteDataStore ds;

	/**
	 * Remove the password value from the object.
	 * @param <T> a type implementing {@code HasPassword}
	 * @param obj the object with HasPassword capability
	 * @return the same obj
	 */
	static <T extends HasPassword> T trimPassword(T obj) {
		obj.password(null);
		return obj;
	}
	/**
	 * Test if two XSerializable object is equal.
	 * @param s1 the first object
	 * @param s2 the second object
	 * @return true if equal
	 */
	static boolean testEquals(XSerializable s1, XSerializable s2) {
		XElement e1 = new XElement("e");
		s1.save(e1);
		XElement e2 = new XElement("e");
		s2.save(e2);
		return testEquals(e1, e2);
	}
	/**
	 * Compare two XElements via their string representation.
	 * @param e1 the first element
	 * @param e2 the second element
	 * @return true if equals
	 */
	static boolean testEquals(XElement e1, XElement e2) {
		return e1.toString().equals(e2.toString());
	}
	/**
	 * @throws java.lang.Exception on error
	 */
	@Before
	public void setUp() throws Exception {
		TestHttpCommunicator comm = new TestHttpCommunicator(new HttpDataStoreListener(new DummyDataStore()), "TestUser");
		ds = new HttpRemoteDataStore(comm);
	}

	/**
	 * @throws java.lang.Exception on error
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryRealms()}.
	 */
	@Test
	public void testQueryRealms() {
		try {
			List<AdvanceRealm> result = ds.queryRealms();
			if (result.size() != 1) {
				fail("One element expected, got " + result.size());
			}
			AdvanceRealm r = result.get(0);
			assertTrue(testEquals(r, DummyDataStore.createTestRealm()));
		} catch (IOException ex) {
			fail("IOException " + ex);
		} catch (AdvanceControlException ex) {
			fail("IOException " + ex);
		}
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryRealm(java.lang.String)}.
	 */
	@Test
	public void testQueryRealm() {
		try {
			AdvanceRealm r = ds.queryRealm("Test");
			assertTrue(testEquals(r, DummyDataStore.createTestRealm()));
		} catch (IOException ex) {
			fail("IOException " + ex);
		} catch (AdvanceControlException ex) {
			fail("IOException " + ex);
		}
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#createRealm(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testCreateRealm() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#deleteRealm(java.lang.String)}.
	 */
	@Test
	public void testDeleteRealm() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#updateRealm(eu.advance.logistics.flow.engine.api.AdvanceRealm)}.
	 */
	@Test
	public void testUpdateRealm() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryUsers()}.
	 */
	@Test
	public void testQueryUsers() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryUser(java.lang.String)}.
	 */
	@Test
	public void testQueryUser() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#enableUser(java.lang.String, boolean, java.lang.String)}.
	 */
	@Test
	public void testEnableUser() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#deleteUser(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testDeleteUser() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#updateUser(eu.advance.logistics.flow.engine.api.AdvanceUser)}.
	 */
	@Test
	public void testUpdateUser() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryNotificationGroups()}.
	 */
	@Test
	public void testQueryNotificationGroups() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#updateNotificationGroups(java.util.Map)}.
	 */
	@Test
	public void testUpdateNotificationGroups() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryJDBCDataSources()}.
	 */
	@Test
	public void testQueryJDBCDataSources() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#updateJDBCDataSource(eu.advance.logistics.flow.engine.api.AdvanceJDBCDataSource)}.
	 */
	@Test
	public void testUpdateJDBCDataSource() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#deleteJDBCDataSource(java.lang.String)}.
	 */
	@Test
	public void testDeleteJDBCDataSource() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryJMSEndpoints()}.
	 */
	@Test
	public void testQueryJMSEndpoints() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#updateJMSEndpoint(eu.advance.logistics.flow.engine.api.AdvanceJMSEndpoint)}.
	 */
	@Test
	public void testUpdateJMSEndpoint() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#deleteJMSEndpoint(java.lang.String)}.
	 */
	@Test
	public void testDeleteJMSEndpoint() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryWebDataSources()}.
	 */
	@Test
	public void testQueryWebDataSources() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#updateWebDataSource(eu.advance.logistics.flow.engine.api.AdvanceWebDataSource)}.
	 */
	@Test
	public void testUpdateWebDataSource() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#deleteWebDataSource(java.lang.String)}.
	 */
	@Test
	public void testDeleteWebDataSource() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryFTPDataSources()}.
	 */
	@Test
	public void testQueryFTPDataSources() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#updateFTPDataSource(eu.advance.logistics.flow.engine.api.AdvanceFTPDataSource)}.
	 */
	@Test
	public void testUpdateFTPDataSource() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#deleteFTPDataSource(java.lang.String)}.
	 */
	@Test
	public void testDeleteFTPDataSource() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryLocalFileDataSources()}.
	 */
	@Test
	public void testQueryLocalFileDataSources() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#updateLocalFileDataSource(eu.advance.logistics.flow.engine.api.AdvanceLocalFileDataSource)}.
	 */
	@Test
	public void testUpdateLocalFileDataSource() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#deleteLocalFileDataSource(java.lang.String)}.
	 */
	@Test
	public void testDeleteLocalFileDataSource() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryKeyStores()}.
	 */
	@Test
	public void testQueryKeyStores() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryKeyStore(java.lang.String)}.
	 */
	@Test
	public void testQueryKeyStore() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#hasUserRight(java.lang.String, eu.advance.logistics.flow.engine.api.AdvanceUserRights)}.
	 */
	@Test
	public void testHasUserRightStringAdvanceUserRights() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#hasUserRight(java.lang.String, java.lang.String, eu.advance.logistics.flow.engine.api.AdvanceUserRealmRights)}.
	 */
	@Test
	public void testHasUserRightStringStringAdvanceUserRealmRights() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#updateKeyStore(eu.advance.logistics.flow.engine.api.AdvanceKeyStore)}.
	 */
	@Test
	public void testUpdateKeyStore() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#deleteKeyStore(java.lang.String)}.
	 */
	@Test
	public void testDeleteKeyStore() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryNotificationGroup(eu.advance.logistics.flow.engine.api.AdvanceNotificationGroupType, java.lang.String)}.
	 */
	@Test
	public void testQueryNotificationGroup() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryJDBCDataSource(java.lang.String)}.
	 */
	@Test
	public void testQueryJDBCDataSource() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryJMSEndpoint(java.lang.String)}.
	 */
	@Test
	public void testQueryJMSEndpoint() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#querySOAPChannel(java.lang.String)}.
	 */
	@Test
	public void testQuerySOAPChannel() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryFTPDataSource(java.lang.String)}.
	 */
	@Test
	public void testQueryFTPDataSource() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryWebDataSource(java.lang.String)}.
	 */
	@Test
	public void testQueryWebDataSource() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryLocalFileDataSource(java.lang.String)}.
	 */
	@Test
	public void testQueryLocalFileDataSource() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryBlockState(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testQueryBlockState() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#updateBlockState(java.lang.String, java.lang.String, eu.advance.logistics.flow.engine.xml.typesystem.XElement)}.
	 */
	@Test
	public void testUpdateBlockState() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#queryFlow(java.lang.String)}.
	 */
	@Test
	public void testQueryFlow() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.advance.logistics.flow.engine.api.impl.HttpRemoteDataStore#querySOAPChannels()}.
	 */
	@Test
	public void testQuerySOAPChannels() {
		fail("Not yet implemented");
	}

}
