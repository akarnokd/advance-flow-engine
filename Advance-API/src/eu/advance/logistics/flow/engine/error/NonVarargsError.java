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
import hu.akarnokd.utils.xml.XNElement;

import java.util.Map;

import eu.advance.logistics.flow.engine.model.AdvanceCompilationError;

/**
 * The referenced parameter is not a varargs parameter.
 * @author akarnokd, 2011.07.07.
 */
public class NonVarargsError implements AdvanceCompilationError {
	/** The block ID where this happened. */
	public String id;
	/** The missing block type. */
	public String type;
	/** The name of the input. */
	public String input;
	/**
	 * Constructor.
	 * <p>The referenced parameter is not a varargs parameter.</p>
	 * @param id the block id
	 * @param type the block type
	 * @param input the input name
	 */
	public NonVarargsError(String id, String type, String input) {
		this.id = id;
		this.type = type;
		this.input = input;
	}
	/** Empty constructor. */
	public NonVarargsError() {
		
	}
	@Override
	public void load(XNElement source) {
		id = source.get("id");
		type = source.get("type");
		input = source.get("input");
	}
	@Override
	public void save(XNElement destination) {
		destination.set("type", getClass().getSimpleName());
		destination.set("message", toString());
		destination.set("id", id, "type", type, "input", input);
	}
	/** Creates a new instance of this class. */
	public static final Func0<NonVarargsError> CREATOR = new Func0<NonVarargsError>() {
		@Override
		public NonVarargsError invoke() {
			return new NonVarargsError();
		}
	};
	/**
	 * Register this class in the supplied map.
	 * @param map the map from error type name to function to create an instance
	 */
	public static void register(Map<String, Func0<? extends AdvanceCompilationError>> map) {
		map.put(NonVarargsError.class.getSimpleName(), CREATOR);
	}
	@Override
	public String toString() {
		return "Block reference " + id + " (" + type + ") uses a non-varargs input " + input;
	}
}