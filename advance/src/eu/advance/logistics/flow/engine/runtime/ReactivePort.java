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

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.reactive4java.reactive.DefaultObservable;
import hu.akarnokd.reactive4java.reactive.Observable;
import hu.akarnokd.reactive4java.reactive.Reactive;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a concrete reactive block port instance.
 * @author akarnokd, 2011.06.22.
 * @param <T> the runtime type of the flow
 * @param <X> the compile time type of the flow
 */
public class ReactivePort<T, X> extends DefaultObservable<T> implements Port<T, X> {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(ReactivePort.class);
	/** The parent base block. */
	public final Block<T, X, ?> parent;
	/** The parameter name. */
	public final String name;
	/** The schema definition. */
	public X type;
	/** The observer registration to another output port. */
	private Closeable observer;
	/** The diagnostic observable port. */
	private Observable<PortDiagnostic> diagnostic;
	/**
	 * Constructor.
	 * @param parent the parent block used for diagnostic purposes
	 * @param name the parameter name
	 */
	public ReactivePort(final Block<T, X, ?> parent, final String name) {
		super(false, false);
		this.parent = parent;
		this.name = name;
	}
	/** Initialize the inner observers. */
	public void init() {
		this.diagnostic = Reactive.select(
				Reactive.materialize(this), new Func1<Option<T>, PortDiagnostic>() {
			@Override
			public PortDiagnostic invoke(Option<T> param1) {
				return new PortDiagnostic(parent.settings.realm, parent.description().id, name, param1);
			}
		});
	}
	/**
	 * @return the diagnostic port of this block parameter
	 */
	public Observable<PortDiagnostic> getDiagnosticPort() {
		return diagnostic;
	}
	/**
	 * Connect to an output parameter.
	 * @param obs the the observable
	 */
	public void connect(Observable<T> obs) {
		disconnect();
		observer = obs.register(this);
	}
	/** Disconnect from the observed output parameter. */
	public void disconnect() {
		if (observer != null) {
			try {
				observer.close();
				observer = null;
			} catch (IOException ex) {
				LOG.error("", ex);
			}
		}
	}
	@Override
	public String name() {
		return name;
	}
	@Override
	public Block<T, X, ?> parent() {
		return parent;
	}
	@Override
	public X type() {
		return type;
	}
	@Override
	public String toString() {
		return name + " of " + parent + " with " + type;
	}
}
