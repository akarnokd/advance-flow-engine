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

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.advance.logistics.flow.engine.AdvanceFlowEngine;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceEngineVersion;
import eu.advance.logistics.flow.engine.api.AdvanceFlowCompiler;
import eu.advance.logistics.flow.engine.api.AdvanceFlowExecutor;
import eu.advance.logistics.flow.engine.api.core.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceGenerateKey;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyEntry;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStoreExport;
import eu.advance.logistics.flow.engine.api.ds.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.ds.AdvanceRealmStatus;
import eu.advance.logistics.flow.engine.api.ds.AdvanceSchemaRegistryEntry;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUser;
import eu.advance.logistics.flow.engine.comm.FTPPoolManager;
import eu.advance.logistics.flow.engine.comm.JDBCPoolManager;
import eu.advance.logistics.flow.engine.comm.JMSPoolManager;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockDiagnostic;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockPort;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockRegistryEntry;
import eu.advance.logistics.flow.engine.model.rt.AdvanceCompilationResult;
import eu.advance.logistics.flow.engine.model.rt.AdvanceParameterDiagnostic;
import eu.advance.logistics.flow.engine.model.rt.AdvancePort;
import eu.advance.logistics.flow.engine.util.KeystoreFault;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * A synchronized local flow engine control object storing data in local XML file.
 * <p>May be used to test GUI without the need to connect to real remote data source.
 * @author akarnokd, 2011.09.20.
 */
public class LocalEngineControl implements AdvanceEngineControl {
	/** The logger object. */
	private static final Logger LOG = LoggerFactory.getLogger(LocalEngineControl.class);
	/** The local data store. */
	protected final AdvanceDataStore datastore;
	/** The set of schema locations. */
	protected final List<String> schemas;
	/** The flow compiler. */
	protected final AdvanceFlowCompiler compiler;
	/** The flow executor. */
	protected final AdvanceFlowExecutor executor;
	/**
	 * The realm runtimes.
	 */
	protected final Map<String, List<AdvanceBlock>> realmRuntime = Maps.newConcurrentMap();
	/**
	 * The output of the realm verification.
	 */
	protected final Map<String, AdvanceCompilationResult> realmVerifications = Maps.newConcurrentMap();
	/** The working directory. */
	protected final String workDir;
	/**
	 * Constructor initializing the configuration.
	 * @param datastore the backing datastore to use
	 * @param schemas the sequence of schemas
	 * @param compiler the compiler used to (re)compile a realm
	 * @param executor the flow executor
	 * @param workDir the working directory, mainly for the keystores
	 */
	public LocalEngineControl(
			AdvanceDataStore datastore, 
			Iterable<String> schemas, 
			AdvanceFlowCompiler compiler,
			AdvanceFlowExecutor executor,
			String workDir) {
		this.datastore = datastore;
		this.workDir = workDir;
		this.schemas = Lists.newArrayList(schemas);
		this.compiler = compiler;
		this.executor = executor;
	}
	@Override
	public AdvanceUser getUser() throws IOException, AdvanceControlException {
		throw new AdvanceControlException("The getUser() does not work at this level.");
	}
	@Override
	public List<AdvanceBlockRegistryEntry> queryBlocks()
			throws IOException, AdvanceControlException {
		return compiler.blocks();
	}
	/** @return the datastore instance */
	@Override
	public AdvanceDataStore datastore() {
		return datastore;
	}

	@Override
	public List<AdvanceKeyEntry> queryKeys(
			String keyStore) throws IOException,
			AdvanceControlException {
		AdvanceKeyStore e = datastore.queryKeyStore(keyStore);
		try {
			e.locationPrefix = workDir + "/";
			return e.queryKeys();
		} catch (KeystoreFault ex) {
			throw new AdvanceControlException(ex);
		} catch (KeyStoreException ex) {
			throw new AdvanceControlException(ex);
		}
	}

	@Override
	public void deleteKeyEntry(String keyStore,
			String keyAlias) throws IOException,
			AdvanceControlException {
		AdvanceKeyStore e = datastore.queryKeyStore(keyStore);
		if (e != null) {
			try {
				e.locationPrefix = workDir + "/";
				e.deleteKey(keyAlias);
			} catch (KeyStoreException ex) {
				throw new AdvanceControlException(ex);
			} catch (KeystoreFault ex) {
				throw new AdvanceControlException(ex);
			}
		} else {
			throw new AdvanceControlException("Keystore not found");
		}
	}

