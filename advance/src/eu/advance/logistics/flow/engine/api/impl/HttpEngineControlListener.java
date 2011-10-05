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

import hu.akarnokd.reactive4java.reactive.Observer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceGenerateKey;
import eu.advance.logistics.flow.engine.api.AdvanceXMLExchange;
import eu.advance.logistics.flow.engine.api.AdvanceHttpListener;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStoreExport;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockDiagnostic;
import eu.advance.logistics.flow.engine.model.rt.AdvanceParameterDiagnostic;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * The listener for Enginge control messages coming through the HTTP XML interface.
 * @author karnokd, 2011.09.29.
 */
public class HttpEngineControlListener implements AdvanceHttpListener {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(HttpEngineControlListener.class);
	/** The wrapped engine control. */
	protected final AdvanceEngineControl control;
	/** The wrapped datastore. */
	protected final HttpDataStoreListener datastoreListener;
	/** The datastore of the control. */
	private final AdvanceDataStore datastore;
	/**
	 * Constructor. Wraps the given control.
	 * @param control the control to wrap
	 */
	public HttpEngineControlListener(@NonNull AdvanceEngineControl control) {
		this.control = control;
		datastore = control.datastore();
		datastoreListener = new HttpDataStoreListener(datastore);
	}
	@Nullable
	@Override
	public void dispatch(@NonNull final AdvanceXMLExchange exch) throws IOException, AdvanceControlException {
		XElement request = exch.request();
		String userName = exch.userName();
		AdvanceEngineControl ctrl = new CheckedEngineControl(control, userName);
		String function = request.name;
		LOG.debug(function);
		if ("get-user".equals(function)) {
			exch.next(HttpRemoteUtils.storeItem("user", ctrl.getUser()));
		} else
		if ("query-blocks".equals(function)) {
			exch.next(HttpRemoteUtils.storeList("blocks", "block", ctrl.queryBlocks()));
		} else
		if ("query-schemas".equals(function)) {
			exch.next(HttpRemoteUtils.storeList("schemas", "schema", ctrl.querySchemas()));
		} else
		if ("query-version".equals(function)) {
			exch.next(HttpRemoteUtils.storeItem("version", ctrl.queryVersion()));
		} else
		if ("update-schema".equals(function)) {
			ctrl.updateSchema(request.get("name"), request.children().get(0).copy());
		} else
		if ("query-schema".equals(function)) {
			exch.next(HttpRemoteUtils.storeItem("schema", ctrl.querySchema(request.get("name"))));
		} else
		if ("delete-key-entry".equals(function)) {
			ctrl.deleteKeyEntry(request.get("keystore"), request.get("keyalias"));
			
		} else
		if ("generate-key".equals(function)) {
			ctrl.generateKey(HttpRemoteUtils.parseItem(request, AdvanceGenerateKey.CREATOR));
			
		} else
		if ("export-certificate".equals(function)) {
			XElement response = new XElement("certificate");
			response.content = ctrl.exportCertificate(HttpRemoteUtils.parseItem(request, AdvanceKeyStoreExport.CREATOR));
			exch.next(response);
		} else
		if ("export-private-key".equals(function)) {
			XElement response = new XElement("private-key");
			response.content = ctrl.exportPrivateKey(HttpRemoteUtils.parseItem(request, AdvanceKeyStoreExport.CREATOR));
			exch.next(response);
		} else
		if ("import-certificate".equals(function)) {
			ctrl.importCertificate(HttpRemoteUtils.parseItem(request, AdvanceKeyStoreExport.CREATOR), request.content);
			
		} else
		if ("import-private-key".equals(function)) {
			ctrl.importPrivateKey(HttpRemoteUtils.parseItem(request, AdvanceKeyStoreExport.CREATOR),
					request.childValue("private-key"), request.childValue("certificate"));
			
		} else
		if ("export-signing-request".equals(function)) {
			XElement response = new XElement("signing-request");
			response.content = ctrl.exportSigningRequest(HttpRemoteUtils.parseItem(request, AdvanceKeyStoreExport.CREATOR));
			exch.next(response);
		} else
		if ("import-signing-response".equals(function)) {
			ctrl.importSigningResponse(HttpRemoteUtils.parseItem(request, AdvanceKeyStoreExport.CREATOR), request.content);
			
		} else
		if ("test-jdbc-data-source".equals(function)) {
			XElement result = new XElement("datastore-test-result");
			result.content = ctrl.testJDBCDataSource(request.get("dataSourceName")).toString();
			exch.next(result);
		} else
		if ("test-jms-endpoint".equals(function)) {
			XElement result = new XElement("datastore-test-result");
			result.content = ctrl.testJMSEndpoint(request.get("jms-name")).toString();
			exch.next(result);
		} else
		if ("test-ftp-data-source".equals(function)) {
			XElement result = new XElement("datastore-test-result");
			result.content =  ctrl.testFTPDataSource(request.get("jms-name")).toString();
			exch.next(result);
		} else
		if ("query-keys".equals(function)) {
			exch.next(HttpRemoteUtils.storeList("keys", "keyentry", ctrl.queryKeys(request.get("keystore"))));
		} else
		if ("stop-realm".equals(function)) {
			ctrl.stopRealm(request.get("name"), request.get("by-user"));
			
		} else
		if ("start-realm".equals(function)) {
			ctrl.startRealm(request.get("name"), request.get("by-user"));
			
		} else
		if ("query-flow".equals(function)) {
			exch.next(AdvanceCompositeBlock.serializeFlow(ctrl.queryFlow(request.get("realm"))));
		} else
		if ("update-flow".equals(function)) {
			AdvanceCompositeBlock flow = AdvanceCompositeBlock.parseFlow(request.children().get(0));
			ctrl.updateFlow(request.get("realm"), flow);
			
		} else
		if ("verify-flow".equals(function)) {
			exch.next(HttpRemoteUtils.storeItem("compilation-result", 
					ctrl.verifyFlow(AdvanceCompositeBlock.parseFlow(request.children().get(0)))));
		} else
		if ("debug-block".equals(function)) {
			final String realm = request.get("realm");
			exch.startMany();
			ctrl.debugBlock(request.get("realm"), request.get("block-id")).register(new Observer<AdvanceBlockDiagnostic>() {
				@Override
				public void error(Throwable ex) {
					LOG.error(ex.toString(), ex);
					try {
						exch.finishMany();
					} catch (IOException exc) {
						LOG.error(exc.toString(), exc);
					}
				}
				@Override
				public void finish() {
					try {
						exch.finishMany();
					} catch (IOException ex) {
						LOG.error(ex.toString(), ex);
					}
				}
				@Override
				public void next(AdvanceBlockDiagnostic value) {
					value.realm = realm;
					try {
						exch.next(HttpRemoteUtils.storeItem("block-diagnostic", value));
					} catch (IOException ex) {
						LOG.error(ex.toString(), ex);
					}
				}
			});
		} else
		if ("debug-parameter".equals(function)) {
			final String realm = request.get("realm");
			exch.startMany();
			ctrl.debugParameter(realm, request.get("block-id"), request.get("port"))
			.register(new Observer<AdvanceParameterDiagnostic>() {
				@Override
				public void error(Throwable ex) {
					LOG.error(ex.toString(), ex);
					try {
						exch.finishMany();
					} catch (IOException exc) {
						LOG.error(exc.toString(), exc);
					}
				}
				@Override
				public void finish() {
					try {
						exch.finishMany();
					} catch (IOException ex) {
						LOG.error(ex.toString(), ex);
					}
				}
				@Override
				public void next(AdvanceParameterDiagnostic value) {
					try {
						value.realm = realm;
						exch.next(HttpRemoteUtils.storeItem("parameter-diagnostic", value));
					} catch (IOException ex) {
						LOG.error(ex.toString(), ex);
					}
				}
			});
			
		} else
		if ("inject-value".equals(function)) {
			String realm = request.get("realm");
			String blockId = request.get("block-id");
			String port = request.get("port");
			XElement value = request.children().get(0);
			ctrl.injectValue(realm, blockId, port, value);
		} else
		if ("shutdown".equals(function)) {
			ctrl.shutdown();
		} else
		if ("delete-schema".equals(function)) {
			ctrl.deleteSchema(request.get("name"));
		} else
		if ("query-compilation-result".equals(function)) {
			exch.next(HttpRemoteUtils.storeItem("compilation-result", ctrl.queryCompilationResult(request.get("realm"))));
		} else {
			// try datastore
			datastoreListener.dispatch(exch);
		}
	}
}
