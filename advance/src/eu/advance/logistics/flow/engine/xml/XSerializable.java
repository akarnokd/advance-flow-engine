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

package eu.advance.logistics.flow.engine.xml;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Common interface to denote objects which can be serialized and deserialized into XElements.
 * @author akarnokd, Jul 6, 2011
 */
public interface XSerializable {
	/**
	 * Load the contents from the given source.
	 * @param source the source XElement
	 */
	void load(@NonNull XElement source);
	/**
	 * Save the contents into the given destination.
	 * @param destination the destination XElement
	 */
	void save(@NonNull XElement destination);
}
