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

package eu.advance.logistics.flow.engine.error;

import hu.akarnokd.reactive4java.base.Func0;

import java.util.Map;

import eu.advance.logistics.flow.engine.model.AdvanceCompilationError;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * The varargs parameter has no counts set in the block reference.
 * @author akarnokd, 2011.07.07.
 */
public class UnsetVarargsError implements AdvanceCompilationError {
	/** The block ID where this happened. */
	public String id;
	/** The missing block type. */
	public String type;
	/** The name of the input. */
	public String input;
	/**
	 * Constructor.
	 * <p>The varargs parameter has no counts set in the block reference.</p>
	 * @param id the block id
	 * @param type the block type
	 * @param input the input name
	 */
	public UnsetVarargsError(String id, String type, String input) {
		this.id = id;
		this.type = type;
		this.input = input;
	}
	/** Empty constructor. */
	public UnsetVarargsError() {
		
	}
	@Override
	public void load(XElement source) {
		id = source.get("id");
		type = source.get("type");
		input = source.get("input");
	}
	@Override
	public void save(XElement destination) {
		destination.set("type", getClass().getSimpleName());
		destination.set("message", toString());
		destination.set("id", id, "type", type, "input", input);
	}
	/** Creates a new instance of this class. */
	public static final Func0<UnsetVarargsError> CREATOR = new Func0<UnsetVarargsError>() {
		@Override
		public UnsetVarargsError invoke() {
			return new UnsetVarargsError();
		}
	};
	/**
	 * Register this class in the supplied map.
	 * @param map the map from error type name to function to create an instance
	 */
	public static void register(Map<String, Func0<? extends AdvanceCompilationError>> map) {
		map.put(UnsetVarargsError.class.getSimpleName(), CREATOR);
	}
	@Override
	public String toString() {
		return "The varargs input " + type + ":" + input + " count is not set by the block reference " + id;
	}
}
