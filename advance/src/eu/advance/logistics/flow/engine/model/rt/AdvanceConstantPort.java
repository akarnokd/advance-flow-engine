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
import hu.akarnokd.reactive4java.reactive.Observable;
import hu.akarnokd.reactive4java.reactive.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XType;


/**
 * A special port which returns a constant value.
 * @author akarnokd, 2011.06.24.
 */
public class AdvanceConstantPort implements AdvancePort {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceConstantPort.class);
	/** The parent base block. */
	public final AdvanceBlock parent;
	/** The parameter name. */
	public final String name;
	/** The schema definition. */
	public XType type;
	/** The diagnostic observable port. */
	private Observable<AdvanceParameterDiagnostic> diagnostic;
	/** The single element to return to observers. */
	@NonNull
	public XElement value;
	/**
	 * Constructor.
	 * @param parent the parent block used for diagnostic purposes
	 * @param name the parameter name
	 */
	public AdvanceConstantPort(final AdvanceBlock parent, final String name) {
		this.parent = parent;
		this.name = name;
	}
	/** Initialize the inner observers. */
	public void init() {
		this.diagnostic = Reactive.select(
				Reactive.materialize(this), new Func1<Option<XElement>, AdvanceParameterDiagnostic>() {
			@Override
			public AdvanceParameterDiagnostic invoke(Option<XElement> param1) {
				return new AdvanceParameterDiagnostic("", parent.description.id, name, param1);
			}
		});
	}
	/**
	 * @return the diagnostic port of this block parameter
	 */
	public Observable<AdvanceParameterDiagnostic> getDiagnosticPort() {
		return diagnostic;
	}
	@Override
	public String name() {
		return name;
	}
	@Override
	public AdvanceBlock parent() {
		return parent;
	}
	@Override
	public XType type() {
		return type;
	}
	@Override
	public Closeable register(Observer<? super XElement> observer) {
		observer.next(value);
		return new Closeable() {
			@Override
			public void close() throws IOException {
				// NO OP
			}
		};
	}
}
