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

import hu.akarnokd.utils.xml.XNElement;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.advance.logistics.flow.engine.api.core.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.ds.AdvanceCreateModifyInfo;
import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceEmailBox;
import eu.advance.logistics.flow.engine.api.ds.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceFTPProtocols;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJDBCDrivers;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJMSDrivers;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJMSEndpoint;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceLocalFileDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceLoginType;
import eu.advance.logistics.flow.engine.api.ds.AdvanceNotificationGroupType;
import eu.advance.logistics.flow.engine.api.ds.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.ds.AdvanceRealmStatus;
import eu.advance.logistics.flow.engine.api.ds.AdvanceSOAPEndpoint;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUser;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRights;
import eu.advance.logistics.flow.engine.api.ds.AdvanceWebDataSource;

/**
 * A test datastore which usually returns empty lists and simple initialized objects and
 * does nothing on method calls.
 * @author akarnokd, 2011.10.03.
 */
public class DummyDataStore implements AdvanceDataStore {
	/** The logger object. */
	protected static final Logger LOG = LoggerFactory.getLogger(DummyDataStore.class);
	/**
	 * Set the creation and modification details.
	 * @param result the object to set
	 */
	private static void setCreator(AdvanceCreateModifyInfo result) {
		result.createdAt = new Date(0);
		result.createdBy = "Test";
		result.modifiedAt = new Date(24L * 60 * 60 * 1000);
		result.modifiedBy = "Test2";
	}
	/** @return create a test realm object. */
	public static AdvanceRealm createTestRealm() {
		AdvanceRealm r = new AdvanceRealm();
		r.name = "TestRealm";
		r.status = AdvanceRealmStatus.STOPPED;
		setCreator(r);
		return r;
	}
	@Override
	public List<AdvanceRealm> queryRealms() throws IOException,
			AdvanceControlException {
		return Lists.newArrayList(createTestRealm());
	}

	@Override
	public AdvanceRealm queryRealm(String realm) throws IOException,
			AdvanceControlException {
		return createTestRealm();
	}

	@Override
	public void createRealm(String realm, String byUser) throws IOException,
			AdvanceControlException {
		// NO operation
	}

	@Override
	public void deleteRealm(String realm) throws IOException,
			AdvanceControlException {
		// NO operation
	}

	@Override
	public void updateRealm(AdvanceRealm realm)
			throws IOException, AdvanceControlException {
		// NO operation
	}
	/** @return create a test user object. */
	public static AdvanceUser createTestUser() {
		AdvanceUser u = new AdvanceUser();
		u.name = "TestUser";
		
		u.dateFormat = "yyyy-MM-dd";
		u.dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
		u.enabled = true;
		u.email = "test@advance-logistics.eu";
		u.decimalSeparator = ',';
		u.thousandSeparator = ' ';
		u.passwordLogin = true;
		u.password("test".toCharArray());
		
		u.rights.addAll(Arrays.asList(AdvanceUserRights.values()));
		u.realmRights.putAll("TestRealm", Arrays.asList(AdvanceUserRealmRights.values()));

		setCreator(u);

		return u;
	}
	@Override
	public List<AdvanceUser> queryUsers() throws IOException,
			AdvanceControlException {
		return Lists.newArrayList(createTestUser());
	}

	@Override
	public AdvanceUser queryUser(String userName) throws IOException,
			AdvanceControlException {
		return createTestUser();
	}

	@Override
	public void enableUser(String userName, boolean enabled, String byUser)
			throws IOException, AdvanceControlException {
		// NO operation
	}

	@Override
	public void deleteUser(String userName, String byUser) throws IOException,
			AdvanceControlException {
		// NO operation
	}

	@Override
	public void updateUser(AdvanceUser user) throws IOException,
			AdvanceControlException {
		// NO operation
	}

	@Override
	public Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> queryNotificationGroups()
			throws IOException, AdvanceControlException {
		Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> result = Maps.newHashMap();
		
		result.put(AdvanceNotificationGroupType.EMAIL, 
				Collections.singletonMap("Group1", 
						(Collection<String>)Collections.singleton("test@advance-logistics.eu")));
		return result;
	}

