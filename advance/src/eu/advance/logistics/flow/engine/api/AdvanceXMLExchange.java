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

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * The interface to provide information about the request and give opportunities for direct
 * or streaming responses.
 * @author karnokd, 2011.10.04.
 */
public interface AdvanceXMLExchange {
	/** @return the logged-in user name. */
	@NonNull 
	String userName();
	/** @return the request object. */
	@NonNull 
	XElement request();
	/** 
	 * Indicate that there will be multiple responses.
	 * @throws IOException if the multiresponse could not be sent 
	 */
	void startMany() throws IOException;
	/**
	 * Send the next response XML.
	 * @param value the value to send
	 * @throws IOException if a network error occurs
	 */
	void next(XElement value) throws IOException;
	/**
	 * Indicate the completion of the multiple responses response.
	 * @throws IOException if a network error occurs
	 */
	void finishMany() throws IOException;
}
