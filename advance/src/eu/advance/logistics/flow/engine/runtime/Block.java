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

package eu.advance.logistics.flow.engine.runtime;

import hu.akarnokd.reactive4java.base.Action1;
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
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * The generic block.
 * @author akarnokd, 2011.06.22.
 * @param <T> the runtime type of the data flowing through
 * @param <X> the the type system type for the block
 * @param <C> the runtime context for the block
 */
public abstract class Block<T, X, C> {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(Block.class);
	/** The input ports. */
	protected final Map<String, Port<T, X>> inputs = Maps.newLinkedHashMap();
	/** The output ports. */
	protected final Map<String, ReactivePort<T, X>> outputs = Maps.newLinkedHashMap();
	/** The block diagnostic observable. */
	protected DefaultObservable<BlockDiagnostic> diagnostic;
	/** List of functions to close when the block is terminated via done(). */
	private final List<Closeable> functionClose = Collections.synchronizedList(Lists.<Closeable>newArrayList());
	/** The block execution context. */
	protected BlockSettings<T, C> settings;
	/** The current parameter map. */
	protected final Map<String, T> params = Maps.newLinkedHashMap();
	/** 
	 * Initialize the block input and output ports, prepare the diagnostic ports.
	 * @param settings the block initialization settings
	 */
	public void init(BlockSettings<T, C> settings) {
		this.settings = settings;
		for (AdvanceBlockParameterDescription in : description().inputs.values()) {
			T cb = settings.constantValues.get(in.id); 
			if (cb == null) {
				ReactivePort<T, X> p = new ReactivePort<T, X>(this, in.id);
				p.init();
				inputs.put(p.name, p);
			} else {
				ConstantPort<T, X> p = new ConstantPort<T, X>(this, in.id);
				p.value = cb;
				inputs.put(p.name, p);
			}
		}
		for (AdvanceBlockParameterDescription out : description().outputs.values()) {
			ReactivePort<T, X> p = new ReactivePort<T, X>(this, out.id);
			p.init();
			outputs.put(p.name, p);
		}
		diagnostic = new DefaultObservable<BlockDiagnostic>(false, false);
	}
	/**
	 * @return Retrieves a list of ports which are of reactive nature, e.g., they are not constants.
	 */
	public List<Port<T, X>> getReactivePorts() {
		return Lists.newArrayList(Interactive.where(inputs.values(), new Func1<Port<T, X>, Boolean>() {
			@Override
			public Boolean invoke(Port<T, X> param1) {
				return !(param1 instanceof ConstantPort);
			}
		}));
	}
	/**
	 * @return Retrieves a list of ports which are of reactive nature, e.g., they are not constants.
	 */
	public List<Port<T, X>> getConstantPorts() {
		return Lists.newArrayList(Interactive.where(inputs.values(), new Func1<Port<T, X>, Boolean>() {
			@Override
			public Boolean invoke(Port<T, X> param1) {
				return (param1 instanceof ConstantPort);
			}
		}));
	}
	/** 
	 * Schedule the execution of the body function.
	 * <p>The block implementations may override this method to perform any
	 * custom registration to the input ports (e.g., observe them independently.)</p>
	 * <p>The returned observer will be invoked once all block run() has been called
	 * in the current realm. This entry point may be used to issue computation in case
	 * a block only uses constant inputs.</p>
	 * @return the observer to trigger in the run phase
	 */
	public Observer<Void> run() {
		List<Port<T, X>> reactivePorts = getReactivePorts(); 
		
		if (inputs.size() == 0 || reactivePorts.size() == 0) {
			return runConstantBlock();
		}
		return runReactiveBlock(reactivePorts);
	}
	/**
	 * <p>By default bind the reactive ports of the block with a combiner function which will
	 * execute the body function if all of the inputs are available.</p>
	 * <p>Override this method if you want different input-parameter reaction.</p>
	 * @param reactivePorts the list of the reactive ports to use.
	 * @return the observer to trigger the execution in the run phase but it is empty
	 */
	protected Observer<Void> runReactiveBlock(
			List<Port<T, X>> reactivePorts) {
		addCloseable(Reactive.observeOn(
				Reactive.combine(reactivePorts), scheduler()).register(new InvokeObserver<List<T>>() {
			@Override
			public void next(List<T> value) {
				invokeBody(value, scheduler());
			}
		}));
		return new RunObserver();
	}
	/**
	 * The observer to invoke in the body function.
	 * Contains the default implementations for error() and finish() to perform a diagnostic report.
	 * @author akarnokd, 2011.07.01.
	 * @param <U> the observed value type 
	 */
	public abstract class InvokeObserver<U> implements Observer<U> {
		
