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

package eu.advance.logistics.flow.engine.model.rt;

import hu.akarnokd.reactive4java.base.Func0;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.advance.logistics.flow.engine.error.ErrorLookup;
import eu.advance.logistics.flow.engine.error.GeneralCompilationError;
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
	public final List<AdvanceCompilationError> errors = Lists.newArrayList();
	/**
	 * The inferred wire types.
	 */
	public final Map<String, AdvanceType> wireTypes = Maps.newHashMap();
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
			Func0<? extends AdvanceCompilationError> errFun = ErrorLookup.get(source.get("type"));
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
}
