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

package eu.advance.logistics.flow.engine.model.fd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.api.core.Copyable;
import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.flow.engine.xml.XSerializable;

/**
 * An input or output parameter description of an ADVANCE block.
 * @author akarnokd, 2011.06.21.
 */
public class AdvanceBlockParameterDescription implements XSerializable, Copyable<AdvanceBlockParameterDescription> {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceBlockParameterDescription.class);
	/** The unique (among other inputs or outputs of this block) identifier of the input parameter. This ID will be used by the block wiring within the flow description. */
	@NonNull
	public String id;
	/** Optional display text for this attribute. Can be used as a key into a translation table. */
	@Nullable
	public String displayName;
	/** The URI pointing to the documentation describing this parameter. */
	public String documentation;
	/** The type variable. */
	public AdvanceType type;
	/** Indicates that this input is required. */
	public boolean required;
	/** If non-null, it contains the default constant. */
	public XElement defaultValue;
	/** Indicates that the parameter is actually a variable argument. */
	public boolean varargs;
	/**
	 * Load a parameter description from an XML element which conforms the {@code block-description.xsd}.
	 * @param root the root element of an input/output node.
	 */
	@Override
	public void load(XElement root) {
		id = root.get("id");
		displayName = root.get("displayname");
		documentation = root.get("documentation");
		required = root.getBoolean("required", true);
		XElement c = root.childElement("default");
		if (c != null) {
			if (c.children().size() == 1) {
				defaultValue = c.children().get(0);
			} else {
				LOG.error("Missing default value: " + root);
			}
		}
		varargs = root.getBoolean("varargs", false);
		type = new AdvanceType();
		type.load(root);
	}
	@Override
	public void save(XElement destination) {
		destination.set("id", id);
		destination.set("displayname", displayName);
		destination.set("documentation", documentation);
		type.save(destination);
	}
	@Override
	public AdvanceBlockParameterDescription copy() {
		AdvanceBlockParameterDescription result = new AdvanceBlockParameterDescription();
		result.id = id;
		result.displayName = displayName;
		result.documentation = documentation;
		result.required = required;
		result.defaultValue = defaultValue;
		result.varargs = varargs;
		result.type = type.copy();
		
		return result;
	}
}
