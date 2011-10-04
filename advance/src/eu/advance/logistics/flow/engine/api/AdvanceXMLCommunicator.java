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

package eu.advance.logistics.flow.engine.api;

import java.io.IOException;

import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Base interface for classes that communicate via XML based messages.
 * @author karnokd, 2011.10.03.
 */
public interface AdvanceXMLCommunicator {
	/**
	 * Send a query and wait for the response.
	 * @param request the request XML
	 * @return the response XML
	 * @throws IOException if a network error occurred
	 */
	XElement query(XElement request) throws IOException;
	/**
	 * Query for a (default) response without sending a query message.
	 * @return the response XML
	 * @throws IOException if a network error occurs
	 */
	XElement query() throws IOException;
	/**
	 * Send a request that doesn't return a response (other than acknowledgement).
	 * @param request the request XML
	 * @throws IOException if a network error occurred
	 */
	void send(XElement request) throws IOException;
}