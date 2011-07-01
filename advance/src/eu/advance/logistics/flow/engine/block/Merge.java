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

import hu.akarnokd.reactive4java.base.Scheduler;
import hu.akarnokd.reactive4java.reactive.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.AdvancePort;
import eu.advance.logistics.flow.engine.SchedulerPreference;
import eu.advance.logistics.flow.model.AdvanceCompositeBlock;
import eu.advance.logistics.xml.typesystem.XElement;

/**
 * A block that merges the incoming values of its parameters but without waiting for both of them.
 * @author karnokd, 2011.07.01.
 */
public class Merge extends AdvanceBlock {
	
	/**
	 * Constuctor.
	 * @param gid the block global id
	 * @param parent the parent composite block
	 * @param name the block's type name
	 * @param schedulerPreference the scheduler preference
	 */
	public Merge(int gid, AdvanceCompositeBlock parent, String name,
			SchedulerPreference schedulerPreference) {
		super(gid, parent, name, schedulerPreference);
	}

	
	@Override
	protected void invoke(Map<String, XElement> params) {
		// The default zip-behavior is ignored
	}
	@Override
	protected Observer<Void> runReactiveBlock(Scheduler scheduler,
			List<AdvancePort> reactivePorts) {
		functionClose = Reactive.observeOn(
				Reactive.merge(reactivePorts), scheduler).register(new InvokeObserver<XElement>() {
			@Override
			public void next(XElement value) {
				dispatchOutput(Collections.singletonMap("out", value));
			}
		});
		return new RunObserver();
	}
}
