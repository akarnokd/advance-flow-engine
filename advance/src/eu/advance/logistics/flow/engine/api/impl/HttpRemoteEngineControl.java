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
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.AdvanceBlockDiagnostic;
import eu.advance.logistics.flow.engine.AdvanceParameterDiagnostic;
import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceControlToken;
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceEngineVersion;
import eu.advance.logistics.flow.engine.api.AdvanceGenerateKey;
import eu.advance.logistics.flow.engine.api.AdvanceHttpAuthentication;
import eu.advance.logistics.flow.engine.api.AdvanceKeyEntry;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStoreExport;
import eu.advance.logistics.flow.engine.api.AdvanceSchemaRegistryEntry;
import eu.advance.logistics.flow.engine.api.AdvanceWebLoginType;
import eu.advance.logistics.flow.engine.error.AdvanceCompilationError;
import eu.advance.logistics.flow.engine.model.AdvanceBlockRegistryEntry;
import eu.advance.logistics.flow.engine.model.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * @author karnokd, 2011.09.29.
 */
public class HttpRemoteEngineControl implements AdvanceEngineControl {
	/** The communicator used to send and receive requests. */
	protected HttpCommunicator comm;
	/**
	 * Initialize the engine control with the given remote address and authentication
	 * record.
	 * @param remote the remote datastore URL
	 * @param auth the authentication record
	 */
	public HttpRemoteEngineControl(@NonNull URL remote, @NonNull AdvanceHttpAuthentication auth) {
		init(remote, auth);
	}
	/**
	 * Convenience method to initialize the engine control with the given remote address
	 * and BASIC authentication mode.
	 * @param remote the remote address
	 * @param username the username
	 * @param password the password
	 */
	public HttpRemoteEngineControl(@NonNull URL remote, @NonNull String username, @NonNull char[] password) {
		AdvanceHttpAuthentication auth = new AdvanceHttpAuthentication();
		auth.loginType = AdvanceWebLoginType.BASIC;
		auth.name = username;
		auth.password = password;
		
		init(remote, auth);
	}
	/**
	 * Initialize the internal communicator with the given address and authentication.
	 * @param remote the remote address
	 * @param auth the authentication record
	 */
	private void init(@NonNull URL remote, @NonNull AdvanceHttpAuthentication auth) {
		comm = new HttpCommunicator();
		comm.url = remote;
		comm.authentication = auth;
	}

	@Override
	public AdvanceControlToken login(URI target, String userName,
			char[] password) throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdvanceControlToken login(URI target, KeyStore keyStore,
			String keyAlias, char[] keyPassword) throws IOException,
			AdvanceControlException, KeyStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AdvanceBlockRegistryEntry> queryBlocks(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AdvanceSchemaRegistryEntry> querySchemas(
			AdvanceControlToken token) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdvanceEngineVersion queryVersion(AdvanceControlToken token)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateSchema(AdvanceControlToken token, String name,
			XElement schema) throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteKeyEntry(AdvanceControlToken token, String keyStore,
			String keyAlias) throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void generateKey(AdvanceControlToken token, AdvanceGenerateKey key)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public String exportCertificate(AdvanceControlToken token,
			AdvanceKeyStoreExport request) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String exportPrivateKey(AdvanceControlToken token,
			AdvanceKeyStoreExport request) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void importCertificate(AdvanceControlToken token,
			AdvanceKeyStoreExport request, String data) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void importPrivateKey(AdvanceControlToken token,
			AdvanceKeyStoreExport request, String keyData, String certData)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public String exportSigningRequest(AdvanceControlToken token,
			AdvanceKeyStoreExport request) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void importSigningResponse(AdvanceControlToken token,
			AdvanceKeyStoreExport request, String data) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void testJDBCDataSource(AdvanceControlToken token, int dataSourceId)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void testJMSEndpoint(AdvanceControlToken token, int jmsId)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void testFTPDataSource(AdvanceControlToken token, int ftpId)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<AdvanceKeyEntry> queryKeys(AdvanceControlToken token,
			String keyStore) throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdvanceDataStore datastore() {
		// TODO Auto-generated method stub
		return null;
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
	public AdvanceCompositeBlock queryFlow(AdvanceControlToken token,
			String realm) throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateFlow(AdvanceControlToken token, String realm,
			AdvanceCompositeBlock flow) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<AdvanceCompilationError> verifyFlow(AdvanceControlToken token,
			AdvanceCompositeBlock flow) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observable<AdvanceBlockDiagnostic> debugBlock(
			AdvanceControlToken token, String realm, String blockId)
			throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Observable<AdvanceParameterDiagnostic> debugParameter(
			AdvanceControlToken token, String realm, String blockId,
			String port, boolean isImput) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void injectValue(AdvanceControlToken token, String realm,
			String blockId, String port, XElement value) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub

	}

}