	@Override
	public void generateKey(AdvanceGenerateKey key)
			throws IOException, AdvanceControlException {
		AdvanceKeyStore e = datastore.queryKeyStore(key.keyStore);
		if (e != null) {
			try {
				e.locationPrefix = workDir + "/";
				e.generateKey(key);
			} catch (KeyStoreException ex) {
				LOG.error(ex.toString(), ex);
				throw new AdvanceControlException("Failed to generate key");
			}
			e.modifiedAt = new Date();
			e.modifiedBy = key.modifiedBy;
			datastore.updateKeyStore(e);
		} else {
			throw new AdvanceControlException("Keystore not found");
		}
	}

	@Override
	public String exportCertificate(
			AdvanceKeyStoreExport request) throws IOException,
			AdvanceControlException {
		AdvanceKeyStore e = datastore.queryKeyStore(request.keyStore);
		if (e != null) {
			try {
				e.locationPrefix = workDir + "/";
				return e.exportCertificate(request.keyAlias);
			} catch (KeystoreFault ex) {
				throw new AdvanceControlException(ex);
			}
		} else {
			throw new AdvanceControlException("Keystore not found");
		}
	}

	@Override
	public String exportPrivateKey(
			AdvanceKeyStoreExport request) throws IOException,
			AdvanceControlException {
		AdvanceKeyStore e = datastore.queryKeyStore(request.keyStore);
		if (e != null) {
			try {
				e.locationPrefix = workDir + "/";
				return e.exportPrivateKey(request.keyAlias, request.password());
			} catch (KeystoreFault ex) {
				throw new AdvanceControlException(ex);
			}
		} else {
			throw new AdvanceControlException("Keystore not found");
		}
	}

	@Override
	public void importCertificate(
			AdvanceKeyStoreExport request, String data) throws IOException,
			AdvanceControlException {
		AdvanceKeyStore e = datastore.queryKeyStore(request.keyStore);
		if (e != null) {
			try {
				e.locationPrefix = workDir + "/";
				e.importCertificate(request.keyAlias, data);
			} catch (KeyStoreException ex) {
				throw new AdvanceControlException(ex);
			} catch (KeystoreFault ex) {
				throw new AdvanceControlException(ex);
			}
		} else {
			throw new AdvanceControlException("Keystore not found");
		}
	}

	@Override
	public void importPrivateKey(
			AdvanceKeyStoreExport request, String keyData, String certData) throws IOException,
			AdvanceControlException {
		AdvanceKeyStore e = datastore.queryKeyStore(request.keyStore);
		if (e != null) {
			try {
				e.locationPrefix = workDir + "/";
				e.importPrivateKey(request.keyAlias, request.password(), keyData, certData);
			} catch (KeystoreFault ex) {
				throw new AdvanceControlException(ex);
			}
		} else {
			throw new AdvanceControlException("Keystore not found");
		}
	}

	@Override
	public String exportSigningRequest(
			AdvanceKeyStoreExport request) throws IOException,
			AdvanceControlException {
		AdvanceKeyStore e = datastore.queryKeyStore(request.keyStore);
		if (e != null) {
			try {
				e.locationPrefix = workDir + "/";
				return e.exportSigningRequest(request.keyAlias, request.password());
			} catch (KeystoreFault ex) {
				throw new AdvanceControlException(ex);
			}
		} else {
			throw new AdvanceControlException("Keystore not found");
		}
	}

	@Override
	public void importSigningResponse(
			AdvanceKeyStoreExport request, String data) throws IOException,
			AdvanceControlException {
		AdvanceKeyStore e = datastore.queryKeyStore(request.keyStore);
		if (e != null) {
			try {
				e.locationPrefix = workDir + "/";
				e.importSigningResponse(request.keyAlias, request.password(), data);
			} catch (KeystoreFault ex) {
				throw new AdvanceControlException(ex);
			}
		} else {
			throw new AdvanceControlException("Keystore not found");
		}
	}

