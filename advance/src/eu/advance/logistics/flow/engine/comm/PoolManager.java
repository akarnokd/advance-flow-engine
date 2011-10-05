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

package eu.advance.logistics.flow.engine.comm;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The pool manager interface for creating, verifying and closing objects.
 * @author karnokd, 2011-10-05
 * @param <T> the object type
 */
public interface PoolManager<T> {
	/**
	 * Creates a new object of type T.
	 * @return the object
	 * @throws Exception if the object could not be created
	 */
	@NonNull
	T create() throws Exception;
	/**
	 * Verify the validity of the given object.
	 * @param obj the object to verify
	 * @return true if the object is valid
	 * @throws Exception if the verification failure indicates a permanent error
	 */
	boolean verify(T obj) throws Exception;
	/**
	 * Close the specified object.
	 * @param obj the object to close
	 * @throws Exception to aggregate exceptions
	 */
	void close(T obj) throws Exception;
}