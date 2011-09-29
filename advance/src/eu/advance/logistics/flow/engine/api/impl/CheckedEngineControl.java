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

import hu.akarnokd.reactive4java.reactive.Observable;

import java.io.IOException;
import java.util.List;

import eu.advance.logistics.flow.engine.AdvanceBlockDiagnostic;
import eu.advance.logistics.flow.engine.AdvanceParameterDiagnostic;
import eu.advance.logistics.flow.engine.api.AdvanceAccessDenied;
import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceEngineVersion;
import eu.advance.logistics.flow.engine.api.AdvanceGenerateKey;
import eu.advance.logistics.flow.engine.api.AdvanceKeyEntry;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStoreExport;
import eu.advance.logistics.flow.engine.api.AdvanceSchemaRegistryEntry;
import eu.advance.logistics.flow.engine.api.AdvanceUser;
import eu.advance.logistics.flow.engine.api.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.api.AdvanceUserRights;
import eu.advance.logistics.flow.engine.error.AdvanceCompilationError;
import eu.advance.logistics.flow.engine.model.AdvanceBlockRegistryEntry;
import eu.advance.logistics.flow.engine.model.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Wraps the given engine control object and for each call, it checks
 * if the user has the appropriate rights.
 * @author karnokd, 2011.09.29.
 */
public class CheckedEngineControl implements AdvanceEngineControl {
	/** The wrapped control. */
	protected final AdvanceEngineControl control;
	/** The target user name. */
	protected final String userName;
	/**
	 * Check if the expected right is present for the user.
	 * @param expected the expected right
	 * @throws AdvanceAccessDenied thrown if the user has no right
	 * @throws IOException if a network error occurs
	 */
	protected void check(AdvanceUserRights expected) throws IOException, AdvanceAccessDenied {
		if (!control.datastore().hasUserRight(userName, expected)) {
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
		if (!control.datastore().hasUserRight(userName, realm, expected)) {
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
		this.userName = userName;
	}
	@Override
	public AdvanceUser getUser() throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AdvanceBlockRegistryEntry> queryBlocks() throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AdvanceSchemaRegistryEntry> querySchemas() throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdvanceEngineVersion queryVersion() throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateSchema(String name, XElement schema, String byUser)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteKeyEntry(String keyStore, String keyAlias)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void generateKey(AdvanceGenerateKey key) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public String exportCertificate(AdvanceKeyStoreExport request)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String exportPrivateKey(AdvanceKeyStoreExport request)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void importCertificate(AdvanceKeyStoreExport request, String data)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void importPrivateKey(AdvanceKeyStoreExport request, String keyData,
			String certData) throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public String exportSigningRequest(AdvanceKeyStoreExport request)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void importSigningResponse(AdvanceKeyStoreExport request, String data)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void testJDBCDataSource(String dataSourceName) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void testJMSEndpoint(String jmsName) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void testFTPDataSource(String ftpName) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<AdvanceKeyEntry> queryKeys(String keyStore) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdvanceDataStore datastore() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stopRealm(String name, String byUser) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startRealm(String name, String byUser) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public AdvanceCompositeBlock queryFlow(String realm) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateFlow(String realm, AdvanceCompositeBlock flow)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<AdvanceCompilationError> verifyFlow(AdvanceCompositeBlock flow)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observable<AdvanceBlockDiagnostic> debugBlock(String realm,
			String blockId) throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observable<AdvanceParameterDiagnostic> debugParameter(String realm,
			String blockId, String port, boolean isImput) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void injectValue(String realm, String blockId, String port,
			XElement value) throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown() throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

}
