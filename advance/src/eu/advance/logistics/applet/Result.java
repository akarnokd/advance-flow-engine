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
package eu.advance.logistics.applet;

import java.io.Serializable;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * A result or the error enum.
 * @author karnokd
 * @param <T> the content type.
 */
public class Result<T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3214617618024430878L;
	/** The result value. */
	@Nullable
	private T value;
	/** The error value. */
	@CheckForNull
	private DataError error;
	/** Default constructor. */
	public Result() {
		
	}
	/** 
	 * Constructor with an error. 
	 * @param error the error result
	 */
	public Result(@NonNull DataError error) {
		this.error = error;
	}
	/** 
	 * Constructor with a value. 
	 * @param value the actual value 
	 */
	public Result(@Nullable T value) {
		this.value = value;
	}
	/** 
	 * Return the value.
	 * @return the value
	 */
	@Nullable
	public T get() {
		return value;
	}
	/** 
	 * @return the error enum
	 */
	@Nullable
	public DataError error() {
		return error;
	}
	/**
	 * @return The result indicates a success?
	 */
	public boolean success() {
		return error == null;
	}
}
