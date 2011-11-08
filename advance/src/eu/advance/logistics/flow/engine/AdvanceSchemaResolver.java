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

import java.net.URI;

import eu.advance.logistics.flow.engine.xml.typesystem.XType;

/**
 * Interface for providing support to resolve a schema URI into an XType.
 * @author akarnokd, 2011.09.28.
 */
public interface AdvanceSchemaResolver {
	/**
	 * Resolve a schema URI link.
	 * @param schemaURI the schema URI.
	 * @return the parsed schema
	 */
	XType resolve(URI schemaURI);
}
