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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.flow.engine.xml.XSerializable;

/**
 * Definition for binding parameters of blocks in the {@code flow-description.xsd}.
 * @author akarnokd, 2011.06.21.
 */
public class AdvanceBlockBind implements XSerializable {
	/** Identifier of this binding. May be used to communicate problematic bindings for the Flow Editor / Compiler. */
	@NonNull
	public String id;
	/** The parent composite block. */
	@NonNull
	public AdvanceCompositeBlock parent;
	/** The source block identifier for the binding. If {@code null}, the source-parameter refers to the enclosing composite-block's input parameter. */
	@Nullable
	public String sourceBlock;
	/** The source parameter identifier of the source-block or the enclosing composite-block's input parameter. */
	@NonNull
	public String sourceParameter;
	/** The destination block identifier for the binding. If {@code null}, the destination-parameter refers to the enclosing composite-block's output parameter. */
	@Nullable
	public String destinationBlock;
	/** The destination parameter identifier of the destination-block or the enclosing composite-block's output parameter. */
	@NonNull
	public String destinationParameter;
	/**
	 * Load a binding definition from an XML element which conforms the {@code flow-description.xsd}.
	 * @param root the element
	 */
	@Override
	public void load(XElement root) {
		id = root.get("id");
		sourceBlock = root.get("source-block");
		sourceParameter = root.get("source-parameter");
		destinationBlock = root.get("destination-block");
		destinationParameter = root.get("destination-parameter");
	}
	@Override
	public void save(XElement destination) {
		destination.set("id", id);
		destination.set("source-block", sourceBlock);
		destination.set("source-parameter", sourceParameter);
		destination.set("destination-block", destinationBlock);
		destination.set("destination-parameter", destinationParameter);
	}
	/**
	 * @return Test if a source block is given or this binding refers to a parameter of the enclosing composite block
	 */
	public boolean hasSourceBlock() {
		return sourceBlock != null && !sourceBlock.isEmpty();
	}
	/**
	 * @return Test if a source block is given or this binding refers to a parameter of the enclosing composite block
	 */
	public boolean hasDestinationBlock() {
		return destinationBlock != null && !destinationBlock.isEmpty();
	}
	@Override
	public String toString() {
		return String.format("{ id = %s, source-block = %s, source-parameter = %s, destination-block = %s, destination-parameter = %s }",
				id, sourceBlock, sourceParameter, destinationBlock, destinationParameter
				);
	}
}
