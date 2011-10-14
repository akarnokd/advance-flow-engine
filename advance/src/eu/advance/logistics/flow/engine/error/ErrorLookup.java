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

import com.google.common.collect.Maps;

import eu.advance.logistics.flow.engine.model.AdvanceCompilationError;

/**
 * Utility class to store and manage creator functions for various compilation error classes.
 * @author karnokd, 2011.10.04.
 */
public final class ErrorLookup {
	/**
	 * Utility class. 
	 */
	private ErrorLookup() {
	}
	/** The map from error type to error class. */
	protected static final Map<String, Func0<? extends AdvanceCompilationError>> ERROR_LOOKUP;
	/** Initialize the map. */
	static {
		ERROR_LOOKUP = Maps.newHashMap();
		CombinedTypeError.register(ERROR_LOOKUP);
		ConcreteVsParametricTypeError.register(ERROR_LOOKUP);
		ConstantOutputError.register(ERROR_LOOKUP);
		DestinationToCompositeInputError.register(ERROR_LOOKUP);
		DestinationToCompositeOutputError.register(ERROR_LOOKUP);
		DestinationToOutputError.register(ERROR_LOOKUP);
		IncompatibleBaseTypesError.register(ERROR_LOOKUP);
		IncompatibleTypesError.register(ERROR_LOOKUP);
		MissingDestinationError.register(ERROR_LOOKUP);
		MissingDestinationPortError.register(ERROR_LOOKUP);
		MissingSourceError.register(ERROR_LOOKUP);
		MissingSourcePortError.register(ERROR_LOOKUP);
		MultiInputBindingError.register(ERROR_LOOKUP);
		SourceToCompositeInputError.register(ERROR_LOOKUP);
		SourceToCompositeOutputError.register(ERROR_LOOKUP);
		SourceToInputBindingError.register(ERROR_LOOKUP);
		TypeMismatchError.register(ERROR_LOOKUP);
		MissingBlockError.register(ERROR_LOOKUP);
	}
	/**
	 * Locate the error via its simple class name.
	 * @param name the class.getSimpleName()
	 * @return the creator function
	 */
	public static Func0<? extends AdvanceCompilationError> get(String name) {
		return ERROR_LOOKUP.get(name);
	}
}
