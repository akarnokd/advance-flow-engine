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

package eu.advance.logistics.flow.engine.api.ds;

import hu.akarnokd.reactive4java.base.Func0;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.flow.engine.xml.XSerializable;

/**
 * Describes an input or output port of a running realm.
 * @author karnokd, 2012.02.28.
 */
public class AdvancePortSpecification implements XSerializable {
	/** Is this an input port? */
	public boolean isInput;
	/** The port identifier. */
	public String id;
	/** The port's type. */
	public AdvanceType type;
	/** Creates a new instance of this class. */
	public static final Func0<AdvancePortSpecification> CREATOR = new Func0<AdvancePortSpecification>() {
		@Override
		public AdvancePortSpecification invoke() {
			return new AdvancePortSpecification();
		}
	};
	@Override
	public void load(XElement source) {
		isInput = source.getBoolean("is-input");
		id = source.get("id");
		type = new AdvanceType();
		type.load(source);
	}

	@Override
	public void save(XElement destination) {
		destination.set("is-input", isInput);
		destination.set("id", id);
		type.save(destination);
	}

}
