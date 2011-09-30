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

package eu.advance.logistics.flow.engine;

import hu.akarnokd.reactive4java.base.Func0;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.advance.logistics.flow.engine.error.AdvanceCompilationError;
import eu.advance.logistics.flow.engine.error.CombinedTypeError;
import eu.advance.logistics.flow.engine.error.ConcreteVsParametricTypeError;
import eu.advance.logistics.flow.engine.error.ConstantOutputError;
import eu.advance.logistics.flow.engine.error.DestinationToCompositeInputError;
import eu.advance.logistics.flow.engine.error.DestinationToCompositeOutputError;
import eu.advance.logistics.flow.engine.error.DestinationToOutputError;
import eu.advance.logistics.flow.engine.error.GeneralCompilationError;
import eu.advance.logistics.flow.engine.error.IncompatibleBaseTypesError;
import eu.advance.logistics.flow.engine.error.IncompatibleTypesError;
import eu.advance.logistics.flow.engine.error.MissingDestinationError;
import eu.advance.logistics.flow.engine.error.MissingDestinationPortError;
import eu.advance.logistics.flow.engine.error.MissingSourceError;
import eu.advance.logistics.flow.engine.error.MissingSourcePortError;
import eu.advance.logistics.flow.engine.error.MultiInputBindingError;
import eu.advance.logistics.flow.engine.error.SourceToCompositeInputError;
import eu.advance.logistics.flow.engine.error.SourceToCompositeOutputError;
import eu.advance.logistics.flow.engine.error.SourceToInputBindingError;
import eu.advance.logistics.flow.engine.error.TypeMismatchError;
import eu.advance.logistics.flow.engine.model.AdvanceType;
import eu.advance.logistics.flow.engine.model.XSerializable;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Record to store compiler errors, warnings and the computed types of various wires.
 * @author karnokd, 2011.09.28.
 */
public class AdvanceCompilationResult implements XSerializable {
	/**
	 * The list of compilation errors.
	 */
	public final List<AdvanceCompilationError> errors = Lists.newArrayList();
	/**
	 * The inferred wire types.
	 */
	public final Map<String, AdvanceType> wireTypes = Maps.newHashMap();
	/** The map from error type to error class. */
	protected static final Map<String, Func0<? extends AdvanceCompilationError>> ERROR_LOOKUP;
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
	}
	/** Creates a new instance of this class. */
	public static final Func0<AdvanceCompilationResult> CREATOR = new Func0<AdvanceCompilationResult>() {
		@Override
		public AdvanceCompilationResult invoke() {
			return new AdvanceCompilationResult();
		}
	};
	@Override
	public void load(XElement source) {
		for (XElement e : source.childElement("errors").childrenWithName("error")) {
			Func0<? extends AdvanceCompilationError> errFun = ERROR_LOOKUP.get(source.get("type"));
			if (errFun == null) {
				errors.add(new GeneralCompilationError(e));
			} else {
				AdvanceCompilationError err = errFun.invoke();
				err.load(e);
				errors.add(err);
			}
			
		}
		for (XElement e : source.childElement("wire-types").childrenWithName("wire-type")) {
			AdvanceType at = new AdvanceType();
			at.load(e.children().get(0));
			wireTypes.put(e.get("wire-id"), at);
		}
	}
	@Override
	public void save(XElement destination) {
		XElement errors = destination.add("errors");
		for (AdvanceCompilationError e : this.errors) {
			e.save(errors.add("error"));
		}
		XElement types = destination.add("wire-types");
		for (Map.Entry<String, AdvanceType> e : this.wireTypes.entrySet()) {
			XElement wt = types.add("wire-type");
			wt.set("wire-id", e.getKey());
			e.getValue().save(wt.add("type"));
		}
	}
}
