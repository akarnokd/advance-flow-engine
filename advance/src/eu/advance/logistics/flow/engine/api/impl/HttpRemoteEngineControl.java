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
import eu.advance.logistics.flow.engine.AdvanceCompilationResult;
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
import eu.advance.logistics.flow.engine.api.AdvanceXMLCommunicator;
import eu.advance.logistics.flow.engine.api.DataStoreTestResult;
import eu.advance.logistics.flow.engine.model.AdvanceBlockRegistryEntry;
import eu.advance.logistics.flow.engine.model.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * @author karnokd, 2011.09.29.
 */
public class HttpRemoteEngineControl implements AdvanceEngineControl {
	/** The communicator used to send and receive requests. */
	protected AdvanceXMLCommunicator comm;
	/** The datastore interface. */
	protected AdvanceDataStore datastore;
	/**
	 * Initialize the engine control with the given remote address and authentication
	 * record.
	 * @param remote the remote datastore URL
	 * @param auth the authentication record
	 */
	public HttpRemoteEngineControl(@NonNull URL remote, @NonNull AdvanceHttpAuthentication auth) {
		init(remote, auth);
		datastore = new HttpRemoteDataStore(comm);
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
		datastore = new HttpRemoteDataStore(comm);
	}
	/**
	 * Initialize the control with the supplied communicator.
	 * @param comm the communicator to use
	 */
	public HttpRemoteEngineControl(@NonNull AdvanceXMLCommunicator comm) {
		this.comm = comm;
		datastore = new HttpRemoteDataStore(comm);
	}
	/**
	 * Initialize the internal communicator with the given address and authentication.
	 * @param remote the remote address
	 * @param auth the authentication record
	 */
	private void init(@NonNull URL remote, @NonNull AdvanceHttpAuthentication auth) {
		HttpCommunicator comm = new HttpCommunicator();
		comm.url = remote;
		comm.authentication = auth;
		this.comm = comm;
	}

