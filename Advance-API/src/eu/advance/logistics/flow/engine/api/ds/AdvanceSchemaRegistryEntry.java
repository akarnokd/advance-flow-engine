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

package eu.advance.logistics.flow.engine.api.ds;

import hu.akarnokd.reactive4java.base.Func0;
import hu.akarnokd.utils.xml.XNElement;
import hu.akarnokd.utils.xml.XNSerializable;

/**
 * The schema registry used to ask the engine about known schemas.
 * @author akarnokd, 2011.09.28.
 */
public class AdvanceSchemaRegistryEntry implements XNSerializable {
	/** The schema's filename (without path). */
	public String name;
	/** The schema content. */
	public XNElement schema;
	/** Function to create a new instance of this class. */
	public static final Func0<AdvanceSchemaRegistryEntry> CREATOR = new Func0<AdvanceSchemaRegistryEntry>() {
		@Override
		public AdvanceSchemaRegistryEntry invoke() {
			return new AdvanceSchemaRegistryEntry();
		}
	};
	@Override
	public void load(XNElement source) {
		name = source.get("name");
		schema = source.childElement("schema", XNElement.XSD).copy();
	}
	@Override
	public void save(XNElement destination) {
		destination.set("name", name);
		destination.add(schema);
	}
}
