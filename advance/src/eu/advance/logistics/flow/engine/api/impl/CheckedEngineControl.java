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

import hu.akarnokd.reactive4java.base.Observable;
import hu.akarnokd.reactive4java.base.Pair;

import java.io.IOException;
import java.util.List;

import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceEngineVersion;
import eu.advance.logistics.flow.engine.api.core.AdvanceAccessDenied;
import eu.advance.logistics.flow.engine.api.core.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceGenerateKey;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyEntry;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStoreExport;
import eu.advance.logistics.flow.engine.api.ds.AdvancePortSpecification;
import eu.advance.logistics.flow.engine.api.ds.AdvanceSchemaRegistryEntry;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUser;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRights;
import eu.advance.logistics.flow.engine.compiler.AdvanceCompilationResult;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.runtime.BlockDiagnostic;
import eu.advance.logistics.flow.engine.runtime.BlockRegistryEntry;
import eu.advance.logistics.flow.engine.runtime.PortDiagnostic;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Wraps the given engine control object and for each call, it checks
 * if the user has the appropriate rights.
 * @author akarnokd, 2011.09.29.
 */
public class CheckedEngineControl implements AdvanceEngineControl {
	/** The wrapped control. */
	protected final AdvanceEngineControl control;
	/** The datastore. */
	protected final AdvanceDataStore datastore;
	/** The target user name. */
	protected final String userName;
	/**
	 * Check if the expected right is present for the user.
	 * @param expected the expected right
	 * @throws AdvanceAccessDenied thrown if the user has no right
	 * @throws IOException if a network error occurs
	 */
	protected void check(AdvanceUserRights expected) throws IOException, AdvanceAccessDenied {
		if (!datastore.hasUserRight(userName, expected)) {
			throw new AdvanceAccessDenied();
		}
	}
	/**
	 * Check if the expected right is present for the user.
	 * @param expected the expected rights
	 * @throws AdvanceAccessDenied thrown if the user has no right
	 * @throws IOException if a network error occurs
	 */
	protected void check(AdvanceUserRights... expected) throws IOException, AdvanceAccessDenied {
		boolean allow = false;
		for (AdvanceUserRights r : expected) {
			allow |= datastore.hasUserRight(userName, r);
		}
		if (!allow) {
			throw new AdvanceAccessDenied();
		}
	}
	/**
	 * Check if the expected right is present for the user.
	 * @param realm the realm name
	 * @param expected the expected right
	 * @throws AdvanceAccessDenied thrown if the user has no right
	 * @throws IOException if a network error occurs
	 */
	protected void check(String realm, AdvanceUserRealmRights expected) throws IOException, AdvanceAccessDenied {
		if (!datastore.hasUserRight(userName, realm, expected)) {
			throw new AdvanceAccessDenied();
		}
	}
	/**
	 * Construct the wrapper for the given datastore and user name.
	 * @param control the control to wrap
	 * @param userName the user name to check the access rights
	 */
	public CheckedEngineControl(AdvanceEngineControl control, String userName) {
		this.control = control;
		this.datastore = control.datastore();
		this.userName = userName;
	}
	@Override
	public AdvanceUser getUser() throws IOException, AdvanceControlException {
		return datastore.queryUser(userName);
	}

