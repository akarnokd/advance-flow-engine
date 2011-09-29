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
import java.net.URL;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.AdvanceBlockDiagnostic;
import eu.advance.logistics.flow.engine.AdvanceParameterDiagnostic;
import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceEngineVersion;
import eu.advance.logistics.flow.engine.api.AdvanceGenerateKey;
import eu.advance.logistics.flow.engine.api.AdvanceHttpAuthentication;
import eu.advance.logistics.flow.engine.api.AdvanceKeyEntry;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStoreExport;
import eu.advance.logistics.flow.engine.api.AdvanceSchemaRegistryEntry;
import eu.advance.logistics.flow.engine.api.AdvanceUser;
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
	public AdvanceUser getUser() throws IOException, AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("get-user"));
		return HttpRemoteUtils.parseItem(response, AdvanceUser.CREATOR);
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
