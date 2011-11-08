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

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.ds.AdvanceControlException;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Base interface for the capability to dispatch an XML message and the logged-in user name.
 * @author akarnokd, 2011.10.03.
 */
public interface AdvanceHttpListener {
	/**
	 * Dispatch a {@code request} under the given {@code userName}.
	 * @param request the request object
	 * @param userName the logged-in user
	 * @return the exchange sequence
	 * @throws IOException if a network error occurs
	 * @throws AdvanceControlException if the user rights are inadequate
	 */
	@NonNull
	AdvanceXMLExchange dispatch(@NonNull final XElement request, 
			@NonNull final String userName) throws IOException, AdvanceControlException;
}
