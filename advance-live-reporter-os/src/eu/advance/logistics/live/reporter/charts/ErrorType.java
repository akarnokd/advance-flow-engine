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

package eu.advance.logistics.live.reporter.charts;

/**
 * Different error types during the ajax calling.
 * @author csirobi, 2013.04.28.
 */
public enum ErrorType {
	/** User not logged in or session is time out. */
	TIME_OUT("User not logged in or session is time out."),
	/** Number format problem for the enum code. */
	ENUM_CODE_NUMBER_FORMAT("Number format problem for the enum code."),
	/** Illegal argument for the enum code. */
	ENUM_CODE_ILLEGAL_FORMAT("Illegal argument for the enum code."),
	/** Illegal format for the date time. */
	DATE_PARSE_FORMAT("Illegal format for the date time.");
	/** The message. */
	private String message;
	/** 
	 * Constructor, sets the message.
	 * @param m the message 
	 */
	private ErrorType(String m)	{
		this.message = m;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}


}
