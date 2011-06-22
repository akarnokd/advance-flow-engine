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

package eu.advance.logistics.flow.engine;

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.reactive4java.reactive.DefaultObservable;
import hu.akarnokd.reactive4java.reactive.Observable;
import hu.akarnokd.reactive4java.reactive.Reactive;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.advance.logistics.xml.typesystem.XElement;
import eu.advance.logistics.xml.typesystem.XType;

/**
 * @author karnokd, 2011.06.22.
 */
public class AdvanceBlockPort extends DefaultObservable<XElement> {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceBlockPort.class);
	/** The parent base block. */
	public final AdvanceBlock parent;
	/** The parameter name. */
	public final String name;
	/** The schema definition. */
	public XType type;
	/** The observer registration to another output port. */
	private Closeable observer;
	/** The diagnostic observable port. */
	private Observable<AdvanceParameterDiagnostic> diagnostic;
	/**
	 * Constructor.
	 * @param parent the parent block used for diagnostic purposes
	 * @param name the parameter name
	 */
	public AdvanceBlockPort(final AdvanceBlock parent, final String name) {
		super(false, false);
		this.parent = parent;
		this.name = name;
	}
	/** Initialize the inner observers. */
	public void init() {
		this.diagnostic = Reactive.select(
				Reactive.materialize(this), new Func1<Option<XElement>, AdvanceParameterDiagnostic>() {
			@Override
			public AdvanceParameterDiagnostic invoke(Option<XElement> param1) {
				return new AdvanceParameterDiagnostic(parent, AdvanceBlockPort.this, param1);
			}
		});
	}
	/**
	 * @return the diagnostic port of this block parameter
	 */
	public Observable<AdvanceParameterDiagnostic> getDiagnosticPort() {
		return diagnostic;
	}
	/**
	 * Connect to an output parameter.
	 * @param obs the the observable
	 */
	public void connect(Observable<XElement> obs) {
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
}
