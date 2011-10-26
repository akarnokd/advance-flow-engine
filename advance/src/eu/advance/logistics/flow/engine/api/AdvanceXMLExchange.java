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

package eu.advance.logistics.flow.engine.api;

import hu.akarnokd.reactive4java.base.CloseableIterable;
import hu.akarnokd.reactive4java.base.CloseableIterator;
import hu.akarnokd.reactive4java.reactive.Observable;
import hu.akarnokd.reactive4java.reactive.Reactive;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * An interface wrapping a blocking iterable which returns one or multiple values.
 * @author akarnokd, 2011.10.04.
 */
public class AdvanceXMLExchange implements CloseableIterable<XElement> {
	/** Expect multiple responses? */
	public final boolean multiple;
	/** The iterable sequence. */
	private CloseableIterable<XElement> sequence;
	/**
	 * No response.
	 * @return no response
	 */
	public static AdvanceXMLExchange none() {
		AdvanceXMLExchange r = new AdvanceXMLExchange(false);
		r.sequence = new CloseableIterable<XElement>() {
			@Override
			public CloseableIterator<XElement> iterator() {
				return new CloseableIterator<XElement>() {
					@Override
					public boolean hasNext() {
						return false;
					}
					@Override
					public XElement next() {
						throw new NoSuchElementException();
					}
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					@Override
					public void close() throws IOException {
						// NO operation
					}
				};
			}
		};
		return r;
	}
	/**
	 * Single response.
	 * @param response the response
	 * @return an exchange object with single response
	 */
	public static AdvanceXMLExchange single(XElement response) {
		AdvanceXMLExchange r = new AdvanceXMLExchange(false);
		final Iterable<XElement> singleton = Collections.singletonList(response);
		r.sequence = new CloseableIterable<XElement>() {
			@Override
			public CloseableIterator<XElement> iterator() {
				return new CloseableIterator<XElement>() {
					/** The backing iterator. */
					Iterator<XElement> it = singleton.iterator();
					@Override
					public boolean hasNext() {
						return it.hasNext();
					}
					@Override
					public XElement next() {
						return it.next();
					}
					@Override
					public void remove() {
						it.remove();
					}
					@Override
					public void close() throws IOException {
						// NO operation
					}
				};
			}
		};
		return r;
	}
	/**
	 * Return multiple values.
	 * @param source the source observable
	 * @return the exchange with multiple objects
	 */
	public static AdvanceXMLExchange multiple(Observable<XElement> source) {
		AdvanceXMLExchange r = new AdvanceXMLExchange(true);
		r.sequence = Reactive.toIterable(source);
		return r;
	}
	/**
	 * Create a single element returner.
	 * @param multiple 
	 */
	protected AdvanceXMLExchange(boolean multiple) {
		this.multiple = multiple;
	}
	@Override
	public CloseableIterator<XElement> iterator() {
		return sequence.iterator();
	}
}