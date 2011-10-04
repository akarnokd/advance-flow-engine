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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.SchedulerPreference;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * A simple generic block that reserves the children of the supplied advance:collection type object.
 * @author karnokd, 2011.07.01.
 */
public class Reverse extends AdvanceBlock {
	
	/**
	 * Constuctor.
	 * @param gid the block global id
	 * @param parent the parent composite block
	 * @param name the block's type name
	 * @param schedulerPreference the scheduler preference
	 */
	public Reverse(int gid, AdvanceCompositeBlock parent, String name,
			SchedulerPreference schedulerPreference) {
		super(gid, parent, name, schedulerPreference);
	}

	@Override
	protected void invoke(Map<String, XElement> params) {
		XElement in = params.get("in");
		XElement out = in.copy();
		// locate the place where the item begins
		int idx = Iterables.indexOf(out, new Predicate<XElement>() {
			@Override
			public boolean apply(XElement input) {
				return input.name.equals("item");
			}
		});
		if (idx >= 0) {
			// reverse by removing the originals
			Iterator<XElement> it = out.childrenWithName("item").iterator();
			LinkedList<XElement> list = Lists.newLinkedList();
			while (it.hasNext()) {
				list.addFirst(it.next());
				it.remove();
			}
			// place them back starting from the original first
			out.children().addAll(idx, list);
		}
		dispatchOutput(Collections.singletonMap("out", out));
	}

}
