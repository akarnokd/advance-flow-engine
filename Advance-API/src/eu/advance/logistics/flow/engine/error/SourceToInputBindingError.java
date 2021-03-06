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
 * The wire's source is bound to an input port of an block. 
 * @author akarnokd, 2011.07.07.
 */
public class SourceToInputBindingError implements AdvanceCompilationError, HasBinding {
	/** The wire identifier. */
	public AdvanceBlockBind binding;
	/**
	 * Constructor.
	 * <p>The wire's source is bound to an input port of an block.</p>
	 * @param binding the actual binding causing the problem
	 */
	public SourceToInputBindingError(AdvanceBlockBind binding) {
		this.binding = binding;
	}
	/** Empty constructor. */
	public SourceToInputBindingError() {
		
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
	public static final Func0<SourceToInputBindingError> CREATOR = new Func0<SourceToInputBindingError>() {
		@Override
		public SourceToInputBindingError invoke() {
			return new SourceToInputBindingError();
		}
	};
	/**
	 * Register this class in the supplied map.
	 * @param map the map from error type name to function to create an instance
	 */
	public static void register(Map<String, Func0<? extends AdvanceCompilationError>> map) {
		map.put(SourceToInputBindingError.class.getSimpleName(), CREATOR);
	}
	@Override
	public String toString() {
		return "Wire " + binding.id + " input is bound to the block input of (" + binding.sourceBlock + ", " + binding.sourceParameter + ")";
	}
	@Override
	public AdvanceBlockBind binding() {
		return binding;
	}
}
