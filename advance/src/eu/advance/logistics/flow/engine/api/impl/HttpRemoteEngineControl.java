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
import hu.akarnokd.reactive4java.base.Observer;
import hu.akarnokd.reactive4java.base.Pair;
import hu.akarnokd.reactive4java.scheduler.NewThreadScheduler;
import hu.akarnokd.utils.xml.XNElement;
import hu.akarnokd.utils.xml.XNSerializables;

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
import eu.advance.logistics.flow.engine.api.ds.AdvancePortSpecification;
import eu.advance.logistics.flow.engine.api.ds.AdvanceSchemaRegistryEntry;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUser;
import eu.advance.logistics.flow.engine.compiler.AdvanceCompilationResult;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.runtime.BlockDiagnostic;
import eu.advance.logistics.flow.engine.runtime.BlockRegistryEntry;
import eu.advance.logistics.flow.engine.runtime.PortDiagnostic;

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
		XNElement response = comm.query(XNSerializables.createRequest("get-user"));
		return XNSerializables.parseItem(response, AdvanceUser.CREATOR);
	}
	@Override
	public List<BlockRegistryEntry> queryBlocks() throws IOException,
			AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-blocks"));
		return XNSerializables.parseList(response, "block", BlockRegistryEntry.CREATOR);
	}
	@Override
	public List<AdvanceSchemaRegistryEntry> querySchemas() throws IOException,
			AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-schemas"));
		return XNSerializables.parseList(response, "schema", AdvanceSchemaRegistryEntry.CREATOR);
	}
	@Override
	public AdvanceEngineVersion queryVersion() throws IOException,
			AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-version"));
		return XNSerializables.parseItem(response, AdvanceEngineVersion.CREATOR);
	}
	@Override
	public void updateSchema(String name, XNElement schema)
			throws IOException, AdvanceControlException {
		XNElement query = XNSerializables.createRequest("update-schema", "name", name);
		query.add(schema.copy());
		comm.send(query);
	}
	@Override
	public AdvanceSchemaRegistryEntry querySchema(String name)
			throws IOException, AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-schema", "name", name));
		return XNSerializables.parseItem(response, AdvanceSchemaRegistryEntry.CREATOR);
	}
	@Override
	public void deleteKeyEntry(String keyStore, String keyAlias)
			throws IOException, AdvanceControlException {
		comm.send(XNSerializables.createRequest("delete-key-entry", "keystore", keyStore, "keyalias", keyAlias));
	}
	@Override
	public void generateKey(AdvanceGenerateKey key) throws IOException,
			AdvanceControlException {
		comm.send(XNSerializables.createUpdate("generate-key", key));
	}
	@Override
	public String exportCertificate(AdvanceKeyStoreExport request)
			throws IOException, AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createUpdate("export-certificate", request));
		return response.content;
	}
	@Override
	public String exportPrivateKey(AdvanceKeyStoreExport request)
			throws IOException, AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createUpdate("export-private-key", request));
		return response.content;
	}
	@Override
	public void importCertificate(AdvanceKeyStoreExport request, String data)
			throws IOException, AdvanceControlException {
		XNElement xrequest = new XNElement("import-certificate");
		xrequest.content = data;
		comm.send(xrequest);
	}
	@Override
	public void importPrivateKey(AdvanceKeyStoreExport request, String keyData,
			String certData) throws IOException, AdvanceControlException {
		XNElement xrequest = new XNElement("import-private-key");
		xrequest.add("private-key").content = keyData;
		xrequest.add("certificate").content = certData;
		comm.send(xrequest);
	}
	@Override
	public String exportSigningRequest(AdvanceKeyStoreExport request)
			throws IOException, AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createUpdate("export-signing-request", request));
		return response.content;
	}
	@Override
	public void importSigningResponse(AdvanceKeyStoreExport request, String data)
			throws IOException, AdvanceControlException {
		XNElement xrequest = new XNElement("import-signing-response");
		request.save(xrequest);
		xrequest.content = data;
		comm.send(xrequest);
	}
	@Override
	public String testJDBCDataSource(String dataSourceName) throws IOException,
			AdvanceControlException {
		XNElement result = comm.query(XNSerializables.createRequest("test-jdbc-data-source", "data-source-source", dataSourceName));
		return result.content;
	}
	@Override
	public String testJMSEndpoint(String jmsName) throws IOException,
			AdvanceControlException {
		XNElement result = comm.query(XNSerializables.createRequest("test-jms-endpoint", "jms-name", jmsName));
		return result.content;
	}
	@Override
	public String testFTPDataSource(String ftpName) throws IOException,
			AdvanceControlException {
		XNElement result = comm.query(XNSerializables.createRequest("test-ftp-endpoint", "ftp-name", ftpName));
		return result.content;
	}
	@Override
	public List<AdvanceKeyEntry> queryKeys(String keyStore) throws IOException,
			AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-keys", "keystore", keyStore));
		return XNSerializables.parseList(response, "keyentry", AdvanceKeyEntry.CREATOR);
	}
	@Override
	public AdvanceDataStore datastore() {
		return datastore;
	}
	@Override
	public void stopRealm(String name, String byUser) throws IOException,
			AdvanceControlException {
		comm.send(XNSerializables.createRequest("stop-realm", "name", name, "by-user", byUser));
	}
	@Override
	public void startRealm(String name, String byUser) throws IOException,
			AdvanceControlException {
		comm.send(XNSerializables.createRequest("start-realm", "name", name, "by-user", byUser));
	}
	@Override
	public AdvanceCompositeBlock queryFlow(String realm) throws IOException,
			AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-flow", "realm", realm));
		return AdvanceCompositeBlock.parseFlow(response);
	}
	@Override
	public void updateFlow(String realm, AdvanceCompositeBlock flow, String byUser)
			throws IOException, AdvanceControlException {
		XNElement request = XNSerializables.createRequest("update-flow", "realm", realm, "by-user", byUser);
		request.add(flow.serializeFlow());
		comm.send(request);
	}
	@Override
	public AdvanceCompilationResult verifyFlow(AdvanceCompositeBlock flow)
			throws IOException, AdvanceControlException {
		XNElement request = XNSerializables.createRequest("verify-flow");
		request.add(flow.serializeFlow());
		return XNSerializables.parseItem(comm.query(request), AdvanceCompilationResult.CREATOR);
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
				
				return comm.receive(XNSerializables.createRequest(
						"debug-block", "realm", realm, "block-id", blockId), new NewThreadScheduler())
					.register(
						new Observer<XNElement>() {
					@Override
					public void error(Throwable ex) {
						observer.error(ex);
					}
					@Override
					public void finish() {
						observer.finish();
					}
					@Override
					public void next(XNElement value) {
						observer.next(XNSerializables.parseItem(value, BlockDiagnostic.CREATOR));
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
				return comm.receive(XNSerializables.createRequest(
						"debug-parameter", "realm", realm, "block-id", blockId, "port", port), new NewThreadScheduler())
				.register(new Observer<XNElement>() {
					@Override
					public void error(Throwable ex) {
						observer.error(ex);
					}
					@Override
					public void finish() {
						observer.finish();
					}
					@Override
					public void next(XNElement value) {
						observer.next(XNSerializables.parseItem(value, PortDiagnostic.CREATOR));
					};
				});
			}
		};
	}
	@Override
	public void injectValue(String realm, String blockId, String port,
			XNElement value) throws IOException, AdvanceControlException {
		XNElement request = XNSerializables.createRequest("inject-value", "realm", realm, "block-id", blockId, "port", port);
		request.add(value.copy());
		comm.send(request);
	}
	@Override
	public void shutdown() throws IOException, AdvanceControlException {
		comm.send(XNSerializables.createRequest("shutdown"));
	}
	@Override
	public void deleteSchema(String name) throws IOException,
			AdvanceControlException {
		comm.send(XNSerializables.createRequest("delete-schema", "name", name));
	}
	@Override
	public AdvanceCompilationResult queryCompilationResult(String realm)
			throws IOException, AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-compilation-result", "realm", realm));
		return XNSerializables.parseItem(response, AdvanceCompilationResult.CREATOR);
	}
	
	@Override
	public List<AdvancePortSpecification> queryPorts(String realm)
			throws IOException, AdvanceControlException {
		XNElement response = comm.query(XNSerializables.createRequest("query-ports", "realm", realm));
		return XNSerializables.parseList(response, "ports", AdvancePortSpecification.CREATOR);
	}
	@Override
	public Observable<XNElement> receivePort(final String realm, final String portId)
			throws IOException, AdvanceControlException {
		return new Observable<XNElement>() {
			@Override
			public Closeable register(final
					Observer<? super XNElement> observer) {
				return comm.receive(XNSerializables.createRequest(
						"receive-port", "realm", realm, "port", portId), new NewThreadScheduler())
				.register(observer);
			}
		};
	}
	@Override
	public void sendPort(String realm,
			Iterable<Pair<String, XNElement>> portValues) throws IOException,
			AdvanceControlException {
		XNElement request = XNSerializables.createRequest("send-port", "realm", realm);
		for (Pair<String, XNElement> pv : portValues) {
			XNElement entry = request.add("entry");
			entry.set("port", pv.first);
			entry.add(pv.second.copy());
		}
		comm.send(request);
	}
}
