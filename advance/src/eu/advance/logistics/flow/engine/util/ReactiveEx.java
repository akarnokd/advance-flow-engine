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

package eu.advance.logistics.flow.engine.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Extension methods to the Reactive4Java operators for now. They may be moved later on into
 * the library.
 * @author karnokd, 2011.06.22.
 */
public final class ReactiveEx {

	/**
	 * Utility class.
	 */
	private ReactiveEx() {
	}
	/** An empty closeable. */
	private static final Closeable EMPTY_CLOSEABLE = new Closeable() {
		@Override
		public void close() throws IOException {
			// NO OP
		}
	};
	/** @return an empty, no-op closeable instance. */
	public static Closeable emptyCloseable() {
		return EMPTY_CLOSEABLE;
	}
}