	/**
	 * Backup the current data store.
	 * @param dsFile the datastore file name
	 */
	protected void backupDataStore(File dsFile) {
		File dsFileBackup1 = new File("datastore.xml.bak");
		File dsFileBackup2 = new File("datastore.xml.ba1");
		if (dsFileBackup2.exists()) {
			if (!dsFileBackup2.delete()) {
				LOG.warn("Could not delete file " + dsFileBackup2);
			}
		}
		if (dsFileBackup1.exists()) {
			if (!dsFileBackup1.renameTo(dsFileBackup2)) {
				LOG.warn("Could not rename file " + dsFileBackup1 + " into " + dsFileBackup2);
			}
			if (!dsFileBackup1.delete()) {
				LOG.warn("Could not delete file " + dsFileBackup1);
			}
		}
		if (dsFile.exists()) {
			if (!dsFile.renameTo(dsFileBackup1)) {
				LOG.warn("Could not rename file " + dsFile + " into " + dsFileBackup1);
			}
		}
	}
	@Override
	public String testJDBCDataSource(String dataSourceName)
			throws IOException, AdvanceControlException {
		return JDBCPoolManager.test(datastore.queryJDBCDataSource(dataSourceName));
	}
	@Override
	public String testJMSEndpoint(String jmsName)
			throws IOException, AdvanceControlException {
		return JMSPoolManager.test(datastore.queryJMSEndpoint(jmsName));
	}
	@Override
	public String testFTPDataSource(String ftpName)
			throws IOException, AdvanceControlException {
		return FTPPoolManager.test(datastore.queryFTPDataSource(ftpName), datastore);
	}
	@Override
	public void stopRealm(String name, String byUser)
			throws IOException, AdvanceControlException {
		AdvanceRealm r = datastore.queryRealm(name);
		if (r != null) {
			if (r.status == AdvanceRealmStatus.RUNNING) {
				r.modifiedAt = new Date();
				tryShutdownRealm(r, AdvanceRealmStatus.STOPPED);
			} else {
				throw new AdvanceControlException("Realm not running");
			}
		} else {
			throw new AdvanceControlException("Realm not found");
		}
	}