	@Override
	public void updateNotificationGroups(
			Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> groups)
			throws IOException, AdvanceControlException {
		// NO operation
	}
	/**
	 * @return Create a test JDBC record.
	 */
	public static AdvanceJDBCDataSource createTestJDBC() {
		AdvanceJDBCDataSource result = new AdvanceJDBCDataSource();
		result.name = "Test";
		result.driver = AdvanceJDBCDrivers.MYSQL.driverClass;
		result.url = AdvanceJDBCDrivers.MYSQL.urlTemplate;
		result.schema = "advance";
		result.user = "Test";
		result.password("test".toCharArray());
		setCreator(result);
		return result;
	}
	@Override
	public List<AdvanceJDBCDataSource> queryJDBCDataSources()
			throws IOException, AdvanceControlException {
		return Lists.newArrayList(createTestJDBC());
	}

	@Override
	public void updateJDBCDataSource(AdvanceJDBCDataSource dataSource)
			throws IOException, AdvanceControlException {
		// NO operation
	}

	@Override
	public void deleteJDBCDataSource(String dataSourceName) throws IOException,
			AdvanceControlException {
		// NO operation
	}

	/** @return create a test JMS endpoint. */
	public static AdvanceJMSEndpoint createTestJMS() {
		AdvanceJMSEndpoint result = new AdvanceJMSEndpoint();
		
		result.name = "Test";
		result.driver = AdvanceJMSDrivers.OPEN_JMS.driverClass;
		result.url = AdvanceJMSDrivers.OPEN_JMS.urlTemplate;
		result.queueManager = "DEFAULT";
		result.queue = "Advance";
		result.user = "Test";
		result.password("test".toCharArray());
		
		setCreator(result);
		
		return result;
	}
	@Override
	public List<AdvanceJMSEndpoint> queryJMSEndpoints() throws IOException,
			AdvanceControlException {
		return Lists.newArrayList(createTestJMS());
	}

	@Override
	public void updateJMSEndpoint(AdvanceJMSEndpoint endpoint)
			throws IOException, AdvanceControlException {
		// NO operation
	}

	@Override
	public void deleteJMSEndpoint(String jmsName) throws IOException,
			AdvanceControlException {
		// NO operation
	}
	/** @return a test web data source object. */
	public static AdvanceWebDataSource createTestWeb() {
		AdvanceWebDataSource r = new AdvanceWebDataSource();
		r.name = "Advance";
		try {
			r.url = new URL("http://www.advance-logistics.eu");
		} catch (MalformedURLException ex) {
			LOG.error(ex.toString(), ex);
		}
		r.loginType = AdvanceLoginType.NONE;
		setCreator(r);
		return r;
	}
	@Override
	public List<AdvanceWebDataSource> queryWebDataSources() throws IOException,
			AdvanceControlException {
		return Lists.newArrayList(createTestWeb());
	}

	@Override
	public void updateWebDataSource(AdvanceWebDataSource endpoint)
			throws IOException, AdvanceControlException {
		// NO operation
	}

	@Override
	public void deleteWebDataSource(String webName) throws IOException,
			AdvanceControlException {
		// NO operation
	}
	/** @return create a test ftp record. */
	public static AdvanceFTPDataSource createTestFTP() {
		AdvanceFTPDataSource r = new AdvanceFTPDataSource();
		r.name = "Test";
		r.protocol = AdvanceFTPProtocols.FTP;
		r.remoteDirectory = "/";
		r.address = "ftp.advance-logistics.eu";
		setCreator(r);
		return r;
	}
	@Override
	public List<AdvanceFTPDataSource> queryFTPDataSources() throws IOException,
			AdvanceControlException {
		return Lists.newArrayList(createTestFTP());
	}

	@Override
	public void updateFTPDataSource(AdvanceFTPDataSource dataSource)
			throws IOException, AdvanceControlException {
		// NO operation
	}

	@Override
	public void deleteFTPDataSource(String ftpName) throws IOException,
			AdvanceControlException {
		// NO operation
	}
	/** @return create test local record. */
	public static AdvanceLocalFileDataSource createTestLocal() {
		AdvanceLocalFileDataSource r = new AdvanceLocalFileDataSource();
		
		r.name = "Test";
		r.directory = "test";
		
		setCreator(r);
		return r;
	}
	@Override
	public List<AdvanceLocalFileDataSource> queryLocalFileDataSources()
			throws IOException, AdvanceControlException {
		return Lists.newArrayList(createTestLocal());
	}

	@Override
	public void updateLocalFileDataSource(AdvanceLocalFileDataSource dataSource)
			throws IOException, AdvanceControlException {
		// NO operation
	}

