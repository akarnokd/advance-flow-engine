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

package eu.advance.logistics.flow.engine.error;

import hu.akarnokd.reactive4java.base.Func0;

import java.util.Map;

import eu.advance.logistics.flow.engine.model.AdvanceCompilationError;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * The referenced block type cannot be found.
 * @author akarnokd, 2011.07.07.
 */
public class MissingBlockError implements AdvanceCompilationError {
	/** The block ID where this happened. */
	public String id;
	/** The missing block type. */
	public String type;
	/**
	 * Constructor.
	 * <p>The referenced block type cannot be found</p>
	 * @param id the block id
	 * @param type the block type
	 */
	public MissingBlockError(String id, String type) {
		this.id = id;
		this.type = type;
	}
	/** Empty constructor. */
	public MissingBlockError() {
		
	}
	@Override
	public void load(XElement source) {
		id = source.get("id");
		type = source.get("type");
	}
	@Override
	public void save(XElement destination) {
		destination.set("type", getClass().getSimpleName());
		destination.set("message", toString());
		destination.set("id", id, "type", type);
	}
	/** Creates a new instance of this class. */
	public static final Func0<MissingBlockError> CREATOR = new Func0<MissingBlockError>() {
		@Override
		public MissingBlockError invoke() {
			return new MissingBlockError();
		}
	};
	/**
	 * Register this class in the supplied map.
	 * @param map the map from error type name to function to create an instance
	 */
	public static void register(Map<String, Func0<? extends AdvanceCompilationError>> map) {
		map.put(MissingBlockError.class.getSimpleName(), CREATOR);
	}
	@Override
	public String toString() {
		return "Missing block type " + type + " referenced by ID " + id;
	}
}