	@Override
	public void startRealm(String name, String byUser)
			throws IOException, AdvanceControlException {
		AdvanceRealm r = datastore.queryRealm(name);
		if (r != null) {
			if (r.status == AdvanceRealmStatus.STOPPED 
					|| r.status == AdvanceRealmStatus.ERROR
					|| r.status == AdvanceRealmStatus.RESUME) {
				r.modifiedBy = byUser;
				tryStartRealm(r);
			} else {
				throw new AdvanceControlException("Realm not stopped");
			}
		} else {
			throw new AdvanceControlException("Realm not found");
		}
	}
	@Override
	public Observable<AdvanceBlockDiagnostic> debugBlock(
			String realm, String blockId)
			throws IOException, AdvanceControlException {
		List<AdvanceBlock> blocks = realmRuntime.get(realm);
		if (blocks != null) {
			for (AdvanceBlock b : blocks) {
				if (b.id().equals(blockId)) {
					return b.getDiagnosticPort();
				}
			}
			throw new AdvanceControlException("Missing block " + blockId + " in realm " + realm);
		}
		throw new AdvanceControlException("Missing realm " + realm);
	}
	@Override
	public Observable<AdvanceParameterDiagnostic> debugParameter(
			String realm, String blockId,
			String port) throws IOException,
			AdvanceControlException {
		List<AdvanceBlock> blocks = realmRuntime.get(realm);
		if (blocks != null) {
			for (AdvanceBlock b : blocks) {
				if (b.id().equals(blockId)) {
					for (AdvancePort p : b.inputs()) {
						if (p instanceof AdvanceBlockPort && p.name().equals(port)) {
							return ((AdvanceBlockPort)p).getDiagnosticPort();
						}
					}
					for (AdvancePort p : b.outputs()) {
						if (p instanceof AdvanceBlockPort && p.name().equals(port)) {
							return ((AdvanceBlockPort)p).getDiagnosticPort();
						}
					}
					throw new AdvanceControlException("Missing port " + port + " on block " + blockId + " in realm " + realm);
				}
			}
			throw new AdvanceControlException("Missing block " + blockId + " in realm " + realm);
		}
		throw new AdvanceControlException("Realm does not have compiled and running blocks: " + realm);
	}
	@Override
	public AdvanceCompositeBlock queryFlow(
			String realm) throws IOException, AdvanceControlException {
		return AdvanceCompositeBlock.parseFlow(datastore.queryFlow(realm));
	}
	@Override
	public void updateFlow(String realm,
			AdvanceCompositeBlock flow, String byUser) throws IOException,
			AdvanceControlException {
		AdvanceRealm r = datastore.queryRealm(realm);
		AdvanceRealmStatus rs = r.status;
		// stop realm if running
		if (r.status == AdvanceRealmStatus.RUNNING) {
			stopRealm(realm, byUser);
		} else {
			r.modifiedBy = byUser;
			datastore.updateRealm(r);
		}
		// clear previous flow
		realmRuntime.remove(realm);
		realmVerifications.remove(realm);
		datastore.deleteBlockStates(realm);
	
		// set new flow
		datastore.updateFlow(realm, flow.serializeFlow());
		// resume if previously running
		if (rs == AdvanceRealmStatus.RUNNING) {
			startRealm(realm, byUser);
		}
	}
	@Override
	public AdvanceCompilationResult verifyFlow(
			AdvanceCompositeBlock flow) throws IOException,
			AdvanceControlException {
		return compiler.verify(flow);
	}
	@Override
	public void injectValue(String realm,
			String blockId, String port, XElement value) throws IOException,
			AdvanceControlException {
		AdvanceRealm r = datastore.queryRealm(realm);
		if (r == null) {
			throw new AdvanceControlException("Missing realm " + realm);
		}
		if (r.status != AdvanceRealmStatus.RUNNING) {
			throw new AdvanceControlException("Realm " + realm + " is not running");
		}
		List<AdvanceBlock> blocks = realmRuntime.get(realm);
		if (blocks == null) {
			throw new AdvanceControlException("Realm " + realm + " is not compiled");
		}
		for (AdvanceBlock b : blocks) {
			if (b.id().equals(blockId)) {
				for (AdvancePort p : b.inputs()) {
					if (p.name().equals(port)) {
						if (p instanceof AdvanceBlockPort) {
							AdvanceBlockPort bp = (AdvanceBlockPort) p;
							bp.next(value);
							return;
						} else {
							throw new AdvanceControlException("The port is constant bound.");
						}
					}
				}
				throw new AdvanceControlException("Port not found");
			}
		}
		throw new AdvanceControlException("Block not found");
	}
	@Override
	public List<AdvanceSchemaRegistryEntry> querySchemas(
			) throws IOException, AdvanceControlException {
		try {
			List<AdvanceSchemaRegistryEntry> result = Lists.newArrayList();
			for (String sd : schemas) {
				File[] files = new File(sd).listFiles();
				if (files != null) {
					for (File f : files) {
						if (f.getName().toLowerCase().endsWith(".xsd")) {
							AdvanceSchemaRegistryEntry e = new AdvanceSchemaRegistryEntry();
							e.name = f.getName();
							e.schema = XElement.parseXML(f);
							result.add(e);
						}
					}
				}
			}
			return result;
		} catch (XMLStreamException ex) {
			throw new IOException(ex);
		}
	}
	@Override
	public AdvanceSchemaRegistryEntry querySchema(String name)
			throws IOException, AdvanceControlException {
		try {
			for (String sd : schemas) {
				File[] files = new File(sd).listFiles();
				if (files != null) {
					for (File f : files) {
						if (f.getName().toLowerCase().endsWith(".xsd")) {
							AdvanceSchemaRegistryEntry e = new AdvanceSchemaRegistryEntry();
							e.name = f.getName();
							e.schema = XElement.parseXML(f);
							return e;
						}
					}
				}
			}
		} catch (XMLStreamException ex) {
			throw new IOException(ex);
		}
		return null;
	}
	@Override
	public void updateSchema(String name,
			XElement schema) throws IOException, AdvanceControlException {
		if (schemas.size() == 0) {
			throw new AdvanceControlException("No place for schemas");
		}
		String sd = schemas.get(0);
		File fname = new File(name);
		File f = new File(sd, fname.getName());
		schema.save(f);
	}
	@Override
	public void deleteSchema(String name) throws IOException, AdvanceControlException {
		if (schemas.size() == 0) {
			throw new AdvanceControlException("No place for schemas");
		}
		String sd = schemas.get(0);
		File fname = new File(name);
		File f = new File(sd, fname.getName());
		if (!f.delete()) {
			LOG.error("Could not delete schema " + f);
		}
	}
	@Override
	public AdvanceEngineVersion queryVersion()
			throws IOException, AdvanceControlException {
		AdvanceEngineVersion result = new AdvanceEngineVersion();
		result.parse(AdvanceFlowEngine.VERSION);
		return result;
	}
	/**
	 * Initialize and start the flows of each realm when the engine starts.
	 * @throws IOException not likely
	 * @throws AdvanceControlException not likely
	 */
	public void startup() throws IOException, AdvanceControlException {
		for (AdvanceRealm r : datastore.queryRealms()) {
			if (r.status == AdvanceRealmStatus.RESUME) {
				tryStartRealm(r);
			}
		}
	}
	/**
	 * Tries to start a realm.
	 * @param r the realm object
	 * @throws IOException on datastore error
	 * @throws AdvanceControlException on datastore error
	 */
	private void tryStartRealm(AdvanceRealm r) throws IOException,
			AdvanceControlException {
		r.status = AdvanceRealmStatus.STARTING;
		r.modifiedAt = new Date();
		datastore.updateRealm(r);
		
		try {
			XElement xflow = datastore.queryFlow(r.name);
			LOG.debug("Parsing flow");
			AdvanceCompositeBlock flow = AdvanceCompositeBlock.parseFlow(xflow);
			LOG.debug("Verifying flow");
			AdvanceCompilationResult verify = compiler.verify(flow);
			realmVerifications.put(r.name, verify);
			if (verify.success()) {
				LOG.debug("Compiling flow");
				List<AdvanceBlock> blocks = compiler.compile(flow);
				realmRuntime.put(r.name, blocks);
				LOG.debug("Restoring block state");
				for (AdvanceBlock b : blocks) {
					XElement state = datastore.queryBlockState(r.name, b.id());
					if (state != null) {
						b.restoreState(state);
					}
				}
				LOG.debug("Running blocks");
				executor.run(blocks);
				LOG.debug("Start success");
				r.status = AdvanceRealmStatus.RUNNING;
				r.modifiedAt = new Date();
				datastore.updateRealm(r);
			} else {
				LOG.debug("Start failed due compilation errors: " + verify.errors());
				r.status = AdvanceRealmStatus.ERROR;
				r.modifiedAt = new Date();
				datastore.updateRealm(r);
			}
		} catch (Throwable t) {
			LOG.error(t.toString(), t);
			r.status = AdvanceRealmStatus.ERROR;
			r.modifiedAt = new Date();
			datastore.updateRealm(r);
		}
	}
	@Override
	public void shutdown() throws IOException, AdvanceControlException {
		for (AdvanceRealm r : datastore.queryRealms()) {
			if (r.status == AdvanceRealmStatus.RUNNING) {
				tryShutdownRealm(r, AdvanceRealmStatus.RESUME);
			} else {
				// force stopped
				tryShutdownRealm(r, AdvanceRealmStatus.STOPPED);
			}
		}
	}
	/**
	 * Try to shut down a realm.
	 * @param r the realm object
	 * @param afterStatus what should be the after status of the realm?
	 * @throws IOException on datastore error
	 * @throws AdvanceControlException on datastore error
	 */
	private void tryShutdownRealm(AdvanceRealm r, AdvanceRealmStatus afterStatus) throws IOException,
			AdvanceControlException {
		r.status = AdvanceRealmStatus.STOPPING;
		r.modifiedAt = new Date();
		datastore.updateRealm(r);
		try {
			List<AdvanceBlock> blocks = realmRuntime.remove(r.name);
			if (blocks != null) {
				executor.done(blocks);
				for (AdvanceBlock b : blocks) {
					XElement state = b.saveState();
					datastore.updateBlockState(r.name, b.id(), state);
				}
			}
		} finally {
			r.status = afterStatus;
			r.modifiedAt = new Date();
			datastore.updateRealm(r);
		}
	}
	@Override
	public AdvanceCompilationResult queryCompilationResult(String realm)
			throws IOException, AdvanceControlException {
		return realmVerifications.get(realm);
	}
}
