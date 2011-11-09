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

import java.io.Closeable;

import edu.umd.cs.findbugs.annotations.NonNull;


/**
 * Represents a generic pooling object which supplies and takes back objects.
 * @author akarnokd, 2011.10.05.
 * @param <T> the pooled object type
 */
public interface Pool<T> extends Closeable {
	/**
	 * Retrieve an object from the pool.
	 * @return the object retrieved
	 * @throws Exception if the object could not be supplied
	 */
	@NonNull
	T get() throws Exception;
	/**
	 * Return an object to the pool.
	 * @param obj the object to return
	 */
	void put(@NonNull T obj);
}
