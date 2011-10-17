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
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Type inference error on the given wire.
 * @author akarnokd, 2011.07.21.
 */
public class TypeMismatchError implements AdvanceCompilationError {
	/** The wire identifier. */
	public AdvanceBlockBind binding;
	/** The left side of the binding. */
	public AdvanceType left;
	/** The right side of the binding. */
	public AdvanceType right;
	/**
	 * Constructor.
	 * <p>Type inference error on the given wire.</p>
	 * @param binding the actual binding causing the problem
	 * @param left the left side of the binding
	 * @param right the right side of the binding
	 */
	public TypeMismatchError(AdvanceBlockBind binding, AdvanceType left, AdvanceType right) {
		this.binding = binding;
		this.left = left;
		this.right = right;
	}
	@Override
	public String toString() {
		return "Type mismatch on wire " + binding.id + ": " + left + " vs. " + right;
	}
	/** Empty constructor. */
	public TypeMismatchError() {
		
	}
	@Override
	public void load(XElement source) {
		binding = new AdvanceBlockBind();
		binding.load(source.childElement("binding"));
		left = new AdvanceType();
		left.load(source.childElement("left-type"));
		right = new AdvanceType();
		right.load(source.childElement("right-type"));
	}
	@Override
	public void save(XElement destination) {
		binding.save(destination.add("binding"));
		left.save(destination.add("left-type"));
		right.save(destination.add("right-type"));
	}
	/** Creates a new instance of this class. */
	public static final Func0<TypeMismatchError> CREATOR = new Func0<TypeMismatchError>() {
		@Override
		public TypeMismatchError invoke() {
			return new TypeMismatchError();
		}
	};
	/**
	 * Register this class in the supplied map.
	 * @param map the map from error type name to function to create an instance
	 */
	public static void register(Map<String, Func0<? extends AdvanceCompilationError>> map) {
		map.put(TypeMismatchError.class.getSimpleName(), CREATOR);
	}
}
