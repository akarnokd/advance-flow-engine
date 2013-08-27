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

package eu.advance.logistics.flow.engine.api.core;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * An exception which wraps many other exception under a common IOException-type.
 * @author akarnokd, 2011.10.05.
 */
public class MultiIOException extends IOException {
	/** */
	private static final long serialVersionUID = 1406886722299385700L;
	/** The list of exceptions. */
	protected final List<Throwable> exceptions;
	/**
	 * Create a MultiIOException with the given sequence of wrapped exceptions.
	 * @param exceptions the sequence of exceptions
	 */
	public MultiIOException(Iterable<? extends Throwable> exceptions) {
		 this.exceptions = Lists.newArrayList(exceptions);
	}
	@Override
	public String getLocalizedMessage() {
		StringBuilder b = new StringBuilder();
		for (Throwable t : exceptions) {
			if (b.length() > 0) {
				b.append(", ");
			}
			b.append(t);
		}
		return b.toString();
	}
	@Override
	public void printStackTrace() {
		super.printStackTrace();
		for (Throwable t : exceptions) {
			t.printStackTrace();
		}
	}
	@Override
	public void printStackTrace(PrintStream s) {
		super.printStackTrace(s);
		for (Throwable t : exceptions) {
			t.printStackTrace(s);
		}
	}
	@Override
	public void printStackTrace(PrintWriter s) {
		super.printStackTrace(s);
		for (Throwable t : exceptions) {
			t.printStackTrace(s);
		}
	}
	/** @return the unmodifiable list of wrapped exceptions. */
	public List<Throwable> getExceptions() {
		return Collections.unmodifiableList(exceptions);
	}
}
