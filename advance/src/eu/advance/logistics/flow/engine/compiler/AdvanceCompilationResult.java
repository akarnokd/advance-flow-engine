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

package eu.advance.logistics.flow.engine.compiler;

import hu.akarnokd.reactive4java.base.Func0;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.error.CombinedTypeError;
import eu.advance.logistics.flow.engine.error.ConcreteVsParametricTypeError;
import eu.advance.logistics.flow.engine.error.ErrorLookup;
import eu.advance.logistics.flow.engine.error.GeneralCompilationError;
import eu.advance.logistics.flow.engine.error.HasBinding;
import eu.advance.logistics.flow.engine.error.IncompatibleBaseTypesError;
import eu.advance.logistics.flow.engine.error.IncompatibleTypesError;
import eu.advance.logistics.flow.engine.error.TypeArgumentCountError;
import eu.advance.logistics.flow.engine.inference.InferenceResult;
import eu.advance.logistics.flow.engine.model.AdvanceCompilationError;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockBind;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.flow.engine.xml.XSerializable;

/**
 * Record to store compiler errors, warnings and the computed types of various wires.
 * @author akarnokd, 2011.09.28.
 */
public class AdvanceCompilationResult implements XSerializable, InferenceResult<AdvanceType, AdvanceBlockBind> {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceCompilationResult.class);
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
			String errorType = e.get("type");
			Func0<? extends AdvanceCompilationError> errFun = ErrorLookup.get(errorType);
			if (errFun == null) {
				if (!tryLoadError(errorType, e)) {
					errors.add(new GeneralCompilationError(e));
				}
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
	/**
	 * Try instantiating the given error class and load its contents.
	 * @param errorClass the error class
	 * @param data the error data XML
	 * @return true if successful
	 */
	boolean tryLoadError(String errorClass, XElement data) {
		try {
			Class<?> clazz = Class.forName(errorClass);
			if (AdvanceCompilationError.class.isAssignableFrom(clazz)) {
				AdvanceCompilationError o = AdvanceCompilationError.class.cast(clazz.newInstance());
				o.load(data);
				errors.add(o);
				return true;
			}
		} catch (ClassNotFoundException ex) {
			LOG.error(ex.toString(), ex);
		} catch (InstantiationException ex) {
			LOG.error(ex.toString(), ex);
		} catch (IllegalAccessException ex) {
			LOG.error(ex.toString(), ex);
		}
		return false;
	}
	@Override
	public void save(XElement destination) {
		XElement errors = destination.add("errors");
		for (Object e : this.errors) {
			XElement xerror = errors.add("error");
			if (e instanceof XSerializable) {
				((XSerializable)e).save(xerror);
			} else {
				xerror.set("type", e.getClass().getName());
				xerror.set("message", e.toString());
			}
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
	@Override
	public AdvanceType getType(AdvanceBlockBind wire) {
		return wireTypes.get(wire.id);
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
	 * Add the list of errors.
	 * @param errors the list of errors
	 */
	public void addError(@NonNull AdvanceCompilationError... errors) {
		addError(Arrays.asList(errors));
	}
	/**
	 * Add a sequence compilation error.
	 * @param errors the compilation error
	 */
	public void addError(@NonNull Iterable<AdvanceCompilationError> errors) {
		for (AdvanceCompilationError e : errors) {
			this.errors.add(e);
		}
	}
	@Override
	public void setType(@NonNull AdvanceBlockBind wire, AdvanceType type) {
		wireTypes.put(wire.id, type);
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
	@Override
	public void errorArgumentCount(AdvanceType type1, AdvanceType type2,
			AdvanceBlockBind wire) {
		addError(new TypeArgumentCountError(wire, type1, type2));
	}
	@Override
	public void errorCombinedType(AdvanceType type1, AdvanceType type2,
			AdvanceBlockBind wire) {
		addError(new CombinedTypeError(wire, type1, type2));
	}
	@Override
	public void errorConcreteVsParametricType(AdvanceType type1,
			AdvanceType type2, AdvanceBlockBind wire) {
		addError(new ConcreteVsParametricTypeError(wire, type1, type2));
	}
	@Override
	public void errorIncompatibleBaseTypes(AdvanceType type1,
			AdvanceType type2, AdvanceBlockBind wire) {
		addError(new IncompatibleBaseTypesError(wire, type1, type2));
	}
	@Override
	public void errorIncompatibleTypes(AdvanceType type1, AdvanceType type2,
			AdvanceBlockBind wire) {
		addError(new IncompatibleTypesError(wire, type1, type2));
	}
}
