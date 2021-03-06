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


/**
 * The base exception for the ADVANCE Flow Engine Control API.
 * @author akarnokd, 2011.09.19.
 */
public class AdvanceControlException extends Exception {
	/** */
	private static final long serialVersionUID = -8958246930488574550L;

	/**
	 * Default constructor. 
	 */
	public AdvanceControlException() {
	}

	/**
	 * @param message the exception message
	 */
	public AdvanceControlException(String message) {
		super(message);
	}

	/**
	 * @param cause the exception clause
	 */
	public AdvanceControlException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message the exception message
	 * @param cause the exception clause
	 */
	public AdvanceControlException(String message, Throwable cause) {
		super(message, cause);
	}
}
