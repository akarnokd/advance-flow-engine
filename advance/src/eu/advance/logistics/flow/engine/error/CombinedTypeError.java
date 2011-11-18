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

import java.util.Arrays;
import java.util.Map;

import eu.advance.logistics.flow.engine.model.AdvanceCompilationError;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockBind;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * The combination (e.g., union) of two types could not be created.
 * @author akarnokd, 2011.07.27.
 */
public class CombinedTypeError implements AdvanceCompilationError, HasBinding, HasTypes {
	/** The wire identifier. */
	public AdvanceBlockBind binding;
	/** The left side of the binding. */
	public AdvanceType left;
	/** The right side of the binding. */
	public AdvanceType right;
	/**
	 * Constructor.
	 * <p>The union of two types could not be created.</p>
	 * @param binding the actual binding causing the problem
	 * @param left the left side of the binding
	 * @param right the right side of the binding
	 */
	public CombinedTypeError(AdvanceBlockBind binding, AdvanceType left, AdvanceType right) {
		this.binding = binding;
		this.left = left;
		this.right = right;
	}
	@Override
	public String toString() {
		return "Combined type could not be created for wire " + binding.id + ": " + left + " vs. " + right;
	}
	/** Empty constructor. */
	public CombinedTypeError() {
		
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
		destination.set("type", getClass().getSimpleName());
		destination.set("message", toString());
		binding.save(destination.add("binding"));
		left.save(destination.add("left-type"));
		right.save(destination.add("right-type"));
	}
	/** Creates a new instance of this class. */
	public static final Func0<CombinedTypeError> CREATOR = new Func0<CombinedTypeError>() {
		@Override
		public CombinedTypeError invoke() {
			return new CombinedTypeError();
		}
	};
	/**
	 * Register this class in the supplied map.
	 * @param map the map from error type name to function to create an instance
	 */
	public static void register(Map<String, Func0<? extends AdvanceCompilationError>> map) {
		map.put(CombinedTypeError.class.getSimpleName(), CREATOR);
	}
	@Override
	public AdvanceBlockBind binding() {
		return binding;
	}
	@Override
	public Iterable<AdvanceType> types() {
		return Arrays.asList(left, right);
	}
}
