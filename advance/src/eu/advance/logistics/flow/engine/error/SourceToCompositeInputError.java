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
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockBind;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * The source (input) of the binding points to a composite input port in the same level (and not the parent composite's input).
 * @author akarnokd, 2011.07.07.
 */
public class SourceToCompositeInputError implements AdvanceCompilationError {
	/** The wire identifier. */
	public AdvanceBlockBind binding;
	/**
	 * Constructor.
	 * <p>The source (input) of the binding points to a composite input port in the same level (and not the parent composite's input).</p>
	 * @param binding the actual binding causing the problem
	 */
	public SourceToCompositeInputError(AdvanceBlockBind binding) {
		this.binding = binding;
	}
	/** Empty constructor. */
	public SourceToCompositeInputError() {
		
	}
	@Override
	public void load(XElement source) {
		binding = new AdvanceBlockBind();
		binding.load(source.childElement("binding"));
	}
	@Override
	public void save(XElement destination) {
		destination.set("type", getClass().getSimpleName());
		destination.set("message", toString());
		binding.save(destination.add("binding"));
	}
	/** Creates a new instance of this class. */
	public static final Func0<SourceToCompositeInputError> CREATOR = new Func0<SourceToCompositeInputError>() {
		@Override
		public SourceToCompositeInputError invoke() {
			return new SourceToCompositeInputError();
		}
	};
	/**
	 * Register this class in the supplied map.
	 * @param map the map from error type name to function to create an instance
	 */
	public static void register(Map<String, Func0<? extends AdvanceCompilationError>> map) {
		map.put(SourceToCompositeInputError.class.getSimpleName(), CREATOR);
	}
	@Override
	public String toString() {
		return "Wire " + binding.id + " input is bound to the composite block input of (" + binding.sourceBlock + ", " + binding.sourceParameter + ")";
	}
}
