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

package eu.advance.logistics.flow.engine.test;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceHttpListener;
import eu.advance.logistics.flow.engine.api.AdvanceXMLCommunicator;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * A simple mocked communicator which dispatches the requests directly to an {@code AdvanceHttpListener}.
 * @author karnokd, 2011.10.03.
 */
public class TestHttpCommunicator implements AdvanceXMLCommunicator {
	/** The logger object. */
	protected static final Logger LOG = LoggerFactory.getLogger(TestHttpCommunicator.class);
	/** The listener. */
	@NonNull 
	protected final AdvanceHttpListener listener;
	/** The user name. */
	protected final String userName;
	/**
	 * Constructor.
	 * @param listener the listener.
	 * @param userName the testing user name
	 */
	public TestHttpCommunicator(@NonNull AdvanceHttpListener listener, String userName) {
		this.listener = listener;
		this.userName = userName;
	}
	@Override
	public XElement query() throws IOException {
		try {
			XElement response = listener.dispatch(userName, new XElement(""));
			if (response == null) {
				return new XElement("");
			}
			return response;
		} catch (AdvanceControlException ex) {
			throw new IOException(ex);
		}
	}
	@Override
	public XElement query(XElement request) throws IOException {
		try {
			XElement response = listener.dispatch(userName, request);
			if (response == null) {
				return new XElement("");
			}
			return response;
		} catch (AdvanceControlException ex) {
			throw new IOException(ex);
		}
	}
	@Override
	public void send(XElement request) throws IOException {
		try {
			XElement response = listener.dispatch(userName, new XElement("")); 
			if (response != null) {
				LOG.error("Response?! " + response);
			}
		} catch (AdvanceControlException ex) {
			throw new IOException(ex);
		}
	}
}