	@Override
	public AdvanceUser getUser() throws IOException, AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("get-user"));
		return HttpRemoteUtils.parseItem(response, AdvanceUser.CREATOR);
	}
	@Override
	public List<AdvanceBlockRegistryEntry> queryBlocks() throws IOException,
			AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-blocks"));
		return HttpRemoteUtils.parseList(response, "block", AdvanceBlockRegistryEntry.CREATOR);
	}
	@Override
	public List<AdvanceSchemaRegistryEntry> querySchemas() throws IOException,
			AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-schemas"));
		return HttpRemoteUtils.parseList(response, "schema", AdvanceSchemaRegistryEntry.CREATOR);
	}
	@Override
	public AdvanceEngineVersion queryVersion() throws IOException,
			AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-version"));
		return HttpRemoteUtils.parseItem(response, AdvanceEngineVersion.CREATOR);
	}
	@Override
	public void updateSchema(String name, XElement schema)
			throws IOException, AdvanceControlException {
		XElement query = HttpRemoteUtils.createRequest("update-schema", "name", name);
		query.add(schema.copy());
		comm.send(query);
	}
	@Override
	public AdvanceSchemaRegistryEntry querySchema(String name)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-schema", "name", name));
		return HttpRemoteUtils.parseItem(response, AdvanceSchemaRegistryEntry.CREATOR);
	}
	@Override
	public void deleteKeyEntry(String keyStore, String keyAlias)
			throws IOException, AdvanceControlException {
		comm.send(HttpRemoteUtils.createRequest("delete-key-entry", "keystore", keyStore, "keyalias", keyAlias));
	}
	@Override
	public void generateKey(AdvanceGenerateKey key) throws IOException,
			AdvanceControlException {
		comm.send(HttpRemoteUtils.createUpdate("generate-key", key));
	}
	@Override
	public String exportCertificate(AdvanceKeyStoreExport request)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createUpdate("export-certificate", request));
		return response.content;
	}
	@Override
	public String exportPrivateKey(AdvanceKeyStoreExport request)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createUpdate("export-private-key", request));
		return response.content;
	}
	@Override
	public void importCertificate(AdvanceKeyStoreExport request, String data)
			throws IOException, AdvanceControlException {
		XElement xrequest = new XElement("import-certificate");
		xrequest.content = data;
		comm.send(xrequest);
	}
	@Override
	public void importPrivateKey(AdvanceKeyStoreExport request, String keyData,
			String certData) throws IOException, AdvanceControlException {
		XElement xrequest = new XElement("import-private-key");
		xrequest.add("private-key").content = keyData;
		xrequest.add("certificate").content = certData;
		comm.send(xrequest);
	}
	@Override
	public String exportSigningRequest(AdvanceKeyStoreExport request)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createUpdate("export-signing-request", request));
		return response.content;
	}
	@Override
	public void importSigningResponse(AdvanceKeyStoreExport request, String data)
			throws IOException, AdvanceControlException {
		XElement xrequest = new XElement("import-signing-response");
		request.save(xrequest);
		xrequest.content = data;
		comm.send(xrequest);
	}
	@Override
	public DataStoreTestResult testJDBCDataSource(String dataSourceName) throws IOException,
			AdvanceControlException {
		XElement result = comm.query(HttpRemoteUtils.createRequest("test-jdbc-data-source", "data-source-source", dataSourceName));
		return DataStoreTestResult.valueOf(result.content);
	}
	@Override
	public DataStoreTestResult testJMSEndpoint(String jmsName) throws IOException,
			AdvanceControlException {
		XElement result = comm.query(HttpRemoteUtils.createRequest("test-jms-endpoint", "jms-name", jmsName));
		return DataStoreTestResult.valueOf(result.content);
	}
	@Override
	public DataStoreTestResult testFTPDataSource(String ftpName) throws IOException,
			AdvanceControlException {
		XElement result = comm.query(HttpRemoteUtils.createRequest("test-ftp-endpoint", "ftp-name", ftpName));
		return DataStoreTestResult.valueOf(result.content);
	}
	@Override
	public List<AdvanceKeyEntry> queryKeys(String keyStore) throws IOException,
			AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-keys", "keystore", keyStore));
		return HttpRemoteUtils.parseList(response, "keyentry", AdvanceKeyEntry.CREATOR);
	}
	@Override
	public AdvanceDataStore datastore() {
		return datastore;
	}
	@Override
	public void stopRealm(String name, String byUser) throws IOException,
			AdvanceControlException {
		comm.send(HttpRemoteUtils.createRequest("stop-realm", "name", name, "by-user", byUser));
	}
	@Override
	public void startRealm(String name, String byUser) throws IOException,
			AdvanceControlException {
		comm.send(HttpRemoteUtils.createRequest("start-realm", "name", name, "by-user", byUser));
	}
	@Override
	public AdvanceCompositeBlock queryFlow(String realm) throws IOException,
			AdvanceControlException {
		XElement response = comm.query(HttpRemoteUtils.createRequest("query-flow", "realm", realm));
		return AdvanceCompositeBlock.parseFlow(response);
	}
	@Override
	public void updateFlow(String realm, AdvanceCompositeBlock flow)
			throws IOException, AdvanceControlException {
		XElement request = HttpRemoteUtils.createRequest("update-flow", "realm", realm);
		request.add(flow.serializeFlow());
		comm.send(request);
	}
	@Override
	public AdvanceCompilationResult verifyFlow(AdvanceCompositeBlock flow)
			throws IOException, AdvanceControlException {
		XElement request = HttpRemoteUtils.createRequest("verify-flow");
		request.add(flow.serializeFlow());
		return HttpRemoteUtils.parseItem(comm.query(request), AdvanceCompilationResult.CREATOR);
	}
	@Override
	public Observable<AdvanceBlockDiagnostic> debugBlock(String realm,
			String blockId) throws IOException, AdvanceControlException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
	@Override
	public Observable<AdvanceParameterDiagnostic> debugParameter(String realm,
			String blockId, String port) throws IOException,
			AdvanceControlException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
	@Override
	public void injectValue(String realm, String blockId, String port,
			XElement value) throws IOException, AdvanceControlException {
		XElement request = HttpRemoteUtils.createRequest("inject-value", "realm", realm, "block-id", blockId, "port", port);
		request.add(value.copy());
		comm.send(request);
	}
	@Override
	public void shutdown() throws IOException, AdvanceControlException {
		comm.send(HttpRemoteUtils.createRequest("shutdown"));
	}
	@Override
	public void deleteSchema(String name) throws IOException,
			AdvanceControlException {
		comm.send(HttpRemoteUtils.createRequest("delete-schema", "name", name));
	}
}
