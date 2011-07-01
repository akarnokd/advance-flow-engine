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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.xml.typesystem.XElement;

/**
 * Definition for binding parameters of blocks in the {@code flow-description.xsd}.
 * @author karnokd, 2011.06.21.
 */
public class AdvanceBlockBind {
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
	public void load(XElement root) {
		sourceBlock = root.get("source-block");
		sourceParameter = root.get("source-parameter");
		destinationBlock = root.get("destination-block");
		destinationParameter = root.get("destination-parameter");
	}
}