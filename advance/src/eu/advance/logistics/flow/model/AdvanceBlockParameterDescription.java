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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.xml.typesystem.XElement;

/**
 * An input or output parameter description of an ADVANCE block.
 * @author karnokd, 2011.06.21.
 */
public class AdvanceBlockParameterDescription {
	/** The unique (among other inputs or outputs of this block) identifier of the input parameter. This ID will be used by the block wiring within the flow description. */
	@NonNull
	public String id;
	/** Optional display text for this attribute. Can be used as a key into a translation table. */
	@Nullable
	public String displayName;
	/** The URI defining the ADVANCE (annotated) XML schema for this parameter. Use res:// to define a Java package-local definition on the classpath. */
	@NonNull
	public URI type;
	/** The variance definition of this input parameter, required for the block bindings to check for compatibility, e.g., allow extension, restriction or exact type matches to be wired in. */
	@NonNull
	public TypeVariance variance = TypeVariance.NONVARIANT;
	/**
	 * Load a parameter description from an XML element which conforms the {@code block-description.xsd}.
	 * @param root the root element of an input/output node.
	 */
	public void load(XElement root) {
		id = root.get("id");
		displayName = root.get("displayname");
		String t = root.get("type");
		if (t != null) {
			try {
				type = new URI(t);
			} catch (URISyntaxException ex) {
				throw new RuntimeException(ex);
			}
		}
		String v = root.get("variance");
		if (v != null) {
			variance = TypeVariance.of(v);
		}
		
	}
}
