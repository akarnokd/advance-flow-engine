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

package eu.advance.logistics.flow.engine.model.rt;

import hu.akarnokd.reactive4java.base.Func0;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.error.ErrorLookup;
import eu.advance.logistics.flow.engine.error.GeneralCompilationError;
import eu.advance.logistics.flow.engine.error.HasBinding;
import eu.advance.logistics.flow.engine.model.AdvanceCompilationError;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializable;

/**
 * Record to store compiler errors, warnings and the computed types of various wires.
 * @author akarnokd, 2011.09.28.
 */
public class AdvanceCompilationResult implements XSerializable {
	/**
	 * The list of compilation errors.
	 */
	private final List<AdvanceCompilationError> errors = Lists.newArrayList();
	/**
	 * The inferred wire types.
	 */
	private final Map<String, AdvanceType> wireTypes = Maps.newHashMap();
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
			Func0<? extends AdvanceCompilationError> errFun = ErrorLookup.get(e.get("type"));
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
	/** @return true if the compilation finished without error. */
	public boolean success() {
		return errors.isEmpty();
	}
	/**
	 * Add the contents of the another compilation result object to this.
	 * @param other the other compulation result
	 */
	public void add(AdvanceCompilationResult other) {
		wireTypes.putAll(other.wireTypes);
		errors.addAll(other.errors);
	}
	/**
	 * Returns the type of the given wire.
	 * @param bindId the wire bind id
	 * @return the type or null if not available
	 */
	public AdvanceType getType(String bindId) {
		return wireTypes.get(bindId);
	}
	/**
	 * Returns a list of the compilation errors associated with the given wire binding.
	 * @param bindId the wire bind id
	 * @return the list of compilation errors
	 */
	@NonNull
	public List<AdvanceCompilationError> getErrors(@NonNull String bindId) {
		List<AdvanceCompilationError> result = Lists.newArrayList();
        for (AdvanceCompilationError e : errors) {
            if (e instanceof HasBinding) {
            	HasBinding hb = (HasBinding)e;
                if (hb.binding().id.equals(bindId)) {
                	result.add(e);
                }
            }
        }
        return result;
	}
	/**
	 * Add a compilation error.
	 * @param error the compilation error
	 */
	public void addError(@NonNull AdvanceCompilationError error) {
		errors.add(error);
	}
	/**
	 * Sets the wire type.
	 * @param wireId the wire id
	 * @param type the wire type
	 */
	public void setType(@NonNull String wireId, AdvanceType type) {
		wireTypes.put(wireId, type);
	}
	/**
	 * Returns the wire type values.
	 * @return the collection of types
	 */
	public Collection<AdvanceType> wireTypes() {
		return wireTypes.values();
	}
	/**
	 * Returns the list of compilation errors.
	 * @return the list of compilation errors
	 */
	public List<AdvanceCompilationError> errors() {
		return Lists.newArrayList(errors);
	}
}
