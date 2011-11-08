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

package eu.advance.logistics.flow.engine.model.rt;

/**
 * The scheduler's thread priority constants.
 * @author akarnokd, 2011.10.25.
 */
public enum AdvanceSchedulerPriority {
	/** Idle. */
	IDLE(Thread.MIN_PRIORITY),
	/** Max priority. */
	MAX(Thread.MAX_PRIORITY),
	/** Very low. */
	VERY_LOW(Thread.MIN_PRIORITY + 1),
	/** Low. */
	LOW(Thread.NORM_PRIORITY - 1),
	/** Normal priority. */
	NORMAL(Thread.NORM_PRIORITY),
	/** Above normal. */
	ABOVE_NORMAL(Thread.NORM_PRIORITY + 1),
	/** High priority. */
	HIGH(Thread.MAX_PRIORITY - 2),
	/** Very high priority. */
	VERY_HIGH(Thread.MAX_PRIORITY - 1)
	;
	/** The priority value. */
	public final int priority;
	/**
	 * Set the priority value.
	 * @param priority the values
	 */
	AdvanceSchedulerPriority(int priority) {
		this.priority = priority;
	}
}
