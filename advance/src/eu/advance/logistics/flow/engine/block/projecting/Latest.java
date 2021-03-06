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

package eu.advance.logistics.flow.engine.block.projecting;

import hu.akarnokd.reactive4java.base.Action1;
import hu.akarnokd.reactive4java.base.Observer;
import hu.akarnokd.utils.xml.XNElement;

import java.util.concurrent.atomic.AtomicReference;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;

/**
 * Remembers the last values of each input and fires the current values whenever one changes.
 * @author karnokd, 2012.02.27.
 */
@Block(id = "Latest", 
category = "projection", scheduler = "NOW", 
description = "Remembers the last values of each input and fires the current values whenever one changes.",
parameters = { "T", "U" })
public class Latest extends AdvanceBlock {
	/** The primary in. */
	@Input("?T")
	protected static final String IN1 = "in1";
	/** The secondary in. */
	@Input("?U")
	protected static final String IN2 = "in2";
	/** The primary out. */
	@Output("?T")
	protected static final String OUT1 = "out1";
	/** The secondar out. */
	@Output("?U")
	protected static final String OUT2 = "out2";
	/** The last value. */
	protected final AtomicReference<XNElement> ref1 = new AtomicReference<XNElement>();
	/** The last value. */
	protected final AtomicReference<XNElement> ref2 = new AtomicReference<XNElement>();
	@Override
	protected synchronized void invoke() {
		XNElement i1 = ref1.get();
		XNElement i2 = ref2.get();
		
		if (i1 != null && i2 != null) {
			dispatch(OUT1, i1);
			dispatch(OUT2, i2);
		}
	}

	@Override
	public Observer<Void> run() {
		RunObserver run = new RunObserver();
		
		observeInput(IN1, new Action1<XNElement>() {
			@Override
			public void invoke(XNElement value) {
				ref1.set(value);
				Latest.this.invoke();
			}
		}, run);
		
		observeInput(IN2, new Action1<XNElement>() {
			@Override
			public void invoke(XNElement value) {
				ref2.set(value);
				Latest.this.invoke();
			}
		}, run);
		
		return run;
	}
	
}
