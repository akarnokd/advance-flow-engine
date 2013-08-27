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

package eu.advance.logistics.flow.engine;

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Pair;
import hu.akarnokd.reactive4java.reactive.Reactive;
import hu.akarnokd.utils.xml.XNElement;
import hu.akarnokd.utils.xml.XNSerializables;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceHttpListener;
import eu.advance.logistics.flow.engine.api.AdvanceXMLExchange;
import eu.advance.logistics.flow.engine.api.core.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceGenerateKey;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStoreExport;
import eu.advance.logistics.flow.engine.api.impl.CheckedEngineControl;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.runtime.BlockDiagnostic;
import eu.advance.logistics.flow.engine.runtime.PortDiagnostic;

/**
 * The listener for Engine control messages coming through the HTTP XML interface.
 * @author akarnokd, 2011.09.29.
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
	public AdvanceXMLExchange dispatch(@NonNull final XNElement request, @NonNull final String userName) throws IOException, AdvanceControlException {
		AdvanceEngineControl ctrl = new CheckedEngineControl(control, userName);
		String function = request.name;
		LOG.debug(function);
		if ("get-user".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeItem("user", ctrl.getUser()));
		} else
		if ("query-blocks".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeList("blocks", "block", ctrl.queryBlocks()));
		} else
		if ("query-schemas".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeList("schemas", "schema", ctrl.querySchemas()));
		} else
		if ("query-version".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeItem("version", ctrl.queryVersion()));
		} else
		if ("update-schema".equals(function)) {
			ctrl.updateSchema(request.get("name"), request.children().get(0).copy());
		} else
		if ("query-schema".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeItem("schema", ctrl.querySchema(request.get("name"))));
		} else
		if ("delete-key-entry".equals(function)) {
			ctrl.deleteKeyEntry(request.get("keystore"), request.get("keyalias"));
			
		} else
		if ("generate-key".equals(function)) {
			ctrl.generateKey(XNSerializables.parseItem(request, AdvanceGenerateKey.CREATOR));
			
		} else
		if ("export-certificate".equals(function)) {
			XNElement response = new XNElement("certificate");
			response.content = ctrl.exportCertificate(XNSerializables.parseItem(request, AdvanceKeyStoreExport.CREATOR));
			return AdvanceXMLExchange.single(response);
		} else
		if ("export-private-key".equals(function)) {
			XNElement response = new XNElement("private-key");
			response.content = ctrl.exportPrivateKey(XNSerializables.parseItem(request, AdvanceKeyStoreExport.CREATOR));
			return AdvanceXMLExchange.single(response);
		} else
		if ("import-certificate".equals(function)) {
			ctrl.importCertificate(XNSerializables.parseItem(request, AdvanceKeyStoreExport.CREATOR), request.content);
			
		} else
		if ("import-private-key".equals(function)) {
			ctrl.importPrivateKey(XNSerializables.parseItem(request, AdvanceKeyStoreExport.CREATOR),
					request.childValue("private-key"), request.childValue("certificate"));
			
		} else
		if ("export-signing-request".equals(function)) {
			XNElement response = new XNElement("signing-request");
			response.content = ctrl.exportSigningRequest(XNSerializables.parseItem(request, AdvanceKeyStoreExport.CREATOR));
			return AdvanceXMLExchange.single(response);
		} else
		if ("import-signing-response".equals(function)) {
			ctrl.importSigningResponse(XNSerializables.parseItem(request, AdvanceKeyStoreExport.CREATOR), request.content);
			
		} else
		if ("test-jdbc-data-source".equals(function)) {
			XNElement result = new XNElement("datastore-test-result");
			result.content = ctrl.testJDBCDataSource(request.get("dataSourceName")).toString();
			return AdvanceXMLExchange.single(result);
		} else
		if ("test-jms-endpoint".equals(function)) {
			XNElement result = new XNElement("datastore-test-result");
			result.content = ctrl.testJMSEndpoint(request.get("jms-name")).toString();
			return AdvanceXMLExchange.single(result);
		} else
		if ("test-ftp-data-source".equals(function)) {
			XNElement result = new XNElement("datastore-test-result");
			result.content =  ctrl.testFTPDataSource(request.get("jms-name")).toString();
			return AdvanceXMLExchange.single(result);
		} else
		if ("query-keys".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeList("keys", "keyentry", ctrl.queryKeys(request.get("keystore"))));
		} else
		if ("stop-realm".equals(function)) {
			ctrl.stopRealm(request.get("name"), request.get("by-user"));
			
		} else
		if ("start-realm".equals(function)) {
			ctrl.startRealm(request.get("name"), request.get("by-user"));
			
		} else
		if ("query-flow".equals(function)) {
			return AdvanceXMLExchange.single(AdvanceCompositeBlock.serializeFlow(ctrl.queryFlow(request.get("realm"))));
		} else
		if ("update-flow".equals(function)) {
			AdvanceCompositeBlock flow = AdvanceCompositeBlock.parseFlow(request.children().get(0));
			ctrl.updateFlow(request.get("realm"), flow, request.get("by-user"));
			
		} else
		if ("verify-flow".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeItem("compilation-result", 
					ctrl.verifyFlow(AdvanceCompositeBlock.parseFlow(request.children().get(0)))));
		} else
		if ("debug-block".equals(function)) {
			final String realm = request.get("realm");
			return AdvanceXMLExchange.multiple(Reactive.select(ctrl.debugBlock(request.get("realm"), request.get("block-id")), new Func1<BlockDiagnostic, XNElement>() {
				@Override
				public XNElement invoke(BlockDiagnostic param1) {
					param1.realm = realm;
					return XNSerializables.storeItem("block-diagnostic", param1);
				}
			}));
		} else
		if ("debug-parameter".equals(function)) {
			final String realm = request.get("realm");
			return AdvanceXMLExchange.multiple(Reactive.select(
					ctrl.debugParameter(request.get("realm"), request.get("block-id"), request.get("port")), 
					new Func1<PortDiagnostic, XNElement>() {
				@Override
				public XNElement invoke(PortDiagnostic param1) {
					param1.realm = realm;
					return XNSerializables.storeItem("parameter-diagnostic", param1);
				}
			}));
		} else
		if ("inject-value".equals(function)) {
			String realm = request.get("realm");
			String blockId = request.get("block-id");
			String port = request.get("port");
			XNElement value = request.children().get(0);
			ctrl.injectValue(realm, blockId, port, value);
		} else
		if ("shutdown".equals(function)) {
			ctrl.shutdown();
		} else
		if ("delete-schema".equals(function)) {
			ctrl.deleteSchema(request.get("name"));
		} else
		if ("query-compilation-result".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeItem("compilation-result", ctrl.queryCompilationResult(request.get("realm"))));
		} else
		if ("query-ports".equals(function)) {
			return AdvanceXMLExchange.single(XNSerializables.storeList("ports", "port", ctrl.queryPorts(request.get("realm"))));
		} else
		if ("send-port".equals(function)) {
			String realm = request.get("realm");
			List<Pair<String, XNElement>> values = Lists.newArrayList();
			for (XNElement se : request.childrenWithName("entry")) {
				values.add(Pair.of(se.get("port"), se.children().get(0)));
			}
			ctrl.sendPort(realm, values);
		} else
		if ("receive-port".equals(function)) {
			return AdvanceXMLExchange.multiple(ctrl.receivePort(request.get("realm"), request.get("port")));
		} else {
			// try datastore
			return datastoreListener.dispatch(request, userName);
		}
		return AdvanceXMLExchange.none();
	}
}
