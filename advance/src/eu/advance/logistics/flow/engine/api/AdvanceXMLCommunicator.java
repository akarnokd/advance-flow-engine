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

package eu.advance.logistics.flow.engine.api;

import hu.akarnokd.reactive4java.base.Observable;
import hu.akarnokd.reactive4java.base.Scheduler;
import hu.akarnokd.utils.xml.XNElement;

import java.io.IOException;

/**
 * Base interface for classes that communicate via XML based messages.
 * @author akarnokd, 2011.10.03.
 */
public interface AdvanceXMLCommunicator {
	/**
	 * Send a query and wait for the response.
	 * @param request the request XML
	 * @return the response XML
	 * @throws IOException if a network error occurred
	 */
	XNElement query(XNElement request) throws IOException;
	/**
	 * Query for a (default) response without sending a query message.
	 * @return the response XML
	 * @throws IOException if a network error occurs
	 */
	XNElement query() throws IOException;
	/**
	 * Send a request that doesn't return a response (other than acknowledgement).
	 * @param request the request XML
	 * @throws IOException if a network error occurred
	 */
	void send(XNElement request) throws IOException;
	/**
	 * Receive XML responses continuously.
	 * The returned data should be a complete XML, but its parsing is done per each of the root's children,
	 * e.g., an XML of &lt;a>&lt;b/>&lt;b/>&lt/a> will produce two b elements.
	 * Observers should throw a CancellationException to stop the reception
	 * @param request the request to send the response messages or exceptions
	 * @param scheduler where the blocking wait occurs
	 * @return the closeable to terminate the streaming
	 * @throws IOException
	 */
	Observable<XNElement> receive(XNElement request, Scheduler scheduler);
}
