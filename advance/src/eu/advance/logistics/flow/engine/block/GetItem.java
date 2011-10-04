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

package eu.advance.logistics.flow.engine.block;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Iterables;

import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.SchedulerPreference;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * A simple generic block that extracts an item from an advance:collection type construct.
 * @author karnokd, 2011.07.01.
 */
public class GetItem extends AdvanceBlock {
	
	/**
	 * Constuctor.
	 * @param gid the block global id
	 * @param parent the parent composite block
	 * @param name the block's type name
	 * @param schedulerPreference the scheduler preference
	 */
	public GetItem(int gid, AdvanceCompositeBlock parent, String name,
			SchedulerPreference schedulerPreference) {
		super(gid, parent, name, schedulerPreference);
	}

	@Override
	protected void invoke(Map<String, XElement> params) {
		XElement in = params.get("in");
		int index = Integer.parseInt(params.get("index").content);
		dispatchOutput(Collections.singletonMap("out", Iterables.get(in.childrenWithName("item"), index)));
	}

}