	@Override
	public void deleteLocalFileDataSource(String fileName) throws IOException,
			AdvanceControlException {
		// NO operation
	}
	/** @return create test keystore. */
	public static AdvanceKeyStore createTestKeyStore() {
		AdvanceKeyStore r = new AdvanceKeyStore();
		r.name = "Test";
		r.password("test".toCharArray());
		setCreator(r);
		return r;
	}
	@Override
	public List<AdvanceKeyStore> queryKeyStores() throws IOException,
			AdvanceControlException {
		return Lists.newArrayList(createTestKeyStore());
	}

	@Override
	public AdvanceKeyStore queryKeyStore(String name) throws IOException,
			AdvanceControlException {
		return createTestKeyStore();
	}

	@Override
	public boolean hasUserRight(String userName, AdvanceUserRights expected)
			throws IOException {
		return true;
	}

	@Override
	public boolean hasUserRight(String userName, String realm,
			AdvanceUserRealmRights expected) throws IOException {
		return true;
	}

	@Override
	public void updateKeyStore(AdvanceKeyStore keyStore) throws IOException,
			AdvanceControlException {
		// NO operation
	}

	@Override
	public void deleteKeyStore(String keyStore) throws IOException,
			AdvanceControlException {
		// NO operation
	}

	@Override
	public AdvanceJDBCDataSource queryJDBCDataSource(String name)
			throws IOException, AdvanceControlException {
		return createTestJDBC();
	}

	@Override
	public AdvanceJMSEndpoint queryJMSEndpoint(String name) throws IOException,
			AdvanceControlException {
		return createTestJMS();
	}

	/** @return create test SOAP record. */
	public static AdvanceSOAPEndpoint createTestSOAP() {
		AdvanceSOAPEndpoint r = new AdvanceSOAPEndpoint();
		r.name = "Test";
		try {
			r.endpoint = new URL("http://soap.advance-logistics.eu");
		} catch (MalformedURLException ex) {
			LOG.error(ex.toString(), ex);
		}
		r.method = "test";
		try {
			r.targetObject = new URI("advance:test");
		} catch (URISyntaxException ex) {
			LOG.error(ex.toString(), ex);
		}
		
		setCreator(r);
		return r;
	}
	@Override
	public AdvanceSOAPEndpoint querySOAPEndpoint(String name) throws IOException,
			AdvanceControlException {
		return createTestSOAP();
	}

	@Override
	public AdvanceFTPDataSource queryFTPDataSource(String name)
			throws IOException, AdvanceControlException {
		return createTestFTP();
	}

	@Override
	public AdvanceWebDataSource queryWebDataSource(String name)
			throws IOException, AdvanceControlException {
		return createTestWeb();
	}

	@Override
	public AdvanceLocalFileDataSource queryLocalFileDataSource(String name)
			throws IOException, AdvanceControlException {
		return createTestLocal();
	}

	@Override
	public Set<String> queryNotificationGroup(
			AdvanceNotificationGroupType type, String name) throws IOException,
			AdvanceControlException {
		return Sets.newHashSet("test@advance-logistics.eu");
	}
	/** @return Create an empty test XML. */
	public static XNElement createTestXML() {
		return new XNElement("test");
	}
	@Override
	public XNElement queryBlockState(String realm, String blockId)
			throws IOException, AdvanceControlException {
		return createTestXML();
	}

	@Override
	public void updateBlockState(String realm, String blockId, XNElement state)
			throws IOException, AdvanceControlException {
		// NO operation
	}

	@Override
	public XNElement queryFlow(String realm) throws IOException,
			AdvanceControlException {
		XNElement r = new XNElement("flow-description");
		r.add("composite-block");
		return r;
	}

	@Override
	public List<AdvanceSOAPEndpoint> querySOAPEndpoints() throws IOException,
			AdvanceControlException {
		return Lists.newArrayList(createTestSOAP());
	}
	@Override
	public void deleteBlockStates(String realm) throws IOException,
			AdvanceControlException {
		// NO operation
	}
	@Override
	public void updateFlow(String realm, XNElement flow) throws IOException,
			AdvanceControlException {
		// NO operation
	}
	@Override
	public void deleteEmailBox(String name) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
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
	public void updateEmailBox(AdvanceEmailBox box) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
	}
	@Override
	public void deleteSOAPEndpoint(String name) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void updateSOAPEndpoint(AdvanceSOAPEndpoint channel)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		
	}
}
