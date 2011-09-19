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

package eu.advance.logistics.util;

/**
 * An unchecked runtime exception used to report
 * the keystore and key management related exceptions.
 * @author karnokd, 2007.12.07.
 * @version $Revision 1.0$
 */
public class KeystoreFault extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1075300973961671049L;

	/**
	 * Constructor. Do not use it because it is no helpful to have an empty exception
	 * whithout a message or cause.
	 */
	public KeystoreFault() {
		super();
	}

	/**
	 * Constructor with given message.
	 * @param message the error message
	 */
	public KeystoreFault(String message) {
		super(message);
	}

	/**
	 * Constructor with the cause.
	 * @param cause the cause
	 */
	public KeystoreFault(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with the message and cause.
	 * @param message the message
	 * @param cause the cause
	 */
	public KeystoreFault(String message, Throwable cause) {
		super(message, cause);
	}

}
