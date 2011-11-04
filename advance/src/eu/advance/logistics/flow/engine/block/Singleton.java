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

import java.util.Map;

import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceSchedulerPreference;
import eu.advance.logistics.flow.engine.xml.typesystem.XData;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * @author akarnokd, 2011.11.04.
 *
 */
public class Singleton extends AdvanceBlock {
	/**
	 * Constructor.
	 * @param id the block global id
	 * @param parent the parent composite block
	 * @param schedulerPreference the scheduler preference
	 */
	public Singleton(String id, AdvanceCompositeBlock parent, 
			AdvanceSchedulerPreference schedulerPreference) {
		super(id, parent, schedulerPreference);
	}

	@Override
	protected void invoke(Map<String, XElement> params) {
		dispatch("out", XData.createSingleton(params.get("in")));
	}

}