		@Override
		public void error(Throwable ex) {
			diagnostic.next(new BlockDiagnostic("", description().id, Option.<BlockState>error(ex)));
		}

		@Override
		public void finish() {
			LOG.info("Finish? " + description().id);
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
			diagnostic.next(new BlockDiagnostic("", description().id, Option.<BlockState>error(ex)));
		}

		@Override
		public void finish() {
			LOG.info("Finish? " + description().id);
		}
	}
	/**
	 * Wraps the body function into a callback-style invocation scheme for constant or no-input blocks
	 * to execute them immediately when the flow begins to execute.
	 * @return the observer to trigger the execution
	 */
	protected Observer<Void> runConstantBlock() {
		return new RunObserver() {
			@Override
			public void next(Void value) {
				addCloseable(scheduler().schedule(new Runnable() {
					@Override
					public void run() {
						// no reactive parameters
						invokeBody(Lists.<T>newArrayList(), scheduler());
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
	void invokeBody(List<T> value, Scheduler scheduler) {
		diagnostic.next(new BlockDiagnostic("", description().id, Option.some(BlockState.START)));
		try {
			params.clear();
			int j = 0;
			for (Port<T, X> p : inputs.values()) {
				if (p instanceof ConstantPort) {
					params.put(p.name(), settings.constantValues.get(p.name()));
				} else {
					params.put(p.name(), value.get(j++));
				}
			}
			
			invoke();
			
		} catch (Throwable t) {
			log(t);
		}
	}
	/**
	 * Log the exception to the diagnostic port.
	 * @param t the throwable
	 */
	protected void log(@NonNull Throwable t) {
		LOG.error(t.toString(), t);
		diagnostic.next(new BlockDiagnostic("", description().id, Option.<BlockState>error(t)));
	}
	/**
	 * Dispatch a set of outputs given by subsequent String and XElement types.
	 * @param nameValuePairs the name-value pairs as {@code String}, {@code XElement} or {@code Iterable<XElement>}
	 */
	protected void dispatch(Object... nameValuePairs) {
		Map<String, Iterable<T>> values = Maps.newHashMap();
		for (int i = 0; i < nameValuePairs.length; i += 2) {
			String name = (String)nameValuePairs[i];
			Object value = nameValuePairs[i + 1];
			if (value instanceof Iterable) {
				@SuppressWarnings("unchecked")
				T v = (T)value;
				values.put(name, Collections.singleton(v));
			} else {
				@SuppressWarnings("unchecked")
				Iterable<T> v = (Iterable<T>)value;
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
	protected void dispatch(String outputName, T value) {
		dispatchOutput(Collections.singletonMap(outputName, value));
	}
	/**
	 * Dispatch a value to the named output.
	 * @param outputName the output name
	 * @param values the sequence of values
	 */
	protected void dispatch(String outputName, Iterable<T> values) {
		dispatch(Collections.singletonMap(outputName, values));
	}
	/**
	 * Dispatches the given map of output values to various ports.
	 * @param funcOut the function output
	 */
	protected void dispatch(Map<String, ? extends Iterable<T>> funcOut) {
		boolean valid = true;
		for (Map.Entry<String, ? extends Iterable<T>> e : funcOut.entrySet()) {
			if (!outputs.containsKey(e.getKey())) {
				valid = false;
				diagnostic.next(new BlockDiagnostic("", description().id, Option.<BlockState>error(new IllegalArgumentException(e.getKey() + " missing"))));
				LOG.error("missing output '" + e.getKey() + "' at the block type " + description().id);
				valid = false;
			} else {
				ReactivePort<T, X> p = getOutput(e.getKey());
				for (T xe : e.getValue()) {
					p.next(xe);
				}
			}
		}
		if (valid) {
			diagnostic.next(new BlockDiagnostic("", description().id, Option.some(BlockState.FINISH)));
		}
	}
	/**
	 * Dispatch a multimap of values.
	 * @param output the multimap of values
	 */
	protected void dispatch(Multimap<String, T> output) {
		dispatch(output.asMap());
	}
	/**
	 * Dispatches the given map of output values to various ports.
	 * @param funcOut the function output
	 */
	protected void dispatchOutput(Map<String, T> funcOut) {
		boolean valid = true;
		for (Map.Entry<String, T> e : funcOut.entrySet()) {
			if (!outputs.containsKey(e.getKey())) {
				valid = false;
				diagnostic.next(new BlockDiagnostic("", description().id, Option.<BlockState>error(new IllegalArgumentException(e.getKey() + " missing"))));
				LOG.error("missing output '" + e.getKey() + "' at the block type " + description().id);
				valid = false;
			} else {
				getOutput(e.getKey()).next(e.getValue());
			}
		}
		if (valid) {
			diagnostic.next(new BlockDiagnostic("", description().id, Option.some(BlockState.FINISH)));
		}

	}
	/**
	 * The body function to invoke. Implementation should should invoke one of the 
	 * {@code dispatch} methods 
	 * when the computation is over. This may happen synchronously (or asynchronously).
	 * The input parameter values might be accessed through the
	 * {@code getXYZ()} methods or through the private {@code params} map.
	 */
	protected abstract void invoke();
	/** Terminate the block. */
	public void done() {
		for (Closeable c : functionClose) {
			try {
				c.close();
			} catch (IOException ex) {
				LOG.info("", ex);
			}
		}
		for (Port<T, X> ap : inputs.values()) {
			if (ap instanceof ReactivePort) {
				((ReactivePort<?, ?>) ap).disconnect();
			}
		}
		diagnostic.finish();
	}
	/** @return the block's description. */
	public AdvanceBlockDescription description() {
		return settings.description;
	}
	/**
	 * @return the diagnostic port for watch the invocation of the body function
	 */
	public Observable<BlockDiagnostic> getDiagnosticPort() {
		return diagnostic;
	}
	/** 
	 * Get the given output port by name.
	 * @param name the port name
	 * @return the block port
	 */
	public ReactivePort<T, X> getOutput(@NonNull String name) {
		return outputs.get(name);
	}
	/**
	 * Returns the input port with the given name.
	 * @param name the input port name
	 * @return the input port
	 */
	public Port<T, X> getInput(@NonNull String name) {
		return inputs.get(name);
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
	 * The default scheduler for this block.
	 * @return the scheduler
	 */
	public Scheduler scheduler() {
		return scheduler(settings.preferredScheduler());
	}
	/**
	 * Get a specific scheduler.
	 * @param preference the requested scheduler
	 * @return the scheduler
	 */
	public Scheduler scheduler(@NonNull SchedulerPreference preference) {
		return settings.schedulers.get(preference);
	}
	/**
	 * The block unique identifier.
	 * @return the unique identifier
	 */
	public String id() {
		return settings.id;
	}
	/**
	 * The parent composite block.
	 * @return the parent composite block
	 */
	public AdvanceCompositeBlock parent() {
		return settings.parent;
	}
	/**
	 * Add a closeable object ot the list of things to close when the block is terminated.
	 * @param c the closeable
	 */
	public void addCloseable(@NonNull Closeable c) {
		functionClose.add(c);
	}
	/** 
	 * Returns the collection of inputs.
	 * @return the collection of inputs
	 */
	public Collection<Port<T, X>> inputs() {
		return inputs.values();
	}
	/**
	 * Returns the collection of outputs.
	 * @return the collection of outputs
	 */
	public Collection<ReactivePort<T, X>> outputs() {
		return outputs.values();
	}
	/**
	 * Returns a boolean parameter value.
	 * @param name the parameter name
	 * @return the value
	 */
	protected boolean getBoolean(String name) {
		return settings.resolver.getBoolean(get(name));
	}
	/**
	 * Returns a int parameter value.
	 * @param name the parameter name
	 * @return the value
	 */
	protected int getInt(String name) {
		return settings.resolver.getInt(get(name));
	}
	/**
	 * Returns a double parameter value.
	 * @param name the parameter name
	 * @return the value
	 */
	protected double getDouble(String name) {
		return settings.resolver.getDouble(get(name));
	}
	/**
	 * Returns a string parameter value.
	 * @param name the parameter name
	 * @return the value
	 */
	protected String getString(String name) {
		return settings.resolver.getString(get(name));
	}
	/**
	 * Returns a timestamp parameter value.
	 * @param name the parameter name
	 * @return the value
	 * @throws ParseException if the date parsing failed
	 */
	protected Date getTimestamp(String name) throws ParseException {
		return settings.resolver.getTimestamp(get(name));
	}
	/**
	 * Returns a XML parameter value.
	 * <p>It checks for the {@code params} mapping first, and if that fails, it tries
	 * the constant parameters of this block.</p>
	 * @param name the parameter name
	 * @return the value
	 */
	protected T get(String name) {
		T p = params.get(name);
		if (p == null) {
			return settings.constantValues.get(name);
		}
		return p;
	}
	/**
	 * Dispatches the output value to the given port.
	 * @param name the port name
	 * @param value the value
	 */
	protected void set(String name, boolean value) {
		dispatch(name, resolver().create(value));
	}
	/**
	 * Dispatches the output value to the given port.
	 * @param name the port name
	 * @param value the value
	 */
	protected void set(String name, int value) {
		dispatch(name, resolver().create(value));
	}
	/**
	 * Dispatches the output value to the given port.
	 * @param name the port name
	 * @param value the value
	 */
	protected void set(String name, double value) {
		dispatch(name, resolver().create(value));
	}
	/**
	 * Dispatches the output value to the given port.
	 * @param name the port name
	 * @param value the value
	 */
	protected void set(String name, String value) {
		dispatch(name, resolver().create(value));
	}
	/**
	 * Dispatches the output value to the given port.
	 * @param name the port name
	 * @param value the value
	 */
	protected void set(String name, Date value) {
		dispatch(name, resolver().create(value));
	}
	/**
	 * Dispatches the output value to the given port.
	 * @param name the port name
	 * @param value the value
	 */
	protected void set(String name, T value) {
		dispatch(name, value);
	}
	/**
	 * Register an observer on the given port and observe it on the preferred scheduler.
	 * @param port the target port
	 * @param observer the observer to register
	 */
    protected void register(Port<T, X> port, Observer<T> observer) {
        addCloseable(Reactive.observeOn(port, scheduler()).register(observer));
    }
    /**
     * Returns the data resolver.
     * @return the resolver
     */
    protected DataResolver<T> resolver() {
    	return settings.resolver;
    }
    /**
     * Convenience method to register an independent observer for the given input port
     * and call the given action once a value arrives.
     * @param portName the port name
     * @param nextAction the action to invoke on each values
     */
    protected void observeInput(String portName, final Action1<T> nextAction) {
    	if (getInput(portName) instanceof ConstantPort<?, ?>) {
    		nextAction.invoke(get(portName));
    	} else {
	    	addCloseable(Reactive.observeOn(getInput(portName), scheduler()).register(new InvokeObserver<T>() {
	    		@Override
	    		public void next(T value) {
	    			nextAction.invoke(value);
	    		}
			}));
    	}
    }
}
