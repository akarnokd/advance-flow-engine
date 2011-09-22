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

package eu.advance.logistics.flow.engine.model;

import java.net.URI;

/**
 * The exception thrown when the specified schema uri cannot be properly resolved.
 * @author karnokd, 2011.06.22.
 */
public class UnresolvableSchemaURIException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1874430876536751248L;
	/** The schema URI. */
	public final URI schemaURI;
	/**
	 * Constructor.
	 * @param schemaURI the schema uri causing the problem
	 */
	public UnresolvableSchemaURIException(URI schemaURI) {
		this.schemaURI = schemaURI;
	}
	/**
	 * Constructor.
	 * @param schemaURI the schema uri causing the problem
	 * @param exception the exception raised
	 */
	public UnresolvableSchemaURIException(URI schemaURI, Throwable exception) {
		super(exception);
		this.schemaURI = schemaURI;
	}	
	@Override
	public String getMessage() {
		return "" + schemaURI;
	}
}
