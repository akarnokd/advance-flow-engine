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
 * The status enumeration of the ADVANCE Flow Engine Realm.
 * @author akarnokd, 2011.09.19.
 */
public enum AdvanceRealmStatus {
	/** The realm is stopped. */
	STOPPED,
	/** The realm is about to stop. */
	STOPPING,
	/** The realm is executing a flow. */
	RUNNING,
	/** The realm is about to run. */
	STARTING,
	/** Indicates the realm was running when the last shutdown happened and will automatically resume. */
	RESUME,
	/** The realm failed the verification. */
	ERROR
}
