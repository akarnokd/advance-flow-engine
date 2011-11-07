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

import java.io.Closeable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.io.Closeables;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceSchedulerPreference;
import eu.advance.logistics.flow.engine.xml.typesystem.XData;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Represents a Timer which periodically relays the last value of its {@code in} parameter.
 * @author akarnokd, 2011.10.27.
 */
@Block(parameters = { "T" }, description = "Periodically emits the last value received on its 'in' parameter.")
public class Timer extends AdvanceBlock {
	/** Delay. */
    @Input("advance:integer")
    private static final String DELAY = "delay";
    /** In. */
    @Input("?T")
    private static final String IN = "in";
    /** Out. */
    @Output("?T")
    private static final String OUT = "out";
    
	/** The scheduled repeating timer. */
	protected Closeable timer;
	/** The current interval. */
	protected int interval = -1;
	/** The last value to submit. */
	protected final AtomicReference<XElement> last = new AtomicReference<XElement>();
	/**
	 * Constructor.
	 * @param id the block global id
	 * @param parent the parent composite block
	 * @param schedulerPreference the scheduler preference
	 */
	public Timer(String id, AdvanceCompositeBlock parent, 
			AdvanceSchedulerPreference schedulerPreference) {
		super(id, parent, schedulerPreference);
	}
	@Override
	protected void invoke(Map<String, XElement> params) {
		final int delay = XData.getInt(params.get(DELAY));
		last.set(params.get(IN));
		if (delay != interval) {
			interval = delay;
			startTimer();
		}
	}
	/**
	 * Start the timer.
	 */
	public void startTimer() {
		Closeables.closeQuietly(timer);
		timer = scheduler().schedule(new Runnable() {
			@Override
			public void run() {
				XElement e = last.get();
				if (e != null) {
					dispatchOutput(Collections.singletonMap(OUT, e));
				}
			}
		}, interval, interval, TimeUnit.MILLISECONDS);
	}
	@Override
	public void done() {
		Closeables.closeQuietly(timer);
		super.done();
	}
}
