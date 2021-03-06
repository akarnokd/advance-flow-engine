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
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockBind;

/**
 * The destination object of the binding cannot be found.
 * @author akarnokd, 2011.07.07.
 */
public class MissingDestinationError implements AdvanceCompilationError, HasBinding {
	/** The wire identifier. */
	public AdvanceBlockBind binding;
	/**
	 * Constructor.
	 * @param binding the actual binding causing the problem
	 */
	public MissingDestinationError(AdvanceBlockBind binding) {
		this.binding = binding;
	}
	/** Empty constructor. */
	public MissingDestinationError() {
		
	}
	@Override
	public void load(XNElement source) {
		binding = new AdvanceBlockBind();
		binding.load(source.childElement("binding"));
	}
	@Override
	public void save(XNElement destination) {
		destination.set("type", getClass().getSimpleName());
		destination.set("message", toString());
		binding.save(destination.add("binding"));
	}
	/** Creates a new instance of this class. */
	public static final Func0<MissingDestinationError> CREATOR = new Func0<MissingDestinationError>() {
		@Override
		public MissingDestinationError invoke() {
			return new MissingDestinationError();
		}
	};
	/**
	 * Register this class in the supplied map.
	 * @param map the map from error type name to function to create an instance
	 */
	public static void register(Map<String, Func0<? extends AdvanceCompilationError>> map) {
		map.put(MissingDestinationError.class.getSimpleName(), CREATOR);
	}
	@Override
	public String toString() {
		return "Wire " + binding.id + " has missing destination block " + binding.destinationBlock;
	}
	@Override
	public AdvanceBlockBind binding() {
		return binding;
	}
}