	@Override
	public List<BlockRegistryEntry> queryBlocks() throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_BLOCKS);
		return control.queryBlocks();
	}

	@Override
	public List<AdvanceSchemaRegistryEntry> querySchemas() throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_SCHEMAS);
		return control.querySchemas();
	}

	@Override
	public AdvanceEngineVersion queryVersion() throws IOException,
			AdvanceControlException {
		return control.queryVersion();
	}

	@Override
	public void updateSchema(String name, XElement schema)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.CREATE_SCHEMA, AdvanceUserRights.MODIFY_SCHEMA);
		control.updateSchema(name, schema);
	}
	@Override
	public AdvanceSchemaRegistryEntry querySchema(String name)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.LIST_SCHEMAS);
		return control.querySchema(name);
	}
	@Override
	public void deleteSchema(String name) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.DELETE_SCHEMA);
		control.deleteSchema(name);
	}
	@Override
	public void deleteKeyEntry(String keyStore, String keyAlias)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.DELETE_KEY);
		control.deleteKeyEntry(keyStore, keyAlias);
	}

	@Override
	public void generateKey(AdvanceGenerateKey key) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.GENERATE_KEY);
		control.generateKey(key);
	}

	@Override
	public String exportCertificate(AdvanceKeyStoreExport request)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.EXPORT_CERTIFICATE);
		return control.exportCertificate(request);
	}

	@Override
	public String exportPrivateKey(AdvanceKeyStoreExport request)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.EXPORT_PRIVATE_KEY);
		return control.exportPrivateKey(request);
	}

	@Override
	public void importCertificate(AdvanceKeyStoreExport request, String data)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.IMPORT_CERTIFICATE);
		control.importCertificate(request, data);
	}

	@Override
	public void importPrivateKey(AdvanceKeyStoreExport request, String keyData,
			String certData) throws IOException, AdvanceControlException {
		check(AdvanceUserRights.IMPORT_PRIVATE_KEY);
		control.importPrivateKey(request, keyData, certData);
	}

	@Override
	public String exportSigningRequest(AdvanceKeyStoreExport request)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.EXPORT_CERTIFICATE);
		return control.exportSigningRequest(request);
	}

	@Override
	public void importSigningResponse(AdvanceKeyStoreExport request, String data)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.IMPORT_CERTIFICATE);
		control.importSigningResponse(request, data);
	}

	@Override
	public String testJDBCDataSource(String dataSourceName) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_JDBC_DATA_SOURCES);
		return control.testJDBCDataSource(dataSourceName);
	}

	@Override
	public String testJMSEndpoint(String jmsName) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_JMS_ENDPOINTS);
		return control.testJMSEndpoint(jmsName);
	}

	@Override
	public String testFTPDataSource(String ftpName) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_FTP_DATA_SOURCES);
		return control.testFTPDataSource(ftpName);
	}

	@Override
	public List<AdvanceKeyEntry> queryKeys(String keyStore) throws IOException,
			AdvanceControlException {
		check(AdvanceUserRights.LIST_KEYS);
		return control.queryKeys(keyStore);
	}

	@Override
	public AdvanceDataStore datastore() {
		return new CheckedDataStore(datastore, userName);
	}

	@Override
	public void stopRealm(String name, String byUser) throws IOException,
			AdvanceControlException {
		check(name, AdvanceUserRealmRights.STOP);
		control.stopRealm(name, userName);
	}

	@Override
	public void startRealm(String name, String byUser) throws IOException,
			AdvanceControlException {
		check(name, AdvanceUserRealmRights.START);
		control.startRealm(name, userName);
	}

	@Override
	public AdvanceCompositeBlock queryFlow(String realm) throws IOException,
			AdvanceControlException {
		check(realm, AdvanceUserRealmRights.READ);
		return control.queryFlow(realm);
	}

	@Override
	public void updateFlow(String realm, AdvanceCompositeBlock flow, String byUser)
			throws IOException, AdvanceControlException {
		check(realm, AdvanceUserRealmRights.WRITE);
		control.updateFlow(realm, flow, userName);
	}

	@Override
	public AdvanceCompilationResult verifyFlow(AdvanceCompositeBlock flow)
			throws IOException, AdvanceControlException {
		check(AdvanceUserRights.LIST_BLOCKS);
		check(AdvanceUserRights.LIST_SCHEMAS);
		return control.verifyFlow(flow);
	}

	@Override
	public Observable<BlockDiagnostic> debugBlock(String realm,
			String blockId) throws IOException, AdvanceControlException {
		check(realm, AdvanceUserRealmRights.DEBUG);
		return control.debugBlock(realm, blockId);
	}

	@Override
	public Observable<PortDiagnostic> debugParameter(String realm,
			String blockId, String port) throws IOException,
			AdvanceControlException {
		check(realm, AdvanceUserRealmRights.DEBUG);
		return control.debugParameter(realm, blockId, port);
	}

	@Override
	public void injectValue(String realm, String blockId, String port,
			XElement value) throws IOException, AdvanceControlException {
		check(realm, AdvanceUserRealmRights.DEBUG);
		control.injectValue(realm, blockId, port, value);
	}

	@Override
	public void shutdown() throws IOException, AdvanceControlException {
		check(AdvanceUserRights.SHUTDOWN);
		control.shutdown();
	}
	@Override
	public AdvanceCompilationResult queryCompilationResult(String realm)
			throws IOException, AdvanceControlException {
		check(realm, AdvanceUserRealmRights.READ);
		return control.queryCompilationResult(realm);
	}
	@Override
	public List<AdvancePortSpecification> queryPorts(String realm)
			throws IOException, AdvanceControlException {
		check(realm, AdvanceUserRealmRights.IO);
		return control.queryPorts(realm);
	}
	@Override
	public Observable<XElement> receivePort(String realm, String portId)
			throws IOException, AdvanceControlException {
		check(realm, AdvanceUserRealmRights.IO);
		return control.receivePort(realm, portId);
	}
	@Override
	public void sendPort(String realm,
			Iterable<Pair<String, XElement>> portValues) throws IOException,
			AdvanceControlException {
		check(realm, AdvanceUserRealmRights.IO);
		control.sendPort(realm, portValues);
	}
}
