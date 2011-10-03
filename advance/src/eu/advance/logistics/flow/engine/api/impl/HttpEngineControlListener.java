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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceGenerateKey;
import eu.advance.logistics.flow.engine.api.AdvanceHttpListener;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStoreExport;
import eu.advance.logistics.flow.engine.model.AdvanceCompositeBlock;
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
	/**
	 * Dispatch the incoming requests.
	 * @param userName the logged-in user
	 * @param request the raw request XML
	 * @return the optional response XML
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the wrapped control throws it.
	 */
	@Nullable
	@Override
	public XElement dispatch(@NonNull String userName, @NonNull XElement request) throws IOException, AdvanceControlException {
		//throw new AdvanceControlException("Unknown request " + request);
		AdvanceEngineControl ctrl = new CheckedEngineControl(control, userName);
		String function = request.name;
		LOG.debug(function);
		if ("get-user".equals(function)) {
			return HttpRemoteUtils.storeItem("user", ctrl.getUser());
		} else
		if ("query-blocks".equals(function)) {
			return HttpRemoteUtils.storeList("blocks", "block", ctrl.queryBlocks());
		} else
		if ("query-schemas".equals(function)) {
			return HttpRemoteUtils.storeList("schemas", "schema", ctrl.querySchemas());
		} else
		if ("query-version".equals(function)) {
			return HttpRemoteUtils.storeItem("version", ctrl.queryVersion());
		} else
		if ("update-schema".equals(function)) {
			ctrl.updateSchema(request.get("name"), request.children().get(0).copy());
			return null;
		} else
		if ("query-schema".equals(function)) {
			return HttpRemoteUtils.storeItem("schema", ctrl.querySchema(request.get("name")));
		} else
		if ("delete-key-entry".equals(function)) {
			ctrl.deleteKeyEntry(request.get("keystore"), request.get("keyalias"));
			return null;
		} else
		if ("generate-key".equals(function)) {
			ctrl.generateKey(HttpRemoteUtils.parseItem(request, AdvanceGenerateKey.CREATOR));
			return null;
		} else
		if ("export-certificate".equals(function)) {
			XElement response = new XElement("certificate");
			response.content = ctrl.exportCertificate(HttpRemoteUtils.parseItem(request, AdvanceKeyStoreExport.CREATOR));
			return response;
		} else
		if ("export-private-key".equals(function)) {
			XElement response = new XElement("private-key");
			response.content = ctrl.exportPrivateKey(HttpRemoteUtils.parseItem(request, AdvanceKeyStoreExport.CREATOR));
			return response;
		} else
		if ("import-certificate".equals(function)) {
			ctrl.importCertificate(HttpRemoteUtils.parseItem(request, AdvanceKeyStoreExport.CREATOR), request.content);
			return null;
		} else
		if ("import-private-key".equals(function)) {
			ctrl.importPrivateKey(HttpRemoteUtils.parseItem(request, AdvanceKeyStoreExport.CREATOR),
					request.childValue("private-key"), request.childValue("certificate"));
			return null;
		} else
		if ("export-signing-request".equals(function)) {
			XElement response = new XElement("signing-request");
			response.content = ctrl.exportSigningRequest(HttpRemoteUtils.parseItem(request, AdvanceKeyStoreExport.CREATOR));
			return response;
		} else
		if ("import-signing-response".equals(function)) {
			ctrl.importSigningResponse(HttpRemoteUtils.parseItem(request, AdvanceKeyStoreExport.CREATOR), request.content);
			return null;
		} else
		if ("test-jdbc-data-source".equals(function)) {
			XElement result = new XElement("datastore-test-result");
			result.content = ctrl.testJDBCDataSource(request.get("dataSourceName")).toString();
			return result;
		} else
		if ("test-jms-endpoint".equals(function)) {
			XElement result = new XElement("datastore-test-result");
			result.content = ctrl.testJMSEndpoint(request.get("jms-name")).toString();
			return result;
		} else
		if ("test-ftp-data-source".equals(function)) {
			XElement result = new XElement("datastore-test-result");
			result.content =  ctrl.testFTPDataSource(request.get("jms-name")).toString();
			return result;
		} else
		if ("query-keys".equals(function)) {
			return HttpRemoteUtils.storeList("keys", "keyentry", ctrl.queryKeys(request.get("keystore")));
		} else
		if ("stop-realm".equals(function)) {
			ctrl.stopRealm(request.get("name"), request.get("by-user"));
			return null;
		} else
		if ("start-realm".equals(function)) {
			ctrl.startRealm(request.get("name"), request.get("by-user"));
			return null;
		} else
		if ("query-flow".equals(function)) {
			return AdvanceCompositeBlock.serializeFlow(ctrl.queryFlow(request.get("realm")));
		} else
		if ("update-flow".equals(function)) {
			AdvanceCompositeBlock flow = AdvanceCompositeBlock.parseFlow(request.children().get(0));
			ctrl.updateFlow(request.get("realm"), flow);
			return null;
		} else
		if ("verify-flow".equals(function)) {
			return HttpRemoteUtils.storeItem("compilation-result", 
					ctrl.verifyFlow(AdvanceCompositeBlock.parseFlow(request.children().get(0))));
		} else
		if ("debug-block".equals(function)) {
			LOG.error("debug-block not supported");
			return null;
		} else
		if ("debug-parameter".equals(function)) {
			LOG.error("debug-parameter not supported");
			return null;
		} else
		if ("inject-value".equals(function)) {
			String realm = request.get("realm");
			String blockId = request.get("block-id");
			String port = request.get("port");
			XElement value = request.children().get(0);
			ctrl.injectValue(realm, blockId, port, value);
			return null;
		} else
		if ("shutdown".equals(function)) {
			ctrl.shutdown();
			return null;
		} else
		if ("delete-schema".equals(function)) {
			ctrl.deleteSchema(request.get("name"));
			return null;
		}
		// try datastore
		return datastoreListener.dispatch(userName, request);
	}
}
