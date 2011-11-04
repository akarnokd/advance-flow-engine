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
 * A wire is bound to a constant on its destination side.
 * @author akarnokd, 2011.07.07.
 */
public class ConstantOutputError implements AdvanceCompilationError, HasBinding {
	/** The wire identifier. */
	public AdvanceBlockBind binding;
	/**
	 * Constructor.
	 * <p>A wire is bound to a constant on its destination side.</p>
	 * @param binding the actual binding causing the problem
	 */
	public ConstantOutputError(AdvanceBlockBind binding) {
		this.binding = binding;
	}
	/** Empty constructor. */
	public ConstantOutputError() {
		
	}
	@Override
	public String toString() {
		return "Wire " + binding.id + " output is bound to a constant " + binding.destinationBlock;
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
	public static final Func0<ConstantOutputError> CREATOR = new Func0<ConstantOutputError>() {
		@Override
		public ConstantOutputError invoke() {
			return new ConstantOutputError();
		}
	};
	/**
	 * Register this class in the supplied map.
	 * @param map the map from error type name to function to create an instance
	 */
	public static void register(Map<String, Func0<? extends AdvanceCompilationError>> map) {
		map.put(ConstantOutputError.class.getSimpleName(), CREATOR);
	}
	@Override
	public AdvanceBlockBind binding() {
		return binding;
	}
}
