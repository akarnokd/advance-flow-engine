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

import hu.akarnokd.reactive4java.reactive.Observable;

/**
 * Generic interface to describe ports.
 * @author akarnokd, 2011.06.24.
 * @param <T> the runtime type of the data
 * @param <X> the compile time type of the port data
 */
public interface Port<T, X> extends Observable<T> {
	/** @return the port name. */
	String name();
	/** @return the parent advance block. */
	Block<T, X, ?> parent();
	/** @return the value type of the port. */
	X type();
}
