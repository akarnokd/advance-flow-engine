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

import hu.akarnokd.reactive4java.reactive.Observable;
import hu.akarnokd.reactive4java.reactive.Observer;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceEngineVersion;
import eu.advance.logistics.flow.engine.api.AdvanceHttpAuthentication;
import eu.advance.logistics.flow.engine.api.AdvanceXMLCommunicator;
import eu.advance.logistics.flow.engine.api.core.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceGenerateKey;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyEntry;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStoreExport;
import eu.advance.logistics.flow.engine.api.ds.AdvanceLoginType;
import eu.advance.logistics.flow.engine.api.ds.AdvanceSchemaRegistryEntry;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUser;
import eu.advance.logistics.flow.engine.compiler.AdvanceCompilationResult;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.runtime.BlockDiagnostic;
import eu.advance.logistics.flow.engine.runtime.BlockRegistryEntry;
import eu.advance.logistics.flow.engine.runtime.PortDiagnostic;
import eu.advance.logistics.flow.engine.util.NewThreadScheduler;
import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.flow.engine.xml.XSerializables;

/**
 * An {@code AdvanceEngineControl} implementation which translates the API calls into XML message exchanges through
 * a HTTP(s) channel.
 * @author akarnokd, 2011.09.29.
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
	public HttpRemoteEngineControl(@NonNull URL remote, 
			@NonNull AdvanceHttpAuthentication auth) {
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
	public HttpRemoteEngineControl(@NonNull URL remote, 
			@NonNull String username, @NonNull char[] password) {
		AdvanceHttpAuthentication auth = new AdvanceHttpAuthentication();
		auth.loginType = AdvanceLoginType.BASIC;
		auth.name = username;
		auth.password(password);
		
		init(remote, auth);
		datastore = new HttpRemoteDataStore(comm);
	}
	/**
	 * Convenience method to initialize the engine control with the given remote address
	 * and BASIC authentication mode.
	 * @param remote the remote address
	 * @param username the username
	 * @param password the password
	 * @param trustedCertStore the certificate store to verify the server's public key.
	 */
	public HttpRemoteEngineControl(@NonNull URL remote, 
			@NonNull String username, 
			@NonNull char[] password,
			@NonNull KeyStore trustedCertStore) {
		if (!remote.getProtocol().equals("https") && trustedCertStore != null) {
			throw new IllegalArgumentException("Use HTTPS remote address!");
		}
		AdvanceHttpAuthentication auth = new AdvanceHttpAuthentication();
		auth.loginType = AdvanceLoginType.BASIC;
		auth.name = username;
		auth.password(password);
		auth.certStore = trustedCertStore;
		
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
		XElement response = comm.query(XSerializables.createRequest("get-user"));
		return XSerializables.parseItem(response, AdvanceUser.CREATOR);
	}
	@Override
	public List<BlockRegistryEntry> queryBlocks() throws IOException,
			AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-blocks"));
		return XSerializables.parseList(response, "block", BlockRegistryEntry.CREATOR);
	}
	@Override
	public List<AdvanceSchemaRegistryEntry> querySchemas() throws IOException,
			AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-schemas"));
		return XSerializables.parseList(response, "schema", AdvanceSchemaRegistryEntry.CREATOR);
	}
	@Override
	public AdvanceEngineVersion queryVersion() throws IOException,
			AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-version"));
		return XSerializables.parseItem(response, AdvanceEngineVersion.CREATOR);
	}
	@Override
	public void updateSchema(String name, XElement schema)
			throws IOException, AdvanceControlException {
		XElement query = XSerializables.createRequest("update-schema", "name", name);
		query.add(schema.copy());
		comm.send(query);
	}
	@Override
	public AdvanceSchemaRegistryEntry querySchema(String name)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-schema", "name", name));
		return XSerializables.parseItem(response, AdvanceSchemaRegistryEntry.CREATOR);
	}
	@Override
	public void deleteKeyEntry(String keyStore, String keyAlias)
			throws IOException, AdvanceControlException {
		comm.send(XSerializables.createRequest("delete-key-entry", "keystore", keyStore, "keyalias", keyAlias));
	}
	@Override
	public void generateKey(AdvanceGenerateKey key) throws IOException,
			AdvanceControlException {
		comm.send(XSerializables.createUpdate("generate-key", key));
	}
	@Override
	public String exportCertificate(AdvanceKeyStoreExport request)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(XSerializables.createUpdate("export-certificate", request));
		return response.content;
	}
	@Override
	public String exportPrivateKey(AdvanceKeyStoreExport request)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(XSerializables.createUpdate("export-private-key", request));
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
		XElement response = comm.query(XSerializables.createUpdate("export-signing-request", request));
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
	public String testJDBCDataSource(String dataSourceName) throws IOException,
			AdvanceControlException {
		XElement result = comm.query(XSerializables.createRequest("test-jdbc-data-source", "data-source-source", dataSourceName));
		return result.content;
	}
	@Override
	public String testJMSEndpoint(String jmsName) throws IOException,
			AdvanceControlException {
		XElement result = comm.query(XSerializables.createRequest("test-jms-endpoint", "jms-name", jmsName));
		return result.content;
	}
	@Override
	public String testFTPDataSource(String ftpName) throws IOException,
			AdvanceControlException {
		XElement result = comm.query(XSerializables.createRequest("test-ftp-endpoint", "ftp-name", ftpName));
		return result.content;
	}
	@Override
	public List<AdvanceKeyEntry> queryKeys(String keyStore) throws IOException,
			AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-keys", "keystore", keyStore));
		return XSerializables.parseList(response, "keyentry", AdvanceKeyEntry.CREATOR);
	}
	@Override
	public AdvanceDataStore datastore() {
		return datastore;
	}
	@Override
	public void stopRealm(String name, String byUser) throws IOException,
			AdvanceControlException {
		comm.send(XSerializables.createRequest("stop-realm", "name", name, "by-user", byUser));
	}
	@Override
	public void startRealm(String name, String byUser) throws IOException,
			AdvanceControlException {
		comm.send(XSerializables.createRequest("start-realm", "name", name, "by-user", byUser));
	}
	@Override
	public AdvanceCompositeBlock queryFlow(String realm) throws IOException,
			AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-flow", "realm", realm));
		return AdvanceCompositeBlock.parseFlow(response);
	}
	@Override
	public void updateFlow(String realm, AdvanceCompositeBlock flow, String byUser)
			throws IOException, AdvanceControlException {
		XElement request = XSerializables.createRequest("update-flow", "realm", realm, "by-user", byUser);
		request.add(flow.serializeFlow());
		comm.send(request);
	}
	@Override
	public AdvanceCompilationResult verifyFlow(AdvanceCompositeBlock flow)
			throws IOException, AdvanceControlException {
		XElement request = XSerializables.createRequest("verify-flow");
		request.add(flow.serializeFlow());
		return XSerializables.parseItem(comm.query(request), AdvanceCompilationResult.CREATOR);
	}
	/**
	 * {@inheritDoc}
	 * <p>Remark: registering to this observable starts the HTTP message exchange and receives
	 * the records in the caller's thread. 
	 * Consider using the {@link hu.akarnokd.reactive4java.reactive.Reactive#registerOn(Observable, hu.akarnokd.reactive4java.base.Scheduler)}</p>
	 */
	@Override
	public Observable<BlockDiagnostic> debugBlock(final String realm,
			final String blockId) throws IOException, AdvanceControlException {
		return new Observable<BlockDiagnostic>() {
			@Override
			public Closeable register(final
					Observer<? super BlockDiagnostic> observer) {
				
				return comm.receive(XSerializables.createRequest(
						"debug-block", "realm", realm, "block-id", blockId), new NewThreadScheduler())
					.register(
						new Observer<XElement>() {
					@Override
					public void error(Throwable ex) {
						observer.error(ex);
					}
					@Override
					public void finish() {
						observer.finish();
					}
					@Override
					public void next(XElement value) {
						observer.next(XSerializables.parseItem(value, BlockDiagnostic.CREATOR));
					};
				});
			}
		};
	}
	/**
	 * {@inheritDoc}
	 * <p>Remark: registering to this observable starts the HTTP message exchange and receives
	 * the records in the caller's thread. 
	 * Consider using the {@link hu.akarnokd.reactive4java.reactive.Reactive#registerOn(Observable, hu.akarnokd.reactive4java.base.Scheduler)}</p>
	 */
	@Override
	public Observable<PortDiagnostic> debugParameter(final String realm,
			final String blockId, final String port) throws IOException,
			AdvanceControlException {
		return new Observable<PortDiagnostic>() {
			@Override
			public Closeable register(final
					Observer<? super PortDiagnostic> observer) {
				return comm.receive(XSerializables.createRequest(
						"debug-parameter", "realm", realm, "block-id", blockId, "port", port), new NewThreadScheduler())
				.register(new Observer<XElement>() {
					@Override
					public void error(Throwable ex) {
						observer.error(ex);
					}
					@Override
					public void finish() {
						observer.finish();
					}
					@Override
					public void next(XElement value) {
						observer.next(XSerializables.parseItem(value, PortDiagnostic.CREATOR));
					};
				});
			}
		};
	}
	@Override
	public void injectValue(String realm, String blockId, String port,
			XElement value) throws IOException, AdvanceControlException {
		XElement request = XSerializables.createRequest("inject-value", "realm", realm, "block-id", blockId, "port", port);
		request.add(value.copy());
		comm.send(request);
	}
	@Override
	public void shutdown() throws IOException, AdvanceControlException {
		comm.send(XSerializables.createRequest("shutdown"));
	}
	@Override
	public void deleteSchema(String name) throws IOException,
			AdvanceControlException {
		comm.send(XSerializables.createRequest("delete-schema", "name", name));
	}
	@Override
	public AdvanceCompilationResult queryCompilationResult(String realm)
			throws IOException, AdvanceControlException {
		XElement response = comm.query(XSerializables.createRequest("query-compilation-result", "realm", realm));
		return XSerializables.parseItem(response, AdvanceCompilationResult.CREATOR);
	}
}
