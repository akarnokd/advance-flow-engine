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

package eu.advance.logistics.flow.engine.api.ds;

/**
 * The enumeration of email box protocols.
 * @author akarnokd, 2011.10.10.
 */
public enum AdvanceEmailReceiveProtocols {
	/** The box will not receive emails. */
	NONE,
	/** Post Office Protocol v3. */
	POP3,
	/** Post Office Protocol v3 over SSL. */ 
	POP3S,
	/** IMAP. */
	IMAP,
	/** IMAP over SSL. */
	IMAPS
}
