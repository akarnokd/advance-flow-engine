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

/**
 * Exception to indicate the user has no right to perform the operation.
 * @author karnokd, 2011.09.21.
 */
public class AdvanceAccessDenied extends AdvanceControlException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3913685096778293578L;

	/**
	 * 
	 */
	public AdvanceAccessDenied() {
	}

	/**
	 * @param message the message
	 */
	public AdvanceAccessDenied(String message) {
		super(message);
	}

	/**
	 * @param cause the cause
	 */
	public AdvanceAccessDenied(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message the message
	 * @param cause the cause
	 */
	public AdvanceAccessDenied(String message, Throwable cause) {
		super(message, cause);
	}

}
