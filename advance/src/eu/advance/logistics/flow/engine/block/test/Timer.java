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

package eu.advance.logistics.flow.engine.block.test;

import hu.akarnokd.reactive4java.util.Closeables;
import hu.akarnokd.utils.xml.XNElement;

import java.io.Closeable;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * Represents a Timer which periodically relays the last value of its {@code in} parameter.
 * @author akarnokd, 2011.10.27.
 */
@Block(parameters = { "T" }, 
description = "Periodically emits the last value received on its 'in' parameter.",
category = "user-interface")
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
	protected final AtomicReference<XNElement> last = new AtomicReference<XNElement>();
	@Override
	protected void invoke() {
		final int delay = getInt(DELAY);
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
		Closeables.closeSilently(timer);
		timer = scheduler().schedule(new Runnable() {
			@Override
			public void run() {
				XNElement e = last.get();
				if (e != null) {
					dispatchOutput(Collections.singletonMap(OUT, e));
				}
			}
		}, interval, interval, TimeUnit.MILLISECONDS);
	}
	@Override
	public void done() {
		Closeables.closeSilently(timer);
		super.done();
	}
}
