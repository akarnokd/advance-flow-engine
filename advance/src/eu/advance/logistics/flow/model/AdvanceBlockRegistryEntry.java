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

package eu.advance.logistics.flow.model;

import java.util.List;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.SchedulerPreference;
import eu.advance.logistics.xml.typesystem.XElement;

/**
 * The block registry entry of the block-registry.xml and xsd.
 * @author karnokd, 2011.07.05.
 */
public class AdvanceBlockRegistryEntry extends AdvanceBlockDescription {
	/** The implementation class. */
	public String clazz;
	/** The preferred scheduler. */
	public SchedulerPreference scheduler;
	@Override
	public void load(XElement root) {
		super.load(root);
		clazz = root.get("class");
		String s = root.get("scheduler");
		if (s != null) {
			scheduler = SchedulerPreference.valueOf(s);
		} else {
			scheduler = SchedulerPreference.CPU;
		}
	}
	/**
	 * Parse an XML tree which contains block registry descriptions as a list.
	 * @param root the root element conforming the {@code block-registry.xsd}.
	 * @return the list of block registry definitions
	 */
	public static List<AdvanceBlockRegistryEntry> parseRegistry(XElement root) {
		List<AdvanceBlockRegistryEntry> result = Lists.newArrayList();
		
		for (XElement e : root.childrenWithName("block-description")) {
			AdvanceBlockRegistryEntry abd = new AdvanceBlockRegistryEntry();
			abd.load(e);
			result.add(abd);
		}
		
		return result;
	}
}
