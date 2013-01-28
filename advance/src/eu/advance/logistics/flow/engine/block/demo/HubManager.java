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
package eu.advance.logistics.flow.engine.block.demo;

import hu.akarnokd.reactive4java.base.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.google.common.collect.Sets;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.AdvanceData;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.runtime.ConstantPort;
import eu.advance.logistics.flow.engine.runtime.Port;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 *.
 * @author szmarcell
 */
@Block(description = "Superwises the incoming trucks and creates statistics.", category = "demo")
public class HubManager extends AdvanceBlock {
	/** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(HubManager.class.getName());
    /** Incoming pallets. */
    @Input("advance:truck")
    protected static final String TRUCK = "truck";
    /** The capacity limit. */
    @Input("advance:integer")
    protected static final String CRITICAL_LOAD_LIMIT = "critical-load-limit";
    /** The trigger signal for the end of shift. */
    @Input("advance:object")
    protected static final String END_OF_SHIFT = "end-of-shift";
    /** Current load of each bays. */
    @Output("advance:collection<advance:integer>")
    protected static final String CURRENT_LOAD = "current-load";
    /** Trigger output to singla a critical load. */
    @Output("advance:boolean")
    protected static final String CRITICAL_LOAD_TRIGGER = "critical-load-trigger";
    /** The consumption throughput. */
    @Output("advance:collection<advance:integer>")
    protected static final String THROUGHPUT = "throughput";
    /** The critical load limit. */
    protected final AtomicInteger criticalLoadLimit = new AtomicInteger(100);
    /** The random number generator. */
    protected ThreadLocal<Random> rnd = new ThreadLocal<Random>() {
    	@Override
    	protected Random initialValue() {
    		return new Random();
    	}
    };
    @Override
    protected void invoke() {
    	// solved otherwise
    }
    @Override
    public Observer<Void> run() {
    	Port<XElement, AdvanceType> cll = getInput(CRITICAL_LOAD_LIMIT);
    	if (cll instanceof ConstantPort) {
    		criticalLoadLimit.set(resolver().getInt(((ConstantPort<XElement, AdvanceType>)cll).value));
    	} else {
    		addCloseable(Reactive.observeOn(cll, scheduler()).register(new Observer<XElement>() {
    			@Override
    			public void next(XElement value) {
    				criticalLoadLimit.set(resolver().getInt(value));    				
    			}
    			@Override
    			public void error(Throwable ex) {
    				// ignored
    			}
    			@Override
    			public void finish() {
    				// ignored
    			}
    		}));
    	}
    	addCloseable(Reactive.observeOn(getInput(END_OF_SHIFT), scheduler()).register(new Observer<XElement>() {
    		@Override
    		public void next(XElement value) {
    			doEndOfShift();
    		}
    		@Override
    		public void error(Throwable ex) {
				// ignored
    		}
    		@Override
    		public void finish() {
				// ignored
    		}
    	}));
    	addCloseable(Reactive.observeOn(getInput(TRUCK), scheduler()).register(new Observer<XElement>() {
    		@Override
    		public void next(XElement value) {
    			doTruckArrived(value);
    		}
    		@Override
    		public void error(Throwable ex) {
    			// ignored
    		}
    		@Override
    		public void finish() {
    			// ignored
    		}
    	}));
    	return new RunObserver();
    }
    /** Initiate the end-of-shift operations. */
    synchronized void doEndOfShift() {
    	DemoDatastore ds = DemoDatastore.instance();
    	int n = ds.getMaxDestinations();
    	
    	XElement throughput = resolver().create();
   	
    	for (int i = 0; i < n; i++) {
    		int bayCount = ds.bayCount(i);
    		int delta = rnd.get().nextInt(bayCount + 1);
    		ds.removeFromBay(i, delta);
    		throughput.add(AdvanceData.rename(resolver().create(delta), "item"));
    	}    	
    	dispatch(THROUGHPUT, throughput);
    	sendStatus(ds);
    }
    /** 
     * Initiate the actions when a truck arrived.
     * @param truck the truck object 
     */
    synchronized void doTruckArrived(XElement truck) {
    	DemoDatastore ds = DemoDatastore.instance();
    	Set<Integer> baysAffected = Sets.newHashSet();
    	for (XElement pallet : truck.childElement("pallets").childrenWithName("item")) {
    		baysAffected.add(ds.addToBay(AdvanceData.unrename(pallet)));
    	}
    	sendStatus(ds);
    }
	/**
	 * Send out the status of the bays.
	 * @param ds the demo datastore
	 */
	public void sendStatus(DemoDatastore ds) {
		int cll = criticalLoadLimit.get();
    	int n = ds.getMaxDestinations();
    	XElement bayLoads = resolver().create();
    	boolean alert = false;
    	for (int i = 0; i < n; i++) {
    		int bayCount = ds.bayCount(i);
    		if (bayCount >= cll) {
    			alert = true;
    		}
    		bayLoads.add(AdvanceData.rename(resolver().create(bayCount), "item"));
    	}
    	dispatch(CURRENT_LOAD, bayLoads);
    	dispatch(CRITICAL_LOAD_TRIGGER, resolver().create(alert));
	}
}
