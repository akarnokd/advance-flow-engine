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

package eu.advance.logistics.flow.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.google.common.collect.Lists;

import eu.advance.logistics.xml.typesystem.XElement;

/**
 * The type parameter bound definition for a generic type parameter.
 * @author karnokd, 2011.07.01.
 */
public class AdvanceType {
	/** Reference to another type parameter within the same set of declarations. */
	public String typeVariableName;
	/** The actual type variable object. */
	public AdvanceTypeVariable typeVariable;
	/** An existing concrete type definition schema. */
	public URI type;
	/** The type arguments used by the concrete type. */
	public final List<AdvanceType> typeArguments = Lists.newArrayList();
	/**
	 * Load a type description from an XML element which conforms the {@code block-description.xsd}.
	 * @param root the root element of an input/output node.
	 */
	public void load(XElement root) {
		typeVariableName = root.get("type-variable");
		String tu = root.get("type");
		if ((tu != null) == (typeVariableName != null)) {
			throw new IllegalArgumentException("Only one of the type-variable and type arguments should be defined! " + root);
		}
		if (tu != null) {
			try {
				type = new URI(tu);
			} catch (URISyntaxException ex) {
				throw new RuntimeException(ex);
			}
		}
		for (XElement ta : root.childrenWithName("type-argument")) {
			AdvanceType at = new AdvanceType();
			at.load(ta);
			typeArguments.add(at);
		}
	}

}