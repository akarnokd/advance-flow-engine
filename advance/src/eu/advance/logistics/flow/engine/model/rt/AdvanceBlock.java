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

package eu.advance.logistics.flow.engine.model.rt;

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.reactive4java.base.Scheduler;
import hu.akarnokd.reactive4java.interactive.Interactive;
import hu.akarnokd.reactive4java.reactive.DefaultObservable;
import hu.akarnokd.reactive4java.reactive.Observable;
import hu.akarnokd.reactive4java.reactive.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.fd.AdvanceConstantBlock;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * The generic ADVANCE block.
 * @author akarnokd, 2011.06.22.
 */
public abstract class AdvanceBlock {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceBlock.class);
	/** The global identifier of this block. */ 
	public final String id;
	/** The input ports. */
	public final List<AdvancePort> inputs;
	/** The output ports. */
	public final List<AdvanceBlockPort> outputs;
	/** The parent composite block. */
	public final AdvanceCompositeBlock parent;
	/** The original description of this block. */
	protected AdvanceBlockDescription description;
	/** The block diagnostic observable. */
	protected DefaultObservable<AdvanceBlockDiagnostic> diagnostic;
	/** List of functions to close when the block is terminated via done(). */
	protected final List<Closeable> functionClose;
	/** The preferred scheduler type. Filled in by the AdvanceBlockLookup.create(). */
	public final AdvanceSchedulerPreference schedulerPreference;
	/** The scheduler instance to use. Filled in by the AdvanceCompiler.run(). */
	private Scheduler scheduler;
	/**
	 * Constructor.  
	 * @param id The global identifier of this block. 
	 * @param parent the parent composite block.
	 * @param schedulerPreference the scheduler preference
	 */
	public AdvanceBlock(String id, 
			AdvanceCompositeBlock parent, 
			AdvanceSchedulerPreference schedulerPreference) {
		this.id = id;
		this.parent = parent;
		this.schedulerPreference = schedulerPreference;
		this.inputs = Lists.newArrayList();
		this.outputs = Lists.newArrayList();
		this.functionClose = Collections.synchronizedList(Lists.<Closeable>newArrayList());
	}
	/** 
	 * Initialize the block with the given definition and body function.
	 * @param desc the description of the block
	 * @param constantParams the map of those parameters who have a constant input instead of other output ports
	 */
	public void init(
			AdvanceBlockDescription desc, 
			final Map<String, AdvanceConstantBlock> constantParams) {
		this.description = desc;
		for (AdvanceBlockParameterDescription in : desc.inputs.values()) {
			AdvanceConstantBlock cb = constantParams.get(in.id); 
			if (cb == null) {
				AdvanceBlockPort p = new AdvanceBlockPort(this, in.id);
				p.init();
				inputs.add(p);
			} else {
				AdvanceConstantPort p = new AdvanceConstantPort(this, in.id);
				p.value = cb.value;
				inputs.add(p);
			}
		}
		for (AdvanceBlockParameterDescription out : desc.outputs.values()) {
			AdvanceBlockPort p = new AdvanceBlockPort(this, out.id);
			p.init();
			outputs.add(p);
		}
		diagnostic = new DefaultObservable<AdvanceBlockDiagnostic>(false, false);
	}
	/**
	 * @return Retrieves a list of ports which are of reactive nature, e.g., they are not constants.
	 */
	public List<AdvancePort> getReactivePorts() {
		return Lists.newArrayList(Interactive.where(inputs, new Func1<AdvancePort, Boolean>() {
			@Override
			public Boolean invoke(AdvancePort param1) {
				return !(param1 instanceof AdvanceConstantPort);
			}
		}));
	}
	/**
	 * @return Retrieves a list of ports which are of reactive nature, e.g., they are not constants.
	 */
	public List<AdvancePort> getConstantPorts() {
		return Lists.newArrayList(Interactive.where(inputs, new Func1<AdvancePort, Boolean>() {
			@Override
			public Boolean invoke(AdvancePort param1) {
				return (param1 instanceof AdvanceConstantPort);
			}
		}));
	}
	/** 
	 * Schedule the execution of the body function.
	 * @param scheduler the scheduler based on the block's preference 
	 * @return the observer to trigger in the run phase
	 */
	public Observer<Void> run(final Scheduler scheduler) {
		this.scheduler = scheduler;
		List<AdvancePort> reactivePorts = getReactivePorts(); 
		
		if (inputs.size() == 0 || reactivePorts.size() == 0) {
			return runConstantBlock(scheduler);
		}
		return runReactiveBlock(scheduler, reactivePorts);
	}
	/**
	 * <p>By default bind the reactive ports of the block with a combiner function which will
	 * execute the body function if all of the inputs are available.</p>
	 * <p>Override this method if you want different input-parameter reaction.</p>
	 * @param scheduler the scheduler to be used by the body function
	 * @param reactivePorts the list of the reactive ports to use.
	 * @return the observer to trigger the execution in the run phase but it is empty
	 */
	protected Observer<Void> runReactiveBlock(final Scheduler scheduler,
			List<AdvancePort> reactivePorts) {
		functionClose.add(Reactive.observeOn(
				Reactive.combine(reactivePorts), scheduler).register(new InvokeObserver<List<XElement>>() {
			@Override
			public void next(List<XElement> value) {
				invokeBody(value, scheduler);
			}
		}));
		return new RunObserver();
	}
	/**
	 * The observer to invoke in the body function.
	 * Contains the default implementations for error() and finish() to perform a diagnostic report.
	 * @author akarnokd, 2011.07.01.
	 * @param <T> the observed value type 
	 */
	public abstract class InvokeObserver<T> implements Observer<T> {
		
		@Override
		public void error(Throwable ex) {
			diagnostic.next(new AdvanceBlockDiagnostic("", description.id, Option.<AdvanceBlockState>error(ex)));
		}

		@Override
		public void finish() {
			LOG.info("Finish? " + description.id);
		}
	}
	/** 
	 * The observer returned by the run functions. 
	 * Contains the default implementations for error() and finish() to perform a diagnostic report. 
	 */
	public class RunObserver implements Observer<Void> {
		@Override
		public void next(Void value) {
			// no operation in this case
		}

		@Override
		public void error(Throwable ex) {
			diagnostic.next(new AdvanceBlockDiagnostic("", description.id, Option.<AdvanceBlockState>error(ex)));
		}

		@Override
		public void finish() {
			LOG.info("Finish? " + description.id);
		}
	}
	/**
	 * Wraps the body function into a callback-style invocation scheme for constant or no-input blocks
	 * to execute them immediately when the flow begins to execute.
	 * @param scheduler the scheduler to use for the invocation
	 * @return the observer to trigger the execution
	 */
	protected Observer<Void> runConstantBlock(final Scheduler scheduler) {
		return new RunObserver() {
			@Override
			public void next(Void value) {
				functionClose.add(scheduler.schedule(new Runnable() {
					@Override
					public void run() {
						// no reactive parameters
						invokeBody(Lists.<XElement>newArrayList(), scheduler);
					}
				}));
			}
		};
	}
	
	/**
	 * Invoke the body function when all elements are available.
	 * @param value the list of reactive parameters in the same order as defined by the block-description.
	 * @param scheduler the scheduler
	 */
	void invokeBody(List<XElement> value, Scheduler scheduler) {
		diagnostic.next(new AdvanceBlockDiagnostic("", description.id, Option.some(AdvanceBlockState.START)));
		try {
			// prepare input parameters
			Map<String, XElement> funcIn = Maps.newHashMap();
			int j = 0;
			for (int i = 0; i < inputs.size(); i++) {
				AdvancePort p = inputs.get(i);
				if (p instanceof AdvanceConstantPort) {
					funcIn.put(p.name(), ((AdvanceConstantPort) p).value);
				} else {
					funcIn.put(p.name(), value.get(j++));
				}
			}
			
			invoke(funcIn);
			
		} catch (Throwable t) {
			diagnostic.next(new AdvanceBlockDiagnostic("", description.id, Option.<AdvanceBlockState>error(t)));
		}
	}
	/**
	 * Dispatch a set of outputs given by subsequent String and XElement types.
	 * @param nameValuePairs the name-value pairs as {@code String}, {@code XElement} or {@code Iterable<XElement>}
	 */
	protected void dispatch(Object... nameValuePairs) {
		Map<String, Iterable<XElement>> values = Maps.newHashMap();
		for (int i = 0; i < nameValuePairs.length; i += 2) {
			String name = (String)nameValuePairs[i];
			Object value = nameValuePairs[i + 1];
			if (value instanceof XElement) {
				values.put(name, Collections.singleton((XElement)value));
			} else {
				@SuppressWarnings("unchecked")
				Iterable<XElement> v = (Iterable<XElement>)value;
				values.put(name, v);
			}
		}
		dispatch(values);
	}
	/**
	 * Dispatch a value to the named output.
	 * @param outputName the output name
	 * @param value the output value
	 */
	protected void dispatch(String outputName, XElement value) {
		dispatchOutput(Collections.singletonMap(outputName, value));
	}
	/**
	 * Dispatch a value to the named output.
	 * @param outputName the output name
	 * @param values the sequence of values
	 */
	protected void dispatch(String outputName, Iterable<XElement> values) {
		dispatch(Collections.singletonMap(outputName, values));
	}
	/**
	 * Dispatches the given map of output values to various ports.
	 * @param funcOut the function output
	 */
	protected void dispatch(Map<String, ? extends Iterable<XElement>> funcOut) {
		boolean valid = true;
		for (int i = 0; i < outputs.size(); i++) {
			if (!funcOut.containsKey(outputs.get(i).name)) {
				diagnostic.next(new AdvanceBlockDiagnostic("", description.id, Option.<AdvanceBlockState>error(new IllegalArgumentException(outputs.get(i).name + " missing"))));
				LOG.error("missing output '" + outputs.get(i).name + "' at the block type " + description.id);
				valid = false;
			}
		}
		if (valid) {
			for (int i = 0; i < outputs.size(); i++) {
				AdvanceBlockPort p = outputs.get(i);
				for (XElement xe : funcOut.get(p.name)) {
					p.next(xe);
				}
			}						
			diagnostic.next(new AdvanceBlockDiagnostic("", description.id, Option.some(AdvanceBlockState.FINISH)));
		}

	}
	/**
	 * Dispatches the given map of output values to various ports.
	 * @param funcOut the function output
	 */
	protected void dispatchOutput(Map<String, XElement> funcOut) {
		boolean valid = true;
		for (int i = 0; i < outputs.size(); i++) {
			if (!funcOut.containsKey(outputs.get(i).name)) {
				diagnostic.next(new AdvanceBlockDiagnostic("", description.id, Option.<AdvanceBlockState>error(new IllegalArgumentException(outputs.get(i).name + " missing"))));
				LOG.error("missing output '" + outputs.get(i).name + "' at the block type " + description.id);
				valid = false;
			}
		}
		if (valid) {
			for (int i = 0; i < outputs.size(); i++) {
				AdvanceBlockPort p = outputs.get(i);
				p.next(funcOut.get(p.name));
			}						
			diagnostic.next(new AdvanceBlockDiagnostic("", description.id, Option.some(AdvanceBlockState.FINISH)));
		}

	}
	/**
	 * The body function to invoke. Implementation should should invoke the {@link #dispatchOutput(Map)} method
	 * when the computation is over. This may happen synchronously (or asynchronously)
	 * @param params the parameters
	 */
	protected abstract void invoke(Map<String, XElement> params);
	/** Terminate the block. */
	public void done() {
		for (Closeable c : functionClose) {
			try {
				c.close();
			} catch (IOException ex) {
				LOG.info("", ex);
			}
		}
		for (AdvancePort ap : inputs) {
			if (ap instanceof AdvanceBlockPort) {
				((AdvanceBlockPort) ap).disconnect();
			}
		}
		diagnostic.finish();
	}
	/** @return the block's description. */
	public AdvanceBlockDescription getDescription() {
		return description;
	}
	/**
	 * @return the diagnostic port for watch the invocation of the body function
	 */
	public Observable<AdvanceBlockDiagnostic> getDiagnosticPort() {
		return diagnostic;
	}
	/** 
	 * Get the given output port by name.
	 * @param name the port name
	 * @return the block port
	 */
	public AdvanceBlockPort getOutput(@NonNull String name) {
		for (AdvanceBlockPort p : outputs) {
			if (p.name().equals(name)) {
				return p;
			}
		}
		return null;
	}
	/**
	 * Called after the block has been detached and done() has been called in
	 * case the engine is shut down (but not when the realm is stopped).
	 * @return if a non-null value is returned, it indicates the state to be saved
	 */
	public XElement saveState() {
		return null;
	}
	/**
	 * Called before the observer of the run() method is signalled in case
	 * the engine is restarted after a shutdown (but not when the realm was manually stopped)
	 * and the saveState() returned an object.
	 * @param state the state to restore
	 */
	public void restoreState(XElement state) {
		
	}
	/**
	 * The scheduler for this block.
	 * @return the scheduler, null until the run() method is invoked
	 */
	public Scheduler scheduler() {
		return scheduler;
	}
}
